package dto;

import sheet.range.api.RangeGetters;

public class RangeDto {
    public String name;
    public BoundariesDto boundaries;

    public RangeDto(RangeGetters range) {
        this.name = range.getName();
        this.boundaries = new BoundariesDto(range.getBoundaries());
    }

    public String getName() {
        return name;
    }

    public BoundariesDto getBoundaries() {
        return boundaries;
    }
}
