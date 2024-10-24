package component.main.center.app.model.api;

import dto.CoordinateDto;

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
