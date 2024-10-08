package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

public class Equal extends BinaryExpression {


    public Equal(Expression left, Expression right) {
        super(left, right);
        setDataType(DataType.BOOLEAN);
    }

    @Override
    protected Data dynamicEvaluate(Data left, Data right) {
        return left.getType() == right.getType() ? new DataImpl(DataType.BOOLEAN, true)
                : new DataImpl(DataType.BOOLEAN, false);
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return true;
    }
}
