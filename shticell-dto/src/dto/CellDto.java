package dto;

import expression.api.Data;
import expression.api.Expression;
import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;

import java.util.Set;

public class CellDto {
    public  Coordinate coordinate;
    public int version;
    public String originalValue;
    public Data effectiveValue;
    public Set<CellDto> influenceFrom;
    public Set<CellDto> influenceOn;
}
