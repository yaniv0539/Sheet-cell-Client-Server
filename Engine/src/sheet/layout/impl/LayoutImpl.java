package sheet.layout.impl;

import sheet.layout.api.Layout;
import sheet.layout.size.api.Size;
import sheet.layout.size.api.SizeGetters;

import java.io.Serializable;

public class LayoutImpl implements Layout, Serializable {
    private Size size;
    private int rows;
    private int columns;

    private LayoutImpl(Size size, int rows, int columns) {
        setSize(size);
        setRows(rows);
        setColumns(columns);
    }

    public static LayoutImpl create(Size size, int row, int column) {
        return new LayoutImpl(size, row, column);
    }

    @Override
    public SizeGetters getSize() {
        return this.size;
    }

    @Override
    public int getRows() {
        return this.rows;
    }

    @Override
    public int getColumns() {
        return this.columns;
    }

    @Override
    public void setSize(Size size) {
        if (size == null) {
            throw new IllegalArgumentException("Size cannot be null");
        }
        this.size = size;
    }

    @Override
    public void setRows(int rows) {
        if (!isValidRows(rows))
            throw new IllegalArgumentException(rows + " is not a valid rows, it must be a positive integer");
        this.rows = rows;
    }

    @Override
    public void setColumns(int columns) {
        if (!isValidColumns(columns))
            throw new IllegalArgumentException(columns + " is not a valid columns, it must be a positive integer");
        this.columns = columns;
    }

    private static boolean isValidRows(int rows) {
        return rows > 0;
    }

    private static boolean isValidColumns(int columns) {
        return columns > 0;
    }
}
