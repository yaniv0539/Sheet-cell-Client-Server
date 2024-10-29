package component.main.bottom.users;

import component.main.bottom.chatroom.ChatRoomMainController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import okhttp3.Callback;

import java.io.Closeable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static utils.Constants.REFRESH_RATE;

public class UsersListController implements Closeable {


    // FXML Members

    @FXML private ListView<String> usersListView;
    @FXML private Label chatUsersLabel;


    // Members

    private ChatRoomMainController chatRoomMainController;

    private Timer timer;
    private TimerTask listRefresher;
    private final BooleanProperty autoUpdate;
    private final IntegerProperty totalUsers;


    // Constructor

    public UsersListController() {
        autoUpdate = new SimpleBooleanProperty();
        totalUsers = new SimpleIntegerProperty();
    }


    // Initializers

    @FXML
    public void initialize() {
        chatUsersLabel.textProperty().bind(Bindings.concat("Chat Users: (", totalUsers.asString(), ")"));
    }


    // Getters

    public BooleanProperty autoUpdatesProperty() {
        return autoUpdate;
    }


    // Setters

    public void setChatCommands(ChatRoomMainController chatRoomMainController) {
        this.chatRoomMainController = chatRoomMainController;
    }


    // Http requests

    public void getUsersList(Callback callback) {
        this.chatRoomMainController.getUsersList(callback);
    }


    // General Methods

    public void updateUsersList(List<String> usersNames) {
        Platform.runLater(() -> {
            ObservableList<String> items = usersListView.getItems();
            items.clear();
            items.addAll(usersNames);
            totalUsers.set(usersNames.size());
        });
    }

    public void startListRefresher() {
        UserListRefresher newListRefresher = new UserListRefresher(autoUpdate);
        newListRefresher.setUsersListController(this);

        listRefresher = newListRefresher;

        timer = new Timer();
        timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
    }


    // Implementations

    @Override
    public void close() {
        usersListView.getItems().clear();
        totalUsers.set(0);
        if (listRefresher != null && timer != null) {
            listRefresher.cancel();
            timer.cancel();
        }
    }
}
