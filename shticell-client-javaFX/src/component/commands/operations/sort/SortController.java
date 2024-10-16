package component.commands.operations.sort;
import component.commands.CommandsController;
import dto.BoundariesDto;
import dto.SortDto;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;

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
    private BoundariesDto boundariesDto;

    public void init() {
        buttonSort.disableProperty().bind(anyChecked.not());
        buttonGetColumns.disableProperty().bind(validRange.not());
        // Initially hide the Tooltip
        validationTooltip.setAutoHide(false);
        Tooltip.install(textFieldRange, validationTooltip);
        validationTooltip.hide();

    }

    @FXML
    void buttonSortAction(ActionEvent event) {
        mainController.sortRange(new SortDto(boundariesDto, columToSort));
    }
    @FXML
    void buttonGetColumnsAction(ActionEvent event) {
        mainController.getNumericColumnsInBoundaries(textFieldRange.getText());
    }

    public void buttonGetColumnsActionRunLater(SortDto sortDto){
        //to add check box to the hbox.
        flowPaneColumns.getChildren().clear();
        anyChecked.setValue(false);
        columToSort.clear();
        boundariesDto = sortDto.getBoundariesDto();
        //adding all possible numeric column
        sortDto.getSortByColumns().forEach(columnLetter -> {
            CheckBox checkBox = new CheckBox(columnLetter);
            checkBox.selectedProperty().addListener((observable,oldValue,newValue) -> this.handleCheckBoxSelect(columnLetter,newValue));
            flowPaneColumns.getChildren().add(checkBox);
        });

        //todo: logic for serverlet.
//        for (int i = boundariesDto.getFrom().getColumn(); i <= boundariesDto.getTo().getColumn(); i++) {
//            if(mainController.isNumericColumn(i ,boundariesDto.getFrom().getRow(),boundariesDto.getTo().getRow())){
//                char character = (char) ('A' + i); // Compute the character
//                String column = String.valueOf(character);
//                CheckBox checkBox = new CheckBox(column);
//                checkBox.selectedProperty().addListener((observable,oldValue,newValue) -> this.handleCheckBoxSelect(column,newValue));
//                flowPaneColumns.getChildren().add(checkBox);
//            }
//        }

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
