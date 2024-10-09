package dto;

import sheet.range.api.RangeGetters;
import sheet.range.boundaries.impl.BoundariesImpl;

public class RangeDto {
    public String name;
    public BoundariesImpl bounds;

    public RangeDto(RangeGetters range) {
        this.name = range.getName();
        this.bounds = (BoundariesImpl) range.getBoundaries();
    }
}
