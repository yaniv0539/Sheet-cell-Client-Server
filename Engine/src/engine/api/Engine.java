package engine.api;

import dto.*;

import dto.enums.PermissionType;
import sheet.range.boundaries.api.Boundaries;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {

    // Sheet:

        // Get Functions
        SheetDto getSheetDTO(String userName, String sheetName);
        SheetDto getSheetDTO(String userName, String sheetName, int sheetVersion);

        // Post Functions
        String addNewSheet(String userName, InputStream inputStream);

        // Logical Operations:

                // Filter
                SheetDto filter(String userName, String sheetName, Boundaries boundaries, String column, List<String> values, int version);
                Map<CoordinateDto, CoordinateDto> filteredMap(String userName, String sheetName, Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues, int version);
                List<String> getColumnUniqueValuesInRange(String userName, String sheetName, int column, int startRow, int endRow, int version);

                // Sort
                SheetDto sort(String userName, String sheetName, Boundaries boundaries, List<String> column, int version);
                List<List<CoordinateDto>> sortCellsInRange(String userName, String sheetName, Boundaries boundaries, List<String> column, int version);
                List<String> getNumericColumnsInRange(String userName, String sheetName, Boundaries boundaries, int version);

        // Permissions:

                // Get Functions
                PermissionsDto getSheetPermissions(String sheetName);
                PermissionType getUserPermission(String userName, String sheetName);

// Cells:

        // Post Functions
        void updateCell(String userName, String sheetName, String cellName, String cellValue);

// Ranges:

        // Post Functions
        void addRange(String userName, String sheetName, String rangeName, String boundariesString);

        // Delete Functions
        void deleteRange(String userName, String sheetName, String name);

// Boundaries:

        // Get Functions
        BoundariesDto getBoundaries(String userName, String sheetName, String boundaries);

        // Post Functions

// Users:

        List<String> getUsers();
        void addUser(String userName);

        Set<SheetOverviewDto> getSheetOverviewDto(String userName);
}
