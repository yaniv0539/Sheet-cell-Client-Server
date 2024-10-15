package component.commands.operations.filter;

import component.commands.CommandsController;
import dto.BoundariesDto;
import dto.FilterDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;

import java.util.ArrayList;
import java.util.List;


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

    private BoundariesDto boundariesToFilter = null;
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
        textFieldRange.setOnAction((ActionEvent event) -> textRangeAction());
        //textFieldRange.textProperty().addListener((observableValue, oldValue, newValue) -> textRangeAction());
    }

    private void textRangeAction() {
        comboBoxColumn1.getItems().clear();
        mainController.getBoundriesDto(textFieldRange.getText());
    }

    public void textRangeActionRunLater(BoundariesDto boundaries) {

        this.boundariesToFilter = boundaries;
        List<String> ranges = new ArrayList<>();

        for (int i = boundariesToFilter.getFrom().getColumn(); i <= boundariesToFilter.getTo().getColumn(); i++) {
            char character = (char) ('A' + i); // Compute the character
            String str = String.valueOf(character);
            ranges.add(str);
        }
        comboBoxColumn1.getItems().addAll(ranges);
        validRange.set(true);
    }


    @FXML
    void columnAction(ActionEvent event) {
        if (comboBoxColumn1.getSelectionModel().getSelectedIndex() != -1) {
            filteringByColumn = comboBoxColumn1.getSelectionModel().getSelectedItem();
            //take uniqe values
            //add them to filter HbOX
            anyValueChecked.set(false);
            flowPaneValues.getChildren().clear();

            mainController.getColumnUniqueValuesInRange(filteringByColumn.toUpperCase().toCharArray()[0] - 'A'
                    ,boundariesToFilter.getFrom().getRow()
                    ,boundariesToFilter.getTo().getRow());
        }
    }

    public void  columActionRunLater(List<String> uniqueValues){

        for (String uniqueValue : uniqueValues) {
            CheckBox checkBox = new CheckBox(uniqueValue);
            checkBox.setWrapText(true);
            checkBox.selectedProperty().addListener((observable,oldValue,newValue) -> this.handleCheckBoxSelect(uniqueValue,newValue));
            flowPaneValues.getChildren().add(checkBox);
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
        FilterDto data = new FilterDto(boundariesToFilter, filteringByColumn, uniqueValuesToFilter);
        this.mainController.filterRange(data);
    }

    private boolean isInputValid(String newValue) {
        return (BoundariesFactory.isValidBoundariesFormat(newValue) &&
                mainController.isBoundariesValidForCurrentSheet(BoundariesFactory.toBoundaries(newValue)));
    }

}
