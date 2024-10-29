package engine.impl;

import dto.*;
import dto.enums.PermissionType;
import dto.enums.Status;
import engine.api.Engine;
import engine.chat.ChatManager;
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
import sheet.layout.size.api.Size;
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

        canRead(userName, sheetName);

        return new SheetDto(versionManager.getLastVersion());
    }

    @Override
    public SheetDto getSheetDTO(String userName, String sheetName, int sheetVersion) {
        VersionManager versionManager = this.versionManagers.get(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);
        Sheet sheet = versionManager.getVersion(sheetVersion);

        canRead(userName, sheetName);

        if (sheet == null) {
            throw new RuntimeException("Sheet " + sheetName + " not found");
        }

        return new SheetDto(sheet);
    }

    @Override
    public String addNewSheet(String userName, InputStream inputStream) {
        try {
            STLSheet stlSheet = deserializeFrom(inputStream);
            Sheet sheet = STLSheetToSheet.generate(stlSheet);

            sheet.getActiveCells().forEach((coordinate, cell) -> cell.setUpdateBy(userName));

            synchronized (this) {

                if (this.versionManagers.containsKey(sheet.getName())) {
                    throw new RuntimeException("Sheet " + sheet.getName() + " already exists");
                }

                if (!isValidLayout(sheet.getLayout())) {
                    throw new IndexOutOfBoundsException("Layout is invalid !" + "\n" +
                            "valid scale: rows <= 50 , columns <= 20");
                }

                VersionManager versionManager = this.versionManagers.computeIfAbsent(sheet.getName(), k -> VersionManagerImpl.create());
                versionManager.init(sheet);

                this.permissionManagers.computeIfAbsent(sheet.getName(), k -> PermissionManagerImpl.create(userName));
            }

            return sheet.getName();

        } catch (JAXBException e) {
            throw new RuntimeException("Failed to read XML file", e);
        }
    }

    @Override
    public void updateCell(String userName, String sheetName, int sheetVersion, String cellName, String cellValue) {

        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        canWrite(userName, sheetName, sheetVersion);

        versionManager.makeNewVersion();

        try {
            Sheet lastVersion = versionManager.getLastVersion();
            int version = lastVersion.getVersion();

            lastVersion.setCell(CoordinateFactory.toCoordinate(cellName.toUpperCase()), cellValue);

            lastVersion.getActiveCells().values().stream()
                    .filter(cell -> cell.getVersion() == version)
                    .forEach(cell -> cell.setUpdateBy(userName));

        } catch (Exception e) {
            versionManager.deleteLastVersion();
            throw e;
        }
    }

    @Override
    public SheetDto filter(String userName, String sheetName, Boundaries boundaries, String column, List<String> values, int version) {
        VersionManager versionManager = getVersionManager(sheetName);

        canRead(userName, sheetName);

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

        canRead(userName, sheetName);

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

        canRead(userName, sheetName);

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

        canRead(userName, sheetName);

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

        canRead(userName, sheetName);

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

        canRead(userName, sheetName);

        Sheet sheet = versionManager.getVersion(version);

        if (sheet == null) {
            throw new IllegalArgumentException("Sheet " + sheetName + " does not have a version manager");
        }

        return sheet.getColumnUniqueValuesInRange(column, startRow, endRow);
    }

    @Override
    public void addRange(String userName, String sheetName, int sheetVersion, String name, String boundariesString) {
        VersionManager versionManager = getVersionManager(sheetName);

        canWrite(userName, sheetName, sheetVersion);

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
    public void deleteRange(String userName, String sheetName, int sheetVersion, String rangeName) {

        VersionManager versionManager = getVersionManager(sheetName);

        canWrite(userName, sheetName, sheetVersion);

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

        canRead(userName, sheetName);

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
    public PermissionsDto getSheetPermissions(String sheetName) {
        PermissionManager permissionManager = getPermissionManager(sheetName);
        return new PermissionsDto(permissionManager);
    }

    @Override
    public PermissionType getUserPermission(String userName, String sheetName) {
        return permissionManagers.get(sheetName).getPermission(userName);
    }

    @Override
    public List<String> getUsers() {
        return userManager.getUsers();
    }

    @Override
    public synchronized void addUser(String userName) {
        this.userManager.addUser(userName);
    }

    @Override
    public Set<SheetOverviewDto> getSheetOverviewDto(String userName) {
        if(!userManager.isUserExists(userName)) {
            throw new RuntimeException("User " + userName + " does not exist");
        }

        Set<SheetOverviewDto> sheetOverviewDtoSet = new HashSet<>();

        synchronized (this) {
            versionManagers.forEach((sheetName, versionManager) -> {
                Sheet sheet = versionManager.getLastVersion();
                String owner = permissionManagers.get(sheetName).getOwner();
                PermissionType userPermission = getUserPermission(userName, sheetName);

                sheetOverviewDtoSet.add(new SheetOverviewDto(sheet, userPermission, owner));
            });
        }

        return sheetOverviewDtoSet;
    }

    @Override
    public void addRequestPermission(String sheetName, String userName, PermissionType permissionType) {

        if (!userManager.isUserExists(userName)) {
            throw new RuntimeException("User " + userName + " does not exist");
        }

        PermissionManager permissionManager = getPermissionManager(sheetName);

        permissionManager.addRequest(userName, permissionType);


    }

    @Override
    public void setResponseToRequest(String sheetName, String userName, PermissionType permissionType, Status status,Status response) {
        if (!userManager.isUserExists(userName)) {
            throw new RuntimeException("User " + userName + " does not exist");
        }

        PermissionManager permissionManager = getPermissionManager(sheetName);
        permissionManager.updateRequest(userName,permissionType,status,response);
    }

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

    private void canRead(String userName, String sheetName) {

        String basicErrorMessage = "Can't read because ";
        PermissionManager permissionManager = getPermissionManager(sheetName);

        if (!permissionManager.canRead(userName)) {
            throw new RuntimeException(basicErrorMessage + "your don't have permission to read sheet " + sheetName + ".");
        }
    }

    private void canWrite(String userName, String sheetName, int sheetVersion) {

        String basicErrorMessage = "Can't write because ";
        VersionManager versionManager = getVersionManager(sheetName);
        PermissionManager permissionManager = getPermissionManager(sheetName);

        if (versionManager.getLastVersion().getVersion() != sheetVersion) {
            throw new RuntimeException(basicErrorMessage + "your sheet is not updated to the last version.");
        }

        if (!permissionManager.canWrite(userName)) {
            throw new RuntimeException(basicErrorMessage + "your don't have permission to write sheet " + sheetName + ".");
        }
    }

}
