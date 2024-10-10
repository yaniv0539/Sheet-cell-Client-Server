package dto;

import sheet.range.boundaries.api.Boundaries;

public class BoundariesDto {
    public CoordinateDto from;
    public CoordinateDto to;

    public BoundariesDto(Boundaries boundaries) {
        this.from = new CoordinateDto(boundaries.getFrom());
        this.to = new CoordinateDto(boundaries.getTo());
    }
}
