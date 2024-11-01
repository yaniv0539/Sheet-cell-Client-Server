package component.main.center.app.analysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import component.main.center.app.AppController;
import dto.CellDto;
import dto.SheetDto;
import dto.deserializer.CellDtoDeserializer;
import javafx.application.Platform;
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
import java.util.Set;

public class DynamicAnalysisController {

    @FXML
    private GridPane mainGridPane;
    @FXML
    private Button buttonAdd;

    @FXML
    private Button buttonResetAll;
    @FXML
    private Button buttonDeleteAll;

    AppController mainAppController;

    private int rowIndex = 1;  // Start after the header row

    @FXML
    public void initialize() {
        buttonAdd.setOnAction(e -> addRow());
        buttonResetAll.setOnAction(e -> resetAllRows());
        buttonDeleteAll.setOnAction(e -> deleteAllRows());
    }

    public void init() {
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
        deleteButton.setOnAction(e -> removeRow(finalRowIndex));


        //here we have all the componnents
        comboBox.setOnAction(e -> resetRow(comboBox, spinnerStep, spinnerMin, slider, spinnerMax));

        //TODO:shoudnt be in comment, this is the http requst for the sheet.
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String coord = comboBox.getValue();
            performActionOnSliderMove(coord, String.valueOf(newValue));
        });

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

    //http request
    private void performActionOnSliderMove(String coord, String value) {

        mainAppController.updateCellToDynamicSheet(coord, value, new Callback() {
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
//        slider.minProperty().bind(spinnerMin.valueProperty());
//        slider.maxProperty().bind(spinnerMax.valueProperty());
//        // Bind the slider's block increment to the step value of the spinner
//        slider.blockIncrementProperty().bind(spinnerStep.valueProperty());
//        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            slider.setMajorTickUnit(newValue.doubleValue());
//            slider.setBlockIncrement(newValue.doubleValue());
//        });

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

    private static void initDisableBind(Spinner<Double> spinnerStep, ComboBox<String> comboBox, Spinner<Integer> spinnerMin, Spinner<Integer> spinnerMax, Button resetButton, Button deleteButton) {
        spinnerStep.disableProperty().bind(comboBox.getSelectionModel().selectedItemProperty().isNull());
        spinnerMin.disableProperty().bind(comboBox.getSelectionModel().selectedItemProperty().isNull());
        spinnerMax.disableProperty().bind(comboBox.getSelectionModel().selectedItemProperty().isNull());
        resetButton.disableProperty().bind(comboBox.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(comboBox.getSelectionModel().selectedItemProperty().isNull());
    }


    private void resetRow(ComboBox<String> comboBox, Spinner<Double> spinnerStep, Spinner<Integer> spinnerMin, Slider slider, Spinner<Integer> spinnerMax) {
        String coordinate = comboBox.getSelectionModel().selectedItemProperty().get();
        if (coordinate != null) {
            Double value = mainAppController.getDoubleValueAt(coordinate);

            spinnerStep.getValueFactory().setValue(1.0); // Reset step
            spinnerMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE , Integer.MAX_VALUE, value.intValue() - 100));
            spinnerMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE , Integer.MAX_VALUE, value.intValue() + 100));

            slider.valueProperty().setValue(value);                      // Reset slider to middle
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
        mainAppController.removeDynamicSheet();
    }

    private void deleteAllRows() {
        for (int row = mainGridPane.getRowConstraints().size() - 1; row >= 1; row--) {
            removeRow(row);
        }
        addRow();
        mainAppController.setOperationView(false);
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
