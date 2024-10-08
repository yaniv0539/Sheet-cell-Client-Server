package sheet.cell.api;

import java.util.Set;

public interface CellSetters {
    void computeEffectiveValue();
    void setOriginalValue(String originalValue);
    void setInfluenceOn(Set<Cell> influenceOn);
    void setInfluenceFrom(Set<Cell> influenceFrom);
    void setVersion(int changeInVersion);
    void addInfluenceOn(Cell influenceOn);
    void addInfluenceFrom(Cell influenceFrom);
}
