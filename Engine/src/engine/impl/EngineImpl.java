package engine.impl;

import dto.*;
import engine.api.Engine;
import engine.jaxb.parser.STLSheetToSheet;
import engine.version.manager.api.VersionManager;
import engine.version.manager.impl.VersionManagerImpl;
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

    private Sheet sheet;
    private final VersionManager versionManager;

    private EngineImpl() {
        this.versionManagers = new HashMap<>();
        this.versionManager = VersionManagerImpl.create();
    }

    public static EngineImpl create() {
        return new EngineImpl();
    }

    @Override
    public void readXMLInitFile(String filename) {
        try {
            if (!filename.endsWith(".xml")) {
                throw new FileNotFoundException("File name has to end with '.xml'");
            }

            InputStream inputStream = new FileInputStream(new File(filename));
            readXMLInitFile(inputStream);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to read XML file", e);
        }
    }

    @Override
    public void readXMLInitFile(InputStream inputStream) {
        try {
            STLSheet stlSheet = deserializeFrom(inputStream);
            Sheet sheet = STLSheetToSheet.generate(stlSheet);

            if (!isValidLayout(sheet.getLayout())) {
                throw new IndexOutOfBoundsException("Layout is invalid !" + "\n" +
                        "valid scale: rows <= 50 , columns <= 20");
            }

            this.sheet = sheet;
            versionManager.init(this.sheet);

        } catch (JAXBException e) {
            throw new RuntimeException("Failed to read XML file", e);
        }
    }

    @Override
    public SheetDto getSheetDTO(String sheetName) {
        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new RuntimeException("No version manager found for sheet " + sheetName);
        }

        return new SheetDto(versionManager.getLastVersion());
    }

    @Override
    public SheetDto getSheetDTO(String sheetName, int sheetVersion) {
        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new RuntimeException("No version manager found for sheet " + sheetName);
        }

        SheetGetters sheetGetters = versionManager.getVersion(sheetVersion);

        if (sheetGetters == null) {
            throw new RuntimeException("Sheet " + sheetName + " not found");
        }

        return new SheetDto(sheetGetters);
    }

    @Override
    public void addNewSheet(InputStream inputStream) {
        try {
            STLSheet stlSheet = deserializeFrom(inputStream);
            Sheet sheet = STLSheetToSheet.generate(stlSheet);

            if (!isValidLayout(sheet.getLayout())) {
                throw new IndexOutOfBoundsException("Layout is invalid !" + "\n" +
                        "valid scale: rows <= 50 , columns <= 20");
            }

            VersionManager versionManager = this.versionManagers.computeIfAbsent(sheet.getName(), k -> VersionManagerImpl.create());

            versionManager.init(sheet);

        } catch (JAXBException e) {
            throw new RuntimeException("Failed to read XML file", e);
        }
    }

    @Override
    public void updateCell(String sheetName, String cellName, String cellValue) {

        VersionManager versionManager = versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new RuntimeException("No version manager found for sheet " + sheetName);
        }

        versionManager.makeNewVersion();

        try {
            versionManager.getLastVersion().setCell(CoordinateFactory.toCoordinate(cellName.toUpperCase()), cellValue);
        } catch (Exception e) {
            versionManager.deleteLastVersion();
            throw e;
        }
    }

    @Override
    public SheetDto filter(String sheetName, Boundaries boundaries, String column, List<String> values, int version) {

        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new RuntimeException("No version manager found for sheet " + sheetName);
        }

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


        //todo:itay filter version for exrecise demends.
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

        //todo: this is yaniv version works like Gsheet.
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
    public SheetDto sort(String sheetName, Boundaries boundaries, List<String> columns, int version) {

        VersionManager versionManager = this.versionManagers.get(sheetName);

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
    public List<List<CellDto>> sortCellsInRange(String sheetName, Boundaries boundaries, List<String> columns, int version) {

        VersionManager versionManager = this.versionManagers.get(sheetName);

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

        List<List<CellDto>> dataToSortDto = new ArrayList<>();

        dataToSort.forEach(list -> {
            List<CellDto> tempList = new ArrayList<>();
            list.forEach(cellGetters -> tempList.add(new CellDto(cellGetters)));
            dataToSortDto.add(tempList);
        });

        return dataToSortDto;
    }

    @Override
    public Map<CoordinateDto, CoordinateDto> filteredMap(String sheetName, Boundaries boundariesToFilter, String filteringByColumn, List<String> filteringByValues, int version) {

        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new IllegalArgumentException("Sheet " + sheetName + " does not have a version manager");
        }

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
    public List<String> getColumnUniqueValuesInRange(String sheetName, int column, int startRow, int endRow, int version) {
        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new IllegalArgumentException("Sheet " + sheetName + " does not have a version manager");
        }

        Sheet sheet = versionManager.getVersion(version);

        if (sheet == null) {
            throw new IllegalArgumentException("Sheet " + sheetName + " does not have a version manager");
        }

        return sheet.getColumnUniqueValuesInRange(column, startRow, endRow);
    }

    @Override
    public boolean addRange(String sheetName, String name, String boundariesString) {
        VersionManager versionManager = this.versionManagers.get(sheetName);
        versionManager.makeNewVersion();
        try {
            Boundaries boundaries = BoundariesFactory.toBoundaries(boundariesString);
            boolean sheetChanged = versionManager.getLastVersion().addRange(name, boundaries);
            if(!sheetChanged)
            {
                versionManager.deleteLastVersion();
                versionManager.getLastVersion().addRange(name, boundaries);
            }
            return sheetChanged;

        } catch (Exception e) {
            versionManager.deleteLastVersion();
            throw e;
        }
    }

    @Override
    public RangeDto getRangeDTO(String name) {
        return new RangeDto(sheet.getRange(name));
    }

    @Override
    public void deleteRange(String sheetName, String rangeName) {

        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new RuntimeException("No version found for sheet " + sheetName);
        }

        Sheet lastVersion = versionManager.getLastVersion();
        RangeGetters range = lastVersion.getRange(rangeName);
        Collection<Coordinate> coordinates = lastVersion.rangeUses(range);

        if (!coordinates.isEmpty()) {
            throw new RuntimeException("Can not delete range in use !\nCells that using range: " + coordinates.toString());
        }

        lastVersion.deleteRange(range);
    }

    @Override
    public BoundariesDto getBoundaries(String sheetName, String boundaries) {

        if (!BoundariesFactory.isValidBoundariesFormat(boundaries)) {
            throw new RuntimeException("Invalid boundaries");
        }

        Boundaries boundaries1 = BoundariesFactory.toBoundaries(boundaries);

        VersionManager versionManager = this.versionManagers.get(sheetName);

        if (versionManager == null) {
            throw new RuntimeException("No version found for sheet " + sheetName);
        }

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
        if(!CoordinateFactory.isGreaterThen(boundaries1.getTo(),boundaries1.getFrom()))
        {
            throw new RuntimeException("coordinate " + boundaries1.getFrom() + " > " + boundaries1.getTo());
        }

        return new BoundariesDto(boundaries1);
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
}
