package component.main.center.app;

import component.main.MainController;
import component.main.center.app.commands.CommandsController;
import component.main.center.app.header.HeaderController;
import component.main.center.app.ranges.RangesController;
import component.main.center.app.sheet.SheetController;
import dto.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import component.modelUI.api.EffectiveValuesPoolProperty;
import component.modelUI.api.EffectiveValuesPoolPropertyReadOnly;
import component.modelUI.api.FocusCellProperty;
import component.modelUI.impl.EffectiveValuesPoolPropertyImpl;
import component.modelUI.impl.FocusCellPropertyImpl;
import component.modelUI.impl.TextFieldDesign;
import component.modelUI.impl.VersionDesignManager;
import component.progress.ProgressController;
import okhttp3.*;
import sheet.coordinate.impl.CoordinateFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AppController {

    @FXML private BorderPane appBorderPane;
    @FXML private ScrollPane headerComponent;
    @FXML private HeaderController headerComponentController;
    @FXML private ScrollPane commandsComponent;
    @FXML private CommandsController commandsComponentController;
    @FXML private ScrollPane rangesComponent;
    @FXML private RangesController rangesComponentController;
    private ScrollPane sheetComponent;

    MainController mainController;

    private Stage loadingStage;
    private Stage primaryStage;

    private SheetDto currentSheet;

    private FocusCellProperty cellInFocus;
    private EffectiveValuesPoolProperty effectiveValuesPool;
    private VersionDesignManager versionDesignManager;

    private SimpleBooleanProperty showCommands;
    private SimpleBooleanProperty showRanges;
    private SimpleBooleanProperty showHeaders;

    private SheetController sheetComponentController;
    private ProgressController progressComponentController;

    //private VersionDesignManager versionDesignManager;
    private Map<String,VersionDesignManager> sheetToVersionDesignManager;

    private int mostUpdatedVersionNumber;
    private boolean OperationView;


    // Constructor

    public AppController() {
        this.showHeaders = new SimpleBooleanProperty(false);
        this.showRanges = new SimpleBooleanProperty(false);
        this.showCommands = new SimpleBooleanProperty(false);
        this.cellInFocus = new FocusCellPropertyImpl();
        this.effectiveValuesPool = new EffectiveValuesPoolPropertyImpl();
        this.progressComponentController = new ProgressController();
        this.loadingStage = new Stage();
        this.sheetToVersionDesignManager = new HashMap<>();
        OperationView = false;
    }


    // Initializers

    @FXML
    public void initialize() {
        if (headerComponentController != null && commandsComponentController != null && rangesComponentController != null) {
            headerComponentController.setMainController(this);
            commandsComponentController.setMainController(this);
            rangesComponentController.setMainController(this);
            //versionDesignManager.setMainController(this);

            headerComponentController.init();
            commandsComponentController.init();
            rangesComponentController.init();
            initLoadingStage();

            //cell in focus init.
            cellInFocus.getDependOn().addListener((ListChangeListener<CoordinateDto>) change -> sheetComponentController.changeColorDependedCoordinate(change));
            cellInFocus.getInfluenceOn().addListener((ListChangeListener<CoordinateDto>) change -> sheetComponentController.changeColorInfluenceCoordinate(change));
        }
    }

    private void initLoadingStage() {

        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setScene(new Scene(progressComponentController.getProgressVbox()));
    }


    // Getters

    public SimpleBooleanProperty showCommandsProperty() {
        return showCommands;
    }

    public SimpleBooleanProperty showRangesProperty() {
        return showRanges;
    }

    public SimpleBooleanProperty showHeadersProperty() {
        return showHeaders;
    }

    public FocusCellProperty getCellInFocus() {
        return cellInFocus;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public EffectiveValuesPoolPropertyReadOnly getEffectiveValuesPool() {
        return effectiveValuesPool;
    }

    public Color getBackground(TextField tf) {
        return sheetComponentController.getTextFieldBackgroundColor(tf.getBackground());
    }
    
    // Setters

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void setSheet(SheetDto sheetDto) {
        sheetComponentController = new SheetController();
        sheetComponentController.setMainController(this);
        sheetComponent = sheetComponentController.getInitializedSheet(sheetDto.getLayout(), effectiveValuesPool);
        appBorderPane.setCenter(sheetComponent);
    }
    
    private void setEffectiveValuesPoolProperty(SheetDto sheetToView, EffectiveValuesPoolProperty effectiveValuesPool) {

        Map<String, CellDto> map = sheetToView.getActiveCells();

        for(int row = 0; row < sheetToView.getLayout().getRows(); row++) {
            for(int column = 0; column < sheetToView.getLayout().getColumns(); column++) {
                String coordinateString = CoordinateFactory.createCoordinate(row,column).toString();
                CellDto cell = map.get(coordinateString);
                if(cell != null){
                    effectiveValuesPool.addEffectiveValuePropertyAt(coordinateString, cell.getEffectiveValue());
                }
                else {
                    effectiveValuesPool.addEffectiveValuePropertyAt(coordinateString, "");
                }
            }
        }
    }

    private void saveDesignVersion(GridPane gridPane) {
        sheetToVersionDesignManager.get(currentSheet.getName()).saveVersionDesign(gridPane);
    }


    // Http requests to shticell servlet

    public void getSheet(String numberOfVersion, Callback callback) {
        this.mainController.getSheet(this.currentSheet.getName(), numberOfVersion, callback);
    }

    public void addRange(String name, String boundaries, Callback callback) {
        this.mainController.postRange(this.currentSheet.getName(), String.valueOf(currentSheet.getVersion()), name, boundaries, callback);
    }

    public void deleteRange(String rangeName, Callback callback) {
        this.mainController.deleteRange(this.currentSheet.getName(), String.valueOf(currentSheet.getVersion()), rangeName, callback);
    }

    public void getBoundariesDto(String text, Callback callback) {
        this.mainController.getBoundariesDto(this.currentSheet.getName(), text, callback);
    }

    public void getColumnUniqueValuesInRange(int column, int startRow, int endRow, Callback callback) {
        this.mainController.getColumnUniqueValuesInRange(
                this.currentSheet.getName(),
                String.valueOf(this.currentSheet.getVersion()),
                String.valueOf(column),
                String.valueOf(startRow),
                String.valueOf(endRow), callback);
    }

    public void getFilteredSheet(FilterDto data, Callback callback) {
        this.mainController.getFilteredSheet(this.currentSheet.getName(), String.valueOf(this.currentSheet.getVersion()), data, callback);
    }

    public void getNumericColumnsInBoundaries(String boundaries, Callback callback) {
        this.mainController.getNumericColumnsInBoundaries(this.currentSheet.getName(), String.valueOf(currentSheet.getVersion()), boundaries, callback);
    }

    public void getSortedSheet(SortDto sortDto, Callback callback) {
        this.mainController.getSortedSheet(this.currentSheet.getName(), String.valueOf(currentSheet.getVersion()), sortDto, callback);
    }

    public void uploadXml(String path, Callback callback) {
        this.mainController.postXMLFile(path, callback);
    }

    public void updateCell(Callback callback) {
        this.mainController.postCell(this.currentSheet.getName(), cellInFocus.getCoordinate().get(), cellInFocus.getOriginalValue().get(), callback);
    }


    // Run later functions

    public void getFilteredSheetRunLater(FilterDesignDto responseDto) {

        OperationView = true;
        commandsComponentController.filterCommandsControllerRunLater();
        SheetDto filteredSheet = responseDto.getSheet();
        EffectiveValuesPoolProperty effectiveValuesPoolProperty = new EffectiveValuesPoolPropertyImpl();
        setEffectiveValuesPoolProperty(filteredSheet, effectiveValuesPoolProperty);

        SheetController filteredSheetComponentController = new SheetController();
        filteredSheetComponentController.setMainController(this);
        ScrollPane sheetComponent = filteredSheetComponentController.getInitializedSheet(filteredSheet.getLayout(), effectiveValuesPoolProperty);

        //design
        VersionDesignManager.VersionDesign design;

        if(currentSheet.getVersion() == mostUpdatedVersionNumber){
            design = sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion() + 1 );
        }else{
            design = sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion());
        }

        filteredSheetComponentController.setColumnsDesign(design.getColumnsLayoutVersion());
        filteredSheetComponentController.setRowsDesign(design.getRowsLayoutVersion());

        //Map<Coordinate,Coordinate> oldToNew = engine.filteredMap(boundariesToFilter, filteringByColumn, filteringByValues, currentSheet.getVersion());
        Map<CoordinateDto,CoordinateDto> oldToNew = responseDto.getFilterMap();
        // design on range works
        oldToNew.forEach((coordinateWithDesign,coordinateToDesign) -> {
            int indexDesign = filteredSheetComponentController.getIndexDesign(coordinateWithDesign);

            filteredSheetComponentController.setCoordinateDesign(coordinateToDesign,design.getCellDesignsVersion()
                    .get(indexDesign));

        });
        BoundariesDto boundariesToFilter = responseDto.getBoundariesFilter();
        //design the out of range cells
        for (int row = 0; row <= filteredSheet.getLayout().getRows() ; row++) {
            for (int col = 0;col <= filteredSheet.getLayout().getColumns() ; col++) {
                int indexDesign;
                if(row < boundariesToFilter.getFrom().getRow() || row > boundariesToFilter.getTo().getRow() ||
                        col < boundariesToFilter.getFrom().getColumn() || col > boundariesToFilter.getTo().getColumn()){

                    CoordinateDto coordinate = new CoordinateDto(row,col);
                    indexDesign = filteredSheetComponentController.getIndexDesign(coordinate);
                    filteredSheetComponentController.setCoordinateDesign(coordinate,design.getCellDesignsVersion()
                            .get(indexDesign));
                }
            }
        }

        //design
        appBorderPane.setCenter(sheetComponent);
        showHeaders.set(false);
        showRanges.set(false);
        showCommands.set(false);
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(true);
        commandsComponentController.getButtonFilter().setDisable(false);
    }

    public void onFinishLoadingFile(SheetDto sheetDto) {
        //methode
        showHeaders.set(true);
        showRanges.set(true);
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(false);
        commandsComponentController.getButtonFilter().setDisable(false);
        commandsComponentController.getButtonSort().setDisable(false);
        commandsComponentController.resetButtonFilter();
        commandsComponentController.resetButtonSort();

        this.currentSheet = sheetDto;//this what server bring
        setEffectiveValuesPoolProperty(currentSheet, this.effectiveValuesPool);
        setSheet(currentSheet);
        mostUpdatedVersionNumber = sheetDto.getVersion();
        headerComponentController.clearVersionButton();
        headerComponentController.addMenuOptionToVersionSelection("1");
        rangesComponentController.uploadRanges(currentSheet.getRanges());

        VersionDesignManager designManagerForSheet = new VersionDesignManager();
        designManagerForSheet.setMainController(this);
        sheetToVersionDesignManager.put(currentSheet.getName(), designManagerForSheet);
        saveDesignVersion(sheetComponentController.getGridPane());
        sheetToVersionDesignManager.get(currentSheet.getName()).addVersion();
    }

    public void getSortedSheetRunLater(SortDesignDto sortDesignDto) {
        OperationView = true;
        commandsComponentController.sortCommandsControllerRunLater();
        SheetDto sortedSheet = sortDesignDto.getSheetDto();
        EffectiveValuesPoolProperty effectiveValuesPoolProperty = new EffectiveValuesPoolPropertyImpl();
        setEffectiveValuesPoolProperty(sortedSheet, effectiveValuesPoolProperty);

        SheetController sortedSheetComponentController = new SheetController();
        sortedSheetComponentController.setMainController(this);
        ScrollPane sheetComponent = sortedSheetComponentController.getInitializedSheet(sortedSheet.getLayout(),effectiveValuesPoolProperty);

        //design the cells
        VersionDesignManager.VersionDesign design;


        if(currentSheet.getVersion() == mostUpdatedVersionNumber){
            design = sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion() + 1 );
        }else{
            design = sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion());
        }

        sortedSheetComponentController.setColumnsDesign(design.getColumnsLayoutVersion());
        sortedSheetComponentController.setRowsDesign(design.getRowsLayoutVersion());

        List<List<CoordinateDto>> sortedCellsInRange = sortDesignDto.getCoordinateDtos();
        BoundariesDto boundariesToSort = sortDesignDto.getBoundariesDto();

        for(int row = 0; row <= sortedSheet.getLayout().getRows() ; row++){
            List<CoordinateDto> sortedCells = new ArrayList<>();
            if(row >= boundariesToSort.getFrom().getRow() && row <= boundariesToSort.getTo().getRow()){
                sortedCells = sortedCellsInRange.get(row - boundariesToSort.getFrom().getRow());
            }

            for(int col = 0; col <= sortedSheet.getLayout().getColumns() ; col++){
                CoordinateDto dest = new CoordinateDto(row, col);
                int indexDesign;
                if(row >= boundariesToSort.getFrom().getRow() && row <= boundariesToSort.getTo().getRow() &&
                        col >= boundariesToSort.getFrom().getColumn() && col <= boundariesToSort.getTo().getColumn()){

                    CoordinateDto source = sortedCells.get(col - boundariesToSort.getFrom().getColumn());
                    indexDesign = sortedSheetComponentController.getIndexDesign(source);

                    sortedSheetComponentController.setCoordinateDesign(dest,design.getCellDesignsVersion()
                            .get(indexDesign));

                }
                else{
                    indexDesign = sortedSheetComponentController.getIndexDesign(dest);
                    sortedSheetComponentController.setCoordinateDesign(dest,design.getCellDesignsVersion()
                            .get(indexDesign));
                }

            }
        }

        //finish design
        appBorderPane.setCenter(sheetComponent);

        showHeaders.set(false);
        showRanges.set(false);
        showCommands.set(false);
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(true);
        commandsComponentController.getButtonSort().setDisable(false);
    }

    public void getViewSheetVersionRunLater(SheetDto sheetDto) {
        currentSheet = sheetDto;
        int numberOfVersion = currentSheet.getVersion();

        showCommands.set(numberOfVersion == mostUpdatedVersionNumber);
        showRanges.set(numberOfVersion == mostUpdatedVersionNumber);
        showHeaders.set(numberOfVersion == mostUpdatedVersionNumber);
        setEffectiveValuesPoolProperty(currentSheet, effectiveValuesPool);
        resetSheetToVersionDesign(numberOfVersion);
    }

    public void updateCellRunLater(SheetDto sheetDto){
        if (sheetDto.getVersion() != currentSheet.getVersion()) {
            currentSheet = sheetDto;
            mostUpdatedVersionNumber = currentSheet.getVersion();
            setEffectiveValuesPoolProperty(currentSheet, effectiveValuesPool);
            sheetToVersionDesignManager.get(currentSheet.getName()).addVersion();
            headerComponentController.addMenuOptionToVersionSelection(String.valueOf(currentSheet.getVersion()));
        }
    }


    // On change functions

    public void changeCommandsColumnWidth(double prefWidth) {
        commandsComponentController.changeColumnWidth((int) prefWidth);
    }

    public void changeCommandsRowHeight(double prefHeight) {
        commandsComponentController.changeRowHeight((int) prefHeight);
    }

    public void changeCommandsColumnAlignment(Pos alignment) {
        commandsComponentController.changeColumnAlignment(alignment);
    }

    public void changeCommandsCellBackgroundColor(Color color) {
        commandsComponentController.changeCellBackgroundColor(color);
    }

    public void changeCommandsCellTextColor(Color color) {
        commandsComponentController.changeCellTextColor(color);
    }

    public void changeSheetColumnWidth(int prefWidth) {
        int column =
                CoordinateFactory.parseColumnToInt(
                        CoordinateFactory.extractColumn(
                                cellInFocus
                                        .getCoordinate()
                                        .get()));
        sheetComponentController.changeColumnWidth(column, prefWidth);
        //itay change for saving on edit version the design
        sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion()+1).getColumnsLayoutVersion().put(column,prefWidth);
    }

    public void changeSheetRowHeight(int prefHeight) {
        int row =
                CoordinateFactory.extractRow(
                        cellInFocus
                                .getCoordinate()
                                .get());
        sheetComponentController.changeRowHeight(row, prefHeight);
        //itay change for saving on edit version the design
        sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion()+1).getRowsLayoutVersion().put(row,prefHeight);
    }

    public void changeSheetCellBackgroundColor(Color color) {
        sheetComponentController.changeCellBackgroundColor(color);

        //itay change for saving on edit version the design

        sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion() + 1).getCellDesignsVersion()
                .computeIfPresent(sheetComponentController.getIndexDesign(new CoordinateDto(cellInFocus.getCoordinate().get()))
                        , (k, textFieldDesign) -> new TextFieldDesign(color, textFieldDesign.getTextStyle(), textFieldDesign.getTextAlignment()));
    }

    public void changeSheetTextColor(Color color) {
        sheetComponentController.changeCellTextColor(color);
        //itay change for saving on edit version the design

        sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion() + 1).getCellDesignsVersion()
                .computeIfPresent(sheetComponentController.getIndexDesign(new CoordinateDto(cellInFocus.getCoordinate().get()))
                        , (k, textFieldDesign) -> new TextFieldDesign(textFieldDesign.getBackgroundColor(), "-fx-text-fill: " + sheetComponentController.toHexString(color) + ";", textFieldDesign.getTextAlignment()));
    }


    // Reset functions

    public void resetCellsToDefault() {
        changeCommandsCellBackgroundColor(Color.WHITE);
        changeCommandsCellTextColor(Color.BLACK);
    }

    public void resetRangeOnSheet(RangeDto selectedItem) {
        sheetComponentController.resetRangeOnSheet(selectedItem);
    }

    private void resetSheetToVersionDesign(int numberOfVersion) {
        if(numberOfVersion == mostUpdatedVersionNumber){
            numberOfVersion++;
        }
        sheetComponentController.setGridPaneDesign(sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(numberOfVersion));
    }

    public void resetOperationView() {

        OperationView = false;
        int numberOfVersion = currentSheet.getVersion();
        showCommands.set(numberOfVersion == mostUpdatedVersionNumber);
        showRanges.set(numberOfVersion == mostUpdatedVersionNumber);
        showHeaders.set(numberOfVersion == mostUpdatedVersionNumber);
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(false);
        appBorderPane.setCenter(sheetComponent);
    }


    // Other functions

    public void showAlertPopup(Throwable exception, String error) {
        // Create a new alert dialog for the error
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An Error Occurred While " + error);
        TextArea textArea = new TextArea();
        if (exception != null) {
            textArea.setText(exception.getMessage());
        } else {
            textArea.setText("An unknown error occurred.");
        }
        textArea.setWrapText(true);
        textArea.setEditable(false);

        // Allow TextArea to expand dynamically with the window
        VBox content = new VBox(textArea);
        content.setPrefSize(300, 200);  // Adjust the size of the popup window

        // Add the TextArea to the Alert dialog
        alert.getDialogPane().setContent(content);

        // Make the dialog non-resizable if needed
        alert.initStyle(StageStyle.DECORATED);

        alert.showAndWait();  // Display the popup
    }

    public void focusChanged(boolean newValue, String coordinateString) {

        if (newValue && !OperationView )
        {
            showCommands.set(currentSheet.getVersion() == mostUpdatedVersionNumber);
            CellDto cell = currentSheet.getActiveCells().get(coordinateString);
            cellInFocus.setCoordinate(coordinateString);

            if (cell != null) {
                cellInFocus.setOriginalValue(cell.getOriginalValue());
                cellInFocus.setLastUpdateVersion(String.valueOf(cell.getVersion()));
                cellInFocus.setDependOn(cell.getInfluenceFrom().stream()
                        .map(CellDto::getCoordinate)
                        .collect(Collectors.toSet()));
                cellInFocus.setInfluenceOn(cell.getInfluenceOn().stream()
                        .map(CellDto::getCoordinate)
                        .collect(Collectors.toSet()));
            } else {
                cellInFocus.setOriginalValue("");
                cellInFocus.setLastUpdateVersion("");
                cellInFocus.setDependOn(new HashSet<>());
                cellInFocus.setInfluenceOn(new HashSet<>());
            }
        }
        else{
            cellInFocus.setDependOn(new HashSet<>());
            cellInFocus.setInfluenceOn(new HashSet<>());
        }
    }

    public void alignCells(Pos pos) {
        int column =
                CoordinateFactory.parseColumnToInt(
                        CoordinateFactory.extractColumn(
                                cellInFocus
                                        .getCoordinate()
                                        .get()));
        sheetComponentController.changeColumnAlignment(column, pos);

        //itay change for saving on edit version the design
        for (int i = 0; i < sheetComponentController.getGridPane().getChildren().size(); i++) {
            Node node = sheetComponentController.getGridPane().getChildren().get(i);
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (node instanceof TextField && colIndex == column && rowIndex != 0) {

                sheetToVersionDesignManager.get(currentSheet.getName()).getVersionDesign(currentSheet.getVersion() + 1).getCellDesignsVersion()
                        .computeIfPresent(i, (k, textFieldDesign) -> new TextFieldDesign(textFieldDesign.getBackgroundColor(), textFieldDesign.getTextStyle(), pos));
            }
        }
    }

    public void paintRangeOnSheet(RangeDto range, Color color) {
        this.sheetComponentController.paintRangeOnSheet(range, color);
    }


    // TODO: functions that will be deleted eventually.
//    public boolean isBoundariesValidForCurrentSheet(Boundaries boundaries) {
//        //function in sheet impl.
//        //return currentSheet.isRangeInBoundaries(boundaries);
//        return true;
//    }
//
//    public boolean isNumericColumn(int column, int startRow, int endRow) {
////        return currentSheet.isColumnNumericInRange(column,startRow,endRow);
//        return true;
//    }
}
