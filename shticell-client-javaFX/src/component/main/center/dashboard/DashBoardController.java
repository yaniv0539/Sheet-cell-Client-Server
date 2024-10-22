package component.main.center.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import component.main.MainController;
import component.main.center.dashboard.model.RequestTableLine;
import component.main.center.dashboard.model.SheetTableLine;
import dto.CellDto;
import dto.SheetDto;
import dto.deserializer.CellDtoDeserializer;
import dto.enums.PermissionType;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static dto.enums.Status.PENDING;

public class DashBoardController {

    MainController mainController;
    PermissionType RequestedPermission;

    BooleanBinding disableConfirmDenyButton;
    BooleanBinding disableRequestPermissionButton;
    BooleanProperty disableLoadSheetButton = new SimpleBooleanProperty(false);
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

        initBindButtonDisableProperty();
        initSheetTableView();
        initRequestTableView();
        initListeners();
    }



    @FXML
    void viewSheetAction(ActionEvent event) {
        SheetTableLine selectedLine = sheetTableView.getSelectionModel().getSelectedItem();
        String sheetName = null;
        PermissionType userPermission = null;

        if (selectedLine != null) {
            sheetName = selectedLine.getSheetName();
            userPermission = PermissionType.valueOf(selectedLine.getPermission().toUpperCase());
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

                if (response.code() != 200) {
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


    }

    @FXML
    void denyAction(ActionEvent event) {

    }

    @FXML
    void loadSheetAction(ActionEvent event) {

    }

    @FXML
    void requestPermissionAction(ActionEvent event) {

        SheetTableLine sheetTableLine = sheetTableView.getSelectionModel().getSelectedItem();
        String sheetName = sheetTableLine.getSheetName();

        //to complete function in main
        mainController.postPermission(sheetName, RequestedPermission, new Callback() {
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
                //nothing to do if all good.
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
//                        .and(new SimpleBooleanProperty(requestTableView.getSelectionModel().getSelectedItem().getRequestStatus() != PENDING)))
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
        requestTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("permissionRequested"));
        requestTableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("requestStatus"));

        //set observable list to table
        requestTableView.setItems(requestTableLines);
    }

    private void initListeners() {
        redearCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkBoxPermissionListener(PermissionType.READER,writerCheckBox,newValue));
        writerCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkBoxPermissionListener(PermissionType.WRITER,redearCheckBox,newValue));
    }

    //set main controller
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }



    //handler function
    private void checkBoxPermissionListener(PermissionType permissionToSet,CheckBox toUnselect ,boolean isSelected) {
        if(isSelected){
            RequestedPermission = permissionToSet;
            toUnselect.setSelected(false);
        }
    }

    private void ConfirmDenyOnAction() {




    }

}
