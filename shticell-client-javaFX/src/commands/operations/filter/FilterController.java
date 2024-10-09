package commands.operations.filter;

import commands.CommandsController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class FilterController {

    @FXML
    private Button buttonFilter;

    @FXML
    private ComboBox<String> comboBoxColumn1;

    @FXML
    private TextField textFieldRange;
    @FXML
    private FlowPane flowPaneValues;

    private CommandsController mainController;

    private Boundaries boundariesToFilter = null;
    private String filteringByColumn = null;
    Tooltip validationTooltip = new Tooltip("Input must be a range in this format:\n" +
            "<top left cell coordinate>..<bottom right cell coordinate>");
    private List<String> uniqueValuesToFilter = new ArrayList<>();
    private BooleanProperty anyValueChecked = new SimpleBooleanProperty(false);
    private BooleanProperty validRange = new SimpleBooleanProperty(false);

    public void setMainController(CommandsController mainController) {
        this.mainController = mainController;
    }

    public void init() {
        comboBoxColumn1.disableProperty().bind(validRange.not());
        buttonFilter.disableProperty().bind(anyValueChecked.not());
        textFieldRange.textProperty().addListener((observableValue, oldValue, newValue) -> handleChangeTextRange(newValue));
    }

    private void handleChangeTextRange(String newValue) {
        if (!isInputValid(newValue)) {
            // Show tooltip if the input is invalid
            validationTooltip.show(textFieldRange, textFieldRange.getScene().getWindow().getX() + textFieldRange.getLayoutX() + 170,
                    textFieldRange.getScene().getWindow().getY() + textFieldRange.getLayoutY() + textFieldRange.getHeight() + 45);
            textFieldRange.setStyle("-fx-border-color: red;");
            validRange.set(false);
            comboBoxColumn1.getItems().clear();

        } else {
            // Hide tooltip if the input is valid
            validationTooltip.hide();
            textFieldRange.setStyle("-fx-border-color: lightblue;"); // Reset style
            comboBoxColumn1.getItems().clear();
            setColumnOptions(newValue);
            validRange.set(true);
        }
    }


    @FXML
    void columnAction(ActionEvent event) {
        if (comboBoxColumn1.getSelectionModel().getSelectedIndex() != -1) {
            filteringByColumn = comboBoxColumn1.getSelectionModel().getSelectedItem();
            //take uniqe values
            //add them to filter HbOX
            anyValueChecked.set(false);
            flowPaneValues.getChildren().clear();
            List<String> uniqueValues = mainController.getColumnUniqueValuesInRange(filteringByColumn.toUpperCase().toCharArray()[0] - 'A'
                    ,boundariesToFilter.getFrom().getRow()
                    ,boundariesToFilter.getTo().getRow());

            for (String uniqueValue : uniqueValues) {
                CheckBox checkBox = new CheckBox(uniqueValue);
                checkBox.setWrapText(true);
                checkBox.selectedProperty().addListener((observable,oldValue,newValue) -> this.handleCheckBoxSelect(uniqueValue,newValue));
                flowPaneValues.getChildren().add(checkBox);
            }
        }
    }

    private void handleCheckBoxSelect(String uniqueValue, Boolean newValue) {

        if(newValue){
            uniqueValuesToFilter.add(uniqueValue);
        }
        else{
            uniqueValuesToFilter.remove(uniqueValue);
        }

        // Update the button's disable property
        anyValueChecked.set(!uniqueValuesToFilter.isEmpty());
    }

    @FXML
    void filterAction(ActionEvent event) {
        this.mainController.filterRange(boundariesToFilter, filteringByColumn, uniqueValuesToFilter);
    }

    private void setColumnOptions(String rangeValue) {

            boundariesToFilter = BoundariesFactory.toBoundaries(rangeValue);
            List<String> ranges = new ArrayList<>();

            for (int i = boundariesToFilter.getFrom().getCol(); i <= boundariesToFilter.getTo().getCol(); i++) {
                char character = (char) ('A' + i); // Compute the character
                String str = String.valueOf(character);
                ranges.add(str);
            }
            comboBoxColumn1.getItems().addAll(ranges);
            validRange.set(true);

    }

    private boolean isInputValid(String newValue) {
        return (BoundariesFactory.isValidBoundariesFormat(newValue) &&
                mainController.isBoundariesValidForCurrentSheet(BoundariesFactory.toBoundaries(newValue)));
    }

}
