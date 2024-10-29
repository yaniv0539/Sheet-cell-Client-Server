package component.main.bottom.commands;

import component.main.bottom.api.ChatCommands;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;


public class CommandsController {


    // FXML Members

    @FXML private ToggleButton autoUpdatesButton;


    // Members

    private ChatCommands chatCommands;
    private final BooleanProperty autoUpdates;


    // Constructor

    public CommandsController() {
        autoUpdates = new SimpleBooleanProperty();
    }


    // Initializers

    @FXML
    public void initialize() {
        autoUpdates.bind(autoUpdatesButton.selectedProperty());
    }


    // Getters

    public ReadOnlyBooleanProperty autoUpdatesProperty() {
        return autoUpdates;
    }


    // Setters

    public void setChatCommands(ChatCommands chatRoomMainController) {
        this.chatCommands = chatRoomMainController;
    }


    // FXML Methods

    @FXML
    void logoutClicked() {
        this.chatCommands.logout();
    }

    @FXML
    void quitClicked() {
        Platform.exit();
    }
}
