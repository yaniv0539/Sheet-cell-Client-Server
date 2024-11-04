package component.main.center.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import component.main.MainController;
import component.main.center.app.analysis.DynamicAnalysisController;
import component.main.center.app.commands.CommandsController;
import component.main.center.app.header.HeaderController;
import component.main.center.app.ranges.RangesController;
import component.main.center.app.sheet.SheetController;
import dto.*;
import dto.deserializer.CellDtoDeserializer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import component.main.center.app.model.api.EffectiveValuesPoolProperty;
import component.main.center.app.model.api.FocusCellProperty;
import component.main.center.app.model.impl.EffectiveValuesPoolPropertyImpl;
import component.main.center.app.model.impl.FocusCellPropertyImpl;
import component.main.center.app.model.impl.TextFieldDesign;
import component.main.center.app.model.impl.VersionDesignManager;
import component.main.center.app.progress.ProgressController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheet.coordinate.impl.CoordinateFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static utils.Constants.GSON_INSTANCE;

public class AppController {

    @FXML private Button buttonBackToDashboard;
    @FXML private BorderPane appBorderPane;
    @FXML private GridPane headerComponent;
    @FXML private HeaderController headerComponentController;
    @FXML private ScrollPane commandsComponent;
    @FXML private CommandsController commandsComponentController;
    @FXML private ScrollPane rangesComponent;
    @FXML private RangesController rangesComponentController;
    @FXML private ScrollPane dynamicComponent;
    @FXML private DynamicAnalysisController dynamicComponentController;

    private ScrollPane sheetComponent;

    private MainController mainController;

    private Stage loadingStage;

    private SheetDto currentSheet;
    private SheetDto editableSheet;

    private FocusCellProperty cellInFocusProperty;
    private EffectiveValuesPoolProperty effectiveValuesPool;

    private BooleanProperty showHeadersProperty;
    private BooleanProperty showCommandsProperty;
    private BooleanProperty showRangesProperty;
    private BooleanProperty showLogicalOperationsProperty;
    private BooleanProperty showDynamicSheetOperationsProperty;
    private BooleanProperty isDynamicSheetActiveProperty;
    private BooleanProperty isLogicalOperationsActiveProperty;
    private BooleanProperty isEditorProperty;

    private SheetController sheetComponentController;
    private ProgressController progressComponentController;

    private Map<String,VersionDesignManager> sheetToVersionDesignManager;

    private int mostUpdatedVersionNumber;
    private int tempMostUpdatedVersionNumber;
    private int lastVersionNumberBeforeUpdate;
    private ScheduledExecutorService executorServiceForSheet;
    private boolean isThreadsActive;
    private ObservableList<String> numericCoordinateObservableList;

    private CountDownLatch latch = new CountDownLatch(1);


    // Constructor

    public AppController() {
        this.showHeadersProperty = new SimpleBooleanProperty(true);
        this.showRangesProperty = new SimpleBooleanProperty(true);
        this.showCommandsProperty = new SimpleBooleanProperty(false);
        this.showLogicalOperationsProperty = new SimpleBooleanProperty(true);
        this.showDynamicSheetOperationsProperty = new SimpleBooleanProperty(true);
        this.isDynamicSheetActiveProperty = new SimpleBooleanProperty(false);
        this.isLogicalOperationsActiveProperty = new SimpleBooleanProperty(false);
        this.isEditorProperty = new SimpleBooleanProperty(false);

        this.cellInFocusProperty = new FocusCellPropertyImpl();
        this.effectiveValuesPool = new EffectiveValuesPoolPropertyImpl();
        this.progressComponentController = new ProgressController();

        this.loadingStage = new Stage();
        this.sheetToVersionDesignManager = new HashMap<>();
        this.numericCoordinateObservableList = FXCollections.observableArrayList();
    }


    // Initializers

