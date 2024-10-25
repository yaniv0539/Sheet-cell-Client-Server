package dto;

import sheet.range.boundaries.api.Boundaries;

public record BoundariesDto(
        CoordinateDto from,
        CoordinateDto to
) {
    public BoundariesDto(Boundaries boundaries) {
        this(
                new CoordinateDto(boundaries.getFrom()),
                new CoordinateDto(boundaries.getTo())
        );
    }
}
