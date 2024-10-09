package progress;

import app.AppController;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProgressController {

    AppController mainController;
    VBox ProgressVbox;
    ProgressBar ProgressBar;
    Label ProgressLabel;

    public ProgressController() {
        ProgressVbox = new VBox();
        ProgressBar = new ProgressBar();
        ProgressLabel = new Label("Loading File...");
        ProgressBar.setPrefWidth(300);

        // Set a transparent background for the label programmatically
        ProgressLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        ProgressVbox.getChildren().add(ProgressBar);
        ProgressVbox.getChildren().add(ProgressLabel);
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public VBox getProgressVbox() {
        return ProgressVbox;
    }

    public void init(Task<Boolean> fileTask){
        ProgressBar.progressProperty().bind(fileTask.progressProperty());
        ProgressLabel.textProperty().bind(fileTask.messageProperty());
    }

}
