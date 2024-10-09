package commands.operations.sort;
import commands.CommandsController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;
import sheet.range.boundaries.impl.BoundariesImpl;

import java.util.ArrayList;
import java.util.List;

public class SortController {

    @FXML
    private FlowPane flowPaneColumns;

    @FXML
    private Button buttonGetColumns;

    @FXML
    private Button buttonSort;

    @FXML
    private Label labelRange;

    @FXML
    private Label labelSelectColumn;

    @FXML
    private TextField textFieldRange;

    private CommandsController mainController;
    Tooltip validationTooltip = new Tooltip("Input must be a range in this format:\n" +
            "<top left cell coordinate>..<bottom right cell coordinate>");
    SimpleBooleanProperty anyChecked = new SimpleBooleanProperty(false);
    SimpleBooleanProperty validRange = new SimpleBooleanProperty(false);
    private List<String> columToSort = new ArrayList<>();

    public void init() {
        buttonSort.disableProperty().bind(anyChecked.not());
        buttonGetColumns.disableProperty().bind(validRange.not());
        // Initially hide the Tooltip
        validationTooltip.setAutoHide(false);
        Tooltip.install(textFieldRange, validationTooltip);
        validationTooltip.hide();
        textFieldRange.textProperty().addListener((observable, oldValue, newValue) -> handleChangeTextRange(newValue));

    }

    private void handleChangeTextRange(String newValue) {
        if (!isInputValid(newValue)) {
            // Show tooltip if the input is invalid
            validationTooltip.show(textFieldRange, textFieldRange.getScene().getWindow().getX() + textFieldRange.getLayoutX() + 160,
                    textFieldRange.getScene().getWindow().getY() + textFieldRange.getLayoutY() + textFieldRange.getHeight() + 55);
            textFieldRange.setStyle("-fx-border-color: red;");
            validRange.set(false);
        } else {
            // Hide tooltip if the input is valid
            validationTooltip.hide();
            textFieldRange.setStyle("-fx-border-color: lightblue;"); // Reset style
            validRange.set(true);
        }
    }

    private boolean isInputValid(String newValue) {
        return (BoundariesFactory.isValidBoundariesFormat(newValue) &&
                mainController.isBoundariesValidForCurrentSheet(BoundariesFactory.toBoundaries(newValue)));
    }

    @FXML
    void buttonSortAction(ActionEvent event) {
        Boundaries BoundariesToSort = BoundariesFactory.toBoundaries(textFieldRange.getText());

        mainController.sortRange(BoundariesToSort,columToSort);
    }
    @FXML
    void buttonGetColumnsAction(ActionEvent event) {
       //to add check box to the hbox.
        flowPaneColumns.getChildren().clear();
        anyChecked.setValue(false);
        columToSort.clear();
        Boundaries boundariesToSort = BoundariesFactory.toBoundaries(textFieldRange.getText());

        for (int i = boundariesToSort.getFrom().getCol(); i <= boundariesToSort.getTo().getCol(); i++) {
            if(mainController.isNumericColumn(i ,boundariesToSort.getFrom().getRow(),boundariesToSort.getTo().getRow())){
                char character = (char) ('A' + i); // Compute the character
                String column = String.valueOf(character);
                CheckBox checkBox = new CheckBox(column);
                checkBox.selectedProperty().addListener((observable,oldValue,newValue) -> this.handleCheckBoxSelect(column,newValue));
                flowPaneColumns.getChildren().add(checkBox);
            }
        }
        if(flowPaneColumns.getChildren().isEmpty()){
            Label label = new Label("No numeric columns in range !");
            flowPaneColumns.getChildren().add(label);
        }
    }

    @FXML
    void textFieldRangeAction(ActionEvent event) {

    }

    private void handleCheckBoxSelect(String column,boolean newValue) {
        if (newValue) {
            // If checked, add to selectedLetters
            columToSort.add(column);
        } else {
            // If unchecked, remove from selectedLetters
            columToSort.remove(column);
        }

        // Update the button's disable property
        anyChecked.set(!columToSort.isEmpty());
    }

    @FXML
    void textFieldRangeKeyTyped(KeyEvent event) {

    }

    public void setMainController(CommandsController mainController){
        this.mainController = mainController;
    }


}
