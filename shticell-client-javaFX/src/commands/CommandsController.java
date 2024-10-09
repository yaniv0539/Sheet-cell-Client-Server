package commands;

import app.AppController;
import commands.operations.filter.FilterController;
import commands.operations.sort.SortController;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sheet.range.boundaries.api.Boundaries;

import java.io.IOException;
import java.net.URL;
import java.util.List;


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

    private IntegerProperty heightProperty;

    private IntegerProperty widthProperty;

    private Stage filterStage;
    private Stage sortStage;

    private boolean startFilter = true;
    private boolean startSort = true;
    private BooleanProperty isSortPopupClosed = new SimpleBooleanProperty(false);
    private BooleanProperty isFilterPopupClosed = new SimpleBooleanProperty(false);

    public AppController getMainController() {
        return mainController;
    }

    public Button getButtonResetToDefault() {
        return buttonResetToDefault;
    }

    public ColorPicker getColorPickerBackgroundColor() {
        return colorPickerBackgroundColor;
    }

    public ColorPicker getColorPickerTextColor() {
        return colorPickerTextColor;
    }

    public ComboBox<Pos> getComboBoxAlignment() {
        return comboBoxAlignment;
    }

    public Spinner<Integer> getSpinnerHeight() {
        return spinnerHeight;
    }

    public Spinner<Integer> getSpinnerWidth() {
        return spinnerWidth;
    }

    public int getHeightProperty() {
        return heightProperty.get();
    }

    public IntegerProperty heightPropertyProperty() {
        return heightProperty;
    }

    public int getWidthProperty() {
        return widthProperty.get();
    }

    public IntegerProperty widthPropertyProperty() {
        return widthProperty;
    }

    public Button getButtonFilter() {
        return buttonFilter;
    }

    public Button getButtonSort() {
        return buttonSort;
    }

    public CommandsController() {
        heightProperty = new SimpleIntegerProperty();
        widthProperty = new SimpleIntegerProperty();
    }

    @FXML
    private void initialize() {

    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
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
        if (startFilter) {
            activateFilterPopup(FILTER_POPUP_FXML_INCLUDE_RESOURCE, "Filter");
            startFilter = false;
        } else {
            mainController.resetFilter();
            buttonSort.setDisable(false);
            resetButtonFilter();
        }
    }
    public void resetButtonFilter(){
        buttonFilter.setText("Filter");
        startFilter = true;
    }

    @FXML
    void sortAction(ActionEvent event) throws IOException{
        if (startSort) {
            activateSortPopup(SORT_POPUP_FXML_INCLUDE_RESOURCE, "Sort");
            startSort = false;
        } else {
            mainController.resetSort();
            buttonFilter.setDisable(false);
            resetButtonSort();
        }
    }

    private void activateSortPopup(String resource, String title) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(resource);
        fxmlLoader.setLocation(url);
        Parent popupRoot = fxmlLoader.load(url.openStream());

        SortController sortController = fxmlLoader.getController();

        sortController.setMainController(this);
        sortController.init();

        this.sortStage = new Stage();
        sortStage.initModality(Modality.APPLICATION_MODAL);
        sortStage.setTitle(title);
        sortStage.setOnCloseRequest((WindowEvent event) -> startSort = true);

        Scene popupScene = new Scene(popupRoot, 770, 200);
        sortStage.setResizable(true);
        sortStage.setScene(popupScene);

        sortStage.show();

    }

    public void resetButtonSort() {
        buttonSort.setText("Sort");
        startSort = true;
    }

    @FXML
    void resetToDefaultAction(ActionEvent event) {
        mainController.resetCellsToDefault();
    }

    @FXML
    void textColorAction(ActionEvent event) {

    }

    public void init() {
        BooleanProperty showCommandsProperty = this.mainController.showCommandsProperty();
        buttonResetToDefault.disableProperty().bind(showCommandsProperty.not());
        buttonSort.setDisable(true);
        buttonFilter.setDisable(true);
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

    private void activateFilterPopup(String resource, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(resource);
        fxmlLoader.setLocation(url);
        Parent popupRoot = fxmlLoader.load(url.openStream());

        FilterController filterController = fxmlLoader.getController();

        filterController.setMainController(this);
        filterController.init();

        this.filterStage = new Stage();
        filterStage.initModality(Modality.APPLICATION_MODAL);
        filterStage.setTitle(title);
        filterStage.setOnCloseRequest((WindowEvent event) -> startFilter = true);

        Scene popupScene = new Scene(popupRoot, 770, 220);
        filterStage.setResizable(true);
        filterStage.setScene(popupScene);
        filterStage.show();
    }

    public void filterRange(Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues) {
        this.mainController.getFilteredSheet(boundariesToFilter, filteringByColumn, filteringByValues);
        buttonSort.setDisable(true);
        buttonFilter.setText("Reset Filter");
        filterStage.close();
    }
    public void sortRange(Boundaries boundariesToFilter, List<String> sortingByColumns) {
        this.mainController.getSortedSheet(boundariesToFilter, sortingByColumns);
        buttonFilter.setDisable(true);
        buttonSort.setText("Reset Sort");
        sortStage.close();
    }


    public boolean isBoundariesValidForCurrentSheet(Boundaries boundaries) {
        return this.mainController.isBoundariesValidForCurrentSheet(boundaries);
    }

    public boolean isNumericColumn(int column, int startRow, int endRow) {
        return mainController.isNumericColumn(column,startRow,endRow);
    }

    public List<String> getColumnUniqueValuesInRange(int column, int startRow, int endRow) {
        return mainController.getColumnUniqueValuesInRange(column, startRow,endRow);
    }
}
