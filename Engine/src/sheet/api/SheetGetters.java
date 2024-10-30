package sheet.api;

import sheet.cell.api.Cell;
import sheet.cell.api.CellGetters;
import sheet.coordinate.api.Coordinate;
import sheet.layout.api.LayoutGetters;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SheetGetters {
    String getName();
    LayoutGetters getLayout();
    int getVersion();

    Cell getCell(Coordinate coordinate);
    Map<Coordinate, Cell> getActiveCells();

    RangeGetters getRange(String rangeName);
    Set<RangeGetters> getRanges();
    List<List<CellGetters>> getCellInRange(int startRow, int endRow, int startCol, int endCol);
    List<String> getColumnUniqueValuesInRange(int column, int startRow, int endRow);

    boolean isCoordinateInBoundaries(Coordinate target);
    boolean isRangeInBoundaries(Boundaries boundaries);
    boolean isColumnNumericInRange(int column, int startRow, int endRow);

    Collection<Coordinate> rangeUses(RangeGetters range);
}
