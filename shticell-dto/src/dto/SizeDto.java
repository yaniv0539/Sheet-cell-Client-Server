package dto;

import sheet.layout.size.api.SizeGetters;

public record SizeDto(
        int width,
        int height
) {
    public SizeDto(SizeGetters size) {
        this(
                size.getWidth(),
                size.getHeight()
        );
    }
}
