package dto;

import sheet.layout.api.LayoutGetters;

public record LayoutDto(
        SizeDto size,
        int rows,
        int columns
) {
    public LayoutDto(LayoutGetters layout) {
        this(
                new SizeDto(layout.getSize()),
                layout.getRows(),
                layout.getColumns()
        );
    }
}
