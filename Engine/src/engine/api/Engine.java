package engine.api;

import dto.*;

import sheet.range.boundaries.api.Boundaries;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Engine {

    // XML Files

        // Read Functions
        void readXMLInitFile(String filename);
        void readXMLInitFile(InputStream inputStream);

    // Sheet

        // Get Functions
        SheetDto getSheetDTO(String sheetName);
        SheetDto getSheetDTO(String sheetName, int sheetVersion);

        // Post Functions
        void addNewSheet(InputStream inputStream);

        // Logical Functions
        SheetDto filter(String sheetName, Boundaries boundaries, String column, List<String> values, int version);
        Map<CoordinateDto, CoordinateDto> filteredMap(String sheetName, Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues, int version);
        List<String> getColumnUniqueValuesInRange(String sheetName, int column, int startRow, int endRow, int version);
        SheetDto sort(String sheetName, Boundaries boundaries, List<String> column, int version);
        List<List<CellDto>> sortCellsInRange(String sheetName, Boundaries boundaries, List<String> column, int version);

    // Cells:

        // Update Functions
        void updateCell(String sheetName, String cellName, String cellValue);

    // Ranges:

        // Get Functions
        RangeDto getRangeDTO(String name);

        // Post Functions
        boolean addRange(String sheetName, String rangeName, String boundariesString);

        // Delete Functions
        void deleteRange(String sheetName, String name);

        // Boundaries:
        BoundariesDto getBoundaries(String sheetName, String boundaries);
}
