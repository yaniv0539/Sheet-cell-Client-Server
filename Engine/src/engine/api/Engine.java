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

    // Version Manager

        // Get Functions
        VersionManagerGetters getVersionsManager();
        VersionManagerDto getVersionManager(String sheetName);

    // Sheet

        // XML Files

            // Read Functions

            void readXMLInitFile(String filename);
            void readXMLInitFile(InputStream inputStream);

        // Get Functions

            // SheetGetters
            SheetGetters getSheet();

            // SheetDto
            SheetDto getSheetDTO();
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

        // Get Functions

            // CellGetters
            CellGetters getCell(SheetGetters sheet, String cellName);
            CellGetters getCell(String cellName);
            CellGetters getCell(int row, int col);
            CellGetters getCell(SheetGetters sheet, int row, int col);

            // CellDto
//            CellDto getCellDto(SheetDto sheet, String cellName);
//            CellDto getCellDto(String cellName);
//            CellDto getCellDto(int row, int col);
//            CellDto getCellDto(SheetDto sheet, int row, int col);

        // Update Functions
//        void updateCell(String cellName, String value);
        void updateCell(String sheetName, String cellName, String cellValue);

    // Ranges:

        // Get Functions

            // RangeGetters
            Set<RangeGetters> getRanges(String sheetName);
            RangeGetters getRange(String name);

            // RangeDto
            RangeDto getRangeDTO(String name);

        // Post Functions
        boolean addRange(String sheetName, String rangeName, String boundariesString);

        // Delete Functions
        void deleteRange(String name);


}
