package component.main.center.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import component.main.MainController;
import component.main.center.dashboard.model.RequestTableLine;
import component.main.center.dashboard.model.SheetTableLine;
import dto.CellDto;
import dto.PermissionDto;
import dto.SheetDto;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dto.enums.Status.*;

public class DashBoardController {

    MainController mainController;
    ScheduledExecutorService executorService;
    private boolean isThreadActive;
    PermissionType RequestedPermission;

    //index for current focus in tables.
    private int focusItemIndexInSheetTable;
    private int focusItemIndexInRequestTable;

    BooleanProperty isSelectedNonePendingRequest = new SimpleBooleanProperty(false);
    BooleanProperty disableLoadSheetButton = new SimpleBooleanProperty(false);
    BooleanBinding disableConfirmDenyButton;
    BooleanBinding disableRequestPermissionButton;
    BooleanBinding disableViewSheetButton;

    ObservableList<SheetTableLine> sheetTableLines = FXCollections.observableArrayList();
    ObservableList<RequestTableLine> requestTableLines = FXCollections.observableArrayList();

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



    @FXML
    void initialize() {
        initPullThread();
        initBindButtonDisableProperty();
        initSheetTableView();
        initRequestTableView();
        initListeners();
    }

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
                if(response.code() != 200) {
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonResponse),"loading sheet failed"));
                } else {
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
    void requestPermissionAction(ActionEvent event) {

        SheetTableLine sheetTableLine = sheetTableView.getSelectionModel().getSelectedItem();
        String sheetName = sheetTableLine.getSheetName();

        //to complete function in main
        mainController.postRequestPermission(sheetName, RequestedPermission, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(()-> System.out.println("(()->mainController.showPopupAlert())"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonString = response.body().string();

                if(response.code() != 201) {
                    Platform.runLater(()-> System.out.println("(()->mainController.showPopupAlert(jsonString))"));
                }
                else {
                    //add line to request
                    Platform.runLater(()-> requestTableLines.add(
                            new RequestTableLine(mainController.getUserName(),RequestedPermission,PENDING)));
                }
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


    //INIT
    void initBindButtonDisableProperty() {
        //init booleans
        disableViewSheetButton = sheetTableView.getSelectionModel().selectedItemProperty().isNull()
                .or(requestTableView.focusedProperty());

        disableRequestPermissionButton = sheetTableView.getSelectionModel().selectedItemProperty().isNull()
                .or(redearCheckBox.selectedProperty().not().and(writerCheckBox.selectedProperty().not()));

        disableConfirmDenyButton = requestTableView.getSelectionModel().selectedItemProperty().isNull()
                .or(requestTableView.getSelectionModel().selectedItemProperty().isNotNull())
                        .and(isSelectedNonePendingRequest)
                .or(sheetTableView.focusedProperty());


        loadSheetButton.disableProperty().bind(disableLoadSheetButton); //always false.

        confirmButton.disableProperty().bind(disableConfirmDenyButton);
        denyButton.disableProperty().bind(disableConfirmDenyButton);
        requestPermissionButton.disableProperty().bind(disableRequestPermissionButton);
        viewSheetButton.disableProperty().bind(disableViewSheetButton);
    }

    void initSheetTableView() {
        //bind columns to fields in data model.
        sheetTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        sheetTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("userName"));
        sheetTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("layout"));
        sheetTableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("permission"));

        //set observable list to table
        sheetTableView.setItems(sheetTableLines);
    }

    void initRequestTableView() {
        //bind columns to fields in data model.
        requestTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("userName"));
        requestTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        requestTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("requestStatus"));

        //set observable list to table
        requestTableView.setItems(requestTableLines);
    }

    private void initListeners() {
        redearCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkBoxPermissionListener(PermissionType.READER,writerCheckBox,newValue));
        writerCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkBoxPermissionListener(PermissionType.WRITER,redearCheckBox,newValue));
        sheetTableView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                focusItemIndexInSheetTable = sheetTableView.getSelectionModel().getFocusedIndex();
                sentHttpRequestForPermission(sheetTableView.getItems().get(focusItemIndexInSheetTable).getSheetName());
            }

        });
        requestTableView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                focusItemIndexInRequestTable = requestTableView.getSelectionModel().getFocusedIndex();
                isSelectedNonePendingRequest.set(requestTableView.getItems().get(focusItemIndexInRequestTable).getRequestStatus() != PENDING);
            }
        });
    }


    private void initPullThread() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    //setters
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setActive() {

        if (isThreadActive) {
            return; // Prevent starting multiple threads
        }

        isThreadActive = true;

        executorService.scheduleAtFixedRate(() -> mainController.dashboardPull(new Callback(){
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(),"pull thread fail.."));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String jsonString = response.body().string();
                    if(response.code() != 200) {
                        Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonString),"pull thread fail.."));
                    }
                    else{
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        DashBoardDto dashBoardDto = gson.fromJson(jsonString,DashBoardDto.Class);
                        Platform.runLater(()-> updateDashboard(dashBoardDto));
                    }
                }
            }), 0, 1, TimeUnit.SECONDS); // Sends requests every second
    }

    public void setInActive() {
        if (!isThreadActive) {
            return;
        }

        isThreadActive = false;
        executorService.shutdownNow(); // Stop the background thread

        try {
            // Await termination of all tasks
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Reinitialize the executor service to start again if needed
        executorService = Executors.newSingleThreadScheduledExecutor();
    }



    //handler function
    private void checkBoxPermissionListener(PermissionType permissionToSet,CheckBox toUnselect ,boolean isSelected) {
        if(isSelected){
            RequestedPermission = permissionToSet;
            toUnselect.setSelected(false);
        }
    }
    private String chooseFileFromFileChooser(){
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
        int index = requestTableView.getItems().indexOf(selectedLine);
        String sheetName = null;
        String userNameToConfirm = null;

        if (selectedLine != null) {
            sheetName = selectedLine.getSheetName();
            userNameToConfirm = selectedLine.getUserName();
        }
        final String userNameToConfirmFinal = userNameToConfirm;

        mainController.postResponsePermission(sheetName, userNameToConfirm, new Callback(){
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(()-> mainController.showAlertPopup(new Exception(e.getMessage()),"unexpected error" ));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();

                if(response.code() != 200){
                    Platform.runLater(()-> mainController.showAlertPopup(new Exception(jsonResponse),"update permission for user"));
                }
                else{

                    Platform.runLater(()->{
                        updateUserStatus(index,ownerAnswer);//change in table view requests
                    });
                }
            }
        });
    }

    private void updateUserStatus(int index, Status ownerAnswer) {
        requestTableLines.get(index).setRequestStatus(ownerAnswer);
    }

    private void updateDashboard(DashBoardDto dashBoardDto) {
        //to implemment.
    }
    private void sentHttpRequestForPermission(String sheetName) {

        mainController.getRequests(sheetName,new Callback(){
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(()->mainController.showAlertPopup(new Exception(e.getMessage()),"unexpected error at get requests"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonString = response.body().string();
                if(response.code() != 200) {
                    Platform.runLater(() -> mainController.showAlertPopup(new Exception(jsonString),"unexpected error at get requests"));
                }
                else {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    SheetPermissionDto sheetPermissionDto = gson.fromJson(jsonString,SheetPermissionDto.class);
                    Platform.runLater(()-> addToRequestTableLine(sheetPermissionDto));
                }
            }
        });
    }

}
