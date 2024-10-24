package dto;

import sheet.api.Sheet;

public record SheetOverviewDto(
        String sheetName, LayoutDto layout, int version
) {
    public SheetOverviewDto(Sheet sheet) {
        this(
                sheet.getName(),
                new LayoutDto(sheet.getLayout()),
                sheet.getVersion()
        );
    }
}
