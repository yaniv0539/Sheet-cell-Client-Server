package component.main.bottom.chatarea;

import component.main.bottom.chatarea.model.ChatLinesWithVersion;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.TimerTask;

import static utils.Constants.GSON_INSTANCE;

public class ChatAreaRefresher extends TimerTask {


    // Members

    private ChatAreaController chatAreaController;
    private final IntegerProperty chatVersion;
    private final BooleanProperty shouldUpdate;


    // Constructors

    public ChatAreaRefresher(IntegerProperty chatVersion, BooleanProperty shouldUpdate, ChatAreaController chatAreaController) {
        this.chatAreaController = chatAreaController;
        this.chatVersion = chatVersion;
        this.shouldUpdate = shouldUpdate;
    }


    // Setters

    public void setChatAreaController(ChatAreaController chatAreaController) {
        this.chatAreaController = chatAreaController;
    }


    // Implementations

    @Override
    public void run() {

        if (!shouldUpdate.get()) {
            return;
        }

        this.chatAreaController.getChat(String.valueOf(chatVersion.get()), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String rawBody = response.body().string();
                    ChatLinesWithVersion chatLinesWithVersion = GSON_INSTANCE.fromJson(rawBody, ChatLinesWithVersion.class);
                    chatAreaController.updateChatLines(chatLinesWithVersion);
                }
            }
        });
    }

}
