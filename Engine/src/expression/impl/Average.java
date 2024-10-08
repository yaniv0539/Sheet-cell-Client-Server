package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;
import sheet.api.SheetGetters;
import sheet.cell.api.CellGetters;
import sheet.range.api.Range;
import sheet.range.api.RangeGetters;

import java.util.Objects;

public class Average extends UnaryExpression {

    public static SheetGetters sheetView;

    public Average(Expression input) {
        super(input);
        setDataType(DataType.NUMERIC);
    }

    @Override
    protected Data dynamicEvaluate(Data input) {

        Data data = new DataImpl(DataType.UNKNOWN,Double.NaN);

        if(input.getType() == DataType.STRING) {

            double sumOfNumericCells = (double) new Sum(new RawString((String) input.getValue())).evaluate().getValue();
            RangeGetters range = sheetView.getRange((String) input.getValue());

            if (range != null) {
                double numberOfCells = range.toCoordinateCollection().stream()
                                            .map(coordinate -> sheetView.getCell(coordinate))
                                            .filter(Objects::nonNull)
                                            .map(CellGetters::getEffectiveValue)
                                            .filter(dataInCell -> dataInCell.getType() == DataType.NUMERIC)
                                            .count();

                data = new DataImpl(DataType.NUMERIC,sumOfNumericCells/numberOfCells);

            }
        }

        return data;

    }

    @Override
    public boolean isValidArgs(Object... args) {
        return false;
    }
}
