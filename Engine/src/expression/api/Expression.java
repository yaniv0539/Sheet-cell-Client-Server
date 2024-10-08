package expression.api;

public interface Expression {

    Data evaluate();
    boolean isValidArgs(Object...args);
    DataType getType();
}
