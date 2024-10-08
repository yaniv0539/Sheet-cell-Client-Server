package expression.impl;
import expression.api.*;

import java.util.Arrays;

public class Number extends ExpressionImpl {

    private double value;

    public Number(double value) {
        this.value = value;
        setDataType(DataType.NUMERIC);
        //isValidArgs(value);
    }

    public double getValue() {
        return value;
    }

    @Override
    public Data evaluate() {
        return new DataImpl(DataType.NUMERIC, value);
    }

    @Override
    public boolean isValidArgs(Object... args) {
        try{
            Arrays
                    .stream(args)
                    .map(double.class::cast);
        }
        catch(ClassCastException e) {
            throw new IllegalArgumentException("arguments must be number in " + this.getClass().getSimpleName());
        }

        return true;
    }

    @Override
    public String toString() {
        return value >= 0 ? Double.toString(value): "(" + Double.toString(value) + ")";
    }

}
