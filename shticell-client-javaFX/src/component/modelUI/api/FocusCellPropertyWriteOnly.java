package component.modelUI.api;

import dto.CoordinateDto;
import sheet.coordinate.api.Coordinate;

import java.util.Collection;

public interface FocusCellPropertyWriteOnly {

    void setCoordinate(String coordinate );
    void setOriginalValue(String originalValue );
    void setEffectiveValue(String effectiveValue );
    void setLastUpdateVersion(String lastUpdateVersion );
    void setDependOn(Collection<CoordinateDto> dependOn );
    void setInfluenceOn(Collection<CoordinateDto> influence );
    void clear();
}
