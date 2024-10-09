package modelUI.api;

import sheet.coordinate.api.Coordinate;

public interface EffectiveValuesPoolPropertyWriteOnly {

    boolean setEffectiveValuePropertyAt(Coordinate coordinate, String value);
    void addEffectiveValuePropertyAt(Coordinate coordinate, String value);
    void clear();
}
