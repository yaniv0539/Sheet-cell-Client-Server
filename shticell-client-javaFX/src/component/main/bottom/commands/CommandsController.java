package component.main.bottom.commands;

import component.main.bottom.api.ChatCommands;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.http.HttpClientUtil;

import java.io.IOException;

public class CommandsController {

    private ChatCommands chatCommands;
    private final BooleanProperty autoUpdates;
    @FXML private ToggleButton autoUpdatesButton;

    public CommandsController() {
        autoUpdates = new SimpleBooleanProperty();
    }

    @FXML
    public void initialize() {
        autoUpdates.bind(autoUpdatesButton.selectedProperty());
    }

    public ReadOnlyBooleanProperty autoUpdatesProperty() {
        return autoUpdates;
    }

    @FXML
    void logoutClicked(ActionEvent event) {
        HttpClientUtil.runAsync(Constants.LOGOUT, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() || response.isRedirect()) {
                    chatCommands.logout();
                }
            }
        });
    }

    @FXML
    void quitClicked(ActionEvent event) {
        Platform.exit();
    }

    public void setChatCommands(ChatCommands chatRoomMainController) {
        this.chatCommands = chatRoomMainController;
    }
}
