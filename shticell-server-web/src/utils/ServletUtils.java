package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dto.CellDto;
import dto.CoordinateDto;
import dto.deserializer.CellDtoDeserializer;
import dto.deserializer.CoordinateMapDeserializer;
import dto.enums.PermissionType;
import dto.enums.Status;
import dto.serializer.CellDtoSerializer;
import dto.serializer.CoordinateMapSerializer;
import engine.api.Engine;
import engine.chat.ChatManager;
import engine.impl.EngineImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;

public class ServletUtils {

    private static final String ENGINE_ATTRIBUTE_NAME = "engine";
    private static final String CHAT_ATTRIBUTE_NAME = "chat";
    private static final String GSON_ATTRIBUTE_NAME = "gson";

    private static final Object engineLock = new Object();
    private static final Object chatLock = new Object();
    private static final Object gsonLock = new Object();
    private static final Object versionLock = new Object();


    public static Engine getEngine(ServletContext servletContext) {

        synchronized (engineLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, EngineImpl.create());
            }
        }
        return (Engine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
    }

    public static ChatManager getChatManager(ServletContext servletContext) {

        synchronized (chatLock) {
            if (servletContext.getAttribute(CHAT_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(CHAT_ATTRIBUTE_NAME, ChatManager.create());
            }
        }
        return (ChatManager) servletContext.getAttribute(CHAT_ATTRIBUTE_NAME);
    }

    public static Gson getGson(ServletContext servletContext) {

        synchronized (gsonLock) {
            if (servletContext.getAttribute(GSON_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(GSON_ATTRIBUTE_NAME, new GsonBuilder()
                        .setPrettyPrinting()
                        .registerTypeAdapter(CellDto.class,new CellDtoSerializer())
                        .registerTypeAdapter(CellDto.class,new CellDtoDeserializer())
                        .registerTypeAdapter(new TypeToken<Map<CoordinateDto, CoordinateDto>>(){}.getType(), new CoordinateMapSerializer())
                        .registerTypeAdapter(new TypeToken<Map<CoordinateDto, CoordinateDto>>(){}.getType(), new CoordinateMapDeserializer())
                        .create());
            }
        }
        return (Gson) servletContext.getAttribute(GSON_ATTRIBUTE_NAME);
    }

    public static String getJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder jsonBody = new StringBuilder();
        String line;

        while ((line = request.getReader().readLine()) != null) {
            jsonBody.append(line);
        }

        return jsonBody.toString();
    }

    public static String getUserName(HttpServletRequest request) {
        String userName = request.getParameter(Constants.USER_NAME_PARAMETER);

        if (userName == null || userName.isEmpty()) {
            throw new RuntimeException("User name parameter is null");
        }
        return userName;
    }

    public static String getSheetName(HttpServletRequest request) {
        String sheetName = request.getParameter(Constants.SHEET_NAME_PARAMETER);

        if (sheetName == null || sheetName.isEmpty()) {
            throw new RuntimeException("Sheet name parameter is null");
        }
        return sheetName;
    }

    public static int getSheetVersion(HttpServletRequest request) {
        String sheetVersion = request.getParameter(Constants.SHEET_VERSION_PARAMETER);

        if (sheetVersion == null || sheetVersion.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(sheetVersion);
    }

    public static String getCellName(HttpServletRequest request) {
        String cellName = request.getParameter(Constants.CELL_NAME_PARAMETER);

        if (cellName == null || cellName.isEmpty()) {
            throw new RuntimeException("Cell name parameter is null");
        }
        return cellName;
    }

    public static String getRangeName(HttpServletRequest request) {
        String rangeName = request.getParameter(Constants.RANGE_NAME_PARAMETER);

        if (rangeName == null || rangeName.isEmpty()) {
            throw new RuntimeException("Range name parameter is null");
        }
        return rangeName;
    }

    public static String getBoundaries(HttpServletRequest request) {
        String rangeValue = request.getParameter(Constants.RANGE_BOUNDARIES_PARAMETER);

        if (rangeValue == null || rangeValue.isEmpty()) {
            throw new RuntimeException("Boundaries parameter is null");
        }
        return rangeValue;
    }

    public static int getColumn(HttpServletRequest request) {
        String column = request.getParameter(Constants.SHEET_COLUMN_PARAMETER);

        if (column == null || column.isEmpty()) {
            throw new RuntimeException("Column parameter is null");
        }
        return Integer.parseInt(column);
    }

    public static int getStartRow(HttpServletRequest request) {
        String startRow = request.getParameter(Constants.SHEET_START_ROW_PARAMETER);

        if (startRow == null || startRow.isEmpty()) {
            throw new RuntimeException("Start row parameter is null");
        }
        return Integer.parseInt(startRow);
    }

    public static int getEndRow(HttpServletRequest request) {
        String endRow = request.getParameter(Constants.SHEET_END_ROW_PARAMETER);

        if (endRow == null || endRow.isEmpty()) {
            throw new RuntimeException("End row parameter is null");
        }
        return Integer.parseInt(endRow);
    }

    public static PermissionType getPermissionType(HttpServletRequest request) {
        String permissionType = request.getParameter(Constants.PERMISSION_TYPE_PARAMETER);

        if (permissionType == null || permissionType.isEmpty()) {
            throw new RuntimeException("Permission type parameter is null");
        }

        return PermissionType.valueOf(permissionType);
    }

    public static Status getStatus(HttpServletRequest request) {
        String status = request.getParameter(Constants.STATUS_PARAMETER);

        if (status == null || status.isEmpty()) {
            throw new RuntimeException("Status parameter is null");
        }

        return Status.valueOf(status);
    }

    public static Status getResponse(HttpServletRequest request) {
        String response = request.getParameter(Constants.RESPONSE_PARAMETER);

        if (response == null || response.isEmpty()) {
            throw new RuntimeException("Status parameter is null");
        }

        return Status.valueOf(response);
    }

    public static int getChatVersion(HttpServletRequest request) {
        String chatVersion = request.getParameter(Constants.CHAT_VERSION_PARAMETER);

        if (chatVersion == null || chatVersion.isEmpty()) {
            throw new RuntimeException("Chat version parameter is null");
        }
        return Integer.parseInt(chatVersion);
    }

    public static Object getVersionLock() {
        return versionLock;
    }


}

