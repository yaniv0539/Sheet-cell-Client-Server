package modelUI.impl;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import modelUI.api.EffectiveValuesPoolProperty;
import sheet.coordinate.api.Coordinate;

import java.util.HashMap;
import java.util.Map;

    public class EffectiveValuesPoolPropertyImpl implements EffectiveValuesPoolProperty {

    Map<Coordinate, StringProperty> effectiveValuesMap;

    public EffectiveValuesPoolPropertyImpl() {
        effectiveValuesMap = new HashMap<>();
    }

    @Override
    public StringProperty getEffectiveValuePropertyAt(Coordinate coordinate) {
        return effectiveValuesMap.get(coordinate);
    }

    @Override
    public boolean setEffectiveValuePropertyAt(Coordinate coordinate, String value) {
        if (effectiveValuesMap.containsKey(coordinate)) {
            effectiveValuesMap.get(coordinate).set(value);
            return true;
        }

        return false;
    }

    @Override
    public void addEffectiveValuePropertyAt(Coordinate coordinate, String value) {
        if(effectiveValuesMap.containsKey(coordinate)) {
            setEffectiveValuePropertyAt(coordinate, value);
            return;
        }

        effectiveValuesMap.put(coordinate, new SimpleStringProperty(value));
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
