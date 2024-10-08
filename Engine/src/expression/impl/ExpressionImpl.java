package expression.impl;

import expression.api.DataType;
import expression.api.Expression;

import java.io.Serializable;

public abstract class ExpressionImpl implements Expression, Serializable {

    protected DataType type;

    void setDataType(DataType type) {
        this.type = type;
    }

    @Override
    public DataType getType() {
        return type;
    }
}
