package dto;

import dto.enums.PermissionType;
import sheet.api.Sheet;

public record SheetOverviewDto(
        String sheetName, LayoutDto layout, int version, PermissionType userPerm,String owner
        ) {
    public SheetOverviewDto(Sheet sheet,PermissionType userPerm,String owner) {
        this(
                sheet.getName(),
                new LayoutDto(sheet.getLayout()),
                sheet.getVersion(),
                userPerm,
                owner
        );
    }
}
