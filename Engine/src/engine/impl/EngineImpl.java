package engine.impl;

import dto.*;
import engine.api.Engine;
import engine.jaxb.parser.STLSheetToSheet;
import engine.permissions.impl.PermissionManagerImpl;
import engine.permissions.api.PermissionManager;
import engine.users.UserManager;
import engine.versions.api.VersionManager;
import engine.versions.impl.VersionManagerImpl;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import sheet.api.Sheet;

import java.io.*;
import java.util.*;

import engine.jaxb.generated.STLSheet;
import sheet.api.SheetGetters;
import sheet.cell.api.Cell;
import sheet.cell.api.CellGetters;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.impl.SheetImpl;
import sheet.layout.api.Layout;
import sheet.layout.api.LayoutGetters;
import sheet.layout.impl.LayoutImpl;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesFactory;
import sheet.range.impl.RangeImpl;

public class EngineImpl implements Engine, Serializable {

    private final static String JAXB_XML_GENERATED_PACKAGE_NAME = "engine.jaxb.generated";
    private final static int MAX_ROWS = 50;
    private final static int MAX_COLUMNS = 20;

    private final Map<String, VersionManager> versionManagers;
    private final Map<String, PermissionManager> permissionManagers;
    private final UserManager userManager;

    private EngineImpl() {
        this.versionManagers = new HashMap<>();
        this.permissionManagers = new HashMap<>();
        this.userManager = new UserManager();
    }

    public static EngineImpl create() {
        return new EngineImpl();
    }

    @Override
    public SheetDto getSheetDTO(String userName, String sheetName) {
        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        return new SheetDto(versionManager.getLastVersion());
    }

    @Override
    public SheetDto getSheetDTO(String userName, String sheetName, int sheetVersion) {
        VersionManager versionManager = this.versionManagers.get(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);
        Sheet sheet = versionManager.getVersion(sheetVersion);

        canRead(permissionManager, userName, sheetName);

        if (sheet == null) {
            throw new RuntimeException("Sheet " + sheetName + " not found");
        }

        return new SheetDto(sheet);
    }

    @Override
    public void addNewSheet(String userName, InputStream inputStream) {
        try {
            STLSheet stlSheet = deserializeFrom(inputStream);
            Sheet sheet = STLSheetToSheet.generate(stlSheet);

            if (!isValidLayout(sheet.getLayout())) {
                throw new IndexOutOfBoundsException("Layout is invalid !" + "\n" +
                        "valid scale: rows <= 50 , columns <= 20");
            }

            VersionManager versionManager = this.versionManagers.computeIfAbsent(sheet.getName(), k -> VersionManagerImpl.create());
            versionManager.init(sheet);

            this.permissionManagers.computeIfAbsent(sheet.getName(), k -> PermissionManagerImpl.create(userName));

        } catch (JAXBException e) {
            throw new RuntimeException("Failed to read XML file", e);
        }
    }

    @Override
    public void updateCell(String userName, String sheetName, String cellName, String cellValue) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canWrite(permissionManager, userName, sheetName);

        versionManager.makeNewVersion();

        try {
            versionManager.getLastVersion().setCell(CoordinateFactory.toCoordinate(cellName.toUpperCase()), cellValue);
        } catch (Exception e) {
            versionManager.deleteLastVersion();
            throw e;
        }
    }

    @Override
    public SheetDto filter(String userName, String sheetName, Boundaries boundaries, String column, List<String> values, int version) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        Coordinate to = boundaries.getTo();
        Coordinate from = boundaries.getFrom();

        Sheet sheetToFilter = versionManager.getVersion(version);

        Sheet newSheet = SheetImpl.create(copyLayout(sheetToFilter.getLayout())); //here need to bring the version we now look at.

        int columnInt = CoordinateFactory.parseColumnToInt(column) - 1;

        Map<Integer, Integer> oldRowToNewRow = new HashMap<>();

        int liftUpCellsCounter = 0;
        int liftDownCellsCounter = 0;

        for (int i = from.getRow(); i <= to.getRow(); i++) {
            Cell cell = sheetToFilter.getCell(CoordinateFactory.createCoordinate(i, columnInt));
            String effectiveValueStr;
            if (cell == null) {
                effectiveValueStr = "";
            } else {
                effectiveValueStr = cell.getEffectiveValue().toString();
            }

            if (values.contains(effectiveValueStr)) {
                oldRowToNewRow.put(i, from.getRow() + liftDownCellsCounter);
                liftDownCellsCounter++;
            } else {
                liftUpCellsCounter++;
            }
        }


        // Itay's filter version for exercise demands.
        sheetToFilter
                .getActiveCells()
                .keySet()
                .forEach(oldCoordinate -> {
                    if(oldCoordinate.getRow() < from.getRow() || oldCoordinate.getRow() > to.getRow() ||
                            oldCoordinate.getCol() < from.getCol() || oldCoordinate.getCol() > to.getCol()){
                        newSheet.setCell(oldCoordinate, sheetToFilter.getCell(oldCoordinate).getEffectiveValue().toString());
                    }
                    else{
                        if(oldRowToNewRow.containsKey(oldCoordinate.getRow())){
                            Coordinate newCoordinate =
                                    CoordinateFactory.createCoordinate(
                                            oldRowToNewRow.get(oldCoordinate.getRow()),
                                            oldCoordinate.getCol());
                            newSheet.setCell(newCoordinate, sheetToFilter.getCell(oldCoordinate).getEffectiveValue().toString());
                        }
                    }
                });

        // Yaniv's version works like Gsheet.
