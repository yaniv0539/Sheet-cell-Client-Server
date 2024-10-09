package dto;

import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;
import sheet.layout.api.Layout;
import sheet.range.api.Range;

import java.util.Map;
import java.util.Set;

public class SheetDto {
    public  String name;
    public  LayoutDto layout;
    public int version;
    public Map<Coordinate, CellDto> activeCells;
    public Set<RangeDto> ranges;
}
