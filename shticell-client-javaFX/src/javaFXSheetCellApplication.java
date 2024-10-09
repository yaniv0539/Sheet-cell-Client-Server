import app.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class javaFXSheetCellApplication extends Application {

    private static final String APP_FXML_INCLUDE_RESOURCE = "app/app.fxml";

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(APP_FXML_INCLUDE_RESOURCE);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());

        Scene scene = new Scene(root, 500, 500);

        stage.setTitle("Shticell");
        stage.setScene(scene);
        ((AppController) fxmlLoader.getController()).setPrimaryStage(stage);
        stage.show();
    }
}