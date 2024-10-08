package expression.impl;

import expression.api.Data;
import expression.api.DataType;

public class BooleanExpression extends ExpressionImpl {

    boolean value;

    public BooleanExpression(boolean value) {
        this.value = value;
        setDataType(DataType.BOOLEAN);
        //isValidArgs(value);
    }


    @Override
    public Data evaluate() {
        return new DataImpl(DataType.BOOLEAN, value);
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return true;
    }
}
