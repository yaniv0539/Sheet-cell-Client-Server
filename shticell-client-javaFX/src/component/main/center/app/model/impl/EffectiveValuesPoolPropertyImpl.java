package component.main.center.app.model.impl;

import component.main.center.app.model.api.EffectiveValuesPoolProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sheet.coordinate.api.Coordinate;

import java.util.HashMap;
import java.util.Map;

    public class EffectiveValuesPoolPropertyImpl implements EffectiveValuesPoolProperty {

    Map<String, StringProperty> effectiveValuesMap;

    public EffectiveValuesPoolPropertyImpl() {
        effectiveValuesMap = new HashMap<>();
    }

    @Override
    public StringProperty getEffectiveValuePropertyAt(String coordinateString) {
        return effectiveValuesMap.get(coordinateString);
    }

    @Override
    public boolean setEffectiveValuePropertyAt(String coordinateString, String value) {
        if (effectiveValuesMap.containsKey(coordinateString)) {
            effectiveValuesMap.get(coordinateString).set(value);
            return true;
        }

        return false;
    }

    @Override
    public void addEffectiveValuePropertyAt(String coordinateString, String value) {
        if(effectiveValuesMap.containsKey(coordinateString)) {
            setEffectiveValuePropertyAt(coordinateString, value);
            return;
        }

        effectiveValuesMap.put(coordinateString, new SimpleStringProperty(value));
    }

    @Override
    public void bindPropertyTo(Coordinate coordinate, StringProperty ToBind) {
        effectiveValuesMap.get(coordinate).bind(ToBind);
    }

    @Override
    public boolean isExcite(Coordinate coordinate) {
        return effectiveValuesMap.containsKey(coordinate);
    }

    @Override
    public void clear() {
        effectiveValuesMap.clear();
    }
}
