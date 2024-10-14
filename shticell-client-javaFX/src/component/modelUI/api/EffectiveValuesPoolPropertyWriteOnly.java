package component.modelUI.api;

import sheet.coordinate.api.Coordinate;

public interface EffectiveValuesPoolPropertyWriteOnly {

    boolean setEffectiveValuePropertyAt(String coordinate, String value);
    void addEffectiveValuePropertyAt(String coordinate, String value);
    void clear();
}
