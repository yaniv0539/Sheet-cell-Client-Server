package dto;

import java.util.List;

public class SortDto {
    BoundariesDto boundariesDto;
    List<String> sortByColumns;

    public SortDto() {
    }

    public SortDto(BoundariesDto boundariesDto, List<String> sortByColumns) {
        this.boundariesDto = boundariesDto;
        this.sortByColumns = sortByColumns;
    }

    public BoundariesDto getBoundariesDto() {
        return boundariesDto;
    }

    public List<String> getSortByColumns() {
        return sortByColumns;
    }
}
