package component.main.center.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import component.main.MainController;
import component.main.center.dashboard.model.RequestTableLine;
import component.main.center.dashboard.model.SheetTableLine;
import dto.*;
import dto.deserializer.CellDtoDeserializer;
import dto.enums.PermissionType;
import dto.enums.Status;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dto.enums.Status.*;

public class DashBoardController {

    // Members
    MainController mainController;
    ScheduledExecutorService executorServiceForSheetTable;
    ScheduledExecutorService executorServiceForRequestTable;
    private boolean isThreadsActive;
    PermissionType RequestedPermission;
    String focusSheetName = null;

    //index for current focus in tables.params
    private int focusItemIndexInSheetTable;


    //set's for check if sheet and permission change
    private Map<String,Integer> sheetNameToIndexInSheetList = new HashMap<>();

    BooleanProperty isSelectedRequestPending = new SimpleBooleanProperty(false);
    BooleanProperty isSelectedSheetOwnByUser = new SimpleBooleanProperty(false);
    BooleanProperty disableLoadSheetButton = new SimpleBooleanProperty(false);

    BooleanBinding disableConfirmDenyButton;
    BooleanBinding disableRequestPermissionButton;
    BooleanBinding disableViewSheetButton;

    ObservableList<SheetTableLine> sheetTableLines = FXCollections.observableArrayList();
    ObservableList<RequestTableLine> requestTableLines = FXCollections.observableArrayList();


    // FXML Members

    @FXML
    private Button confirmButton;

    @FXML
    private Button denyButton;

    @FXML
    private Button loadSheetButton;

    @FXML
    private Button requestPermissionButton;

    @FXML
    private Button viewSheetButton;

    @FXML
    private CheckBox redearCheckBox;

    @FXML
    private CheckBox writerCheckBox;

    @FXML
    private TableView<RequestTableLine> requestTableView;

    @FXML
    private TableView<SheetTableLine> sheetTableView;


    // FXML on action methods

