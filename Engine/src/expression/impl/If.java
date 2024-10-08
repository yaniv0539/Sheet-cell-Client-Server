package expression.impl;

import expression.api.Data;
import expression.api.DataType;
import expression.api.Expression;

public class If extends ExpressionImpl {

    Expression condition;
    Expression thenExpression;
    Expression elseExpression;

    public If(Expression condition, Expression thenExpression, Expression elseExpression) {
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
       // setDataType(this.evaluate().getType());
    }

    @Override
    public Data evaluate() {

        Data data;

        if(condition.evaluate().getType() == DataType.BOOLEAN && thenExpression.evaluate().getType() == elseExpression.evaluate().getType()) {
            DataType type = thenExpression.getType();

            data = (boolean) condition.evaluate().getValue() ? new DataImpl(thenExpression.getType(), type.cast(thenExpression.evaluate().getValue()))
                    : new DataImpl(elseExpression.getType(), type.cast(elseExpression.evaluate().getValue()));
        }
        else {
            data = new DataImpl(DataType.UNKNOWN,DataType.UNKNOWN);
        }

        return data;
    }

    @Override
    public boolean isValidArgs(Object... args) {
        return false;
    }
}
