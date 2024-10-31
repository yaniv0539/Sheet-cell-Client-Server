package component.main.center.app.analysis;

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

    private int rowIndex = 1;  // Start after the header row

    @FXML
    public void initialize() {
        buttonAdd.setOnAction(e -> addRow());
        buttonResetAll.setOnAction(e -> resetAllRows());
        buttonDeleteAll.setOnAction(e -> deleteAllRows());
        addRow();
    }

    private void addRow() {
        // Create components for the new row
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(65.0);
        comboBox.setPromptText("Cell");

        Spinner<Integer> spinnerStep = new Spinner<>(0, 100, 0);
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

    private void resetRow(ComboBox<String> comboBox, Spinner<Integer> spinnerStep, Spinner<Integer> spinnerMin, Slider slider, Spinner<Integer> spinnerMax) {
        comboBox.getSelectionModel().clearSelection();
        spinnerStep.getValueFactory().setValue(0); // Reset step
        spinnerMin.getValueFactory().setValue(0);   // Reset min
        slider.setValue(50);                        // Reset slider to middle
        spinnerMax.getValueFactory().setValue(100); // Reset max
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
            Spinner<Integer> spinnerStep = (Spinner<Integer>) getNodeByRowColumnIndex(i, 1);
            Spinner<Integer> spinnerMin = (Spinner<Integer>) getNodeByRowColumnIndex(i, 2);
            Slider slider = (Slider) getNodeByRowColumnIndex(i, 3);
            Spinner<Integer> spinnerMax = (Spinner<Integer>) getNodeByRowColumnIndex(i, 4);
            resetRow(comboBox, spinnerStep, spinnerMin, slider, spinnerMax);
        }
    }

    private void deleteAllRows() {
        for (int row = 1; row < mainGridPane.getRowConstraints().size(); row++) {
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
