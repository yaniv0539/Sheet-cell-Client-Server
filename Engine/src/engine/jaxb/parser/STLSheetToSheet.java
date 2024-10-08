package engine.jaxb.parser;

import engine.jaxb.generated.*;
import sheet.api.Sheet;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.impl.SheetImpl;
import sheet.layout.api.Layout;
import sheet.layout.impl.LayoutImpl;
import sheet.layout.size.api.Size;
import sheet.layout.size.impl.SizeImpl;
import sheet.range.boundaries.api.Boundaries;
import sheet.range.boundaries.impl.BoundariesImpl;
import sheet.range.impl.RangeImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class STLSheetToSheet {

    private STLSheetToSheet() {}

    public static Sheet generate(STLSheet stlSheet) {

        STLLayout stlLayout = stlSheet.getSTLLayout();
        STLSize stlSize = stlLayout.getSTLSize();

        int width = stlSize.getColumnWidthUnits();
        int height = stlSize.getRowsHeightUnits();

        Size size = SizeImpl.create(width, height);

        int column = stlLayout.getColumns();
        int row = stlLayout.getRows();

        Layout layout = LayoutImpl.create(size, row, column);

        String sheetName = stlSheet.getName();

        Sheet sheet = SheetImpl.create(sheetName, layout);

        STLRanges stlRanges = stlSheet.getSTLRanges();

        List<STLRange> stlRangeList = stlRanges.getSTLRange();

        stlRangeList.forEach(stlRange -> {
            String rangeName = stlRange.getName();

            STLBoundaries stlBoundaries = stlRange.getSTLBoundaries();
            Boundaries boundaries = BoundariesImpl.create(CoordinateFactory.toCoordinate(stlBoundaries.getFrom()), CoordinateFactory.toCoordinate(stlBoundaries.getTo()));

            sheet.addRange(rangeName, boundaries);
        });

        STLCells stlCells = stlSheet.getSTLCells();

        List<STLCell> stlCellsList = stlCells.getSTLCell();

        Map<Coordinate, String> originalValuesMap = stlCellsList.stream()
                .collect(Collectors.toMap(
                        stlCell -> CoordinateFactory.createCoordinate(stlCell.getRow() - 1, CoordinateFactory.parseColumnToInt(stlCell.getColumn()) - 1),
                        STLCell::getSTLOriginalValue
                ));

        sheet.setCells(originalValuesMap);

        return sheet;
    }
}
