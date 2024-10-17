package component.ranges;

import component.app.AppController;
import dto.RangeDto;
import dto.SheetDto;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import component.ranges.operations.add.AddRangeController;
import sheet.range.api.RangeGetters;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RangesController {

    private static final String ADD_RANGE_POPUP_FXML_INCLUDE_RESOURCE = "operations/add/addRange.fxml";

    @FXML
    private Button buttonAddRange;

    @FXML
    private Button buttonDeleteRange;

    @FXML
    private TableColumn<RangeDto, String> tableActiveRanges;

    @FXML
    private TableView<RangeDto> tableViewActiveRanges;

    private AppController mainController;

    private final ObservableList<RangeDto> ranges;

    private Stage popupStage;

    private RangeDto lastClickedItem = null;

    public RangesController() {
        ranges = FXCollections.observableArrayList();
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void addRangeAction(ActionEvent event) throws IOException {
        activateRangeAction(ADD_RANGE_POPUP_FXML_INCLUDE_RESOURCE, "Add Range");
    }

    @FXML
    void deleteRangeAction(ActionEvent event) {
        RangeDto selectedRange = tableViewActiveRanges.getSelectionModel().getSelectedItem();
        this.mainController.deleteRange(selectedRange);

    }

    void activateRangeAction(String resource, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(resource);
        fxmlLoader.setLocation(url);
        Parent popupRoot = fxmlLoader.load(url.openStream());

        AddRangeController addRangeController = fxmlLoader.getController();

        addRangeController.setMainController(this);

        this.popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(title);

        Scene popupScene = new Scene(popupRoot, 415, 100);
        popupStage.setResizable(false);
        popupStage.setScene(popupScene);

        popupStage.showAndWait();
    }

    public void init() {
        BooleanProperty showRangesProperty = this.mainController.showRangesProperty();
        Map<Integer, TableCell<RangeGetters, String>> cellMap = new HashMap<>();

        buttonAddRange.disableProperty().bind(showRangesProperty.not());
        buttonDeleteRange.disableProperty().bind(showRangesProperty.not());
        tableViewActiveRanges.disableProperty().bind(showRangesProperty.not());

        tableActiveRanges.setCellValueFactory(new PropertyValueFactory<>("name"));

        tableViewActiveRanges.focusedProperty().addListener((observable, oldValue, newValue) -> {
            TableView.TableViewSelectionModel<RangeDto> selectionModel = tableViewActiveRanges.getSelectionModel();
            RangeDto selectedItem = selectionModel.getSelectedItem();

            if (lastClickedItem != null && !newValue) {
                this.mainController.resetRangeOnSheet(lastClickedItem);
                //selectionModel.clearSelection();
            }
        });

        tableViewActiveRanges.setOnMouseClicked(event -> {
           if (event.getClickCount() == 1 && !event.isConsumed()) {
                // Find the clicked row
                TableView.TableViewSelectionModel<RangeDto> selectionModel = tableViewActiveRanges.getSelectionModel();
                RangeDto selectedItem = selectionModel.getSelectedItem();

                if (selectedItem != null) {
                    if (lastClickedItem != null) {
                        //return all the cells in the range to the previuos background
                        this.mainController.resetRangeOnSheet(lastClickedItem);
                    }
                    this.mainController.paintRangeOnSheet(selectedItem, Color.rgb(251, 238, 166));
                    lastClickedItem = selectedItem;
                }
            }
        });

        tableViewActiveRanges.setItems(ranges);
    }

    public void addRange(String name, String boundaries) {
            this.mainController.addRange(name, boundaries);
    }
    public void runLaterAddRange(RangeDto rangeDto){
        ranges.add(rangeDto);
        popupStage.close();
        tableViewActiveRanges.refresh();
    }

    public void uploadRanges(Set<RangeDto> ranges) {
        this.ranges.clear();
        this.ranges.addAll(ranges);
        tableViewActiveRanges.refresh();
    }

    public void runLaterRemoveRange(RangeDto range) {
        this.ranges.remove(range);
    }
}
