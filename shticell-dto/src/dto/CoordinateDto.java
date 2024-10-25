package dto;

import sheet.coordinate.api.Coordinate;

import java.util.Objects;

import static sheet.coordinate.impl.CoordinateFactory.*;

public record CoordinateDto(
        int row,
        int column
) {
    public CoordinateDto(Coordinate coordinate) {
        this(
                coordinate.getRow(),
                coordinate.getCol()
        );
    }

    public CoordinateDto(String coordinateName) {
        this(
                extractRow(coordinateName) - 1,
                parseColumnToInt(extractColumn(coordinateName)) - 1
        );
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
