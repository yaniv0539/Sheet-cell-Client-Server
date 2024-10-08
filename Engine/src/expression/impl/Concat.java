package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

import java.util.Arrays;


public class Concat extends BinaryExpression {


    private Concat(Expression left, Expression right) {
        super(left, right);
        setDataType(DataType.STRING);
        //isValidArgs(left, right);
     }

    @Override
    protected Data dynamicEvaluate(Data left, Data right) {

        return left.getType() == DataType.STRING && right.getType() == DataType.STRING ?
                new DataImpl(DataType.STRING, String.join("",(String) left.getValue(), (String) right.getValue()))
                : new DataImpl(DataType.UNKNOWN,DataImpl.undefiled);

    }
    @Override
    public boolean isValidArgs(Object... args) {
        boolean value = Arrays
                .stream(args)
                .map(Expression.class::cast)
                .allMatch(arg -> arg.getType() == DataType.STRING || arg.getType() == DataType.UNKNOWN);

        if (!value) {
            //need to throw our own exception.
            throw new IllegalArgumentException("arguments must be string or reference to cell!\n" +
                    "for example: {CONCAT,hello,{REF,A3}}");
        }

        return true;
    }
}