    @FXML
    public void initialize() {
        if (headerComponentController != null && commandsComponentController != null && rangesComponentController != null && dynamicComponentController != null) {
            headerComponentController.setMainController(this);
            commandsComponentController.setMainController(this);
            rangesComponentController.setMainController(this);
            dynamicComponentController.setMainController(this);

            headerComponentController.init(showHeadersProperty, cellInFocusProperty);
            commandsComponentController.init(showCommandsProperty, showLogicalOperationsProperty);
            rangesComponentController.init(showRangesProperty);
            dynamicComponentController.init(showDynamicSheetOperationsProperty);

            // Cell in focus init.
            cellInFocusProperty.getDependOn().addListener((ListChangeListener<CoordinateDto>) change -> sheetComponentController.changeColorDependedCoordinate(change));
            cellInFocusProperty.getInfluenceOn().addListener((ListChangeListener<CoordinateDto>) change -> sheetComponentController.changeColorInfluenceCoordinate(change));

            isDynamicSheetActiveProperty.addListener((observable, oldValue, newValue) -> {
                showRangesProperty.set(!newValue && isEditorProperty.get());
                showHeadersProperty.set(!newValue && isEditorProperty.get());
                showCommandsProperty.set(!newValue && !cellInFocusProperty.getCoordinate().get().isEmpty());
                showLogicalOperationsProperty.set(!newValue);
            });

            isLogicalOperationsActiveProperty.addListener((observable, oldValue, newValue) -> {
                showRangesProperty.set(!newValue && isEditorProperty.get());
                showHeadersProperty.set(!newValue && isEditorProperty.get());
                showCommandsProperty.set(!newValue && !cellInFocusProperty.getCoordinate().get().isEmpty());
                showDynamicSheetOperationsProperty.set(!newValue);
            });

            isEditorProperty.addListener((observable, oldValue, newValue) -> {
                showRangesProperty.set(newValue);
                showHeadersProperty.set(newValue);
            });

            initLoadingStage();
            initPullThread();
        }
    }

    private void initLoadingStage() {
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setScene(new Scene(progressComponentController.getProgressVbox()));
    }

    private void initPullThread() {
        this.executorServiceForSheet = Executors.newSingleThreadScheduledExecutor();
    }

    // Getters

    public FocusCellProperty getCellInFocusProperty() {
        return cellInFocusProperty;
    }

    public Color getBackground(TextField tf) {
        return sheetComponentController.getTextFieldBackgroundColor(tf.getBackground());
    }

    // Setters

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setSheet(SheetDto sheetDto) {
        sheetComponentController = new SheetController();
        sheetComponentController.setMainController(this);
        sheetComponent = sheetComponentController.getInitializedSheet(sheetDto.layout(), effectiveValuesPool);
        appBorderPane.setCenter(sheetComponent);
    }
    
    private void setEffectiveValuesPoolProperty(SheetDto sheetToView, EffectiveValuesPoolProperty effectiveValuesPool) {
        Map<String, CellDto> map = sheetToView.activeCells();

        for (int row = 0; row < sheetToView.layout().rows(); row++) {
            for (int column = 0; column < sheetToView.layout().columns(); column++) {
                String coordinateString = CoordinateFactory.createCoordinate(row,column).toString();
                CellDto cell = map.get(coordinateString);

                if (cell != null) {
                    effectiveValuesPool.addEffectiveValuePropertyAt(coordinateString, cell.effectiveValue());
                } else {
                    effectiveValuesPool.addEffectiveValuePropertyAt(coordinateString, "");
                }
            }
        }
    }

    private void saveDesignVersion(GridPane gridPane) {
        sheetToVersionDesignManager.get(currentSheet.name()).saveVersionDesign(gridPane);
    }

