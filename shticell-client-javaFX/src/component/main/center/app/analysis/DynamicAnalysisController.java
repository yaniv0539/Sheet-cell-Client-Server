package component.main.center.app.analysis;

import component.main.center.app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.Objects;

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

        Spinner<Double> spinnerStep = new Spinner<>(0, 100, 0);
        spinnerStep.setPrefWidth(60.0);

        Spinner<Integer> spinnerMin = new Spinner<>(0, 100, 0);
        spinnerMin.setPrefWidth(60.0);

        Slider slider = new Slider(0, 100, 50); // Default to middle
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);

        Spinner<Integer> spinnerMax = new Spinner<>(0, 100, 100);
        spinnerMax.setPrefWidth(60.0);

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-text-fill: blue;");
        resetButton.setOnAction(e -> resetRow(comboBox, spinnerStep, spinnerMin, slider, spinnerMax));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-text-fill: red;");

        final int finalRowIndex = rowIndex;
        deleteButton.setOnAction(e -> removeRow(finalRowIndex));


        //here we have all the componnents
        
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

    private static void initSliderValuesBinds(Slider slider, Spinner<Integer> spinnerMin, Spinner<Integer> spinnerMax, Spinner<Double> spinnerStep) {
        slider.minProperty().bind(spinnerMin.valueProperty());
        slider.maxProperty().bind(spinnerMax.valueProperty());
        // Bind the slider's block increment to the step value of the spinner
        slider.blockIncrementProperty().bind(spinnerStep.valueProperty());
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
        int value = mainAppController.getIntValueAt(coordinate);
        comboBox.getSelectionModel().clearSelection();
        spinnerStep.getValueFactory().setValue(1.0); // Reset step
        spinnerMin.getValueFactory().setValue(mainAppController.getIntValueAt(coordinate) * (-2));   // Reset min
        spinnerMax.getValueFactory().setValue(mainAppController.getIntValueAt(coordinate) * (-2)); // Reset max
        slider.setValue(mainAppController.getIntValueAt(coordinate));                        // Reset slider to middle
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
    }

    private void deleteAllRows() {
        for (int row = mainGridPane.getRowConstraints().size() - 1; row >= 1; row--) {
            removeRow(row);
        }
        addRow();
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
