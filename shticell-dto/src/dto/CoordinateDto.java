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

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
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
