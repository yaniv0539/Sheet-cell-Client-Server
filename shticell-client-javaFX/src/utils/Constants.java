package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.CellDto;
import dto.deserializer.CellDtoDeserializer;
import dto.serializer.CellDtoSerializer;

public class Constants {
    // GSON instance
    public final static Gson GSON_INSTANCE = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(CellDto.class,new CellDtoSerializer())
            .registerTypeAdapter(CellDto.class,new CellDtoDeserializer())
            .create();

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/shticell";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String SHEET_URL = FULL_SERVER_PATH + "/sheet";
    public final static String CELL_URL = SHEET_URL + "/cell";
    public final static String RANGE_URL = SHEET_URL + "/ranges";

    public final static String GET_BOUNDARIES_URL = RANGE_URL + "/boundaries";
    public final static String FILTER_SHEET_URL = SHEET_URL + "/filter";
    public final static String UNIQUE_COL_VALUES_URL = FILTER_SHEET_URL + "/uniqueColumnValues";
    public final static String SORT_SHEET_URL = SHEET_URL + "/sort";
    public final static String GET_NUMERIC_COLUMNS_IN_RANGE_URL = SORT_SHEET_URL + "/numericColumns";



//    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
//    public final static String LOGOUT = FULL_SERVER_PATH + "/chat/logout";
//    public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/pages/chatroom/sendChat";
//    public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat";

}
