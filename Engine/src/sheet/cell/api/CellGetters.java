package sheet.cell.api;

import expression.api.Data;
import sheet.coordinate.api.Coordinate;

import java.util.Set;

public interface CellGetters {
    Coordinate getCoordinate();
    int getVersion();
    String getOriginalValue();
    Data getEffectiveValue();
    Set<Cell> getInfluenceFrom();
    Set<Cell> getInfluenceOn();
}
