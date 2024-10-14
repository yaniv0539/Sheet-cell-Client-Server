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
    public Set<CellDto> influenceOn;

    public CellDto(CellGetters cell) {
        this.coordinate = new CoordinateDto(cell.getCoordinate());
        this.version = cell.getVersion();
        this.originalValue = cell.getOriginalValue();
        this.effectiveValue = cell.getEffectiveValue().toString();
        this.influenceFrom = new HashSet<>();
        this.influenceOn = new HashSet<>();

        cell.getInfluenceFrom().forEach(cell1 -> influenceFrom.add(new CellDto(cell1)));
    }

    public CellDto() {
    }

    public void setInfluenceOn() {
        for (CellDto cell : influenceFrom) {
            cell.influenceOn.add(this);
        }
    }

    public String getEffectiveValue() {
        return effectiveValue;
    }

    public CoordinateDto getCoordinate() {
        return coordinate;
    }

    public int getVersion() {
        return version;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public Set<CellDto> getInfluenceFrom() {
        return influenceFrom;
    }

    public Set<CellDto> getInfluenceOn() {
        return influenceOn;
    }
}
