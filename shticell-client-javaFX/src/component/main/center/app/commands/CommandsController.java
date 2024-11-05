package component.main.center.app.commands;

import component.main.center.app.AppController;
import component.main.center.app.commands.operations.filter.FilterController;
import component.main.center.app.commands.operations.sort.SortController;
import dto.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import okhttp3.Callback;

import java.io.IOException;
import java.net.URL;


public class CommandsController {

    private static final String FILTER_POPUP_FXML_INCLUDE_RESOURCE = "operations/filter/filter.fxml";
    private static final String SORT_POPUP_FXML_INCLUDE_RESOURCE = "operations/sort/sort.fxml";

    private AppController mainController;

    @FXML
    private Button buttonFilter;

    @FXML
    private Button buttonResetToDefault;

    @FXML
    private Button buttonSort;

    @FXML
    private ColorPicker colorPickerBackgroundColor;

    @FXML
    private ColorPicker colorPickerTextColor;

    @FXML
    private ComboBox<Pos> comboBoxAlignment;

    @FXML
    private Spinner<Integer> spinnerHeight;

    @FXML
    private Spinner<Integer> spinnerWidth;

    private Stage filterStage;
    private Stage sortStage;

    private FilterController filterConroller;
    private SortController sortController;

    private IntegerProperty heightProperty;
    private IntegerProperty widthProperty;

    private BooleanProperty isFilterActiveProperty;
    private BooleanProperty isSortActiveProperty;

    // Constructor

    public CommandsController() {
        heightProperty = new SimpleIntegerProperty();
        widthProperty = new SimpleIntegerProperty();
    }


    // Initializers

    @FXML
    private void initialize() {
        isFilterActiveProperty = new SimpleBooleanProperty(false);
        isSortActiveProperty = new SimpleBooleanProperty(false);
    }

    public void init(BooleanProperty showCommandsProperty, BooleanProperty showLogicalOperationsProperty) {
        buttonResetToDefault.disableProperty().bind(showCommandsProperty.not());
        buttonSort.disableProperty().bind(Bindings.or(showLogicalOperationsProperty.not(), isFilterActiveProperty));
        buttonFilter.disableProperty().bind(Bindings.or(showLogicalOperationsProperty.not(), isSortActiveProperty));
        spinnerWidth.disableProperty().bind(showCommandsProperty.not());
        spinnerHeight.disableProperty().bind(showCommandsProperty.not());
        comboBoxAlignment.disableProperty().bind(showCommandsProperty.not());
        colorPickerBackgroundColor.disableProperty().bind(showCommandsProperty.not());
        colorPickerTextColor.disableProperty().bind(showCommandsProperty.not());

        // Set the alignment options in the combo box and initiate it to Left as default.
        ObservableList<Pos> columnAlignmentOptions = FXCollections.observableArrayList(Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER_RIGHT);
        comboBoxAlignment.setItems(columnAlignmentOptions);
        comboBoxAlignment.getSelectionModel().selectFirst();

        // column width picker
        SpinnerValueFactory<Integer> widthValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0, 1);

        spinnerWidth.setValueFactory(widthValueFactory);
        spinnerWidth
                .valueProperty()
                .addListener((observable, oldValue, newValue) -> mainController.changeSheetColumnWidth(newValue));


