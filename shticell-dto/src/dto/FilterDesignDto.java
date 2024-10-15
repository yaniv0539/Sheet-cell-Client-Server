package dto;

import java.util.Map;

public class FilterDesignDto {
    SheetDto filteredSheet;
    Map<CoordinateDto, CoordinateDto> coordinateBeforeAndAfterFiltering;
    BoundariesDto filteredArea;

    public FilterDesignDto() {
    }

    public FilterDesignDto(SheetDto sheet, Map<CoordinateDto, CoordinateDto> filterMap, BoundariesDto boundariesFilter) {
        this.filteredSheet = sheet;
        this.coordinateBeforeAndAfterFiltering = filterMap;
        this.filteredArea = boundariesFilter;
    }

    public SheetDto getSheet() {
        return filteredSheet;
    }

    public Map<CoordinateDto, CoordinateDto> getFilterMap() {
        return coordinateBeforeAndAfterFiltering;
    }

    public BoundariesDto getBoundariesFilter() {
        return filteredArea;
    }
}
