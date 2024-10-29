package component.main.bottom.chatroom;

import component.main.MainController;
import component.main.bottom.api.ChatCommands;
import component.main.bottom.chatarea.ChatAreaController;
import component.main.bottom.commands.CommandsController;
import component.main.bottom.users.UsersListController;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import okhttp3.Callback;

import java.io.Closeable;
import java.io.IOException;

public class ChatRoomMainController implements Closeable, ChatCommands {


    // FXML Members

    @FXML private VBox usersListComponent;
    @FXML private UsersListController usersListComponentController;
    @FXML private VBox actionCommandsComponent;
    @FXML private CommandsController actionCommandsComponentController;
    @FXML private GridPane chatAreaComponent;
    @FXML private ChatAreaController chatAreaComponentController;


    // Members

    private MainController mainController;


    // Initializers

    @FXML
    public void initialize() {
        usersListComponentController.setChatCommands(this);
        actionCommandsComponentController.setChatCommands(this);
        chatAreaComponentController.setChatCommands(this);

        chatAreaComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
    }


    // Setters

    public void setActive() {
        usersListComponentController.startListRefresher();
        chatAreaComponentController.startListRefresher();
    }

    public void setInActive() {
        try {
            usersListComponentController.close();
            chatAreaComponentController.close();
        } catch (Exception ignored) {}
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    // Http requests

    public void postMessage(String text, Callback callback) {
        this.mainController.postMessage(text, callback);
    }

    public void getChat(String chatVersion, Callback callback) {
        this.mainController.getChat(chatVersion, callback);
    }

    public void getUsersList(Callback callback) {
        this.mainController.getUsersList(callback);
    }


    // Implementations

    @Override
    public void close() throws IOException {
        usersListComponentController.close();
        chatAreaComponentController.close();
    }

    @Override
    public void logout() {
        mainController.deleteUser();
        mainController.switchToLogin();
    }
}
