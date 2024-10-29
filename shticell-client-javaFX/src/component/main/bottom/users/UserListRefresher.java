package component.main.bottom.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.BooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;


public class UserListRefresher extends TimerTask {


    // Members

    private final BooleanProperty shouldUpdate;
    private UsersListController usersListController;


    // Constructor

    public UserListRefresher(BooleanProperty shouldUpdate) {
        this.shouldUpdate = shouldUpdate;
    }


    // Setters

    public void setUsersListController(UsersListController usersListController) {
        this.usersListController = usersListController;
    }


    // Implementations

    @Override
    public void run() {

        if (!shouldUpdate.get()) {
            return;
        }

        this.usersListController.getUsersList(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String jsonString = response.body().string();

                if (response.isSuccessful()) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    Type setType = new TypeToken<List<String>>(){}.getType();
                    List<String> usersNames = gson.fromJson(jsonString, setType);
                    usersListController.updateUsersList(usersNames);
                }
            }
        });
    }
}
