package engine.api;

import engine.version.manager.api.VersionManagerGetters;
import sheet.api.SheetGetters;
import sheet.cell.api.CellGetters;
import sheet.coordinate.api.Coordinate;
import sheet.range.api.Range;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {

    void readXMLInitFile(String filename);
    SheetGetters getSheetStatus();
    CellGetters getCellStatus(SheetGetters sheet, String cellName);
    CellGetters getCellStatus(String cellName);
    CellGetters getCellStatus(int row, int col);
    CellGetters getCellStatus(SheetGetters sheet, int row, int col);
    void updateCellStatus(String cellName, String value);
    VersionManagerGetters getVersionsManagerStatus();
    boolean addRange(String from, String to);
    RangeGetters getRange(String name);
    Set<RangeGetters> getRanges();
    void deleteRange(String name);
    javafx.concurrent.Task<Boolean> loadFileTask(String path);
    SheetGetters filter(Boundaries boundaries, String column, List<String> values,int version);
    SheetGetters sortSheet(Boundaries boundaries, List<String> column, int version);
    List<List<CellGetters>> sortCellsInRange(Boundaries boundaries, List<String> column, int version);
    void exit();

    Map<Coordinate, Coordinate> filteredMap(Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues, int version);

}
