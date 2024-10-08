package expression.impl;

import expression.api.*;

import java.util.Arrays;

public class Sub extends ExpressionImpl {
    private Expression source;
    private Expression left;
    private Expression right;

    public Sub(Expression source, Expression left, Expression right) {

        this.source = source;
        this.left = left;
        this.right = right;
        setDataType(DataType.STRING);
       //isValidArgs(source, left, right);
    }

    @Override
    public Data evaluate() {

        return source.evaluate().getType() == DataType.STRING && left.evaluate().getType() == DataType.NUMERIC && right.evaluate().getType() == DataType.NUMERIC ?
                new DataImpl(DataType.STRING,((String)source.evaluate().getValue()).substring
                        ((int)((double)left.evaluate().getValue()),(int)((double)right.evaluate().getValue())))
                : new DataImpl(DataType.UNKNOWN,DataImpl.undefiled);
    }

    @Override
    public boolean isValidArgs(Object... args) {

        boolean condition1 = Arrays.stream(args)
                .map(Expression.class::cast)
                .limit(1)
                .allMatch(arg -> arg.getType() == DataType.STRING || arg.getType() == DataType.UNKNOWN);

        boolean condition2 = Arrays.stream(args)
                        .map(Expression.class::cast)
                        .skip(1)
                        .allMatch(arg -> arg.getType() == DataType.NUMERIC || arg.getType() == DataType.UNKNOWN);

        if (!(condition1 && condition2)) {
            //need to throw our own exception.
            throw new IllegalArgumentException("first argument must be string and the 2 others number/numeric function\n" +
                    "for example: {SUB,world,{REF,A2},7} ");
        }

        return true;
    }
}
