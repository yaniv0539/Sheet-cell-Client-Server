package component.main.center.app.model.impl;

import component.main.center.app.model.api.FocusCellProperty;
import dto.CoordinateDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.HashSet;

public class FocusCellPropertyImpl implements FocusCellProperty {

    public StringProperty coordinate;
    public StringProperty originalValue;
    public StringProperty effectiveValue;
    public StringProperty lastUpdateVersion;
    public StringProperty lastUpdateBy;
    private ObservableList<CoordinateDto> dependenceOn;
    private ObservableList<CoordinateDto> influenceOn;

    public FocusCellPropertyImpl() {
        coordinate = new SimpleStringProperty("");
        originalValue = new SimpleStringProperty("");
        effectiveValue = new SimpleStringProperty("");
        lastUpdateVersion = new SimpleStringProperty("");
        lastUpdateBy = new SimpleStringProperty("");
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
    public StringProperty getUpdateBy() {
        return this.lastUpdateBy;
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
    public void setUpdateBy(String userName) {
        this.lastUpdateBy.set(userName);
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
