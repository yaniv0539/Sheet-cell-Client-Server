package modelUI.api;

import javafx.beans.property.StringProperty;
import sheet.coordinate.api.Coordinate;

public interface EffectiveValuesPoolPropertyReadOnly {

    boolean isExcite(Coordinate coordinate);
    StringProperty getEffectiveValuePropertyAt(Coordinate coordinate);
    void bindPropertyTo(Coordinate coordinate, StringProperty ToBind);
}
