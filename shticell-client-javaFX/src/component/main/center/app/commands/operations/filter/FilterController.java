package component.main.center.app.commands.operations.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import component.main.center.app.commands.CommandsController;
import dto.*;
import dto.deserializer.CellDtoDeserializer;
import dto.deserializer.CoordinateMapDeserializer;
import dto.serializer.CoordinateMapSerializer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

    private BooleanProperty anyValueChecked = new SimpleBooleanProperty(false);
    private BooleanProperty validRange = new SimpleBooleanProperty(false);

    private BoundariesDto boundariesToFilter = null;
    private String filteringByColumn = null;
    private List<String> uniqueValuesToFilter = new ArrayList<>();

//    Tooltip validationTooltip = new Tooltip("Input must be a range in this format:\n" +
//            "<top left cell coordinate>..<bottom right cell coordinate>");

    public void setMainController(CommandsController mainController) {
        this.mainController = mainController;
    }

    public void init() {
        comboBoxColumn1.disableProperty().bind(validRange.not());
        buttonFilter.disableProperty().bind(anyValueChecked.not());
        textFieldRange.setOnAction((ActionEvent event) -> textRangeAction());
    }

    @FXML
    void columnAction(ActionEvent event) {
        if (comboBoxColumn1.getSelectionModel().getSelectedIndex() != -1) {
            filteringByColumn = comboBoxColumn1.getSelectionModel().getSelectedItem();
            //take uniqe values
            //add them to filter HbOX
            anyValueChecked.set(false);
            flowPaneValues.getChildren().clear();

            mainController.getColumnUniqueValuesInRange(
                    filteringByColumn.toUpperCase().toCharArray()[0] - 'A',
                    boundariesToFilter.getFrom().getRow(),
                    boundariesToFilter.getTo().getRow(),
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Platform.runLater(() -> mainController.showAlertPopup(new Exception(),"get column unique values"));
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            Gson gson = new GsonBuilder().registerTypeAdapter(CellDto.class,new CellDtoDeserializer()).create();
                            assert response.body() != null;
                            String jsonResponse = response.body().string();
                            if(response.code() != 200) {
                                Platform.runLater(() -> mainController.showAlertPopup(new Exception(jsonResponse),"get column unique values"));
                            }
                            else {
                                List<String> values = gson.fromJson(jsonResponse, new TypeToken<List<String>>(){}.getType());

                                Platform.runLater(() -> columActionRunLater(values));
                            }
                        }
                    });
        }
    }

    @FXML
    void filterAction(ActionEvent event) {
        FilterDto data = new FilterDto(boundariesToFilter, filteringByColumn, uniqueValuesToFilter);
        this.mainController.filterRange(data, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> mainController.showAlertPopup(new Exception(),"get Filtered Sheet"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();
                if (response.code() != 200) {
                    Platform.runLater(() -> mainController.showAlertPopup(new Exception(jsonResponse),"get Filtered Sheet"));
                } else {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(CellDto.class,new CellDtoDeserializer())
                            .registerTypeAdapter(new TypeToken<Map<CoordinateDto, CoordinateDto>>(){}.getType(), new CoordinateMapSerializer())
                            .registerTypeAdapter(new TypeToken<Map<CoordinateDto, CoordinateDto>>(){}.getType(), new CoordinateMapDeserializer())
                            .create();
                    FilterDesignDto responseDto = gson.fromJson(jsonResponse, FilterDesignDto.class);
                    Platform.runLater(() -> mainController.getFilteredSheetRunLater(responseDto));
                }
            }
        });
    }

    public void columActionRunLater(List<String> uniqueValues) {

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

    private void textRangeAction() {
        comboBoxColumn1.getItems().clear();

        mainController.getBoundariesDto(textFieldRange.getText(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> mainController.showAlertPopup(new Exception(),"get Boundaries Dto"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonResponse = response.body().string();
                if(response.code() != 200){
                    Platform.runLater(() -> mainController.showAlertPopup(new Exception(jsonResponse),"get Boundaries Dto"));
                }
                else{
                    Gson gson = new GsonBuilder().registerTypeAdapter(CellDto.class,new CellDtoDeserializer()).create();
                    BoundariesDto boundariesDto = gson.fromJson(jsonResponse, BoundariesDto.class);
                    Platform.runLater(() -> textRangeActionRunLater(boundariesDto));
                }
            }
        });
    }

    private void textRangeActionRunLater(BoundariesDto boundaries) {
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

    // TODO: functions that will be deleted eventually.
//    private boolean isInputValid(String newValue) {
//        return (BoundariesFactory.isValidBoundariesFormat(newValue) &&
//                mainController.isBoundariesValidForCurrentSheet(BoundariesFactory.toBoundaries(newValue)));
//    }

}
