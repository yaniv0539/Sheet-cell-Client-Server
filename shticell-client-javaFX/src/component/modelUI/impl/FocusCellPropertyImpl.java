package component.modelUI.impl;

import component.modelUI.api.FocusCellProperty;
import dto.CoordinateDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sheet.coordinate.api.Coordinate;

import java.util.Collection;
import java.util.HashSet;

public class FocusCellPropertyImpl implements FocusCellProperty {

    public StringProperty coordinate;
    public StringProperty originalValue;
    public StringProperty effectiveValue;
    public StringProperty lastUpdateVersion;
    private ObservableList<CoordinateDto> dependenceOn;
    private ObservableList<CoordinateDto> influenceOn;

    public FocusCellPropertyImpl() {
        coordinate = new SimpleStringProperty("");
        originalValue = new SimpleStringProperty("");
        effectiveValue = new SimpleStringProperty("");
        lastUpdateVersion = new SimpleStringProperty("");
        dependenceOn = FXCollections.observableArrayList();
        influenceOn = FXCollections.observableArrayList();
    }

    @Override
    public StringProperty getCoordinate() {
        return coordinate;
    }

    @Override
    public StringProperty getOriginalValue() {
        return originalValue;
    }

    @Override
    public StringProperty getEffectiveValue() {
        return effectiveValue;
    }

    @Override
    public StringProperty getLastUpdateVersion() {
        return lastUpdateVersion;
    }

    @Override
    public ObservableList<CoordinateDto> getDependOn() {
        return dependenceOn;
    }

    @Override
    public ObservableList<CoordinateDto> getInfluenceOn() {
        return influenceOn;
    }

    @Override
    public void setCoordinate(String coordinate) {
        this.coordinate.set(coordinate);
    }

    @Override
    public void setOriginalValue(String originalValue) {
        this.originalValue.set(originalValue);
    }

    @Override
    public void setEffectiveValue(String effectiveValue) {
        this.effectiveValue.set(effectiveValue);
    }

    public void setLastUpdateVersion(String lastUpdateVersion) {
        this.lastUpdateVersion.set(lastUpdateVersion);
    }

    @Override
    public void setDependOn(Collection<CoordinateDto> dependOn) {
        this.dependenceOn.clear();
        this.dependenceOn.addAll(dependOn);
    }
    @Override
    public void setInfluenceOn(Collection<CoordinateDto> influenceOn) {
        this.influenceOn.clear();
        this.influenceOn.addAll(influenceOn);

    }

    @Override
    public void clear() {
        coordinate.set("");
        originalValue.set("");
        effectiveValue.set("");
        lastUpdateVersion.set("");
        setDependOn(new HashSet<>());
        setInfluenceOn(new HashSet<>());
    }
}
