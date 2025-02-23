package component.main.center.app.sheet;

import component.main.center.app.AppController;
import component.main.center.app.model.api.EffectiveValuesPoolPropertyReadOnly;
import component.main.center.app.model.impl.TextFieldDesign;
import component.main.center.app.model.impl.VersionDesignManager;
import dto.BoundariesDto;
import dto.CoordinateDto;
import dto.LayoutDto;
import dto.RangeDto;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SheetController {

    private AppController mainController;

    private ScrollPane scrollPane;
    private GridPane gridPane;

    private final Map<String, TextField> cellsTextFieldMap = new HashMap<>();
    private final Map<String, Background> previousBackgrounds = new HashMap<>();

    private int defaultRowHeight;
    private int defaultColWidth;


    // Constructor

    public SheetController() {
        scrollPane = new ScrollPane();
        gridPane = new GridPane();
    }


    // Initializers

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public ScrollPane getInitializedSheet(LayoutDto layout, EffectiveValuesPoolPropertyReadOnly dataToView) {
        this.defaultRowHeight = layout.size().height();
        this.defaultColWidth = layout.size().width();
        setLayoutGridPane(layout);
        //until here set the grid.
        setBindsTo(dataToView);
        setScrollPane();
        return scrollPane;
    }

    private void setLayoutGridPane(LayoutDto layout) {

        // Create a GridPane
        gridPane.setGridLinesVisible(true);  // Enable grid lines for visualization

        // Add column constraints to match FXML configuration
        for (int col = 0; col <= layout.columns(); col++) {

            ColumnConstraints columnConstraints = new ColumnConstraints();
            if (col == 0) {
                columnConstraints.setMinWidth(30);
                columnConstraints.setMaxWidth(30);
                columnConstraints.setPrefWidth(30);  // First column for row numbers
                columnConstraints.setHgrow(Priority.NEVER);  // No horizontal grow
            } else {
                columnConstraints.setMinWidth(layout.size().width());
                columnConstraints.setPrefWidth(layout.size().width());  // Other columns
                columnConstraints.setHgrow(Priority.NEVER);  // Allow horizontal grow
            }
            gridPane.getColumnConstraints().add(columnConstraints);
        }

        // Add row constraints to match FXML configuration
        for (int row = 0; row <= layout.rows(); row++) {
            RowConstraints rowConstraints = new RowConstraints();
            if (row == 0) {
                rowConstraints.setMinHeight(30);
                rowConstraints.setMaxHeight(30);
                rowConstraints.setPrefHeight(30);
                rowConstraints.setVgrow(Priority.NEVER);  // No horizontal grow
            } else {
                rowConstraints.setMinHeight(layout.size().height());
                rowConstraints.setPrefHeight(layout.size().height());  // Set preferred height for rows
                rowConstraints.setVgrow(Priority.NEVER);  // Allow vertical grow
            }
            gridPane.getRowConstraints().add(rowConstraints);
        }
    }

    private void setBindsTo(EffectiveValuesPoolPropertyReadOnly dataToView) {

        for (int row = 0; row < gridPane.getRowCount(); row++) {
            for (int col = 0; col < gridPane.getColumnCount(); col++) {

                TextField textField = new TextField();
                textField.setEditable(false);  // Disable editing

                textField.setMaxWidth(Double.MAX_VALUE);  // Allow TextField to stretch horizontally
                textField.setMaxHeight(Double.MAX_VALUE);  // Allow TextField to stretch vertically
                textField.setBorder(Border.stroke(Paint.valueOf("gray")));
                textField.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, null)));
                textField.setStyle("-fx-text-fill: black;");

                // Set font size and alignment to match FXML
                textField.setFont(Font.font("System", 12));
                textField.setAlignment(Pos.CENTER);
                // Dynamically set the content of the TextField

                if (row == 0 && col > 0) {
                    textField.setText(Character.toString((char) ('A' + col - 1)));  // Column headers (A, B, C, etc.)
                } else if (col == 0 && row > 0) {
                    textField.setText(Integer.toString(row));  // Row headers (1, 2, 3, etc.)
                } else {
                    if (row != 0 || col != 0) {
                        String coordinateString = CoordinateFactory.createCoordinate(row-1,col-1).toString();
                        cellsTextFieldMap.put(coordinateString, textField);
                        previousBackgrounds.put(coordinateString,textField.getBackground());
                        textField.textProperty().bind(dataToView.getEffectiveValuePropertyAt(coordinateString));
                        //add listener to focus, need to change.
                        int finalCol = col;
                        int finalRow = row;

                        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue) {
                                textField.setBorder(Border.stroke(Paint.valueOf("green")));
                            } else {
                                textField.setBorder(Border.stroke(Paint.valueOf("gray")));
                            }
                            mainController.focusChanged(newValue, coordinateString);
                            mainController.changeCommandsColumnWidth(gridPane.getColumnConstraints().get(finalCol).getPrefWidth());
                            mainController.changeCommandsRowHeight(gridPane.getRowConstraints().get(finalRow).getPrefHeight());
                            mainController.changeCommandsColumnAlignment(textField.getAlignment());
                            mainController.changeCommandsCellBackgroundColor(getTextFieldBackgroundColor(textField.getBackground()));
                            mainController.changeCommandsCellTextColor(getTextFieldTextColor(textField));
                        });
                    }
                }

                gridPane.add(textField, col, row);

                GridPane.setHgrow(textField, Priority.NEVER);
                GridPane.setVgrow(textField, Priority.NEVER);
                GridPane.setHalignment(textField, HPos.CENTER);
                GridPane.setValignment(textField, VPos.CENTER);
            }
        }
    }

    private void setScrollPane() {
        scrollPane.setFitToWidth(true);  // Ensure it stretches to fit the width
        scrollPane.setFitToHeight(true);  // Ensure it stretches to fit the height
        scrollPane.setContent(gridPane);
    }

    public Color getTextFieldBackgroundColor(Background background) {

        if (background != null && !background.getFills().isEmpty()) {
            for (BackgroundFill fill : background.getFills().reversed()) {
                if (fill.getFill() instanceof Color) {
                    return (Color) fill.getFill();
                }
            }
        }
        return null; // No background color found
    }

    private Color getTextFieldTextColor(TextField textField) {
        Text text = (Text) textField.lookup(".text");
        if (text != null) {
            return (Color) text.getFill();
        }
        return Color.BLACK; // Default color if not set
    }


    // Getters

    public GridPane getGridPane() {
        return gridPane;
    }


    // On change functions

    public void changeColorDependedCoordinate(ListChangeListener.Change<? extends CoordinateDto> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (CoordinateDto coordinate : change.getAddedSubList()) {
                    Background currentBackground = Objects.requireNonNull(cellsTextFieldMap.get(coordinate.toString())).getBackground();
                    previousBackgrounds.put(coordinate.toString(), currentBackground);
                    Objects.requireNonNull(cellsTextFieldMap.get(coordinate.toString())).setBackground(Background.fill(Paint.valueOf("lightblue")));
                }
            }

            if (change.wasRemoved()) {
                for (CoordinateDto coordinate : change.getRemoved()) {
                    TextField textField = Objects.requireNonNull(cellsTextFieldMap.get(coordinate.toString()));
                    Background previousBackground = previousBackgrounds.get(coordinate.toString());
                    textField.setBackground(Objects.requireNonNullElseGet(previousBackground, () -> new Background(new BackgroundFill(Paint.valueOf("white"), CornerRadii.EMPTY, null))));
                }
            }
        }
    }

    public void changeColorInfluenceCoordinate(ListChangeListener.Change<? extends CoordinateDto> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                // Do something with the added items
                for (CoordinateDto coordinate : change.getAddedSubList()) {
                    Background currentBackground = Objects.requireNonNull(cellsTextFieldMap.get(coordinate.toString())).getBackground();
                    previousBackgrounds.put(coordinate.toString(), currentBackground);
                    Objects.requireNonNull(cellsTextFieldMap.get(coordinate.toString())).setBackground(Background.fill(Paint.valueOf("lightgreen")));
                }
            }

            if (change.wasRemoved()) {
                for (CoordinateDto coordinate : change.getRemoved()) {
                    TextField textField = Objects.requireNonNull(cellsTextFieldMap.get(coordinate.toString()));
                    Background previousBackground = previousBackgrounds.get(coordinate.toString());
                    textField.setBackground(Objects.requireNonNullElseGet(previousBackground, () -> new Background(new BackgroundFill(Paint.valueOf("white"), CornerRadii.EMPTY, null))));
                }
            }
        }
    }

    public void changeColumnWidth(int column, int prefWidth) {
        gridPane.getColumnConstraints().get(column).setPrefWidth(prefWidth);
        gridPane.getColumnConstraints().get(column).setMinWidth(prefWidth);
        gridPane.getColumnConstraints().get(column).setMaxWidth(prefWidth);
    }

    public void changeRowHeight(int row, int prefHeight) {
        gridPane.getRowConstraints().get(row).setPrefHeight(prefHeight);
        gridPane.getRowConstraints().get(row).setMinHeight(prefHeight);
        gridPane.getRowConstraints().get(row).setMaxHeight(prefHeight);
    }

    public void changeColumnAlignment(int column, Pos pos) {
        gridPane.getChildren().forEach(node -> {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (node instanceof TextField && colIndex == column && rowIndex != 0) {
                ((TextField) node).setAlignment(pos);
            }
        });
    }

    public void changeCellBackgroundColor(Color color) {
        Background background = new Background(new BackgroundFill(color, CornerRadii.EMPTY, null));
        Coordinate coordinate = CoordinateFactory.toCoordinate(mainController.getCellInFocusProperty().getCoordinate().get());
        previousBackgrounds.put(coordinate.toString(), background);
        if(color != null) {

            Objects.requireNonNull(cellsTextFieldMap.get(coordinate.toString())).setBackground(background);
        }
    }

    public void changeCellTextColor(Color color) {
        if (color != null)
            Objects.requireNonNull(cellsTextFieldMap.get(CoordinateFactory.toCoordinate(mainController.getCellInFocusProperty().getCoordinate().get()).toString())).setStyle("-fx-text-fill: " + toHexString(color) + ";");
    }


    // Other functions

    public void resetRangeOnSheet(RangeDto range) {
        BoundariesDto boundaries = range.boundaries();
        CoordinateDto to = boundaries.to();
        CoordinateDto from = boundaries.from();

        for (int i = from.row(); i <= to.row(); i++) {
            for (int j = from.column(); j <= to.column(); j++) {
                String CoordinateString = CoordinateFactory.createCoordinate(i, j).toString();
                TextField textField = cellsTextFieldMap.get(CoordinateString);
                if (textField != null) {

                    textField.setBackground(previousBackgrounds.get(CoordinateString));
                }
            }
        }

    }

    public String toHexString(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);

        return String.format("#%02X%02X%02X", red, green, blue);
    }


    // Design functions

    public void setGridPaneDesign(VersionDesignManager.VersionDesign versionDesign) {
        setNodeDesign(versionDesign.getCellDesignsVersion());
        setColumnsDesign(versionDesign.getColumnsLayoutVersion());
        setRowsDesign(versionDesign.getRowsLayoutVersion());
    }

    public void setRowsDesign(Map<Integer, Integer> rowsLayoutVersion) {
        rowsLayoutVersion.forEach((index,rowHeight)->{
            RowConstraints rowConstraints = gridPane.getRowConstraints().get(index);
            rowConstraints.setPrefHeight(rowHeight);
            rowConstraints.setMinHeight(rowHeight);
            rowConstraints.setMaxHeight(rowHeight);
        });
    }

    public void setColumnsDesign(Map<Integer, Integer> columnsLayoutVersion) {
        columnsLayoutVersion.forEach((index,columnWidth)->{
            ColumnConstraints columnConstraints = gridPane.getColumnConstraints().get(index);
            columnConstraints.setPrefWidth(columnWidth);
            columnConstraints.setMinWidth(columnWidth);
            columnConstraints.setMaxWidth(columnWidth);
        });
    }

    public void setNodeDesign(Map<Integer, TextFieldDesign> cellDesignsVersion) {
        cellDesignsVersion.forEach((index, textFieldDesign) -> {
            if(gridPane.getChildren().get(index) instanceof TextField)
            {
                TextField textField = (TextField) gridPane.getChildren().get(index);
                textField.setStyle(textFieldDesign.getTextStyle());
                textField.setBackground(new Background(new BackgroundFill(textFieldDesign.getBackgroundColor(),CornerRadii.EMPTY,null)));
                textField.setAlignment(textFieldDesign.getTextAlignment());
            }
        });
    }

    public void setCoordinateDesign(CoordinateDto coordinateToDesign,TextFieldDesign design) {
        int row = coordinateToDesign.row();
        int col = coordinateToDesign.column();

        gridPane.getChildren().stream()
                .filter(node -> node instanceof TextField)
                .filter(tf-> GridPane.getColumnIndex(tf) != null && GridPane.getRowIndex(tf) != null
                        && GridPane.getColumnIndex(tf) == col + 1  && GridPane.getRowIndex(tf) == row + 1 )
                .findFirst()
                .ifPresent(tf -> {
                    TextField textField = (TextField) tf;
                    textField.setStyle(design.getTextStyle());
                    textField.setBackground(new Background(new BackgroundFill(design.getBackgroundColor(),CornerRadii.EMPTY,null)));
                    textField.setAlignment(design.getTextAlignment());
                });

    }

    public int getIndexDesign(CoordinateDto coordinate) {
        int row = coordinate.row();
        int col = coordinate.column();

        for(int i = 1 ; i < gridPane.getChildren().size(); i++) {
            if (gridPane.getChildren().get(i) instanceof TextField tf) {
                if(GridPane.getColumnIndex(tf) != null && GridPane.getRowIndex(tf) != null
                        && GridPane.getColumnIndex(tf) == col + 1 && GridPane.getRowIndex(tf) == row + 1){
                    return i;
                }
            }
        }
        //should not get here.
        return -1;
    }

    public void paintRangeOnSheet(RangeDto range, Color color) {
        BoundariesDto boundaries = range.boundaries();
        CoordinateDto to = boundaries.to();
        CoordinateDto from = boundaries.from();

        for (int i = from.row(); i <= to.row(); i++) {
            for (int j = from.column(); j <= to.column(); j++) {
                String coordinateString = CoordinateFactory.createCoordinate(i, j).toString();
                TextField textField = cellsTextFieldMap.get(coordinateString);
                if (textField != null) {
                    previousBackgrounds.put(coordinateString,textField.getBackground());
                    BackgroundFill backgroundFill = new BackgroundFill(color, CornerRadii.EMPTY, null);
                    Background background = new Background(backgroundFill);
                    textField.setBackground(background);
                }
            }
        }
    }


    // TODO: functions that will be deleted eventually.

//    public void resetSheetToDefault() {
//        restRowsHeight();
//        resetColWidth();
//    }
//
//    private void restRowsHeight() {
//        for(int i = 1 ; i < gridPane.getRowCount(); i++){
//            changeRowHeight(i,defaultRowHeight);
//        }
//    }
//
//    private void resetColWidth() {
//        for(int i = 1 ; i < gridPane.getColumnCount(); i++){
//            changeColumnWidth(i,defaultColWidth);
//        }
//    }
//
//    public void resetCellsToDefault(CoordinateDto focusrdCellCoordinate) {
//        TextField textField = cellsTextFieldMap.get(focusrdCellCoordinate.toString());
//
//        if (textField != null) {
//            textField.setStyle("-fx-text-fill: black;");
//            textField.setBackground(new Background(new BackgroundFill(Paint.valueOf("white"), CornerRadii.EMPTY, null)));
//        }
//    }

}



