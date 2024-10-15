package dto;

import java.util.Map;

public class FilterDesignDto {
    SheetDto sheet;
    Map<CoordinateDto, CoordinateDto> filterMap;
    BoundariesDto boundariesFilter;

    public FilterDesignDto() {
    }

    public FilterDesignDto(SheetDto sheet, Map<CoordinateDto, CoordinateDto> filterMap, BoundariesDto boundariesFilter) {
        this.sheet = sheet;
        this.filterMap = filterMap;
        this.boundariesFilter = boundariesFilter;
    }

    public SheetDto getSheet() {
        return sheet;
    }

    public Map<CoordinateDto, CoordinateDto> getFilterMap() {
        return filterMap;
    }

    public BoundariesDto getBoundariesFilter() {
        return boundariesFilter;
    }
}
