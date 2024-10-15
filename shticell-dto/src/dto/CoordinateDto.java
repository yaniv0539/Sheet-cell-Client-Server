package dto;

import sheet.coordinate.api.Coordinate;

import java.util.Objects;

public class CoordinateDto {
    public int row;
    public int column;

    public CoordinateDto(Coordinate coordinate) {
        this.row = coordinate.getRow();
        this.column = coordinate.getCol();
    }

    public CoordinateDto(int row, int col) {
        this.row = row;
        this.column = col;
    }
    public CoordinateDto(String coordinateName ) {
        row = extractRow(coordinateName) - 1;
        column = parseColumnToInt(extractColumn(coordinateName)) - 1;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
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
    @Override
    public String toString() {
        // Convert x to a character, starting with 'A'
        char column = (char) ('A' + this.column);

        // Convert y to a 1-based index for the row
        int row = this.row + 1;

        // Combine column and row into the string representation
        return "" + column + row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinateDto that = (CoordinateDto) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}
