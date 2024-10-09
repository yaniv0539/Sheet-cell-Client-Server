package modelUI.api;

import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sheet.coordinate.api.Coordinate;

import java.util.Collection;

public interface FocusCellPropertyReadOnly {
    StringProperty getCoordinate();
    StringProperty getOriginalValue();
    StringProperty getEffectiveValue();
    StringProperty getLastUpdateVersion();
    ObservableList<Coordinate> getDependOn();
    ObservableList<Coordinate> getInfluenceOn();
}
