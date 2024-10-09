package app;

import commands.CommandsController;
import engine.api.Engine;
import engine.impl.EngineImpl;
import header.HeaderController;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
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
import modelUI.api.EffectiveValuesPoolProperty;
import modelUI.api.EffectiveValuesPoolPropertyReadOnly;
import modelUI.api.FocusCellProperty;
import modelUI.impl.EffectiveValuesPoolPropertyImpl;
import modelUI.impl.FocusCellPropertyImpl;
import modelUI.impl.TextFieldDesign;
import modelUI.impl.VersionDesignManager;
import progress.ProgressController;
import ranges.RangesController;
import sheet.SheetController;
import sheet.api.SheetGetters;
import sheet.cell.api.Cell;
import sheet.cell.api.CellGetters;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;

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

    private SimpleBooleanProperty showCommands;
    private SimpleBooleanProperty showRanges;
    private SimpleBooleanProperty showHeaders;
    private ScrollPane sheetComponent;
    private SheetController sheetComponentController;
    private ProgressController progressComponentController;
    private Stage loadingStage;
    private Stage primaryStage;

    private VersionDesignManager versionDesignManager;
    private FocusCellProperty cellInFocus;
    private SheetGetters currentSheet;
    private EffectiveValuesPoolProperty effectiveValuesPool;
    private Engine engine;
    private boolean OperationView;


    public AppController() {
        this.engine = EngineImpl.create();
        this.showHeaders = new SimpleBooleanProperty(false);
        this.showRanges = new SimpleBooleanProperty(false);
        this.showCommands = new SimpleBooleanProperty(false);
        this.cellInFocus = new FocusCellPropertyImpl();
        this.effectiveValuesPool = new EffectiveValuesPoolPropertyImpl();
        this.progressComponentController = new ProgressController();
        this.loadingStage = new Stage();
        this.versionDesignManager = new VersionDesignManager();
        OperationView = false;
    }

    @FXML
    public void initialize() {
        if (headerComponentController != null && commandsComponentController != null && rangesComponentController != null) {
            headerComponentController.setMainController(this);
            commandsComponentController.setMainController(this);
            rangesComponentController.setMainController(this);
            versionDesignManager.setMainController(this);

            headerComponentController.init();
            commandsComponentController.init();
            rangesComponentController.init();
            initLoadingStage();

            //cell in focus init
            cellInFocus.getDependOn().addListener((ListChangeListener<Coordinate>) change -> sheetComponentController.changeColorDependedCoordinate(change));
            cellInFocus.getInfluenceOn().addListener((ListChangeListener<Coordinate>) change -> sheetComponentController.changeColorInfluenceCoordinate(change));
        }
    }

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

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void initLoadingStage() {

        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setScene(new Scene(progressComponentController.getProgressVbox()));
    }

    public void uploadXml(String path)
    {
        Task<Boolean> FileTask = engine.loadFileTask(path);

        progressComponentController.init(FileTask);

        FileTask.setOnSucceeded(workerStateEvent -> {
            loadingStage.close();
            onFinishLoadingFile();
        });

        FileTask.setOnFailed(workerStateEvent -> {
            loadingStage.close();
            Throwable exception = FileTask.getException();
            Platform.runLater(()->showAlertPopup(exception, "loading a file"));
        });

        loadingStage.show();

        new Thread(FileTask).start();
    }

    private void onFinishLoadingFile() {
        showHeaders.set(true);
        showRanges.set(true);
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(false);
        commandsComponentController.getButtonFilter().setDisable(false);
        commandsComponentController.getButtonSort().setDisable(false);
        commandsComponentController.resetButtonFilter();
        commandsComponentController.resetButtonSort();
        setEffectiveValuesPoolProperty(engine.getSheetStatus(), this.effectiveValuesPool);
        setSheet();
        this.currentSheet = engine.getSheetStatus();
        headerComponentController.clearVersionButton();
        headerComponentController.addMenuOptionToVersionSelection(String.valueOf(engine.getVersionsManagerStatus().getVersions().size()));
        rangesComponentController.uploadRanges(engine.getRanges());
        versionDesignManager.clear();
        saveDesignVersion(sheetComponentController.getGridPane());
        versionDesignManager.addVersion();
    }

    private void setSheet() {
        sheetComponentController = new SheetController();
        sheetComponentController.setMainController(this);
        sheetComponent = sheetComponentController.getInitializedSheet(engine.getSheetStatus().getLayout(), effectiveValuesPool);
        appBorderPane.setCenter(sheetComponent);
    }


    private void setEffectiveValuesPoolProperty(SheetGetters sheetToView, EffectiveValuesPoolProperty effectiveValuesPool) {

        Map<Coordinate,CellGetters> map = sheetToView.getActiveCells();

        for(int row = 0; row < sheetToView.getLayout().getRows(); row++) {
            for(int column = 0; column < sheetToView.getLayout().getColumns(); column++) {
              Coordinate coordinate = CoordinateFactory.createCoordinate(row,column);
              CellGetters cell = map.get(coordinate);
                if(cell != null){
                    effectiveValuesPool.addEffectiveValuePropertyAt(coordinate, cell.getEffectiveValue().toString());
                }
                else {
                    effectiveValuesPool.addEffectiveValuePropertyAt(coordinate, "");
                }
            }
        }
    }

    public void focusChanged(boolean newValue, Coordinate coordinate) {

        if (newValue && !OperationView )
        {
            showCommands.set(currentSheet.getVersion() == engine.getVersionsManagerStatus().getVersions().size());
            Cell cell = currentSheet.getCell(coordinate);
            cellInFocus.setCoordinate(coordinate.toString());

            if (cell != null) {
                cellInFocus.setOriginalValue(cell.getOriginalValue());
                cellInFocus.setLastUpdateVersion(String.valueOf(cell.getVersion()));
                cellInFocus.setDependOn(cell.getInfluenceFrom().stream()
                        .map(CellGetters::getCoordinate)
                        .collect(Collectors.toSet()));
                cellInFocus.setInfluenceOn(cell.getInfluenceOn().stream()
                        .map(CellGetters::getCoordinate)
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

    public EffectiveValuesPoolPropertyReadOnly getEffectiveValuesPool() {
        return effectiveValuesPool;
    }

    public void updateCell() {
        try{
            engine.updateCellStatus(cellInFocus.getCoordinate().get(), cellInFocus.getOriginalValue().get());
            this.currentSheet = engine.getSheetStatus();
            setEffectiveValuesPoolProperty(engine.getSheetStatus(), this.effectiveValuesPool);
            versionDesignManager.addVersion();
            //need to make in engine version manager, current version number.
            headerComponentController.addMenuOptionToVersionSelection(String.valueOf(engine.getVersionsManagerStatus().getVersions().size()));
        }catch(Exception e){
            showAlertPopup(e, "updating cell " + "\"" + cellInFocus.getCoordinate().get() + "\"");
        }
    }

    private void saveDesignVersion(GridPane gridPane) {
        versionDesignManager.saveVersionDesign(gridPane);
    }

    public void viewSheetVersion(String numberOfVersion) {
        //TODO:need to change it to some toggle on/off for disable enable
        //TODO: need to put a current version showing, and if we pick the newest version the button would not be disable.
        //TODO: the disable make exeption.
        currentSheet = engine.getVersionsManagerStatus().getVersion(Integer.parseInt(numberOfVersion));
        showCommands.set(Integer.parseInt(numberOfVersion) == engine.getVersionsManagerStatus().getVersions().size());
        showRanges.set(Integer.parseInt(numberOfVersion) == engine.getVersionsManagerStatus().getVersions().size());
        showHeaders.set(Integer.parseInt(numberOfVersion) == engine.getVersionsManagerStatus().getVersions().size());
        setEffectiveValuesPoolProperty(currentSheet, this.effectiveValuesPool);
        resetSheetToVersionDesign(Integer.parseInt(numberOfVersion));
    }

    private void resetSheetToVersionDesign(int numberOfVersion) {
        if(numberOfVersion == engine.getVersionsManagerStatus().getVersions().size()){
            numberOfVersion++;
        }
        sheetComponentController.setGridPaneDesign(versionDesignManager.getVersionDesign(numberOfVersion));
    }

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
        versionDesignManager.getVersionDesign(currentSheet.getVersion()+1).getColumnsLayoutVersion().put(column,prefWidth);
    }

    public void changeSheetRowHeight(int prefHeight) {
        int row =
                CoordinateFactory.extractRow(
                        cellInFocus
                                .getCoordinate()
                                .get());
        sheetComponentController.changeRowHeight(row, prefHeight);
        //itay change for saving on edit version the design
        versionDesignManager.getVersionDesign(currentSheet.getVersion()+1).getRowsLayoutVersion().put(row,prefHeight);
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

                versionDesignManager.getVersionDesign(currentSheet.getVersion() + 1).getCellDesignsVersion()
                        .compute(i, (k, textFieldDesign) -> new TextFieldDesign(textFieldDesign.getBackgroundColor(), textFieldDesign.getTextStyle(), pos));
            }
        }
    }

    public void changeSheetCellBackgroundColor(Color color) {
        sheetComponentController.changeCellBackgroundColor(color);

        //itay change for saving on edit version the design

        versionDesignManager.getVersionDesign(currentSheet.getVersion() + 1).getCellDesignsVersion()
                .compute(sheetComponentController.getIndexDesign(CoordinateFactory.toCoordinate(cellInFocus.getCoordinate().get()))
                        , (k, textFieldDesign) -> new TextFieldDesign(color, textFieldDesign.getTextStyle(), textFieldDesign.getTextAlignment()));



    }

    public void changeSheetTextColor(Color color) {
        sheetComponentController.changeCellTextColor(color);
        //itay change for saving on edit version the design

        versionDesignManager.getVersionDesign(currentSheet.getVersion() + 1).getCellDesignsVersion()
                .compute(sheetComponentController.getIndexDesign(CoordinateFactory.toCoordinate(cellInFocus.getCoordinate().get()))
                        , (k, textFieldDesign) -> new TextFieldDesign(textFieldDesign.getBackgroundColor(), "-fx-text-fill: " + sheetComponentController.toHexString(color) + ";", textFieldDesign.getTextAlignment()));
    }

    public void resetCellsToDefault() {
       // sheetComponentController.resetCellsToDefault(CoordinateFactory.toCoordinate(cellInFocus.getCoordinate().get()));
        //Coordinate cellToDefault = CoordinateFactory.toCoordinate(cellInFocus.getCoordinate().get());
        changeCommandsCellBackgroundColor(Color.WHITE);
        changeCommandsCellTextColor(Color.BLACK);
    }




    public void addRange(String name, String boundaries) {
           if(engine.addRange(name, boundaries)){
               this.currentSheet = engine.getSheetStatus();
               setEffectiveValuesPoolProperty(engine.getSheetStatus(), this.effectiveValuesPool);
               versionDesignManager.addVersion();
               //need to make in engine version manager, current version number.
               headerComponentController.addMenuOptionToVersionSelection(String.valueOf(engine.getVersionsManagerStatus().getVersions().size()));
           }
    }

    public RangeGetters getRange(String name) {
        return engine.getRange(name);
    }

    public void deleteRange(RangeGetters range) throws Exception {

        Collection<Coordinate> coordinates = rangeUses(range);
        if (coordinates.isEmpty()) {
            engine.deleteRange(range.getName());
        } else {
            throw new Exception("Can not delete range in use !\nCells that using range: " + coordinates.toString());
        }
    }

    private Collection<Coordinate> rangeUses(RangeGetters range) {

        return this.currentSheet.rangeUses(range);
    }

    public void paintRangeOnSheet(RangeGetters range, Color color) {
        this.sheetComponentController.paintRangeOnSheet(range, color);
    }

    public void getFilteredSheet(Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues) {

        OperationView = true;
        SheetGetters filteredSheet = engine.filter(boundariesToFilter, filteringByColumn, filteringByValues, currentSheet.getVersion());

        EffectiveValuesPoolProperty effectiveValuesPoolProperty = new EffectiveValuesPoolPropertyImpl();
        setEffectiveValuesPoolProperty(filteredSheet, effectiveValuesPoolProperty);

        SheetController filteredSheetComponentController = new SheetController();
        filteredSheetComponentController.setMainController(this);
        ScrollPane sheetComponent = filteredSheetComponentController.getInitializedSheet(filteredSheet.getLayout(), effectiveValuesPoolProperty);

        //design
        VersionDesignManager.VersionDesign design;

        if(currentSheet.getVersion() == engine.getVersionsManagerStatus().getVersions().size()){
            design = versionDesignManager.getVersionDesign(currentSheet.getVersion() + 1 );
        }else{
            design = versionDesignManager.getVersionDesign(currentSheet.getVersion());
        }

        filteredSheetComponentController.setColumnsDesign(design.getColumnsLayoutVersion());
        filteredSheetComponentController.setRowsDesign(design.getRowsLayoutVersion());

        Map<Coordinate,Coordinate> oldToNew = engine.filteredMap(boundariesToFilter, filteringByColumn, filteringByValues, currentSheet.getVersion());
        // design on range works
        oldToNew.forEach((coordinateWithDesign,coordinateToDesign) -> {
            int indexDesign = filteredSheetComponentController.getIndexDesign(coordinateWithDesign);

            filteredSheetComponentController.setCoordinateDesign(coordinateToDesign,design.getCellDesignsVersion()
                    .get(indexDesign));

        });

        //design the out of range cells
        for (int row = 0; row <= filteredSheet.getLayout().getRows() ; row++) {
            for (int col = 0;col <= filteredSheet.getLayout().getColumns() ; col++) {
                int indexDesign;
                if(row < boundariesToFilter.getFrom().getRow() || row > boundariesToFilter.getTo().getRow() ||
                        col < boundariesToFilter.getFrom().getCol() || col > boundariesToFilter.getTo().getCol()){

                    Coordinate coordinate = CoordinateFactory.createCoordinate(row,col);
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

    public void resetFilter() {

        OperationView = false;
        viewSheetVersion(String.valueOf(currentSheet.getVersion()));
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(false);
//        showHeaders.set(true);
//        showRanges.set(true);
//        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(false);
//        commandsComponentController.getButtonFilter().setDisable(false);

        appBorderPane.setCenter(sheetComponent);
    }

    public void getSortedSheet(Boundaries boundariesToSort, List<String> sortingByColumns) {

        OperationView = true;

        SheetGetters sortedSheet = engine.sortSheet(boundariesToSort, sortingByColumns, currentSheet.getVersion());

        EffectiveValuesPoolProperty effectiveValuesPoolProperty = new EffectiveValuesPoolPropertyImpl();
        setEffectiveValuesPoolProperty(sortedSheet, effectiveValuesPoolProperty);

        SheetController sortedSheetComponentController = new SheetController();
        sortedSheetComponentController.setMainController(this);
        ScrollPane sheetComponent = sortedSheetComponentController.getInitializedSheet(sortedSheet.getLayout(),effectiveValuesPoolProperty);

        //design the cells
        VersionDesignManager.VersionDesign design;


        if(currentSheet.getVersion() == engine.getVersionsManagerStatus().getVersions().size()){
            design = versionDesignManager.getVersionDesign(currentSheet.getVersion() + 1 );
        }else{
            design = versionDesignManager.getVersionDesign(currentSheet.getVersion());
        }

        sortedSheetComponentController.setColumnsDesign(design.getColumnsLayoutVersion());
        sortedSheetComponentController.setRowsDesign(design.getRowsLayoutVersion());

        List<List<CellGetters>> sortedCellsInRange = engine.sortCellsInRange(boundariesToSort, sortingByColumns, currentSheet.getVersion());

        for(int row = 0; row <= sortedSheet.getLayout().getRows() ; row++){
            List<CellGetters> sortedCells = new ArrayList<>();
            if(row >= boundariesToSort.getFrom().getRow() && row <= boundariesToSort.getTo().getRow()){
                 sortedCells = sortedCellsInRange.get(row - boundariesToSort.getFrom().getRow());
            }

            for(int col = 0; col <= sortedSheet.getLayout().getColumns() ; col++){
                Coordinate dest = CoordinateFactory.createCoordinate(row, col);
                int indexDesign;
                if(row >= boundariesToSort.getFrom().getRow() && row <= boundariesToSort.getTo().getRow() &&
                        col >= boundariesToSort.getFrom().getCol() && col <= boundariesToSort.getTo().getCol()){

                    Coordinate source = sortedCells.get(col - boundariesToSort.getFrom().getCol()).getCoordinate();
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

    public boolean isBoundariesValidForCurrentSheet(Boundaries boundaries) {
        return currentSheet.isRangeInBoundaries(boundaries);
    }

    public Color getBackground(TextField tf) {
        return sheetComponentController.getTextFieldBackgroundColor(tf.getBackground());
    }

    public void resetRangeOnSheet(RangeGetters selectedItem) {
        sheetComponentController.resetRangeOnSheet(selectedItem);
    }

    public void resetSort() {
        OperationView = false;
        viewSheetVersion(String.valueOf(currentSheet.getVersion()));

        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(false);
        appBorderPane.setCenter(sheetComponent);
    }

    public boolean isNumericColumn(int column, int startRow, int endRow) {
        return currentSheet.isColumnNumericInRange(column,startRow,endRow);
    }

    public List<String> getColumnUniqueValuesInRange(int column, int startRow, int endRow) {
        return currentSheet.getColumnUniqueValuesInRange(column,startRow,endRow);
    }

    public void showAlertPopup(Throwable exception,String error) {
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
}
