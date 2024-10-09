package modelUI.impl;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import modelUI.api.FocusCellProperty;
import sheet.coordinate.api.Coordinate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FocusCellPropertyImpl implements FocusCellProperty {

    public StringProperty coordinate;
    public StringProperty originalValue;
    public StringProperty effectiveValue;
    public StringProperty lastUpdateVersion;
    private ObservableList<Coordinate> dependenceOn;
    private ObservableList<Coordinate> influenceOn;

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
    public ObservableList<Coordinate> getDependOn() {
        return dependenceOn;
    }

    @Override
    public ObservableList<Coordinate> getInfluenceOn() {
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
    public void setDependOn(Collection<Coordinate> dependOn) {
        this.dependenceOn.clear();
        this.dependenceOn.addAll(dependOn);
    }
    @Override
    public void setInfluenceOn(Collection<Coordinate> influenceOn) {
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
