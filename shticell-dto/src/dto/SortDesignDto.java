package dto;

import java.util.List;

public class SortDesignDto {
    BoundariesDto boundariesDto;
    SheetDto sheetDto;
    List<List<CoordinateDto>> coordinateDtos;

    public SortDesignDto() {
    }

    public SortDesignDto(SheetDto sheetDto, BoundariesDto boundariesDto, List<List<CoordinateDto>> coordinateDtos) {
        this.sheetDto = sheetDto;
        this.boundariesDto = boundariesDto;
        this.coordinateDtos = coordinateDtos;
    }

    public BoundariesDto getBoundariesDto() {
        return boundariesDto;
    }

    public List<List<CoordinateDto>> getCoordinateDtos() {
        return coordinateDtos;
    }

    public SheetDto getSheetDto() {
        return sheetDto;
    }
}
