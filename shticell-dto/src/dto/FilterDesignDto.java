package dto;

import java.util.Map;

public record FilterDesignDto(
        SheetDto filteredSheet,
        Map<CoordinateDto, CoordinateDto> coordinateBeforeAndAfterFiltering,
        BoundariesDto filteredArea
) {}