    public void setActive(String sheetName) {
        if (isThreadsActive || executorServiceForSheet == null) {
            return; // Prevent starting multiple threads
        }

        isThreadsActive = true;

        executorServiceForSheet.scheduleAtFixedRate(() -> mainController.getSheet(sheetName, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> mainController.showAlertPopup(new Exception(),"show version"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();

                if (response.code() != 200) {
                    mainController.showAlertPopup(new Exception(GSON_INSTANCE.fromJson(jsonResponse,String.class)), "Sheet name " + sheetName);
                } else {
                    Gson gson = new GsonBuilder().registerTypeAdapter(CellDto.class,new CellDtoDeserializer()).create();
                    SheetDto sheetDto = gson.fromJson(jsonResponse, SheetDto.class);
                    Platform.runLater(() -> updateSheetView(sheetDto));
                }
            }
        }), 0, 1, TimeUnit.SECONDS);
    }

    public void setInActive() {
        if (!isThreadsActive) {
            return;
        }

        isThreadsActive = false;
        executorServiceForSheet.shutdownNow(); // Stop the background thread

        try {
            // Await termination of all tasks
            executorServiceForSheet.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Reinitialize the executor service to start again if needed
        executorServiceForSheet = Executors.newSingleThreadScheduledExecutor();
    }

    @FXML
    public void buttonBackToDashboardAction(ActionEvent actionEvent) {
        mainController.switchToDashboard();
    }

    // Http requests to shticell servlet

    public void getSheet(String numberOfVersion, Callback callback) {
        this.mainController.getSheet(this.currentSheet.name(), numberOfVersion, callback);
    }

    public void addRange(String name, String boundaries, Callback callback) {
        this.mainController.postRange(this.currentSheet.name(), String.valueOf(currentSheet.version()), name, boundaries, callback);
    }

    public void deleteRange(String rangeName, Callback callback) {
        this.mainController.deleteRange(this.currentSheet.name(), String.valueOf(currentSheet.version()), rangeName, callback);
    }

    public void getBoundariesDto(String text, Callback callback) {
        this.mainController.getBoundariesDto(this.currentSheet.name(), text, callback);
    }

    public void getColumnUniqueValuesInRange(int column, int startRow, int endRow, Callback callback) {
        this.mainController.getColumnUniqueValuesInRange(
                this.currentSheet.name(),
                String.valueOf(this.currentSheet.version()),
                String.valueOf(column),
                String.valueOf(startRow),
                String.valueOf(endRow), callback);
    }

    public void getFilteredSheet(FilterDto data, Callback callback) {
        this.mainController.getFilteredSheet(this.currentSheet.name(), String.valueOf(this.currentSheet.version()), data, callback);
    }

    public void getNumericColumnsInBoundaries(String boundaries, Callback callback) {
        this.mainController.getNumericColumnsInBoundaries(this.currentSheet.name(), String.valueOf(currentSheet.version()), boundaries, callback);
    }

    public void getSortedSheet(SortDto sortDto, Callback callback) {
        this.mainController.getSortedSheet(this.currentSheet.name(), String.valueOf(currentSheet.version()), sortDto, callback);
    }

    public void uploadXml(String path, Callback callback) {
        this.mainController.postXMLFile(path, callback);
    }

    public void updateCell(Callback callback) {
        this.mainController.postCell(this.currentSheet.name(), String.valueOf(this.currentSheet.version()), cellInFocusProperty.getCoordinate().get(), cellInFocusProperty.getOriginalValue().get(), callback);
    }

    public void updateCellToDynamicSheet(String cellName, String cellValue, Callback callback) {
        this.mainController.postCellToDynamicSheet(this.currentSheet.name(), String.valueOf(this.currentSheet.version()), cellName, cellValue, callback);
    }


    // Run later functions

    public void getFilteredSheetRunLater(FilterDesignDto responseDto) {
        commandsComponentController.filterCommandsControllerRunLater();

        SheetDto filteredSheet = responseDto.filteredSheet();

        EffectiveValuesPoolProperty effectiveValuesPoolProperty = new EffectiveValuesPoolPropertyImpl();
        setEffectiveValuesPoolProperty(filteredSheet, effectiveValuesPoolProperty);

        SheetController filteredSheetComponentController = new SheetController();
        filteredSheetComponentController.setMainController(this);
        ScrollPane sheetComponent = filteredSheetComponentController.getInitializedSheet(filteredSheet.layout(), effectiveValuesPoolProperty);

        //design
        VersionDesignManager.VersionDesign design;

        if (currentSheet.version() == mostUpdatedVersionNumber) {
            design = sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version() + 1 );
        } else{
            design = sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version());
        }

        filteredSheetComponentController.setColumnsDesign(design.getColumnsLayoutVersion());
        filteredSheetComponentController.setRowsDesign(design.getRowsLayoutVersion());

        //Map<Coordinate,Coordinate> oldToNew = engine.filteredMap(boundariesToFilter, filteringByColumn, filteringByValues, currentSheet.getVersion());
        Map<CoordinateDto,CoordinateDto> oldToNew = responseDto.coordinateBeforeAndAfterFiltering();

        // design on range works
        oldToNew.forEach((coordinateWithDesign,coordinateToDesign) -> {
            int indexDesign = filteredSheetComponentController.getIndexDesign(coordinateWithDesign);

            filteredSheetComponentController.setCoordinateDesign(coordinateToDesign,design.getCellDesignsVersion()
                    .get(indexDesign));

        });

        BoundariesDto boundariesToFilter = responseDto.filteredArea();

        //design the out of range cells
        for (int row = 0; row <= filteredSheet.layout().rows(); row++) {
            for (int col = 0;col <= filteredSheet.layout().columns(); col++) {
                int indexDesign;

                if (row < boundariesToFilter.from().row() || row > boundariesToFilter.to().row() ||
                        col < boundariesToFilter.from().column() || col > boundariesToFilter.to().column()) {

                    CoordinateDto coordinate = new CoordinateDto(row, col);
                    indexDesign = filteredSheetComponentController.getIndexDesign(coordinate);
                    filteredSheetComponentController.setCoordinateDesign(coordinate,design.getCellDesignsVersion()
                            .get(indexDesign));
                }
            }
        }

        //design
        appBorderPane.setCenter(sheetComponent);
        isLogicalOperationsActiveProperty.set(true);
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(true);
    }

    public void onFinishLoadingFile(SheetDto sheetDto, boolean isEditor) {
        isEditorProperty.set(isEditor);

        this.currentSheet = sheetDto; //this what server bring

        rangesComponentController.uploadRanges(currentSheet.ranges());
        setEffectiveValuesPoolProperty(currentSheet, this.effectiveValuesPool);
        setNumericCoordinateList();
        setSheet(currentSheet);

        mostUpdatedVersionNumber = sheetDto.version();
        tempMostUpdatedVersionNumber = mostUpdatedVersionNumber;

        setDesignVersions();
    }

    private void setNumericCoordinateList() {
        currentSheet.activeCells().forEach((coordinateString,cellDto) -> {

            if(isParsableAsDouble(cellDto.originalValue())){
                numericCoordinateObservableList.add(coordinateString);
            }
        });
    }

    private boolean isParsableAsDouble(String value) {
        try {
            Double.parseDouble(value); // Attempt to parse the string
            return true;               // Parsing succeeded, return true
        } catch (NumberFormatException e) {
            return false;            // Parsing failed, return false
        }
    }

    private void setDesignVersions() {

        if (sheetToVersionDesignManager.get(currentSheet.name()) == null) {
            VersionDesignManager designManagerForSheet = new VersionDesignManager();
            designManagerForSheet.setMainController(this);
            sheetToVersionDesignManager.put(currentSheet.name(), designManagerForSheet);
            saveDesignVersion(sheetComponentController.getGridPane());
        }

//        int lastVersionInMap = sheetToVersionDesignManager.get(currentSheet.name()).getNumberOfVersions();
//
//        for (int i = lastVersionInMap; i <= mostUpdatedVersionNumber; i++) {
//            sheetToVersionDesignManager.get(currentSheet.name()).addVersion();
//        }

        resetSheetToVersionDesign(mostUpdatedVersionNumber);
    }

    public void getSortedSheetRunLater(SortDesignDto sortDesignDto) {
        commandsComponentController.sortCommandsControllerRunLater();

        SheetDto sortedSheet = sortDesignDto.sheetDto();

        EffectiveValuesPoolProperty effectiveValuesPoolProperty = new EffectiveValuesPoolPropertyImpl();
        setEffectiveValuesPoolProperty(sortedSheet, effectiveValuesPoolProperty);

        SheetController sortedSheetComponentController = new SheetController();
        sortedSheetComponentController.setMainController(this);
        ScrollPane sheetComponent = sortedSheetComponentController.getInitializedSheet(sortedSheet.layout(),effectiveValuesPoolProperty);

        //design the cells.
        VersionDesignManager.VersionDesign design;

        if (currentSheet.version() == mostUpdatedVersionNumber) {
            design = sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version() + 1);
        } else {
            design = sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version());
        }

        sortedSheetComponentController.setColumnsDesign(design.getColumnsLayoutVersion());
        sortedSheetComponentController.setRowsDesign(design.getRowsLayoutVersion());

        List<List<CoordinateDto>> sortedCellsInRange = sortDesignDto.coordinateDtos();
        BoundariesDto boundariesToSort = sortDesignDto.boundariesDto();

        for (int row = 0; row <= sortedSheet.layout().rows() ; row++) {

            List<CoordinateDto> sortedCells = new ArrayList<>();

            if (row >= boundariesToSort.from().row() && row <= boundariesToSort.to().row()) {
                sortedCells = sortedCellsInRange.get(row - boundariesToSort.from().row());
            }

            for (int col = 0; col <= sortedSheet.layout().columns() ; col++) {
                CoordinateDto dest = new CoordinateDto(row, col);
                int indexDesign;

                if (row >= boundariesToSort.from().row() && row <= boundariesToSort.to().row() &&
                        col >= boundariesToSort.from().column() && col <= boundariesToSort.to().column()) {

                    CoordinateDto source = sortedCells.get(col - boundariesToSort.from().column());
                    indexDesign = sortedSheetComponentController.getIndexDesign(source);

                    sortedSheetComponentController.setCoordinateDesign(dest,design.getCellDesignsVersion()
                            .get(indexDesign));

                } else {
                    indexDesign = sortedSheetComponentController.getIndexDesign(dest);
                    sortedSheetComponentController.setCoordinateDesign(dest,design.getCellDesignsVersion()
                            .get(indexDesign));
                }

            }
        }

        //finish design
        appBorderPane.setCenter(sheetComponent);
        isLogicalOperationsActiveProperty.set(true);
        headerComponentController.getSplitMenuButtonSelectVersion().setDisable(true);
    }

    public void getViewSheetVersionRunLater(SheetDto sheetDto) {
        currentSheet = sheetDto;

        isEditorProperty.set(currentSheet.version() == tempMostUpdatedVersionNumber);
        setEffectiveValuesPoolProperty(currentSheet, effectiveValuesPool);
        setNumericCoordinateList();
        resetSheetToVersionDesign(currentSheet.version());
    }

    public void updateCellRunLater(SheetDto sheetDto) {
        if (sheetDto.version() != currentSheet.version()) {
            currentSheet = sheetDto;
            mostUpdatedVersionNumber = sheetDto.version();
            setEffectiveValuesPoolProperty(currentSheet, effectiveValuesPool);
//            sheetToVersionDesignManager.get(currentSheet.name()).addVersion();
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
                                cellInFocusProperty
                                        .getCoordinate()
                                        .get()));
        sheetComponentController.changeColumnWidth(column, prefWidth);
        //itay change for saving on edit version the design
        sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version() + 1).getColumnsLayoutVersion().put(column,prefWidth);
    }

    public void changeSheetRowHeight(int prefHeight) {
        int row =
                CoordinateFactory.extractRow(
                        cellInFocusProperty
                                .getCoordinate()
                                .get());
        sheetComponentController.changeRowHeight(row, prefHeight);
        //itay change for saving on edit version the design
        sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version() + 1).getRowsLayoutVersion().put(row,prefHeight);
    }

    public void changeSheetCellBackgroundColor(Color color) {
        sheetComponentController.changeCellBackgroundColor(color);

        //itay change for saving on edit version the design

        sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version() + 1).getCellDesignsVersion()
                .computeIfPresent(sheetComponentController.getIndexDesign(new CoordinateDto(cellInFocusProperty.getCoordinate().get()))
                        , (k, textFieldDesign) -> new TextFieldDesign(color, textFieldDesign.getTextStyle(), textFieldDesign.getTextAlignment()));
    }

    public void changeSheetTextColor(Color color) {
        sheetComponentController.changeCellTextColor(color);
        //itay change for saving on edit version the design

        sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version() + 1).getCellDesignsVersion()
                .computeIfPresent(sheetComponentController.getIndexDesign(new CoordinateDto(cellInFocusProperty.getCoordinate().get()))
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
        if (numberOfVersion == mostUpdatedVersionNumber) {
            numberOfVersion++;
        }
        VersionDesignManager.VersionDesign versionDesign = sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(numberOfVersion);
        if (versionDesign != null) {
            sheetComponentController.setGridPaneDesign(versionDesign);
        }
    }

    public void resetOperationView() {
        isLogicalOperationsActiveProperty.set(false);
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

        if (newValue) {
            showCommandsProperty.set(currentSheet.version() == mostUpdatedVersionNumber);
            CellDto cell = currentSheet.activeCells().get(coordinateString);
            cellInFocusProperty.setCoordinate(coordinateString);

            if (cell != null) {
                cellInFocusProperty.setOriginalValue(cell.originalValue());
                cellInFocusProperty.setLastUpdateVersion(String.valueOf(cell.version()));
                cellInFocusProperty.setUpdateBy(cell.updateBy());
                cellInFocusProperty.setDependOn(cell.influenceFrom().stream()
                        .map(CellDto::coordinate)
                        .collect(Collectors.toSet()));
                cellInFocusProperty.setInfluenceOn(cell.influenceOn().stream()
                        .map(CellDto::coordinate)
                        .collect(Collectors.toSet()));
            } else {
                cellInFocusProperty.setOriginalValue("");
                cellInFocusProperty.setLastUpdateVersion("");
                cellInFocusProperty.setUpdateBy("");
                cellInFocusProperty.setDependOn(new HashSet<>());
                cellInFocusProperty.setInfluenceOn(new HashSet<>());
            }
        } else{
            cellInFocusProperty.setDependOn(new HashSet<>());
            cellInFocusProperty.setInfluenceOn(new HashSet<>());
        }
    }

    public void alignCells(Pos pos) {
        int column =
                CoordinateFactory.parseColumnToInt(
                        CoordinateFactory.extractColumn(
                                cellInFocusProperty
                                        .getCoordinate()
                                        .get()));
        sheetComponentController.changeColumnAlignment(column, pos);

        //itay change for saving on edit version the design
        for (int i = 0; i < sheetComponentController.getGridPane().getChildren().size(); i++) {
            Node node = sheetComponentController.getGridPane().getChildren().get(i);
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (node instanceof TextField && colIndex == column && rowIndex != 0) {

                sheetToVersionDesignManager.get(currentSheet.name()).getVersionDesign(currentSheet.version() + 1).getCellDesignsVersion()
                        .computeIfPresent(i, (k, textFieldDesign) -> new TextFieldDesign(textFieldDesign.getBackgroundColor(), textFieldDesign.getTextStyle(), pos));
            }
        }
    }

    public void paintRangeOnSheet(RangeDto range, Color color) {
        this.sheetComponentController.paintRangeOnSheet(range, color);
    }

    private void updateSheetView(SheetDto sheetDto) {

        if (sheetDto == null) {
            throw new RuntimeException("Sheet deleted!");
        }

        tempMostUpdatedVersionNumber = sheetDto.version();

        if (tempMostUpdatedVersionNumber > lastVersionNumberBeforeUpdate) {
            for (int i = lastVersionNumberBeforeUpdate + 1; i <= tempMostUpdatedVersionNumber; i++) {
                headerComponentController.addMenuOptionToVersionSelection(String.valueOf(i));
                sheetToVersionDesignManager.get(sheetDto.name()).addVersion();
            }
        }

        if (currentSheet.version() < tempMostUpdatedVersionNumber) {
            headerComponentController.makeSplitMenuButtonBlink();
        } else {
            headerComponentController.stopSplitMenuButtonBlink();
            mostUpdatedVersionNumber = tempMostUpdatedVersionNumber;
        }

        if (currentSheet.ranges().size() != sheetDto.ranges().size()) {
            currentSheet.ranges().clear();
            currentSheet.ranges().addAll(sheetDto.ranges());
            rangesComponentController.uploadRanges(currentSheet.ranges());
        }

        lastVersionNumberBeforeUpdate = tempMostUpdatedVersionNumber;
    }

    public ObservableList<String> getNumericCoordinateObservableList() {
       return  this.numericCoordinateObservableList;
    }

    public void updateDynamicSheetRunLater(SheetDto sheetDto) {
        currentSheet = sheetDto;
        setEffectiveValuesPoolProperty(currentSheet, effectiveValuesPool);
    }

    public void removeDynamicSheet() {
        isDynamicSheetActiveProperty.set(false);
        currentSheet = editableSheet;
        setEffectiveValuesPoolProperty(currentSheet, effectiveValuesPool);
    }

    public Double getStaticSheetCellValue(String coordinate) {
        return Double.parseDouble(editableSheet.activeCells().get(coordinate).originalValue());
    }

    public void activateDynamicSheet() {
        getSheet(String.valueOf(currentSheet.version()), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    mainController.showAlertPopup(new Exception(), "show version");
                    latch.countDown(); // Release latch in case of failure
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();

                if (response.code() != 200) {
                    Platform.runLater(() -> {
                        mainController.showAlertPopup(new Exception(GSON_INSTANCE.fromJson(jsonResponse, String.class)),
                                "show version: " + currentSheet.version());
                        latch.countDown(); // Release latch in case of error
                    });
                } else {
                    Gson gson = new GsonBuilder().registerTypeAdapter(CellDto.class, new CellDtoDeserializer()).create();
                    editableSheet = gson.fromJson(jsonResponse, SheetDto.class);
                    isDynamicSheetActiveProperty.set(true);
                    latch.countDown(); // Release latch after successful completion
                    Platform.runLater(() -> setEffectiveValuesPoolProperty(editableSheet, effectiveValuesPool));
                }
            }
        });
    }

    public void waitForDynamicSheetActivation() {
        try {
            latch.await(); // Wait until latch is released
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle thread interruption properly
        }
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
