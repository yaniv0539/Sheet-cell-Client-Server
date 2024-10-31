package engine.api;

import dto.*;

import dto.enums.PermissionType;
import dto.enums.Status;
import engine.versions.api.VersionManager;
import sheet.api.Sheet;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.range.boundaries.api.Boundaries;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {

// Sheet:

        // Get Methods
        Set<SheetOverviewDto> getSheetOverviewDto(String userName);
        SheetDto getSheetDTO(String userName, String sheetName);
        SheetDto getSheetDTO(String userName, String sheetName, int sheetVersion);

        // Post Methods
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

                // Get Methods
                PermissionsDto getSheetPermissions(String sheetName);
                PermissionType getUserPermission(String userName, String sheetName);

                // Post Methods
                void addRequestPermission(String sheetName, String userName, PermissionType permissionType);
                void setResponseToRequest(String sheetName, String userName, PermissionType permissionType, Status status,Status response);

        // Dynamic Copies:

                // Post Methods
                SheetDto updateDynamicSheetCell(String userName, String sheetName, int sheetVersion, String cellName, String cellValue);

// Cells:

        // Post Methods
        void updateCell(String userName, String sheetName, int sheetVersion, String cellName, String cellValue);

// Ranges:

        // Post Methods
        void addRange(String userName, String sheetName, int sheetVersion, String rangeName, String boundariesString);

        // Delete Methods
        void deleteRange(String userName, String sheetName, int sheetVersion, String name);

// Boundaries:

        // Get Methods
        BoundariesDto getBoundaries(String userName, String sheetName, String boundaries);

// Users:

        // Get Methods
        List<String> getUsers();

        // Post Methods
        void addUser(String userName);

        // Delete Methods
        void deleteUser(String userName);
}
