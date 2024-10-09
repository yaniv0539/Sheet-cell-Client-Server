package ranges.operations.add;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import ranges.RangesController;

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
