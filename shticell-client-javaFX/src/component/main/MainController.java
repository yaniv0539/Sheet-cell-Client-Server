package component.main;

import component.main.bottom.chatroom.ChatRoomMainController;
import component.main.center.app.AppController;
import component.main.center.dashboard.DashBoardController;
import component.main.center.login.LoginController;
import component.main.top.TopController;
import dto.FilterDto;
import dto.RequestDto;
import dto.SheetDto;
import dto.SortDto;
import dto.enums.PermissionType;
import dto.enums.Status;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.http.HttpClientUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static utils.Constants.*;

public class MainController {

    // FXML Members

    @FXML private GridPane topComponent;
    @FXML private TopController topComponentController;
    @FXML private Pane anchorPaneBottomApp;
    @FXML private Pane mainPanel;
    @FXML private SplitPane splitPaneApp;


    // Members

    private Stage primaryStage;

    private GridPane loginComponent;

    private ScrollPane dashboardComponent;
    private DashBoardController dashboardComponentController;

    private BorderPane appComponent;
    private AppController appComponentController;

    private Parent chatRoomComponent;
    private ChatRoomMainController chatRoomComponentController;

    private StringProperty userNameProperty;


    // Constructor

    public MainController() {

    }


    // Initializers

    @FXML
    public void initialize() {
        if (topComponentController != null) {
            topComponentController.setMainController(this);
            this.userNameProperty = new SimpleStringProperty("Guest");

            topComponentController.init();

            // prepare components
            loadLoginPage();
            loadDashboardPage();
            loadAppPage();
            loadChatRoomPage();
        }
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            LoginController loginComponentController = fxmlLoader.getController();
            loginComponentController.setMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboardPage() {
        URL DashboardPageUrl = getClass().getResource(DASHBOARD_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(DashboardPageUrl);
            dashboardComponent = fxmlLoader.load();
            dashboardComponentController = fxmlLoader.getController();
            dashboardComponentController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAppPage() {
        URL AppPageUrl = getClass().getResource(APP_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(AppPageUrl);
            appComponent = fxmlLoader.load();
            appComponentController = fxmlLoader.getController();
            appComponentController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatRoomPage() {
        URL loginPageUrl = getClass().getResource(CHAT_ROOM_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            chatRoomComponent = fxmlLoader.load();
            chatRoomComponentController = fxmlLoader.getController();
            chatRoomComponentController.setMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Getters

    public StringProperty userNameProperty() {
        return userNameProperty;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }


    // Setters

    public void setUserName(String userName) {
        this.userNameProperty.set(userName);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    // Screen switchers

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    public void switchToLogin() {
        chatRoomComponentController.setInActive();
        dashboardComponentController.setInActive();
        appComponentController.setInActive();
        setUserName("Guest");
        setMainPanelTo(loginComponent);
        splitPaneApp.getItems().remove(chatRoomComponent);
    }

    public void switchToDashboard() {
        setMainPanelTo(dashboardComponent);
        dashboardComponentController.setActive();
        appComponentController.setInActive();
    }

    public void switchToApp(String sheetName) {
        setMainPanelTo(appComponent);
        dashboardComponentController.setInActive();
        appComponentController.setActive(sheetName);
    }

    public void switchToChatRoom() {
        chatRoomComponentController.setActive();
        splitPaneApp.getItems().add(chatRoomComponent);
    }


    // Http requests to shticell servlet

    // Get sheet by version
    public void getSheet(String sheetName, String sheetVersion, Callback callback) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(SHEET_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion",sheetVersion)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get sheet by last version
    public void getSheet(String sheetName, Callback callback) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(SHEET_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get boundaries from a specific sheet if exists
    public void getBoundariesDto(String sheetName, String boundaries, Callback callback) {

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(GET_BOUNDARIES_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("boundaries", boundaries)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get the unique values in a specific range that selected
    public void getColumnUniqueValuesInRange(String sheetName, String sheetVersion, String column, String startRow, String endRow, Callback callback) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(UNIQUE_COL_VALUES_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .addQueryParameter("column", column)
                .addQueryParameter("startRow", startRow)
                .addQueryParameter("endRow", endRow)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get filtered sheet
    public void getFilteredSheet(String sheetName, String sheetVersion, FilterDto data, Callback callback) {

        String jsonString = GSON_INSTANCE.toJson(data);
        RequestBody body = RequestBody.create(jsonString, MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(FILTER_SHEET_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName",sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Get only the columns that have numerical values
    public void getNumericColumnsInBoundaries(String sheetName, String sheetVersion, String boundaries, Callback callback) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(GET_NUMERIC_COLUMNS_IN_RANGE_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .addQueryParameter("boundaries", boundaries)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get sorted sheet
    public void getSortedSheet(String sheetName, String sheetVersion, SortDto sortDto, Callback callback) {
        String jsonString = GSON_INSTANCE.toJson(sortDto);
        RequestBody body = RequestBody.create(jsonString, MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(SORT_SHEET_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Get all sheets overview
    public void getSheetsOverview(Callback callback) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(OVERVIEW_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);

    }

    // Get permissions for specific sheet
    public void getPermissions(String sheetName, Callback callback) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(PERMISSIONS_URL))
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get users list
    public void getUsersList(Callback callback) {

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(USERS_LIST))
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get chat
    public void getChat(String chatVersion, Callback callback) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(CHAT_LINES_LIST))
                .newBuilder()
                .addQueryParameter("chatVersion", chatVersion)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Post new xml sheet
    public void postXMLFile(String path, Callback callback) {
        File f = new File(path);
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("sheet",f.getName(),RequestBody.create(f, MediaType.parse("text/plain")))
                .build();

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(SHEET_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new cell to specific sheet
    public void postCell(String sheetName, String sheetVersion, String coordinate, String originalValue, Callback callback) {

        RequestBody body = RequestBody.create(originalValue, MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(CELL_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .addQueryParameter("target", coordinate)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new cell to specific dynamic sheet
    public void postCellToDynamicSheet(String sheetName, String sheetVersion, String coordinate, String originalValue, Callback callback) {
        RequestBody body = RequestBody.create(originalValue, MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(DYNAMIC_SHEET_CELL_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .addQueryParameter("target", coordinate)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new range in a specific sheet
    public void postRange(String sheetName, String sheetVersion, String rangeName, String rangeBoundaries, Callback callback) {

        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(RANGE_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion",String.valueOf(sheetVersion))
                .addQueryParameter("rangeName", rangeName)
                .addQueryParameter("boundaries", rangeBoundaries)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new user
    public void postUser(String userName, Callback callback) {

        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(USER_URL))
                .newBuilder()
                .addQueryParameter("userName", userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new permission request for specific sheet
    public void postRequestPermission(String sheetName, PermissionType requestedPermission, Callback callback) {
        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(REQUEST_PERMISSION_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("permissionType", requestedPermission.toString())
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new permission response for specific sheet
    public void postResponsePermission(String sheetName, Status ownerAnswer, RequestDto requestDto, Callback callback) {
        //transfer the request to confirm. : itay
        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(RESPONSE_PERMISSION_URL))
                .newBuilder()
                .addQueryParameter("userName", requestDto.requesterName())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("permissionType", requestDto.permissionType().toString())
                .addQueryParameter("status", requestDto.status().toString())
                .addQueryParameter("response", ownerAnswer.toString())
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new message by user
    public void postMessage(String message, Callback callback) {
        RequestBody body = RequestBody.create(message, MediaType.parse("text/plain"));

        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(SEND_CHAT_LINE))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Delete range in a specific sheet
    public void deleteRange(String sheetName, String sheetVersion, String rangeName, Callback callback) {

        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(RANGE_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .addQueryParameter("rangeName",rangeName)
                .build()
                .toString();

        HttpClientUtil.runAsyncDelete(finalUrl, body, callback);
    }

    // Delete User
    public void deleteUser() {
        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(USER_URL))
                .newBuilder()
                .addQueryParameter("userName", userNameProperty.get())
                .build()
                .toString();

        HttpClientUtil.runAsyncDelete(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                response.close();
            }
        });
    }


    //error function
    public void showAlertPopup(Throwable exception, String error) {
        // Create a new alert dialog for the error
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An Error Occurred While " + error);
        TextArea textArea = new TextArea();
        if (exception != null) {
            textArea.setText(exception.getMessage());
        } else {
            textArea.setText("An unknown error occurred.");
        }
        textArea.setWrapText(true);
        textArea.setEditable(false);

        // Allow TextArea to expand dynamically with the window
        VBox content = new VBox(textArea);
        content.setPrefSize(300, 200);  // Adjust the size of the popup window

        // Add the TextArea to the Alert dialog
        alert.getDialogPane().setContent(content);

        // Make the dialog non-resizable if needed
        alert.initStyle(StageStyle.DECORATED);

        alert.showAndWait();  // Display the popup
    }

    public void showPopupMessage(String message) {
        // Create a label with the message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 18px;");

        // Create a popup and add the message label
        Popup popup = new Popup();
        popup.getContent().add(messageLabel);
        popup.setAutoHide(true);  // Automatically close the popup when clicking outside

        // Center the popup relative to the main stage
        popup.show(primaryStage);
        popup.setX(primaryStage.getX() + (primaryStage.getWidth() - messageLabel.getWidth()) / 2);
        popup.setY(primaryStage.getY() + (primaryStage.getHeight() - messageLabel.getHeight()) / 2);

        // Create a fade-out transition for the popup
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), messageLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(3));  // Delay for 1 second before fading

        fadeOut.setOnFinished(event -> popup.hide());  // Hide popup after fading
        fadeOut.play();  // Start fade-out transition
    }


    public void uploadSheetToWorkspace(SheetDto sheetDto,boolean isEditor) {
        appComponentController.onFinishLoadingFile(sheetDto,isEditor);
    }
}
