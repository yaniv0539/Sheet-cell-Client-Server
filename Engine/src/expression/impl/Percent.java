package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

public class Percent extends BinaryExpression {

    public Percent(Expression left, Expression right) {
        super(left, right);
        setDataType(DataType.NUMERIC);
    }

    @Override
    protected Data dynamicEvaluate(Data left, Data right) {
        return left.getType() == DataType.NUMERIC && right.getType() == DataType.NUMERIC ?
                new DataImpl(DataType.NUMERIC, (double)left.getValue()*(double)right.getValue()/100)
                : new DataImpl(DataType.UNKNOWN,Double.NaN);
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return false;
    }
}
