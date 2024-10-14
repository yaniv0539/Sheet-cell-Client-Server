package component.modelUI.api;

import dto.CoordinateDto;
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
    ObservableList<CoordinateDto> getDependOn();
    ObservableList<CoordinateDto> getInfluenceOn();
}
