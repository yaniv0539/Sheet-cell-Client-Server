package component.main.center.dashboard;

import component.main.MainController;

import component.main.center.dashboard.model.RequestTableLine;
import component.main.center.dashboard.model.SheetTableLine;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DashBoardController {

    BooleanProperty activeConfirmButton = new SimpleBooleanProperty(false);
    BooleanProperty activeDenyButton = new SimpleBooleanProperty(false);
    BooleanProperty activeLoadSheetButton = new SimpleBooleanProperty(false);
    BooleanProperty activeRequestPermissionButton = new SimpleBooleanProperty(false);
    BooleanProperty activeViewSheetButton = new SimpleBooleanProperty(false);

    ObservableList<SheetTableLine> sheetTableLines = FXCollections.observableArrayList();

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
    private TableView<RequestTableLine> requestTableView;

    @FXML
    private TableView<SheetTableLine> sheetTableView;



    @FXML
    void initialize() {

        //BIND BOOLEAN DISABLE
        confirmButton.disableProperty().bind(activeConfirmButton);
        denyButton.disableProperty().bind(activeDenyButton);
        loadSheetButton.disableProperty().bind(activeLoadSheetButton);
        requestPermissionButton.disableProperty().bind(activeRequestPermissionButton);
        viewSheetButton.disableProperty().bind(activeViewSheetButton);

        //init table

        //bind columns to fields in data model.
        sheetTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        sheetTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("userName"));
        sheetTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("layout"));
        sheetTableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("permission"));

        //set observable list to table
        sheetTableView.setItems(sheetTableLines);

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

    }

    @FXML
    void requestTableViewOnSort(ActionEvent event) {

    }

    @FXML
    void sheetTableViewOnSort(ActionEvent event) {

    }

    @FXML
    void viewSheetAction(ActionEvent event) {

    }

}
