package dto;

import expression.api.Data;
import expression.impl.DataImpl;
import sheet.cell.api.CellGetters;
import sheet.coordinate.api.Coordinate;

import java.util.HashSet;
import java.util.Set;

public class CellDto {
    public  Coordinate coordinate;
    public int version;
    public String originalValue;
    public DataImpl effectiveValue;
    public Set<CellDto> influenceFrom;
    public Set<CellDto> influenceOn;

    public CellDto(CellGetters cell) {
        this.coordinate = cell.getCoordinate();
        this.version = cell.getVersion();
        this.originalValue = cell.getOriginalValue();
        this.effectiveValue = (DataImpl) cell.getEffectiveValue();
        this.influenceFrom = new HashSet<>();
        this.influenceOn = new HashSet<>();

        cell.getInfluenceFrom().forEach(cell1 -> influenceFrom.add(new CellDto(cell1)));

        //cell.getInfluenceOn().forEach(cell1 -> influenceOn.add(new CellDto(cell1)));
    }

    public void setInfluenceOn() {
        influenceFrom.forEach(cell -> cell.influenceOn.add(this));
    }
}