//        this.sheet
//                .getActiveCells()
//                .keySet()
//                .stream()
//                .filter(coordinate -> coordinate.getRow() < from.getRow() || coordinate.getRow() > to.getRow())
//                .forEach(oldCoordinate -> newSheet.setCell(oldCoordinate, sheetToFilter.getCell(oldCoordinate).getEffectiveValue().toString()));
//
//        this.sheet
//                .getActiveCells()
//                .keySet()
//                .stream()
//                .filter(coordinate -> coordinate.getRow() >= from.getRow() && coordinate.getRow() <= to.getRow())
//                .forEach(oldCoordinate -> {
//                    if (oldRowToNewRow.containsKey(oldCoordinate.getRow())) {
//                        Coordinate newCoordinate =
//                                CoordinateFactory.createCoordinate(
//                                        oldRowToNewRow.get(oldCoordinate.getRow()),
//                                        oldCoordinate.getCol());
//                        newSheet.setCell(newCoordinate, sheetToFilter.getCell(oldCoordinate).getEffectiveValue().toString());
//                    }
//                });
        //        int finalLiftUpCellsCounter = liftUpCellsCounter;
//        this.sheet
//                .getActiveCells()
//                .keySet()
//                .stream()
//                .filter(coordinate -> coordinate.getRow() > to.getRow())
//                .forEach(oldCoordinate -> {
//                    Coordinate newCoordinate =
//                            CoordinateFactory.createCoordinate(
//                                    oldCoordinate.getRow() - finalLiftUpCellsCounter,
//                                         oldCoordinate.getCol());
//                    newSheet.setCell(newCoordinate, sheetToFilter.getCell(oldCoordinate).getEffectiveValue().toString());
//                });
        //todo: until here

        return new SheetDto(newSheet);
    }

    @Override
    public SheetDto sort(String userName, String sheetName, Boundaries boundaries, List<String> columns, int version) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        Coordinate from = boundaries.getFrom();
        Coordinate to = boundaries.getTo();

        int startRow = from.getRow();
        int endRow = to.getRow();
        int startCol = from.getCol();
        int endCol = to.getCol();

        SheetGetters sheetToFilter = versionManager.getVersion(version);

        Sheet newSheet = SheetImpl.create(copyLayout(sheetToFilter.getLayout()));
        //get data in range from the sheet;
        List<List<CellGetters>> dataToSort = sheetToFilter.getCellInRange(startRow,endRow,startCol,endCol);
        List<Integer> columnsByInt = columnsToIntList(columns);
        
        //if all columns integer; else exception
        for (int col : columnsByInt) {
            boolean allNumeric = dataToSort.stream()
                    .map(row -> row.get(col - startCol).getEffectiveValue().toString())  // Get the value in the current column
                    .allMatch(this::isNumeric);  // Check if the value is numeric

            if (!allNumeric) {
                throw new IllegalArgumentException("Column " + (char)('A' + col) + " contains non-numeric values");
            }
        }
        
        //stableSort();
        dataToSort.sort(createComparator(columnsByInt, startCol));

        //put data into sheet;
        sheetToFilter
                .getActiveCells()
                .keySet().stream()
                .filter(coordinate -> coordinate.getRow() < from.getRow() || coordinate.getRow() > to.getRow() ||
                        coordinate.getCol() < from.getCol() || coordinate.getCol() > to.getCol())
                .forEach(coordinate -> { newSheet.setCell(coordinate, sheetToFilter.getCell(coordinate).getEffectiveValue().toString());

//                    else{ //it means the coordinate is in the sorted range
//                        newSheet.setCell(coordinate,
//                                dataToSort.get(coordinate.getRow() - startRow)
//                                                .get(coordinate.getCol() - startCol)
//                                                    .getEffectiveValue().toString());
//                    }
                });

        RangeImpl.create("dummy",boundaries).toCoordinateCollection().stream()
                .forEach(coordinate -> {
                    newSheet.setCell(coordinate,
                            dataToSort.get(coordinate.getRow() - startRow)
                                    .get(coordinate.getCol() - startCol)
                                    .getEffectiveValue().toString());
                });

        return new SheetDto(newSheet);
    }

    @Override
    public List<List<CoordinateDto>> sortCellsInRange(String userName, String sheetName, Boundaries boundaries, List<String> columns, int version) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        Coordinate from = boundaries.getFrom();
        Coordinate to = boundaries.getTo();

        int startRow = from.getRow();
        int endRow = to.getRow();
        int startCol = from.getCol();
        int endCol = to.getCol();

        SheetGetters sheetToFilter = versionManager.getVersion(version);
        List<List<CellGetters>> dataToSort = sheetToFilter.getCellInRange(startRow,endRow,startCol,endCol);
        List<Integer> columnsByInt = columnsToIntList(columns);
        dataToSort.sort(createComparator(columnsByInt, startCol));

        List<List<CoordinateDto>> dataToSortDto = new ArrayList<>();

        dataToSort.forEach(list -> {
            List<CoordinateDto> tempList = new ArrayList<>();
            list.forEach(cellGetters -> tempList.add(new CoordinateDto(cellGetters.getCoordinate())));
            dataToSortDto.add(tempList);
        });

        return dataToSortDto;
    }

    @Override
    public List<String> getNumericColumnsInRange(String userName, String sheetName, Boundaries boundaries, int version) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        Sheet lastVersion = versionManager.getLastVersion();

        List<String> numericColumns = new ArrayList<>();

        for (int i = boundaries.getFrom().getCol(); i <= boundaries.getTo().getCol(); i++) {
            if (lastVersion.isColumnNumericInRange(i, boundaries.getFrom().getRow(), boundaries.getTo().getRow())) {
                char character = (char) ('A' + i);
                String column = String.valueOf(character);
                numericColumns.add(column);
            }
        }

        return numericColumns;
    }

    @Override
    public Map<CoordinateDto, CoordinateDto> filteredMap(String userName, String sheetName, Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues, int version) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        Sheet sheet = versionManager.getVersion(version);

        if (sheet == null) {
            throw new IllegalArgumentException("Sheet " + sheetName + " does not have a version manager");
        }

        Coordinate to = boundariesToFilter.getTo();
        Coordinate from = boundariesToFilter.getFrom();

        int columnInt = CoordinateFactory.parseColumnToInt(filteringByColumn) - 1;

        Map<CoordinateDto, CoordinateDto> oldCoordToNewCoord = new HashMap<>();

        int liftDownCellsCounter = 0;

        for (int i = from.getRow(); i <= to.getRow(); i++) {
            Cell cell = sheet.getCell(CoordinateFactory.createCoordinate(i, columnInt));
            String effectiveValueStr;
            if (cell == null) {
                effectiveValueStr = "";
            } else {
                effectiveValueStr = cell.getEffectiveValue().toString();
            }

            if (filteringByValues.contains(effectiveValueStr)) {
                for(int col = from.getCol(); col <= to.getCol(); col++) {
                    Coordinate oldCoord = CoordinateFactory.createCoordinate(i, col);
                    Coordinate newCoord = CoordinateFactory.createCoordinate(from.getRow() +liftDownCellsCounter, col);
                    oldCoordToNewCoord.put(new CoordinateDto(oldCoord), new CoordinateDto(newCoord));
                }
                liftDownCellsCounter++;
            }
        }

        return oldCoordToNewCoord;
    }

    @Override
    public List<String> getColumnUniqueValuesInRange(String userName, String sheetName, int column, int startRow, int endRow, int version) {
        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        Sheet sheet = versionManager.getVersion(version);

        if (sheet == null) {
            throw new IllegalArgumentException("Sheet " + sheetName + " does not have a version manager");
        }

        return sheet.getColumnUniqueValuesInRange(column, startRow, endRow);
    }

    @Override
    public void addRange(String userName, String sheetName, String name, String boundariesString) {
        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canWrite(permissionManager, userName, sheetName);

        versionManager.makeNewVersion();

        try {
            Boundaries boundaries = BoundariesFactory.toBoundaries(boundariesString);
            boolean sheetChanged = versionManager.getLastVersion().addRange(name, boundaries);
            if (!sheetChanged)
            {
                versionManager.deleteLastVersion();
                versionManager.getLastVersion().addRange(name, boundaries);
            }
        } catch (Exception e) {
            versionManager.deleteLastVersion();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteRange(String userName, String sheetName, String rangeName) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canWrite(permissionManager, userName, sheetName);

        Sheet lastVersion = versionManager.getLastVersion();
        RangeGetters range = lastVersion.getRange(rangeName);
        Collection<Coordinate> coordinates = lastVersion.rangeUses(range);

        if (!coordinates.isEmpty()) {
            throw new RuntimeException("Can not delete range in use !\nCells that using range: " + coordinates.toString());
        }

        lastVersion.deleteRange(range);
    }

    @Override
    public BoundariesDto getBoundaries(String userName, String sheetName, String boundaries) {

        if (!BoundariesFactory.isValidBoundariesFormat(boundaries)) {
            throw new RuntimeException("Invalid boundaries");
        }

        Boundaries boundaries1 = BoundariesFactory.toBoundaries(boundaries);

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canRead(permissionManager, userName, sheetName);

        Sheet lastVersion = versionManager.getLastVersion();

        if(!lastVersion.isCoordinateInBoundaries(boundaries1.getFrom())) {
            throw new RuntimeException("coordinate " + boundaries1.getFrom() + "out of boundaries");
        }
        if(!lastVersion.isCoordinateInBoundaries(boundaries1.getTo())) {
            throw new RuntimeException("coordinate " + boundaries1.getTo() + "out of boundaries");
        }
        if(!lastVersion.isCoordinateInBoundaries(boundaries1.getFrom())) {
            throw new RuntimeException("coordinate " + boundaries1.getFrom() + "out of boundaries");
        }
        if(!CoordinateFactory.isGreaterThen(boundaries1.getTo(),boundaries1.getFrom())) {
            throw new RuntimeException("coordinate " + boundaries1.getFrom() + " > " + boundaries1.getTo());
        }

        return new BoundariesDto(boundaries1);
    }

    @Override
    public boolean isUserHasPermission(String userName, String sheetName, String permission) {
        return false;
    }

    @Override
    public synchronized void addUser(String userName) {
        this.userManager.addUser(userName);
    }


//    @Override
//    public PermissionsDto getPermissions() {
//        return new PermissionsDto(this.permissionManagers);

//    }


    //sort function helper
    private Comparator<List<CellGetters>> createComparator(List<Integer> sortByColumns, int startCol) {
        Comparator<List<CellGetters>> comparator = (row1, row2) -> 0;

        for (int col : sortByColumns) {
            Comparator<List<CellGetters>> columnComparator = (row1, row2) -> {

                String value1 = row1.get(col - startCol).getEffectiveValue().toString();
                String value2 = row2.get(col - startCol).getEffectiveValue().toString();

                //knowing it is double.
                return Double.compare(Double.parseDouble(value1), Double.parseDouble(value2));

                // If both values are numeric, compare them as doubles, extend to lexigrhaphic sort.
//                if (isNumeric(value1) && isNumeric(value2)) {
//                    return Double.compare(Double.parseDouble(value1), Double.parseDouble(value2));
//                } else {
//                    return value1.compareTo(value2);  // Lexicographic comparison for non-numeric
//                }
            };

            // Combine comparators in a stable way (order of columns matters)
            comparator = comparator.thenComparing(columnComparator);
        }

        return comparator;
    }

    private List<Integer> columnsToIntList(List<String> columns) {
        List<Integer> columnsByInt = new ArrayList<>();
        for (String column : columns) {
            columnsByInt.add(CoordinateFactory.parseColumnToInt(column) - 1);
        }

        return columnsByInt;
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static STLSheet deserializeFrom(InputStream inputStream) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GENERATED_PACKAGE_NAME);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (STLSheet) unmarshaller.unmarshal(inputStream);
    }

    private static boolean isValidLayout(LayoutGetters layout) {
        return !(layout == null || layout.getRows() > MAX_ROWS || layout.getColumns() > MAX_COLUMNS);
    }

    private Layout copyLayout(LayoutGetters layout) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(layout);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            return (LayoutImpl) ois.readObject();
        } catch (Exception e) {
            // deal with the runtime error that was discovered as part of invocation
            throw new RuntimeException(e);
        }
    }

    private VersionManager getVersionManager(String sheetName) {
        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new RuntimeException("No version manager found for sheet " + sheetName);
        }

        return versionManager;
    }

    private PermissionManager getPermissionManager(String sheetName) {
        PermissionManager permissionManager = this.permissionManagers.get(sheetName);

        if (permissionManager == null) {
            throw new RuntimeException("No permission manager found for sheet " + sheetName);
        }

        return permissionManager;
    }

    private void canRead(PermissionManager permissionManager, String userName, String sheetName) {
        if (!permissionManager.canRead(userName)) {
            throw new RuntimeException("Permission manager is not allowed to read sheet " + sheetName);
        }
    }

    private void canWrite(PermissionManager permissionManager, String userName, String sheetName) {
        if (!permissionManager.canWrite(userName)) {
            throw new RuntimeException("Permission manager is not allowed to write sheet " + sheetName);
        }
    }
}
