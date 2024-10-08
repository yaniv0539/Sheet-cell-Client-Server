package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

public class Or extends BinaryExpression {

    public Or(Expression left, Expression right) {
        super(left, right);
        setDataType(DataType.BOOLEAN);
    }

    @Override
    protected Data dynamicEvaluate(Data left, Data right) {
        return left.getType() == DataType.BOOLEAN && right.getType() == DataType.BOOLEAN  ? new DataImpl(DataType.BOOLEAN,(Boolean)left.getValue() || (Boolean)right.getValue())
                : new DataImpl(DataType.UNKNOWN,DataImpl.BoolUndefiled);
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return false;
    }
}
