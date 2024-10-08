package operation;

import exception.InvalidFunctionArgument;
import expression.api.Expression;
import expression.impl.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public enum Operation {

    PLUS(BinaryExpression.numberOfArgs)
        {
        @Override
        public Expression create(Object... args)
        {
            return (Expression) createInstance(Plus.class, getNumberOfArguments(),args);
        }
    },

    MINUS(BinaryExpression.numberOfArgs) {
        @Override
        public Expression create(Object... args)
        {
            return (Expression) createInstance(Minus.class, getNumberOfArguments(),args);
        }
    },

    TIMES(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return  (Expression) createInstance(Times.class, getNumberOfArguments(),args);
        }
    },

    DIVIDE(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Divide.class, getNumberOfArguments(),args);
        }
    },
    CONCAT(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Concat.class, getNumberOfArguments(),args);
        }
    },
    POW(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Pow.class, getNumberOfArguments(),args);
        }
    },
    MOD(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Mod.class, getNumberOfArguments(),args);
        }
    },
    SUB(3){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Sub.class, getNumberOfArguments(),args);
        }
    },
    ABS(UnaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Abs.class, getNumberOfArguments(),args);
        }
    },
    REF(UnaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Ref.class, getNumberOfArguments(),args);
        }
    },
    EQUAL(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Equal.class, getNumberOfArguments(),args);
        }
    },
    NOT(UnaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Not.class, getNumberOfArguments(),args);
        }
    },
    OR(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Or.class, getNumberOfArguments(),args);
        }
    },
    AND(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(And.class, getNumberOfArguments(),args);
        }
    },
    BIGGER(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Bigger.class, getNumberOfArguments(),args);
        }
    },
    LESS(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Less.class, getNumberOfArguments(),args);
        }
    },
    IF(3){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(If.class, getNumberOfArguments(),args);
        }
    },
    PERCENT(BinaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Percent.class, getNumberOfArguments(),args);
        }
    },
    SUM(UnaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Sum.class, getNumberOfArguments(),args);
        }
    },
    AVERAGE(UnaryExpression.numberOfArgs){
        @Override
        public Expression create(Object... args) {
            return (Expression) createInstance(Average.class, getNumberOfArguments(),args);
        }
    };

    private int numberOfArguments;

    Operation (int args) {
        this.numberOfArguments = args;
    }

    private static Object createInstance(Class<?> clazz, int numberOfArgs, Object... args) {

        return Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == numberOfArgs)
                .findFirst()
                .map(constructor -> {
                    try {
                        constructor.setAccessible(true);
                        return constructor.newInstance(args);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage());
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("input function: " + clazz.getSimpleName().toUpperCase() +"\n" +
                                "input argument: " + Arrays.toString(args) + "\n" + "" +
                                "number of input arguments: " + args.length + "\n" +
                                "expected argument for this function: " + numberOfArgs + "\n" +
                                "Error:\n" + e.getTargetException().getMessage());
                    }catch(Exception e){
                        throw new RuntimeException("input function: " + clazz.getSimpleName().toUpperCase() +"\n" +
                                "input argument: " + Arrays.toString(args) + "\n" + "" +
                                "number of input arguments: " + args.length + "\n" +
                                "expected argument for this function: " + numberOfArgs + "\n" +
                                "Error:\n" + e.getMessage());
                    }
                })
                .get();

    }

    public int getNumberOfArguments() {
        return numberOfArguments;
    }

    public abstract Expression create(Object... args);
}
