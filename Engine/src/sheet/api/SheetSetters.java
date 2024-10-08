package sheet.api;

import sheet.coordinate.api.Coordinate;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;

import java.util.Map;

public interface SheetSetters {
    void setCell(Coordinate coordinate, String value);
    void setCells(Map<Coordinate, String> originalValues);
    void setVersion(int version);
    boolean addRange(String name, Boundaries boundaries);
    void deleteRange(RangeGetters range);

    void addRangeForXml(String rangeName, Boundaries boundaries);
}
