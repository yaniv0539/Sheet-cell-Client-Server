package dto;

import sheet.range.api.RangeGetters;

public record RangeDto(
        String name,
        BoundariesDto boundaries
) {
    public RangeDto(RangeGetters range) {
        this(
                range.getName(),
                new BoundariesDto(range.getBoundaries())
        );
    }

    public String getName() {
        return name;  // Explicit getter for PropertyValueFactory compatibility
    }
}
