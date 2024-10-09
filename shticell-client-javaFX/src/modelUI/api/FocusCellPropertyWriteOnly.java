package modelUI.api;

import sheet.coordinate.api.Coordinate;

import java.util.Collection;

public interface FocusCellPropertyWriteOnly {

    void setCoordinate(String coordinate );
    void setOriginalValue(String originalValue );
    void setEffectiveValue(String effectiveValue );
    void setLastUpdateVersion(String lastUpdateVersion );
    void setDependOn(Collection<Coordinate> dependOn );
    void setInfluenceOn(Collection<Coordinate> influence );
    void clear();
}
