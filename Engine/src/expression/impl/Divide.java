package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

import java.util.Arrays;

public class Divide extends BinaryExpression {


    public Divide(Expression left, Expression right) {
        super(left, right);
        setDataType(DataType.NUMERIC);
        //isValidArgs(left, right);
    }

    @Override
    public Data dynamicEvaluate(Data left, Data right) {
        Data d;

        if(left.getType() == DataType.NUMERIC && right.getType() == DataType.NUMERIC) {
            if((double) right.getValue() == 0) {
               d = new DataImpl(DataType.UNKNOWN, Double.NaN);
            }
            else {
                d = new DataImpl(DataType.NUMERIC, (double) left.getValue() /(double) right.getValue());
            }
        }
        else {
            d = new DataImpl(DataType.UNKNOWN, Double.NaN);
        }

        return d;
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
                    "for example: {DIVIDE,{PLUS,4,5},{REF,A3}}");
        }

        return true;
    }
}
