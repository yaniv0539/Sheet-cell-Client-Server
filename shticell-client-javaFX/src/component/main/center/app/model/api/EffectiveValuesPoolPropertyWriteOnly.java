package component.main.center.app.model.api;

public interface EffectiveValuesPoolPropertyWriteOnly {

    boolean setEffectiveValuePropertyAt(String coordinate, String value);
    void addEffectiveValuePropertyAt(String coordinate, String value);
    void clear();
}
