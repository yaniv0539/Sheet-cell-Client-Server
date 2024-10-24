package dto;

import sheet.cell.api.CellGetters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record CellDto(
        CoordinateDto coordinate,
        int version,
        String originalValue,
        String effectiveValue,
        Set<CellDto> influenceOn,
        Set<CellDto> influenceFrom
) {
    public CellDto(CellGetters cell) {
        this(
                new CoordinateDto(cell.getCoordinate()),
                cell.getVersion(),
                cell.getOriginalValue(),
                cell.getEffectiveValue().toString(),
                new HashSet<>(),
                createInfluenceFrom(cell)
        );
    }

    private static Set<CellDto> createInfluenceFrom(CellGetters cell) {
        Set<CellDto> influenceFrom = new HashSet<>();
        cell.getInfluenceFrom().forEach(cell1 -> influenceFrom.add(new CellDto(cell1)));
        return Collections.unmodifiableSet(influenceFrom);  // Ensure immutability
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellDto cellDto = (CellDto) o;
        return Objects.equals(coordinate, cellDto.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(coordinate);
    }

    @Override
    public String toString() {
        return "CellDto{" +
                "coordinate=" + coordinate +
                ", version=" + version +
                ", originalValue='" + originalValue + '\'' +
                ", effectiveValue='" + effectiveValue + '\'' +
                ", influenceOn.size=" + (influenceOn != null ? influenceOn.size() : 0) +  // Avoid deep printing
                ", influenceFrom.size=" + (influenceFrom != null ? influenceFrom.size() : 0) +  // Avoid deep printing
                '}';
    }

}
