package component.main;

import component.main.center.app.AppController;
import component.main.center.dashboard.DashboardController;
import component.main.center.login.LoginController;
import component.main.top.TopController;
import dto.FilterDto;
import dto.SortDto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import okhttp3.*;
import utils.Constants;
import utils.http.HttpClientUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static utils.Constants.*;

public class MainController {

    @FXML private GridPane topComponent;
    @FXML private TopController topComponentController;

    private GridPane loginComponent;
    private LoginController loginComponentController;

    private GridPane dashboardComponent;
    private DashboardController dashboardComponentController;

    private GridPane appComponent;
    private AppController appComponentController;

    @FXML
    private AnchorPane anchorPaneBottomApp;

    @FXML
    private AnchorPane mainPanel;

    @FXML
    private SplitPane splitPaneApp;


    // Constructor

    public MainController() {

    }


    // Initializers

    @FXML
    public void initialize() {
        if (topComponentController != null) {
            topComponentController.setMainController(this);

            // prepare components
            loadLoginPage();
            loadDashboardPage();
            loadAppPage();
        }
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            loginComponentController = fxmlLoader.getController();
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

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    public void switchToLogin() {
        setMainPanelTo(loginComponent);
        loginComponentController.setActive();
    }

    public void switchToDashboard() {
        setMainPanelTo(dashboardComponent);
        dashboardComponentController.setActive();
    }

    public void switchToApp() {
        setMainPanelTo(appComponent);
        appComponentController.setActive();
    }


    // Http requests to shticell servlet

    // Get sheet by version
    public void getSheet(String name, String version, Callback callback) {
        String finalUrl = HttpUrl
                .parse(SHEET_URL)
                .newBuilder()
                .addQueryParameter("sheetName", name)
                .addQueryParameter("sheetVersion",version)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get boundaries from a specific sheet if exists
    public void getBoundariesDto(String sheetName, String boundaries, Callback callback) {

        String finalUrl = HttpUrl
                .parse(GET_BOUNDARIES_URL)
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("boundaries", boundaries)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, callback);
    }

    // Get the unique values in a specific range that selected
    public void getColumnUniqueValuesInRange(String sheetName, String sheetVersion, String column, String startRow, String endRow, Callback callback) {
        String finalUrl = HttpUrl
                .parse(UNIQUE_COL_VALUES_URL)
                .newBuilder()
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

        String finalUrl = HttpUrl
                .parse(FILTER_SHEET_URL)
                .newBuilder()
                .addQueryParameter("sheetName",sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Get only the columns that have numerical values
    public void getNumericColumnsInBoundaries(String sheetName, String sheetVersion, String boundaries, Callback callback) {
        String finalUrl = HttpUrl
                .parse(GET_NUMERIC_COLUMNS_IN_RANGE_URL)
                .newBuilder()
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

        String finalUrl = HttpUrl
                .parse(SORT_SHEET_URL)
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("sheetVersion", sheetVersion)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Get user details
    public void getUserDetails(String userName, Callback callback) {
        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
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

        HttpClientUtil.runAsyncPost(SHEET_URL, body, callback);
    }

    // Post new cell to specific sheet
    public void postCell(String sheetName, String coordinate, String originalValue, Callback callback) {

        RequestBody body = RequestBody.create(originalValue, MediaType.parse("text/plain"));

        String finalUrl = HttpUrl
                .parse(CELL_URL)
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                .addQueryParameter("target", coordinate)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Post new range in a specific sheet
    public void postRange(String sheetName, String sheetVersion, String rangeName, String rangeBoundaries, Callback callback) {

        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));

        String finalUrl = HttpUrl
                .parse(RANGE_URL)
                .newBuilder()
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

        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncPost(finalUrl, body, callback);
    }

    // Delete range in a specific sheet
    public void deleteRange(String sheetName, String sheetVersion, String rangeName, Callback callback) {

        RequestBody body = RequestBody.create("", MediaType.parse("text/plain"));
        String finalUrl = HttpUrl
                .parse(RANGE_URL)
                .newBuilder()
                .addQueryParameter("sheetName", sheetName)
                //to check if it is really the most update sheet: itay.
                .addQueryParameter("sheetVersion", sheetVersion)
                .addQueryParameter("rangeName",rangeName)
                .build()
                .toString();

        HttpClientUtil.runAsyncDelete(finalUrl, body, callback);
    }

}
