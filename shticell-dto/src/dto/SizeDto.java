package dto;

import sheet.layout.size.api.SizeGetters;

public class SizeDto {
    public int width;
    public int height;

    public SizeDto(SizeGetters size) {
        this.width = size.getWidth();
        this.height = size.getHeight();
    }
}
