package dto;

import sheet.coordinate.api.Coordinate;

public class CoordinateDto {
    public int row;
    public int column;

    public CoordinateDto(Coordinate coordinate) {
        this.row = coordinate.getRow();
        this.column = coordinate.getCol();
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
}
