package engine.api;

import dto.*;

import sheet.range.boundaries.api.Boundaries;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Engine {

    // Sheet:

        // Get Functions
        SheetDto getSheetDTO(String sheetName);
        SheetDto getSheetDTO(String sheetName, int sheetVersion);

        // Post Functions
        void addNewSheet(InputStream inputStream);

        // Logical Operations:

                // Filter
                SheetDto filter(String sheetName, Boundaries boundaries, String column, List<String> values, int version);
                Map<CoordinateDto, CoordinateDto> filteredMap(String sheetName, Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues, int version);
                List<String> getColumnUniqueValuesInRange(String sheetName, int column, int startRow, int endRow, int version);

                // Sort
                SheetDto sort(String sheetName, Boundaries boundaries, List<String> column, int version);
                List<List<CoordinateDto>> sortCellsInRange(String sheetName, Boundaries boundaries, List<String> column, int version);
                List<String> getNumericColumnsInRange(String sheetName, Boundaries boundaries, int version);

        // Cells:

                // Post Functions
                void updateCell(String sheetName, String cellName, String cellValue);

        // Ranges:

                // Post Functions
                void addRange(String sheetName, String rangeName, String boundariesString);

                // Delete Functions
                void deleteRange(String sheetName, String name);

        // Boundaries:

                // Get Functions
                BoundariesDto getBoundaries(String sheetName, String boundaries);

        // Permissions:

                // Get Functions
                PermissionsDto getPermissions();
                boolean isUserHasPermission(String userName, String sheetName, String permission);

                // Post Functions

}