        // row height picker
        SpinnerValueFactory<Integer> heightValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0, 1);
        spinnerHeight.setValueFactory(heightValueFactory);

        spinnerHeight
                .valueProperty()
                .addListener((observable, oldValue, newValue) -> mainController.changeSheetRowHeight(newValue));

        colorPickerBackgroundColor
                .valueProperty()
                .addListener((observable, oldValue, newValue) -> mainController.changeSheetCellBackgroundColor(newValue));

        colorPickerTextColor
                .valueProperty()
                .addListener((observableValue, oldValue, newValue) -> mainController.changeSheetTextColor(newValue));
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }


    // Getters

    public AppController getMainController() {
        return mainController;
    }


    @FXML
    void alignmentAction(ActionEvent event) {
        int selectedIndex = comboBoxAlignment.getSelectionModel().getSelectedIndex();
        switch (selectedIndex) {
            case 0:
                mainController.alignCells(Pos.CENTER_LEFT);
                break;
            case 1:
                mainController.alignCells(Pos.CENTER);
                break;
            case 2:
                mainController.alignCells(Pos.CENTER_RIGHT);
                break;
        }
    }

    @FXML
    void backgroundColorAction(ActionEvent event) {
        //try to elimnate
        //mainController.changeSheetCellBackgroundColor(colorPickerBackgroundColor.getValue());
    }

    @FXML
    void filterAction(ActionEvent event) throws IOException {
        if (!isFilterActiveProperty.get()) {
            activateFilterPopup(FILTER_POPUP_FXML_INCLUDE_RESOURCE, "Filter");
            isFilterActiveProperty.set(true);
        } else {
            mainController.resetOperationView();
            resetButtonFilter();
        }
    }

    @FXML
    void sortAction(ActionEvent event) throws IOException {
        if (!isSortActiveProperty.get()) {
            activateSortPopup(SORT_POPUP_FXML_INCLUDE_RESOURCE, "Sort");
            isSortActiveProperty.set(true);
        } else {
            mainController.resetOperationView();
            resetButtonSort();
        }
    }

    @FXML
    void resetToDefaultAction(ActionEvent event) {
        mainController.resetCellsToDefault();
    }

    @FXML
    void textColorAction(ActionEvent event) {

    }


    // On change functions

    public void changeColumnWidth(int prefWidth) {
        spinnerWidth.getValueFactory().setValue(prefWidth);
    }

    public void changeRowHeight(int prefHeight) {
        spinnerHeight.getValueFactory().setValue(prefHeight);
    }

    public void changeColumnAlignment(Pos alignment) {
        comboBoxAlignment.getSelectionModel().select(alignment);
    }

    public void changeCellBackgroundColor(Color color) {
        colorPickerBackgroundColor.setValue(color);
    }

    public void changeCellTextColor(Color color) {
        colorPickerTextColor.setValue(color);
    }


    // Filter functions

    public void filterRange(FilterDto data, Callback callback) {
        this.mainController.getFilteredSheet(data, callback);
    }

    public void filterCommandsControllerRunLater() {
        buttonFilter.setText("Reset Filter");
        filterStage.close();
    }


    // Sort Functions

    public void sortRange(SortDto sortDto, Callback callback) {
        this.mainController.getSortedSheet(sortDto, callback);
    }

    public void sortCommandsControllerRunLater() {
        buttonSort.setText("Reset Sort");
        sortStage.close();
    }


    // Functions (X Controller -> Commands Controller -> Main Controller)

    public void getColumnUniqueValuesInRange(int column, int startRow, int endRow, Callback callback) {
        mainController.getColumnUniqueValuesInRange(column, startRow, endRow, callback);
    }

    public void getBoundariesDto(String text, Callback callback) {
        mainController.getBoundariesDto(text, callback);
    }

    public void showAlertPopup(Throwable exception, String error) {
        mainController.showAlertPopup(exception, error);
    }

    public void getNumericColumnsInBoundaries(String text, Callback callback) {
        mainController.getNumericColumnsInBoundaries(text, callback);
    }


    // Reset functions

    public void resetButtonSort() {
        buttonSort.setText("Sort");
        isSortActiveProperty.set(false);
    }

    public void resetButtonFilter(){
        buttonFilter.setText("Filter");
        isFilterActiveProperty.set(false);
    }


    // Pop ups

    private void activateFilterPopup(String resource, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(resource);
        fxmlLoader.setLocation(url);
        Parent popupRoot = fxmlLoader.load(url.openStream());

        FilterController filterController = fxmlLoader.getController();
        this.filterConroller = filterController;
        filterController.setMainController(this);
        filterController.init();

        this.filterStage = new Stage();
        filterStage.initModality(Modality.APPLICATION_MODAL);
        filterStage.setTitle(title);
        filterStage.setOnCloseRequest((WindowEvent event) -> isFilterActiveProperty.set(false));

        Scene popupScene = new Scene(popupRoot, 770, 220);
        filterStage.setResizable(true);
        filterStage.setScene(popupScene);
        filterStage.show();
    }

    private void activateSortPopup(String resource, String title) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(resource);
        fxmlLoader.setLocation(url);
        Parent popupRoot = fxmlLoader.load(url.openStream());

        SortController sortController = fxmlLoader.getController();
        this.sortController = sortController;

        sortController.setMainController(this);
        sortController.init();

        this.sortStage = new Stage();
        sortStage.initModality(Modality.APPLICATION_MODAL);
        sortStage.setTitle(title);
        sortStage.setOnCloseRequest((WindowEvent event) -> isSortActiveProperty.set(false));

        Scene popupScene = new Scene(popupRoot, 770, 200);
        sortStage.setResizable(true);
        sortStage.setScene(popupScene);

        sortStage.show();

    }

    public void getFilteredSheetRunLater(FilterDesignDto responseDto) {
        this.mainController.getFilteredSheetRunLater(responseDto);
    }

    public void getSortedSheetRunLater(SortDesignDto responseDto) {
        this.mainController.getSortedSheetRunLater(responseDto);
    }


    // TODO: functions that will be deleted eventually.
//    public boolean isBoundariesValidForCurrentSheet(Boundaries boundaries) {
//        return this.mainController.isBoundariesValidForCurrentSheet(boundaries);
//    }
//
//    public boolean isNumericColumn(int column, int startRow, int endRow) {
//        return mainController.isNumericColumn(column,startRow,endRow);
//    }
}
