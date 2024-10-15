package dto;

import java.util.List;

public class FilterDto {
    private BoundariesDto boundariesDto;
    private String filterByColumn;
    private List<String>  byValues;

    public FilterDto() {}

    public FilterDto(BoundariesDto boundariesDto, String filterByColumn, List<String> byValues) {
        this.boundariesDto = boundariesDto;
        this.filterByColumn = filterByColumn;
        this.byValues = byValues;
    }

    public BoundariesDto getBoundariesDto() {
        return boundariesDto;
    }

    public String getFilterByColumn() {
        return filterByColumn;
    }

    public List<String> getByValues() {
        return byValues;
    }
}
