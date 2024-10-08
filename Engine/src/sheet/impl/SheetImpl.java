package sheet.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;
import expression.impl.Average;
import expression.impl.DataImpl;
import expression.impl.Ref;
import expression.impl.Sum;
import expression.parser.OrignalValueUtilis;
import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.cell.api.CellGetters;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.layout.api.Layout;
import sheet.layout.api.LayoutGetters;
import sheet.range.api.Range;
import sheet.range.api.RangeGetters;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.impl.RangeImpl;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SheetImpl implements Sheet, Serializable {

    private final String name;
    private final Layout layout;
    private int version;
    private Map<Coordinate, Cell> activeCells;
    private Set<Range> ranges;
    private int numberOfCellsThatChanged;

    private SheetImpl(String name, Layout layout) {

        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        if (layout == null) {
            throw new IllegalArgumentException("Layout cannot be null");
        }

        this.name = name;
        this.layout = layout;
        this.version = 1;
        this.activeCells = new HashMap<>();
        this.ranges = new HashSet<>();
    }

    public static SheetImpl create(String name, Layout layout) {
        return new SheetImpl(name, layout);
    }

    public static SheetImpl create(Layout layout) {
        return new SheetImpl("Sheet", layout);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public LayoutGetters getLayout() {
        return this.layout;
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public Cell getCell(Coordinate coordinate) {

        if (!isCoordinateInBoundaries(coordinate)) {
            throw new IndexOutOfBoundsException("coordinate " + coordinate + " is not in sheet boundaries.");
        }

        return activeCells.get(coordinate);
    }

    @Override
    public int getNumberOfCellsThatChanged() {
        return this.numberOfCellsThatChanged;
    }

    @Override
    public Map<Coordinate, CellGetters> getActiveCells() {
        return Collections.unmodifiableMap(this.activeCells);
    }

    // FOR INTERFACE lookupCellService
    //NO NEED
    @Override
    public Data getCellData(String cellId) {
        return new DataImpl(DataType.UNKNOWN,1);
    }

    @Override
    public void setVersion(int version) {

        if (!isValidVersion(version)) {
            throw new IllegalArgumentException("Version cannot be less than 1");
        }

        this.version = version;
    }

    @Override
    public boolean addRange(String name, Boundaries boundaries) {
        if(!isRangeInBoundaries(boundaries)){
            throw new IndexOutOfBoundsException("first coordinate " + boundaries.getFrom() + " < " + boundaries.getTo() + " last coordinate in " + name+ "\n" +
                                                "Range format:<top-left-cell>..<bottom-right-cell>");
        }
        ///itay change
        RangeImpl range = RangeImpl.create(name, boundaries);
        if (!ranges.add(range)) {
            throw new IllegalArgumentException("Range " +"\""+name+"\""+ " already exists in " + "\""+this.name+"\"");
        }
        //itay change
        Collection<Coordinate> coordinates = this.rangeUses(range);
        if (!coordinates.isEmpty()) {
            coordinates.forEach(coordinate -> {
                this.setCell(coordinate, activeCells.get(coordinate).getOriginalValue());
            });
        }
        return !coordinates.isEmpty();
    }

    @Override
    public boolean isRangeInBoundaries(Boundaries boundaries) {

        return (isCoordinateInBoundaries(boundaries.getFrom()) && isCoordinateInBoundaries(boundaries.getTo())
                 && CoordinateFactory.isGreaterThen(boundaries.getTo(),boundaries.getFrom()));
    }

    @Override
    public void deleteRange(RangeGetters range) {
        ranges.remove(range);
    }

    @Override
    public void setCell(Coordinate target, String originalValue) {

         Ref.sheetView = this;
         Sum.sheetView = this;
         Average.sheetView = this;

         isCoordinateInBoundaries(target);

         Cell updatedCell = CellImpl.create(target, version, originalValue);
         Cell previousCell =  insertCellToSheet(updatedCell);

         try {
             circleFrom(updatedCell);
             recalculateSheetFrom(updatedCell);
         }
         catch(IllegalArgumentException circle){
             insertCellToSheet(previousCell);
             throw circle;
         }

    }

    @Override
    public void setCells(Map<Coordinate, String> originalValues) {

        // Preparing flags map so will be able to know if we've been already tried to set a specific cell.
        Map<Coordinate, Boolean> flagMap = new HashMap<>();

        // Preparing old original values map so if we'll have an exception while we'll try to set cells,
        // we'll be able to roll back and redo the operation.
        Map<Coordinate, String> oldOriginalValueMap = new HashMap<>();

        // Preparing a stack of coordinates, so we'll be able to know who is the first and who is the last we updated.
        // The stack will help us to redo if needed.
        Stack<Coordinate> updatedCellsCoordinates = new Stack<>();

        // Initialize the flags map with false values.
        for (Coordinate coordinate : originalValues.keySet()) {
            flagMap.put(coordinate, false);
        }

        try {
            // Sending each original value to helper function.
            originalValues.forEach((coordinate, originalValue) -> setCellsHelper(originalValues, flagMap, oldOriginalValueMap, updatedCellsCoordinates, coordinate));
        } catch (Exception exception) {
            // Undo the operation and move on the exception.
            updatedCellsCoordinates.forEach(coordinate -> {
                if (oldOriginalValueMap.containsKey(coordinate) && "".equals(oldOriginalValueMap.get(coordinate))) {
                    this.activeCells.remove(coordinate);
                }
                else {
                    setCell(coordinate, oldOriginalValueMap.get(coordinate));
                }
            });

            throw exception;
        }
        numberOfCellsThatChanged = originalValues.size();
    }

    private void setCellsHelper(Map<Coordinate, String> newOriginalValuesMap,
                                Map<Coordinate, Boolean> flagMap,
                                Map<Coordinate, String> oldOriginalValueMap,
                                Stack<Coordinate> updatedCellsCoordinates,
                                Coordinate coordinate) {

        // If we touched the coordinate, go back.
        if (flagMap.get(coordinate)) {
            return;
        }

        // We touched the coordinate!
        flagMap.put(coordinate, true);

        String newOriginalValue = newOriginalValuesMap.get(coordinate);

        Set<Coordinate> refCoordinates = OrignalValueUtilis.findInfluenceFrom(newOriginalValue,this);

        // For each cell that we might be depended on, we'll do some checks:
        // If the cell is inside 'newOriginalValuesMap', we'll do recursive operation with this cell.
        // Else if the cell is not inside 'activeCells' map we'll throw exception because this cell is null.
        // Else, it is inside 'activeCells' map, and we'll skip to next iteration.

        refCoordinates.forEach(refCoordinate -> {
            if (newOriginalValuesMap.containsKey(refCoordinate)) {
                setCellsHelper(newOriginalValuesMap, flagMap, oldOriginalValueMap, updatedCellsCoordinates, refCoordinate);
            }
            else if (!this.activeCells.containsKey(refCoordinate)) {
                throw new IndexOutOfBoundsException(refCoordinate + " is not define in file, cannot get data !");
            }
        });

        if (this.activeCells.containsKey(coordinate)) {
            Cell cell = this.activeCells.get(coordinate);
            oldOriginalValueMap.put(coordinate, cell.getOriginalValue());
        }
        else {
            oldOriginalValueMap.put(coordinate, "");
        }

        setCell(coordinate, newOriginalValue);
        updatedCellsCoordinates.push(coordinate);
    }

    @Override
    public boolean isCoordinateInBoundaries(Coordinate target) {

        if(!isRowInSheetBoundaries(target.getRow()) || !isColumnInSheetBoundaries(target.getCol())) {
            throw new IllegalArgumentException(target.toString() + " is out of bounds !");
        }

        return true;
    }

    private boolean isRowInSheetBoundaries(int row) {
        return !(row >= this.layout.getRows());
    }

    private boolean isColumnInSheetBoundaries(int column) {
        return !(column >= this.layout.getColumns());
    }

    public static boolean isValidVersion(int version) {
        return version >= 1;
    }

    private boolean circleFrom(Cell cellToCheck) {
        return hasCircle(cellToCheck);
    }

    private boolean hasCircle(Cell cellToCheck) {
        return recHasCircle(cellToCheck, new HashSet<Coordinate>());
    }

    private boolean recHasCircle(Cell current, Set<Coordinate> visited) {
        try{
            // If the current object is already visited, a cycle is detected
            if (visited.contains(current.getCoordinate())) {
                return true;
            }

            // Mark the current object as visited
            visited.add(current.getCoordinate());

            // Recur for all the objects in the relatedObjects list
            for (Cell affectedBy : current.getInfluenceFrom()) {
                // If a cycle is detected in the recursion, return true
                if (recHasCircle(affectedBy, visited)) {
                    throw new IllegalArgumentException("Circular voting: " + affectedBy.getCoordinate().toString());
                }
            }

            // Remove the current object from the visited set (backtracking)
            visited.remove(current.getCoordinate());

            // If no cycle was found, return false
            return false;
        }catch (IllegalArgumentException exception) {
           throw new IllegalArgumentException(exception.getMessage() + " -> " + current.getCoordinate().toString());
        }

    }

    private Set<Cell> CoordinateToCell(Set<Coordinate> newInfluenceCellsId) {
        Set<Cell> Cells = new HashSet<>();

        for (Coordinate location : newInfluenceCellsId) {
            Cells.add(getCell(location));
        }
        return Cells;
    }

    private Stack<Cell> topologicalSortFrom(Cell cell) {

        Stack<Cell> stack = new Stack<>();
        Set<Coordinate> visited = new HashSet<>();

        // Call the recursive helper function to store topological sort starting from all cells one by one
        for (Cell neighbor : cell.getInfluenceOn()) {

            if (!visited.contains(neighbor.getCoordinate())) {
                dfs(neighbor, visited, stack);
            }
        }
        stack.push(cell);

        return stack;
    }

    private void dfs(Cell cell, Set<Coordinate> visited,Stack<Cell> stack) {
        visited.add(cell.getCoordinate());

        // Visit all the adjacent vertices
        for (Cell neighbor : cell.getInfluenceOn()) {

            if (!visited.contains(neighbor.getCoordinate())) {
                dfs(neighbor, visited, stack);
            }
        }

        // Push current cell to stack which stores the result
        stack.push(cell);
    }

    private Cell insertCellToSheet(Cell toInsert) {

        Cell toReplace = activeCells.put(toInsert.getCoordinate(),toInsert);
        OrignalValueUtilis.findInfluenceFrom(toInsert.getOriginalValue(),this).forEach(coord ->
        {
            isCoordinateInBoundaries(coord);
            if(!activeCells.containsKey(coord)) {
                Cell c = CellImpl.create(coord,version, DataImpl.empty);
                c.computeEffectiveValue();
                activeCells.put(coord,c);
            }
        });

        toInsert.setInfluenceFrom(CoordinateToCell(OrignalValueUtilis.findInfluenceFrom(toInsert.getOriginalValue(),this)));
        toInsert.getInfluenceFrom().forEach(cell -> cell.getInfluenceOn().add(toInsert));

        //if it is a new cell there is no influenceOn, if exist he may have influenced on other cells.
        if(toReplace != null) {
            toInsert.setInfluenceOn(toReplace.getInfluenceOn());
            toInsert.getInfluenceOn().forEach(cell -> cell.getInfluenceFrom().add(toInsert));
            //until here we get a new sheet now we just need to remove
            toReplace.getInfluenceFrom().forEach(cell -> cell.getInfluenceOn().remove(toReplace));
            toReplace.getInfluenceOn().forEach(cell -> cell.getInfluenceFrom().remove(toReplace));

        }

        return toReplace;
    }

    private void recalculateSheetFrom(Cell cell) {

        Stack<Cell> cellStack = topologicalSortFrom(cell);
        numberOfCellsThatChanged = cellStack.size();

        while (!cellStack.isEmpty()) {
            Cell c = cellStack.pop();
            c.computeEffectiveValue();
            c.setVersion(version);

        }
    }

    @Override
    public Range getRange(String name) {

        Range theRange;

        try{
            theRange = ranges.stream().filter(range -> range.getName().equals(name.toUpperCase())).findFirst().get();
        }catch (NoSuchElementException exception) {
            theRange = null;
        }

        return theRange;
    }

    @Override
    public Set<Range> getRanges() {
        return this.ranges;
    }

    @Override
    public List<List<CellGetters>> getCellInRange(int startRow, int endRow, int startCol, int endCol) {
        List<List<CellGetters>> cellsInRange = new ArrayList<>();

        for (int row = startRow; row <= endRow; row++) {
            List<CellGetters> rowCellsInRange = new ArrayList<>();
            for (int col = startCol; col <= endCol; col++) {
                Cell cell = getCell(CoordinateFactory.createCoordinate(row, col));
                if(cell != null){
                    rowCellsInRange.add(cell);
                }
                else{
                    Cell dummyCell = CellImpl.create(CoordinateFactory.createCoordinate(row, col), version, DataImpl.empty);
                    dummyCell.computeEffectiveValue();
                    rowCellsInRange.add(dummyCell);
                }

            }
            cellsInRange.add(rowCellsInRange);
        }

        return cellsInRange;
    }

    @Override
    public boolean isColumnNumericInRange(int column, int startRow, int endRow) {

        for (int row = startRow; row <= endRow; row++) {
            CellGetters cell = activeCells.get(CoordinateFactory.createCoordinate(row, column));
            String value;
            if(cell !=null){
                value = cell.getEffectiveValue().toString();
            }
            else{
                value = "";
            }

            try{
                Double.parseDouble(value);
            }catch (NumberFormatException exception) {
                //maybe throw exception with that coordinate.
                return false;
            }
        }

        return true;
    }

    @Override
    public List<String> getColumnUniqueValuesInRange(int column, int startRow, int endRow) {
        List<String> uniqueValues = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            CellGetters cell = activeCells.get(CoordinateFactory.createCoordinate(row, column));
            if(cell != null && !uniqueValues.contains(cell.getEffectiveValue().toString())){
                uniqueValues.add(cell.getEffectiveValue().toString());
            }
        }
        return uniqueValues;
    }


    @Override
    public Collection<Coordinate> rangeUses(RangeGetters range) {

        Set<String> validFirstStrings = Set.of("sum", "average");
        List<Coordinate> coordinatesThatUseRange = new ArrayList<>();
        String regex = "\\{([a-zA-Z]+),(.+)\\}";// {sum,grades}

        // Compile the regex and match against the input
        Pattern pattern = Pattern.compile(regex);

        this.activeCells.values().forEach(cell -> {
            Matcher matcher = pattern.matcher(cell.getOriginalValue());

            // If it matches the pattern
            if (matcher.matches()) {
                String firstString = matcher.group(1);
                String secondString = matcher.group(2);

                // Check if the first string is within the valid set
                if (validFirstStrings.contains(firstString.toLowerCase())) {
                    if (secondString.toUpperCase().equals(range.getName())) {
                        coordinatesThatUseRange.add(cell.getCoordinate());
                    }
                }
            }
        });

        return coordinatesThatUseRange;
    }

    @Override
    public void addRangeForXml(String rangeName, Boundaries boundaries) {
        if(!isRangeInBoundaries(boundaries)){
            throw new IndexOutOfBoundsException("first coordinate" + boundaries.getFrom() + " < " + boundaries.getTo());
        }
        ///itay change
        RangeImpl range = RangeImpl.create(name, boundaries);
        if (!ranges.add(range)) {
            throw new IllegalArgumentException("Range already exists in " + this.name);
        }
    }
}
