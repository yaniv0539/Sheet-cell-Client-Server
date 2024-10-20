package component.main.center.app.ranges.operations.add;

import component.main.center.app.ranges.RangesController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AddRangeController {

    private RangesController mainController;

    @FXML
    private Button buttonSubmit;

    @FXML
    private TextField textFieldRangeBoundaries;

    @FXML
    private TextField textFieldRangeName;

    public void setMainController(RangesController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void submitAction(ActionEvent event) {
        this.mainController.addRange(textFieldRangeName.getText(), textFieldRangeBoundaries.getText());
    }
}
