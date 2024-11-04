package component.main.center.app.analysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import component.main.center.app.AppController;
import dto.CellDto;
import dto.SheetDto;
import dto.deserializer.CellDtoDeserializer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class DynamicAnalysisController {

    @FXML private GridPane mainGridPane;
    @FXML private Button buttonAdd;
    @FXML private Button buttonResetAll;
    @FXML private Button buttonDeleteAll;

    AppController mainAppController;

    private int rowIndex = 1;  // Start after the header row

    BooleanProperty showDynamicSheetOperationsProperty;
    IntegerProperty numberOfRowsProperty;
    IntegerProperty numberOfEnableRowsProperty;

    @FXML
    public void initialize() {
        buttonAdd.setOnAction(e -> addRow());
        buttonResetAll.setOnAction(e -> resetAllRows());
        buttonDeleteAll.setOnAction(e -> deleteAllRows());

        numberOfRowsProperty = new SimpleIntegerProperty(0);
        numberOfEnableRowsProperty = new SimpleIntegerProperty(0);
        showDynamicSheetOperationsProperty = new SimpleBooleanProperty(true);

        numberOfRowsProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                addRow();
            }
        });

        numberOfEnableRowsProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue.intValue() == 0) {
                mainAppController.activateDynamicSheet();
                mainAppController.waitForDynamicSheetActivation();
            } else if (newValue.intValue() == 0) {
                this.mainAppController.removeDynamicSheet();
            }
        });
    }

    public void init(BooleanProperty showDynamicSheetOperationsProperty) {
        this.showDynamicSheetOperationsProperty = showDynamicSheetOperationsProperty;
        addRow();
    }

    public void setMainController(AppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    private void addRow() {
        // Create components for the new row
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(65.0);
        comboBox.setPromptText("Cell");
        setItems(comboBox);

        numberOfRowsProperty.set(numberOfRowsProperty.get() + 1);

        Spinner<Double> spinnerStep = new Spinner<>(0.0, 100.0, 1.0);
        spinnerStep.setPrefWidth(60.0);
        spinnerStep.setEditable(true);

        Spinner<Integer> spinnerMin = new Spinner<>(0, 100, 0);
        spinnerMin.setPrefWidth(60.0);
        spinnerMin.setEditable(true);

        Slider slider = new Slider(0, 100, 0); // Default to middle
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);

        Spinner<Integer> spinnerMax = new Spinner<>(0, 100, 100);
        spinnerMax.setPrefWidth(60.0);
        spinnerMax.setEditable(true);

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-text-fill: blue;");
        resetButton.setOnAction(e -> resetRow(comboBox, spinnerStep, spinnerMin, slider, spinnerMax));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-text-fill: red;");

        final int finalRowIndex = rowIndex;

        deleteButton.setOnAction(e -> {
            resetRow(comboBox, spinnerStep, spinnerMin, slider, spinnerMax);
            removeRow(finalRowIndex);
        });

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null) {
                numberOfEnableRowsProperty.set(numberOfEnableRowsProperty.get() + 1);
            }
            resetRow(comboBox, spinnerStep, spinnerMin, slider, spinnerMax);
        });

        slider.valueProperty().addListener((observable, oldValue, newValue) -> sliderOnValueChange(newValue, comboBox.getValue(), spinnerStep));

        //init disable property.
        initDisableBind(spinnerStep, comboBox, spinnerMin, spinnerMax, resetButton, deleteButton);
        initSliderValuesBinds(slider, spinnerMin, spinnerMax, spinnerStep);

        // Add RowConstraints to ensure the row is properly added
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMinHeight(30);
        mainGridPane.getRowConstraints().add(rowConstraints);

        // Add components to the new row in the grid
        mainGridPane.add(comboBox, 0, finalRowIndex);
        mainGridPane.add(spinnerStep, 1, finalRowIndex);
        mainGridPane.add(spinnerMin, 2, finalRowIndex);
        mainGridPane.add(slider, 3, finalRowIndex);
        mainGridPane.add(spinnerMax, 4, finalRowIndex);
        mainGridPane.add(resetButton, 5, finalRowIndex);
        mainGridPane.add(deleteButton, 6, finalRowIndex);

        rowIndex++; // Move to the next row for future additions
    }

    private void sliderOnValueChange(Number newValue, String coordinate, Spinner<Double> spinnerStep) {
        Double staticSheetCellValue = mainAppController.getStaticSheetCellValue(coordinate);
        Double stepValue = spinnerStep.getValue();
        double mod = staticSheetCellValue - staticSheetCellValue.intValue();
        if (Math.abs(staticSheetCellValue.intValue() % stepValue.intValue()) == Math.abs(newValue.intValue() % stepValue.intValue())) {
            if (mod == 0.0) {
                updateCellToDynamicSheet(coordinate, String.valueOf(newValue.intValue()));
            } else {
                updateCellToDynamicSheet(coordinate, String.valueOf(newValue.intValue() + mod));
            }
        }
    }

    //http request
    private void updateCellToDynamicSheet(String coordinate, String value) {
        mainAppController.updateCellToDynamicSheet(coordinate, value, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> mainAppController.showAlertPopup(new Exception(),"something went wrong.."));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();

                if(response.code() != 200) {
                    Platform.runLater(() -> mainAppController.showAlertPopup(new Exception(),"something went wrong.."));
                }
                else {
                    Gson gson = new GsonBuilder().registerTypeAdapter(CellDto.class,new CellDtoDeserializer()).create();
                    SheetDto sheetDto = gson.fromJson(jsonResponse, SheetDto.class);
                    Platform.runLater(() -> mainAppController.updateDynamicSheetRunLater(sheetDto));
                }
            }
        });
    }

    private void setItems(ComboBox<String> comboBox) {
        comboBox.setItems(mainAppController.getNumericCoordinateObservableList());
    }

    private static void initSliderValuesBinds(Slider slider, Spinner<Integer> spinnerMin, Spinner<Integer> spinnerMax, Spinner<Double> spinnerStep) {
        spinnerStep.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider.setMajorTickUnit(newValue);
            slider.setBlockIncrement(newValue);
        });

        spinnerMax.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue > slider.getMin()) {
                slider.maxProperty().setValue(newValue);
            }

        });

        spinnerMin.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue < slider.getMax()) {
                slider.minProperty().setValue(newValue);
            }
        });
    }

    private void initDisableBind(Spinner<Double> spinnerStep, ComboBox<String> comboBox, Spinner<Integer> spinnerMin, Spinner<Integer> spinnerMax, Button resetButton, Button deleteButton) {
        comboBox.disableProperty().bind(showDynamicSheetOperationsProperty.not());

        spinnerStep.disableProperty().bind(
                Bindings.or(
                        showDynamicSheetOperationsProperty.not(),
                        comboBox.getSelectionModel().selectedItemProperty().isNull()
                )
        );
        spinnerMin.disableProperty().bind(
                Bindings.or(
                        showDynamicSheetOperationsProperty.not(),
                        comboBox.getSelectionModel().selectedItemProperty().isNull()
                )
        );
        spinnerMax.disableProperty().bind(
                Bindings.or(
                        showDynamicSheetOperationsProperty.not(),
                        comboBox.getSelectionModel().selectedItemProperty().isNull()
                )
        );
        resetButton.disableProperty().bind(
                Bindings.or(
                        showDynamicSheetOperationsProperty.not(),
                        comboBox.getSelectionModel().selectedItemProperty().isNull()
                )
        );
        deleteButton.disableProperty().bind(
                Bindings.or(
                        showDynamicSheetOperationsProperty.not(),
                        comboBox.getSelectionModel().selectedItemProperty().isNull()
                )
        );
    }

    private void resetRow(ComboBox<String> comboBox, Spinner<Double> spinnerStep, Spinner<Integer> spinnerMin, Slider slider, Spinner<Integer> spinnerMax) {
        String coordinate = comboBox.getSelectionModel().selectedItemProperty().get();
        if (coordinate != null) {
            Double value = mainAppController.getStaticSheetCellValue(coordinate);

            spinnerStep.getValueFactory().setValue(1.0); // Reset step
            spinnerMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE , Integer.MAX_VALUE, value.intValue() - 100));
            spinnerMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE , Integer.MAX_VALUE, value.intValue() + 100));

            slider.valueProperty().setValue(value); // Reset slider to middle
        }
    }

    private void removeRow(int targetRowIndex) {
        mainGridPane.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex == targetRowIndex;
        });

        // Shift rows above the deleted row down by 1
        for (javafx.scene.Node node : mainGridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            if (rowIndex != null && rowIndex > targetRowIndex) {
                GridPane.setRowIndex(node, rowIndex - 1);

                if (node instanceof Button && Objects.equals(((Button) node).getText(), "Delete")) {
                    ((Button) node).setOnAction(e -> removeRow(rowIndex - 1));
                }
            }
        }

        mainGridPane.getRowConstraints().removeLast();
        rowIndex--;

        numberOfRowsProperty.set(numberOfRowsProperty.get() - 1);
        numberOfEnableRowsProperty.set(numberOfEnableRowsProperty.get() - 1);
    }

    private void resetAllRows() {
        // Loop through each row and reset values
        for (int i = 1; i < rowIndex; i++) {
            ComboBox<String> comboBox = (ComboBox<String>) getNodeByRowColumnIndex(i, 0);
            Spinner<Double> spinnerStep = (Spinner<Double>) getNodeByRowColumnIndex(i, 1);
            Spinner<Integer> spinnerMin = (Spinner<Integer>) getNodeByRowColumnIndex(i, 2);
            Slider slider = (Slider) getNodeByRowColumnIndex(i, 3);
            Spinner<Integer> spinnerMax = (Spinner<Integer>) getNodeByRowColumnIndex(i, 4);
            resetRow(comboBox, spinnerStep, spinnerMin, slider, spinnerMax);
        }
    }

    private void deleteAllRows() {
        for (int row = mainGridPane.getRowConstraints().size() - 1; row >= 1; row--) {
            removeRow(row);
        }
    }

    private javafx.scene.Node getNodeByRowColumnIndex(final int row, final int column) {
        for (javafx.scene.Node node : mainGridPane.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == column) {
                return node;
            }
        }
        return null;
    }
}
