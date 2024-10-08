package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

import java.util.Arrays;

public class Abs extends UnaryExpression {

    public Abs(Expression input) {

        super(input);
        setDataType(DataType.NUMERIC);
        //isValidArgs(input);
    }

    @Override
    protected Data dynamicEvaluate(Data input) {

        return input.getType() == DataType.NUMERIC ? new DataImpl(DataType.NUMERIC, Math.abs((double)input.getValue()))
                : new DataImpl(DataType.UNKNOWN, Double.NaN);
    }

    @Override
    public boolean isValidArgs(Object... args) {

        boolean value = Arrays
                .stream(args)
                .map(Expression.class::cast)
                .allMatch(arg -> arg.getType() == DataType.NUMERIC || arg.getType() == DataType.UNKNOWN);

        if (!value) {
            //need to throw our own exception.
            throw new IllegalArgumentException("arguments must be number/numeric function or reference to cell!\n" +
                    "for example: {ABS,{REF,A3}},{ABS,{PLUS,4,-5}}");
        }

        return true;
    }
}
