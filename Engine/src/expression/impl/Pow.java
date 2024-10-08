package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

import java.util.Arrays;

public class Pow extends BinaryExpression {

    public Pow(Expression left, Expression right) {
        super(left, right);
        setDataType(DataType.NUMERIC);
        //isValidArgs(left, right);
    }

    @Override
    protected Data dynamicEvaluate(Data left, Data right) {

        return left.getType() == DataType.NUMERIC && right.getType() == DataType.NUMERIC ?
                new DataImpl(DataType.NUMERIC,Math.pow((double)left.getValue(),(double)right.getValue()))
                : new DataImpl(DataType.UNKNOWN,Double.NaN);
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
                    "for example: {POW,{DIVIDE,4,5},{REF,A3}}");
        }

        return true;
    }
}
