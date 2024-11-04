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
import java.util.LinkedList;
import java.util.List;

public class DynamicAnalysisController {

    @FXML private GridPane mainGridPane;
    @FXML private Button buttonAdd;
    @FXML private Button buttonResetAll;
    @FXML private Button buttonDeleteAll;

    AppController mainAppController;

    BooleanProperty showDynamicSheetOperationsProperty;
    IntegerProperty numberOfRowsProperty;
    IntegerProperty numberOfEnableRowsProperty;

    List<ComboBox<String>> cellComboBoxList;
    List<Spinner<Double>> stepSpinnerList;
    List<Spinner<Integer>> minSpinnerList;
    List<Slider> sliderList;
    List<Spinner<Integer>> maxSpinnerList;
    List<Button> resetButtonList;
    List<Button> deleteButtonList;

    @FXML
    public void initialize() {
        buttonAdd.setOnAction(e -> addRow());
        buttonResetAll.setOnAction(e -> resetAllRows());
        buttonDeleteAll.setOnAction(e -> deleteAllRows());

        numberOfRowsProperty = new SimpleIntegerProperty(0);
        numberOfEnableRowsProperty = new SimpleIntegerProperty(0);
        showDynamicSheetOperationsProperty = new SimpleBooleanProperty(true);

        cellComboBoxList = new LinkedList<>();
        stepSpinnerList = new LinkedList<>();
        minSpinnerList = new LinkedList<>();
        sliderList = new LinkedList<>();
        maxSpinnerList = new LinkedList<>();
        resetButtonList = new LinkedList<>();
        deleteButtonList = new LinkedList<>();

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

        Spinner<Double> spinnerStep = new Spinner<>(0.0, 100.0, 1.0);
        spinnerStep.setPrefWidth(60.0);
        spinnerStep.setEditable(true);

        Spinner<Integer> spinnerMin = new Spinner<>(0, 100, 0);
        spinnerMin.setPrefWidth(60.0);
        spinnerMin.setEditable(true);

        Slider slider = new Slider(0, 100, 0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);

        Spinner<Integer> spinnerMax = new Spinner<>(0, 100, 100);
        spinnerMax.setPrefWidth(60.0);
        spinnerMax.setEditable(true);

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-text-fill: blue;");

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-text-fill: red;");

        final int finalRowIndex = numberOfRowsProperty.get();

        resetButton.setOnAction(e -> resetRow(finalRowIndex));
        deleteButton.setOnAction(e -> removeRow(finalRowIndex));

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null) {
                numberOfEnableRowsProperty.set(numberOfEnableRowsProperty.get() + 1);
            } else {
                resetRow(finalRowIndex, oldValue);
            }
            resetRow(finalRowIndex);
        });

        slider.valueProperty().addListener((observable, oldValue, newValue) -> updateCellToDynamicSheet(finalRowIndex, newValue));

        //init disable property.
        initDisableBind(spinnerStep, comboBox, spinnerMin, spinnerMax, resetButton, deleteButton);
        initSliderValuesBinds(slider, spinnerMin, spinnerMax, spinnerStep);

        // Add to lists
        cellComboBoxList.add(comboBox);
        stepSpinnerList.add(spinnerStep);
        minSpinnerList.add(spinnerMin);
        sliderList.add(slider);
        maxSpinnerList.add(spinnerMax);
        resetButtonList.add(resetButton);
        deleteButtonList.add(deleteButton);

        numberOfRowsProperty.set(numberOfRowsProperty.get() + 1);

        // Add RowConstraints to ensure the row is properly added
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMinHeight(30);
        mainGridPane.getRowConstraints().add(rowConstraints);

        // Add components to the new row in the grid
        mainGridPane.add(comboBox, 0, numberOfRowsProperty.get());
        mainGridPane.add(spinnerStep, 1, numberOfRowsProperty.get());
        mainGridPane.add(spinnerMin, 2, numberOfRowsProperty.get());
        mainGridPane.add(slider, 3, numberOfRowsProperty.get());
        mainGridPane.add(spinnerMax, 4, numberOfRowsProperty.get());
        mainGridPane.add(resetButton, 5, numberOfRowsProperty.get());
        mainGridPane.add(deleteButton, 6, numberOfRowsProperty.get());
    }

    private void updateCellToDynamicSheet(int targetRowIndex, Number newValue) {
        String coordinate = cellComboBoxList.get(targetRowIndex).getSelectionModel().selectedItemProperty().get();
        Double staticSheetCellValue = mainAppController.getStaticSheetCellValue(coordinate);
        Double stepValue = stepSpinnerList.get(targetRowIndex).getValue();
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

    private void resetRow(int targetRowIndex) {
        String coordinate = cellComboBoxList.get(targetRowIndex).getSelectionModel().selectedItemProperty().get();
        if (coordinate != null) {
            Double value = mainAppController.getStaticSheetCellValue(coordinate);
            updateCellToDynamicSheet(targetRowIndex, value);

            stepSpinnerList.get(targetRowIndex).getValueFactory().setValue(1.0); // Reset step
            minSpinnerList.get(targetRowIndex).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE , Integer.MAX_VALUE, value.intValue() - 100));
            maxSpinnerList.get(targetRowIndex).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE , Integer.MAX_VALUE, value.intValue() + 100));

            sliderList.get(targetRowIndex).valueProperty().setValue(value); // Reset slider to middle
        }
    }

    private void resetRow(int targetRowIndex, String previousCoordinate) {
        if (previousCoordinate != null) {
            Double previousValue = mainAppController.getStaticSheetCellValue(previousCoordinate);
            updateCellToDynamicSheet(previousCoordinate, String.valueOf(previousValue));
        }
        resetRow(targetRowIndex);
    }

    private void removeRow(int targetRowIndex) {
        // Step 1: Reset the row values (if needed)
        resetRow(targetRowIndex);

        // Step 2: Remove components for the row from GridPane
        mainGridPane.getChildren().removeAll(
                cellComboBoxList.get(targetRowIndex),
                stepSpinnerList.get(targetRowIndex),
                minSpinnerList.get(targetRowIndex),
                sliderList.get(targetRowIndex),
                maxSpinnerList.get(targetRowIndex),
                resetButtonList.get(targetRowIndex),
                deleteButtonList.get(targetRowIndex)
        );

        // Step 3: Remove components from each list
        cellComboBoxList.remove(targetRowIndex);
        stepSpinnerList.remove(targetRowIndex);
        minSpinnerList.remove(targetRowIndex);
        sliderList.remove(targetRowIndex);
        maxSpinnerList.remove(targetRowIndex);
        resetButtonList.remove(targetRowIndex);
        deleteButtonList.remove(targetRowIndex);

        // Step 4: Shift components in rows below the target row up by one
        for (int i = targetRowIndex; i < cellComboBoxList.size(); i++) {
            GridPane.setRowIndex(cellComboBoxList.get(i), i + 1);
            GridPane.setRowIndex(stepSpinnerList.get(i), i + 1);
            GridPane.setRowIndex(minSpinnerList.get(i), i + 1);
            GridPane.setRowIndex(sliderList.get(i), i + 1);
            GridPane.setRowIndex(maxSpinnerList.get(i), i + 1);
            GridPane.setRowIndex(resetButtonList.get(i), i + 1);
            GridPane.setRowIndex(deleteButtonList.get(i), i + 1);

            // Update delete button action to match new row index
            int currentIndex = i; // Capture current index for lambda
            deleteButtonList.get(i).setOnAction(e -> removeRow(currentIndex));
        }

        // Step 5: Remove last row constraint if necessary
        if (!mainGridPane.getRowConstraints().isEmpty()) {
            mainGridPane.getRowConstraints().remove(mainGridPane.getRowConstraints().size() - 1);
        }

        // Step 6: Update properties
        numberOfRowsProperty.set(numberOfRowsProperty.get() - 1);
        numberOfEnableRowsProperty.set(numberOfEnableRowsProperty.get() - 1);
    }

    private void resetAllRows() {
        // Loop through each row and reset values
        for (int i = 0; i < numberOfRowsProperty.get(); i++) {
            resetRow(i);
        }
    }

    private void deleteAllRows() {
        int size = numberOfRowsProperty.get();
        for (int row = size - 1; row >= 0; row--) {
            removeRow(row);
        }
    }
}
