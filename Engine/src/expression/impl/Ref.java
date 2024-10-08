package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;
import sheet.api.SheetGetters;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;

import java.util.Arrays;

public class Ref extends ExpressionImpl {

    private Coordinate cellToReferTo;
    public static SheetGetters sheetView;

    public Ref(Expression cellId) {
        setDataType(DataType.UNKNOWN);
        isValidArgs(cellId);
        cellToReferTo = CoordinateFactory.toCoordinate(((String) cellId.evaluate().getValue()).toUpperCase());
    }
    @Override
    public Data evaluate() {
        return sheetView.getCell(cellToReferTo).getEffectiveValue();
    }

    @Override
    public boolean isValidArgs(Object... args) {

        boolean value = Arrays
                 .stream(args)
                 .map(Expression.class::cast)
                 .allMatch(arg-> arg.getClass() == RawString.class);

        if(!value)
        {
            throw new IllegalArgumentException("arguments must be cell-id!\n" +
                    "for example: {REF,A3}");
        }

        return true;
    }
}
