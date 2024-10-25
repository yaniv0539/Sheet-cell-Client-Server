package dto;

import sheet.api.SheetGetters;

import java.util.*;

public record SheetDto(
        String name,
        LayoutDto layout,
        int version,
        Map<String, CellDto> activeCells,
        Set<RangeDto> ranges
) {

    public SheetDto(SheetGetters sheet) {
        this(
                sheet.getName(),
                new LayoutDto(sheet.getLayout()),
                sheet.getVersion(),
                createActiveCells(sheet),
                createRanges(sheet)
        );

        // TODO: Ask Itay how he thinks we can fix it.
//        activeCells().values().forEach(CellDto::setInfluenceOn);
    }

    private static Map<String, CellDto> createActiveCells(SheetGetters sheet) {
        Map<String, CellDto> activeCells = new HashMap<>();
        sheet.getActiveCells().forEach((coordinate, cell) -> activeCells.put(coordinate.toString(), new CellDto(cell)));
        return Collections.unmodifiableMap(activeCells);  // Ensure immutability
    }

    private static Set<RangeDto> createRanges(SheetGetters sheet) {
        Set<RangeDto> ranges = new HashSet<>();
        sheet.getRanges().forEach(range -> ranges.add(new RangeDto(range)));
        return Collections.unmodifiableSet(ranges);  // Ensure immutability
    }
}
