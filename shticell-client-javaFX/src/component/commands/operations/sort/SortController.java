package component.commands.operations.sort;
import component.commands.CommandsController;
import dto.BoundariesDto;
import dto.SortDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.GSON_INSTANCE;

public class SortController {

    private CommandsController mainController;

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

    SimpleBooleanProperty anyChecked = new SimpleBooleanProperty(false);
    SimpleBooleanProperty validRange = new SimpleBooleanProperty(false);

    private List<String> columToSort = new ArrayList<>();
    private BoundariesDto boundariesDto;

    Tooltip validationTooltip = new Tooltip("Input must be a range in this format:\n" +
            "<top left cell coordinate>..<bottom right cell coordinate>");

    public void init() {
        buttonSort.disableProperty().bind(anyChecked.not());
//        buttonGetColumns.disableProperty().bind(validRange.not());
        textFieldRange.setOnAction((ActionEvent event) -> textRangeAction());

        // Initially hide the Tooltip
        validationTooltip.setAutoHide(false);
        Tooltip.install(textFieldRange, validationTooltip);
        validationTooltip.hide();

    }

    public void setMainController(CommandsController mainController){
        this.mainController = mainController;
    }

    @FXML
    void buttonSortAction(ActionEvent event) {
        mainController.sortRange(new SortDto(boundariesDto, columToSort));
    }

    @FXML
    void buttonGetColumnsAction(ActionEvent event) {
        textRangeAction();
    }

    @FXML
    void textFieldRangeAction(ActionEvent event) {

    }

    @FXML
    void textFieldRangeKeyTyped(KeyEvent event) {

    }

    public void buttonGetColumnsActionRunLater(SortDto sortDto) {
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

        if (flowPaneColumns.getChildren().isEmpty()) {
            Label label = new Label("No numeric columns in range !");
            flowPaneColumns.getChildren().add(label);
        }
    }

    private void textRangeAction() {
        mainController.getNumericColumnsInBoundaries(textFieldRange.getText(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> mainController.showAlertPopup(new Exception(),"get Filtered Sheet"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();

                if(response.code() != 200){
                    Platform.runLater(() -> mainController.showAlertPopup(new Exception(jsonResponse),"get Numeric columns"));
                }
                else{
                    SortDto sortDto = GSON_INSTANCE.fromJson(jsonResponse, SortDto.class);
                    Platform.runLater(()-> buttonGetColumnsActionRunLater(sortDto));
                }
            }
        });
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

}
