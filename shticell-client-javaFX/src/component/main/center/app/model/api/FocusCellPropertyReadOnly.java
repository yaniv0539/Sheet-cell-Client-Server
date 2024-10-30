package component.main.center.app.model.api;

import dto.CoordinateDto;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public interface FocusCellPropertyReadOnly {
    StringProperty getCoordinate();
    StringProperty getOriginalValue();
    StringProperty getEffectiveValue();
    StringProperty getUpdateBy();
    StringProperty getLastUpdateVersion();
    ObservableList<CoordinateDto> getDependOn();
    ObservableList<CoordinateDto> getInfluenceOn();
}
