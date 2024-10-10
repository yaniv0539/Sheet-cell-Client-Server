package dto;

import expression.impl.DataImpl;
import sheet.cell.api.CellGetters;

import java.util.HashSet;
import java.util.Set;

public class CellDto {
    public CoordinateDto coordinate;
    public int version;
    public String originalValue;
    public String effectiveValue;
    public Set<CellDto> influenceFrom;
//    public Set<CellDto> influenceOn;


    public CellDto(CellGetters cell) {
        this.coordinate = new CoordinateDto(cell.getCoordinate());
        this.version = cell.getVersion();
        this.originalValue = cell.getOriginalValue();
        this.effectiveValue = cell.getEffectiveValue().toString();
        this.influenceFrom = new HashSet<>();
//        this.influenceOn = new HashSet<>();

        cell.getInfluenceFrom().forEach(cell1 -> influenceFrom.add(new CellDto(cell1)));

//        cell.getInfluenceOn().forEach(cell1 -> influenceOn.add(new CellDto(cell1)));
    }

//    public void setInfluenceOn() {
//        influenceFrom.forEach(cell -> cell.influenceOn.add(this));
//    }
}
