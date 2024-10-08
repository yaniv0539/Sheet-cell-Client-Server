import com.google.gson.Gson;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttp;

public class SimpleJavaFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a label
        Label label = new Label("Hello, JavaFX!");

        // Create a text field
        TextField textField = new TextField();
        textField.setPromptText("Enter some text");

        // Create a button
        Button button = new Button("Click Me!");

        // Set an action for the button
        button.setOnAction(e -> {
            String inputText = textField.getText();
            if (!inputText.isEmpty()) {
                label.setText(inputText);
            } else {
                label.setText("Hello, JavaFX!");
            }
        });

        // Create a VBox layout and add the controls to it
        VBox layout = new VBox(10);  // 10px spacing between elements
        layout.getChildren().addAll(label, textField, button);

        // Create a scene and set it on the stage
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simple JavaFX Application");

        // Show the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}