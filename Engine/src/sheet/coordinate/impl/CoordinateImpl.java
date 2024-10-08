package sheet.coordinate.impl;

import sheet.coordinate.api.Coordinate;

import java.io.Serializable;
import java.util.Objects;

public class CoordinateImpl implements Coordinate, Serializable {

    private final int row;
    private final int column;

    private CoordinateImpl(int row, int column) {

        if (!isValidRow(row)) {
            throw new IllegalArgumentException("Row must be a non-negative integer");
        }

        if (!isValidColumn(column)) {
            throw new IllegalArgumentException("Column must be a non-negative integer");
        }

        this.row = row;
        this.column = column;
    }

    public static CoordinateImpl create(int row, int column) {
        return new CoordinateImpl(row, column);
    }

    @Override
    public int getRow() {
        return this.row;
    }

    @Override
    public int getCol() {
        return this.column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinateImpl that = (CoordinateImpl) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    private static boolean isValidRow(int row)
    {
        return row >= 0;
    }

    private static boolean isValidColumn(int column)
    {
        return column >= 0;
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
