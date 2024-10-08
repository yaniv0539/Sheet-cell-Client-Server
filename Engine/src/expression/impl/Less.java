package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

public class Less extends BinaryExpression {

    public Less(Expression left, Expression right) {
        super(left, right);
        setDataType(DataType.BOOLEAN);


    }

    @Override
    protected Data dynamicEvaluate(Data left, Data right) {
        return left.getType() == DataType.NUMERIC && right.getType() == DataType.NUMERIC  ? new DataImpl(DataType.BOOLEAN,(double)left.getValue() <= (double)right.getValue())
                : new DataImpl(DataType.UNKNOWN,DataImpl.BoolUndefiled);
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return false;
    }


}
