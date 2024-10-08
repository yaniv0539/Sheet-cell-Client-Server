package expression.impl;
import expression.api.Data;
import expression.api.DataType;

import java.util.Arrays;

public class RawString extends ExpressionImpl {

    private String value;

    public RawString(String value) {
        this.value = value;
        setDataType(DataType.STRING);
        //isValidArgs(value);
    }

    @Override
    public Data evaluate() {

        return new DataImpl(DataType.STRING, (String)this.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean isValidArgs(Object... args) {
        try{
            Arrays
                    .stream(args)
                    .map(String.class::cast);
        }
        catch(ClassCastException e) {
            throw new IllegalArgumentException("arguments must be String in " + this.getClass().getSimpleName());
        }


        return true;
    }
}
