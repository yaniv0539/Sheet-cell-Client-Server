package dto;

import java.util.List;

public record SortDesignDto(
        SheetDto sheetDto,
        BoundariesDto boundariesDto,
        List<List<CoordinateDto>> coordinateDtos
) {}
