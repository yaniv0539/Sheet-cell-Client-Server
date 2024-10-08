package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;
import sheet.api.SheetGetters;
import sheet.cell.api.CellGetters;
import sheet.range.api.Range;
import sheet.range.api.RangeGetters;

import java.util.Objects;

public class Sum extends UnaryExpression {

    public static SheetGetters sheetView;

    public Sum(Expression RangeName) {
        super(RangeName);
        setDataType(DataType.NUMERIC);
    }

    @Override
    protected Data dynamicEvaluate(Data input) {

        Data data = new DataImpl(DataType.UNKNOWN,Double.NaN);

        if(input.getType() == DataType.STRING) {

            RangeGetters range = sheetView.getRange((String)input.getValue());

            if(range != null) {
                double sum = range.toCoordinateCollection().stream()
                        .map(coordinate -> sheetView.getCell(coordinate))
                        .filter(Objects::nonNull)
                        .map(CellGetters::getEffectiveValue)
                        .filter(dataInCell -> dataInCell.getType() == DataType.NUMERIC || dataInCell.equals(new DataImpl(DataType.UNKNOWN,Double.NaN)))
                        .mapToDouble(dataInCell-> (double)dataInCell.getValue())
                        .sum();

                if(!Double.isNaN(sum)) {
                    data = new DataImpl(DataType.NUMERIC,sum);
                }

            }
        }

        return data;
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return false;
    }
}
