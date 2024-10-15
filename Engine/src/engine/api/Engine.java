package engine.api;

import dto.RangeDto;
import dto.SheetDto;
import dto.VersionManagerDto;
import engine.version.manager.api.VersionManagerGetters;
import sheet.api.SheetGetters;
import sheet.cell.api.CellGetters;
import sheet.coordinate.api.Coordinate;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        SheetGetters filter(Boundaries boundaries, String column, List<String> values, int version);
        SheetGetters sort(Boundaries boundaries, List<String> column, int version);
        Map<Coordinate, Coordinate> filteredMap(Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues, int version);
        List<List<CellGetters>> sortCellsInRange(Boundaries boundaries, List<String> column, int version);

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
}
