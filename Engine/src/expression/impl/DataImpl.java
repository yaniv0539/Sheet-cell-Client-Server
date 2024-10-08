package expression.impl;

import expression.api.Data;
import expression.api.DataType;

import java.io.Serializable;
import java.util.Objects;

public class DataImpl implements Data, Serializable {

    public static final String undefiled = "!UNDEIFINED!";
    public static final String BoolUndefiled = "UNKNOWN";
    public static final String empty = "";

    private DataType type;
    private Object value;

    public DataImpl() {}

    public DataImpl(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (type == DataType.NUMERIC) {
            double valueDouble = (double) value;
            int valueInt = (int) valueDouble;
            if (valueInt == valueDouble) {
                return Integer.toString(valueInt);
            }
            else{
                return String.format("%.2f", valueDouble);
            }
        }
        if (type == DataType.BOOLEAN) {
            return value.toString().toUpperCase();
        }
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataImpl data = (DataImpl) o;
        return type == data.type && Objects.equals(value, data.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