    @FXML
    void viewSheetAction(ActionEvent event) {
        SheetTableLine selectedLine = sheetTableView.getSelectionModel().getSelectedItem();
        String sheetName = null;

        if (selectedLine != null) {
            sheetName = selectedLine.getSheetName();
        }

        mainController.getSheet(sheetName, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(()-> mainController.showAlertPopup(new Exception(e.getMessage()),"unexpected error" ));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();

                if(response.code() != 200) {
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonResponse),"loading sheet failed" ));
                }
                else {
                    Gson gson = new GsonBuilder().registerTypeAdapter(CellDto.class,new CellDtoDeserializer()).create();
                    SheetDto sheetDto = gson.fromJson(jsonResponse, SheetDto.class);

                    Platform.runLater(()->{
                        mainController.uploadSheetToWorkspace(sheetDto); //prepare the scene
                        mainController.switchToApp();
                    });
                }
            }
        });
    }

    @FXML
    void confirmAction(ActionEvent event) {
        confirmDenyOnAction(CONFIRMED);
    }

    @FXML
    void denyAction(ActionEvent event) {
        confirmDenyOnAction(DENIED);
    }

    @FXML
    void loadSheetAction(ActionEvent event) {
        String path = chooseFileFromFileChooser();

        mainController.postXMLFile(path, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> mainController.showAlertPopup(new Exception(),"Loading file"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string(); // Raw response
                if (response.code() != 200) {
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonResponse),"loading sheet failed"));
                } else {
                    Gson gson = new GsonBuilder().registerTypeAdapter(CellDto.class,new CellDtoDeserializer()).create();
                    SheetDto sheetDto = gson.fromJson(jsonResponse, SheetDto.class);

                    Platform.runLater(()->{
                        mainController.uploadSheetToWorkspace(sheetDto); //prepare the scene
                    });
                }
            }
        });
    }

    @FXML
    void requestPermissionAction(ActionEvent event) {
        String sheetName = focusSheetName;
        //to complete function in main
        mainController.postRequestPermission(sheetName, RequestedPermission, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> mainController.showAlertPopup(e,"request permission"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonString = response.body().string();

                if(response.code() != 201) {
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonString),"request permission"));
                }
                //maybe to show the user that the action is done successfully.
            }
        });
    }

    @FXML
    void requestTableViewOnSort(ActionEvent event) {

    }

    @FXML
    void sheetTableViewOnSort(ActionEvent event) {

    }

    @FXML
    void readerAction(ActionEvent event) {

    }

    @FXML
    void writerAction(ActionEvent event) {

    }


    // Initializers

    @FXML
    void initialize() {
        initPullThread();
        initBindButtonDisableProperty();
        initSheetTableView();
        initRequestTableView();
        initListeners();
    }

    private void initBindButtonDisableProperty() {
        // Init booleans
        disableViewSheetButton = sheetTableView.getSelectionModel().selectedItemProperty().isNull()
                .or(requestTableView.focusedProperty());

        disableRequestPermissionButton = sheetTableView.getSelectionModel().selectedItemProperty().isNull()
                .or(redearCheckBox.selectedProperty().not().and(writerCheckBox.selectedProperty().not()));


        disableConfirmDenyButton = requestTableView.focusedProperty().not()
                .or(requestTableView.getSelectionModel().selectedItemProperty().isNull())
                .or(isSelectedRequestPending.not())
                .or(isSelectedSheetOwnByUser.not());


        loadSheetButton.disableProperty().bind(disableLoadSheetButton); //always false.
        confirmButton.disableProperty().bind(disableConfirmDenyButton);
        denyButton.disableProperty().bind(disableConfirmDenyButton);
        requestPermissionButton.disableProperty().bind(disableRequestPermissionButton);
        viewSheetButton.disableProperty().bind(disableViewSheetButton);
    }

    private void initSheetTableView() {
        // Bind columns to fields in data model.
        sheetTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        sheetTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("userName"));
        sheetTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("layout"));
        sheetTableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("permission"));

        // Set observable list to table
        sheetTableView.setItems(sheetTableLines);
    }

    private void initRequestTableView() {
        // Bind columns to fields in data model.
        requestTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("userName"));
        requestTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("permissionType"));
        requestTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("requestStatus"));

        // Set observable list to table
        requestTableView.setItems(requestTableLines);
    }

    private void initListeners() {
        redearCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkBoxPermissionListener(PermissionType.READER, writerCheckBox, newValue));
        writerCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkBoxPermissionListener(PermissionType.WRITER, redearCheckBox, newValue));
        sheetTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null ) {
                   focusSheetName = newValue.getSheetName();
                    isSelectedSheetOwnByUser.set(newValue.getPermission().equals(PermissionType.OWNER.toString()));
                }

        });

        requestTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                isSelectedRequestPending.set(newValue.getRequestStatus() == PENDING);
            }
        });
    }

    private void initPullThread() {
        this.executorServiceForSheetTable = Executors.newSingleThreadScheduledExecutor();
        this.executorServiceForRequestTable = Executors.newSingleThreadScheduledExecutor();
    }


    // Setters

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setActive() {

        if (isThreadsActive) {
            return; // Prevent starting multiple threads
        }

        isThreadsActive = true;

        executorServiceForSheetTable.scheduleAtFixedRate(() -> mainController.getSheetsOverview(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(),"pull thread fail.."));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    assert response.body() != null;
                    String jsonString = response.body().string();
                    if (response.code() != 200) {
                        Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonString),"pull thread fail.."));
                    } else {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        Type setType = new TypeToken<Set<SheetOverviewDto>>(){}.getType();
                        Set<SheetOverviewDto> sheetOverviewDtos = gson.fromJson(jsonString, setType);
                        Platform.runLater(()-> updateSheetsTable(sheetOverviewDtos));
                    }
                }
            }), 0, 1, TimeUnit.SECONDS); // Sends requests every second
        executorServiceForRequestTable.scheduleAtFixedRate(()-> {

            //only if is there sheet selected
            if(focusSheetName != null) {

                mainController.getPermissions(focusSheetName,new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Platform.runLater(()->mainController.showAlertPopup(new Exception(e.getMessage()),"unexpected error at get requests"));
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        assert response.body() != null;
                        String jsonString = response.body().string();
                        if (response.code() != 200) {
                            Platform.runLater(() -> mainController.showAlertPopup(new Exception(jsonString),"unexpected error at get requests"));
                        } else {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                    Type setType = new TypeToken<List<RequestDto>>(){}.getType();
                            PermissionsDto permissionsDto = gson.fromJson(jsonString, PermissionsDto.class);
                            Platform.runLater(()-> updatePermissionsTable(permissionsDto));
                        }
                    }
                });
            }
        }, 0,500 , TimeUnit.MILLISECONDS);
    }

    public void setInActive() {
        if (!isThreadsActive) {
            return;
        }

        isThreadsActive = false;
        executorServiceForSheetTable.shutdownNow(); // Stop the background thread
        executorServiceForRequestTable.shutdownNow();

        try {
            // Await termination of all tasks
            executorServiceForRequestTable.awaitTermination(1, TimeUnit.SECONDS);
            executorServiceForSheetTable.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Reinitialize the executor service to start again if needed
        executorServiceForSheetTable = Executors.newSingleThreadScheduledExecutor();
        executorServiceForRequestTable = Executors.newSingleThreadScheduledExecutor();
    }


    // Handlers methods

    private void checkBoxPermissionListener(PermissionType permissionToSet, CheckBox toUnselect, boolean isSelected) {
        if (isSelected){
            RequestedPermission = permissionToSet;
            toUnselect.setSelected(false);
        }
    }

    private String chooseFileFromFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select xml file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(mainController.getPrimaryStage());
        if (selectedFile == null) {
            return null;
        }
        return selectedFile.getAbsolutePath();
    }

    private void confirmDenyOnAction(Status ownerAnswer) {

        RequestTableLine selectedLine = requestTableView.getSelectionModel().getSelectedItem();
        RequestDto requestDto = new RequestDto(selectedLine.getUserName(),selectedLine.getPermissionType(),selectedLine.getRequestStatus());

        mainController.postResponsePermission(focusSheetName, requestDto, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(()-> mainController.showAlertPopup(new Exception(e.getMessage()),"unexpected error" ));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();

                if (response.code() != 200) {
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonResponse),"update permission for user"));
                }
            }
        });
    }

    //no need this function we have a thread
    private void sentHttpRequestForPermission(String sheetName) {

//        mainController.getPermissions(sheetName, new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                Platform.runLater(()->mainController.showAlertPopup(new Exception(e.getMessage()),"unexpected error at get requests"));
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                assert response.body() != null;
//                String jsonString = response.body().string();
//                if (response.code() != 200) {
//                    Platform.runLater(() -> mainController.showAlertPopup(new Exception(jsonString),"unexpected error at get requests"));
//                } else {
//                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                    Type setType = new TypeToken<List<RequestDto>>(){}.getType();
//                    List<RequestDto> listRequestDto = gson.fromJson(jsonString, setType);
//                    Platform.runLater(()-> updatePermissionsTable(listRequestDto));
//                }
//            }
//        });
    }

    private void updateSheetsTable(Set<SheetOverviewDto> sheetOverviewDtos) {
        System.out.println("pulling sheets from server");
        // TODO, done by itay 25/10.

        sheetOverviewDtos.forEach(sheetOverviewDto -> {

            String sheetName = sheetOverviewDto.sheetName();
            Integer lineIndex = sheetNameToIndexInSheetList.get(sheetName);

            if(lineIndex == null) {

                sheetTableLines.add(new SheetTableLine(
                        sheetOverviewDto.owner(),
                        sheetOverviewDto.sheetName(),
                        sheetOverviewDto.layout().toString(),
                        sheetOverviewDto.userPerm().toString()));

                //size is always bigger be one.
                sheetNameToIndexInSheetList.put(sheetName, sheetTableLines.size() - 1);
            }
            else {
               SheetTableLine line = sheetTableLines.get(lineIndex);

                if(!line.getPermission().equals(sheetOverviewDto.userPerm().toString())) {

                    sheetTableLines.set(lineIndex, new SheetTableLine(
                            sheetOverviewDto.owner(),
                            sheetOverviewDto.sheetName(),
                            sheetOverviewDto.layout().toString(),
                            sheetOverviewDto.userPerm().toString()));
                }
            }

        });
    }

    private void updatePermissionsTable(PermissionsDto permissionsDto) {
        System.out.println("pulling permissions from server !");
        // TODO
        int sizeOfObservableList = requestTableLines.size();

        List<RequestDto> requests = permissionsDto.requests();

        for(int indexLine = 0; indexLine < requests.size(); indexLine++) {

            RequestDto requestDtoNewLine = requests.get(indexLine);
            //converting to ui model
            RequestTableLine requestTableLineNewLine = new RequestTableLine(requestDtoNewLine.requesterName(),requestDtoNewLine.permissionType(),requestDtoNewLine.status());

            if (indexLine < sizeOfObservableList) {
                // +1 only bcz in ui we put the owner in first line.
                if (!requestTableLineNewLine.equals(requestTableLines.get(indexLine))) {

                    requestTableLines.set(indexLine, requestTableLineNewLine);
                }
            }
            else {
                //add the request.
                requestTableLines.add(requestTableLineNewLine);
            }
        }

    }

}
