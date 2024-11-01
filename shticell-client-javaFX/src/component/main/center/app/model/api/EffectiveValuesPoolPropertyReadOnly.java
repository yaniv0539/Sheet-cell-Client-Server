package component.main.center.app.model.api;

import javafx.beans.property.StringProperty;
import sheet.coordinate.api.Coordinate;

import java.util.Map;

public interface EffectiveValuesPoolPropertyReadOnly {

    boolean isExcite(Coordinate coordinate);
    StringProperty getEffectiveValuePropertyAt(String coordinate);
    void bindPropertyTo(Coordinate coordinate, StringProperty ToBind);
    Map<String, StringProperty> getEffectiveValuePropertyMap();
}
