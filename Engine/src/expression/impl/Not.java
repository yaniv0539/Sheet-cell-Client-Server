package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

public class Not extends UnaryExpression {

    public Not(Expression expr) {
        super(expr);
        setDataType(DataType.BOOLEAN);
    }

    @Override
    protected Data dynamicEvaluate(Data input) {
        return input.getType() == DataType.BOOLEAN ? new DataImpl(DataType.BOOLEAN,!(boolean)input.getValue())
                : new DataImpl(DataType.UNKNOWN,DataImpl.BoolUndefiled);
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return false;
    }
}
