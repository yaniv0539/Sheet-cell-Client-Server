package expression.impl;

import expression.api.Data;
import expression.api.DataType;

public class NonValueExpression extends ExpressionImpl{

    public NonValueExpression() {

        setDataType(DataType.UNKNOWN);
    }
    @Override
    public Data evaluate() {
        return new DataImpl(DataType.UNKNOWN,"");
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return true;
    }
}
