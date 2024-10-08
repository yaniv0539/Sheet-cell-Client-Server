package sheet.coordinate.impl;

import sheet.coordinate.api.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class CoordinateFactory {

    private static final Map<String, Coordinate> coordinateMap = new HashMap<String, Coordinate>();

    public static Coordinate createCoordinate(int row, int col) {
        String key = row + "," + col;

        Coordinate coordinate = coordinateMap.get(key);

        if (coordinate == null) {
            coordinate = CoordinateImpl.create(row, col);
            coordinateMap.put(key, coordinate);
        }
        return coordinate;
    }

    public static Coordinate toCoordinate(String coordinateName) {

        coordinateName = coordinateName.toUpperCase();

        if (!isValidCoordinateFormat(coordinateName))
            throw new IllegalArgumentException("Invalid cell-id: " + "\"" + coordinateName + "\"\n"+
                    "format example: \"A5\" (make sure you dont have spaces in input).");

        int row = extractRow(coordinateName) - 1;
        int column = parseColumnToInt(extractColumn(coordinateName)) - 1;

        return CoordinateFactory.createCoordinate(row, column);
    }

    private static boolean isValidCoordinateFormat(String coordinateName) {

        if (coordinateName == null || coordinateName.isEmpty()) {
            return false;
        }
        coordinateName = coordinateName.toUpperCase();

        return coordinateName.matches("^[A-Z]+[0-9]+$");
    }

    public static int extractRow(String coordinateName) {

        int index = 0;

        while (index < coordinateName.length() && !Character.isDigit(coordinateName.charAt(index))) {
            index++;
        }

        return Integer.parseInt(coordinateName.substring(index));
    }

    public static String extractColumn(String coordinateName) {

        int index = 0;
        while (index < coordinateName.length() && !Character.isDigit(coordinateName.charAt(index))) {
            index++;
        }

        return coordinateName.substring(0, index);
    }

    public static int parseColumnToInt(String column) {
        int result = 0;
        int length = column.length();

        for (int i = 0; i < length; i++) {
            char c = column.charAt(i);
            int value = c - 'A' + 1;
            result = result * 26 + value;
        }

        return result;
    }

    public static boolean isGreaterThen(Coordinate to, Coordinate from) {
        return (to.getRow() >= from.getRow()) && (to.getCol() >= from.getCol());
    }
}
