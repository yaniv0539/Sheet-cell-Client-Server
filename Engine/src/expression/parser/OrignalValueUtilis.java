package expression.parser;

import expression.api.DataType;
import expression.api.Expression;
import expression.impl.BooleanExpression;
import expression.impl.NonValueExpression;
import expression.impl.Number;
import expression.impl.RawString;
import operation.Operation;
import sheet.api.SheetGetters;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.range.api.Range;
import sheet.range.api.RangeGetters;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class OrignalValueUtilis {


    private static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private static Expression primitiveParseToExpression(String inputToCell) {

        if (isBoolean(inputToCell)) {
            return new BooleanExpression(Boolean.parseBoolean(inputToCell));
        } else if (isNumeric(inputToCell)) {
            return new Number(Double.parseDouble(inputToCell));
        } else {
            return new RawString(inputToCell);
        }

    }

    //this function need to get trimmed expression already !!!!!
    public static Expression toExpression(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        if(input.isEmpty())
        {
            return new NonValueExpression();
        }

        if (input.startsWith("{") && input.endsWith("}")) {

            String functionContent = input.substring(1, input.length() - 1);

            for (char c : functionContent.toCharArray()) {
                if (c == '{') {
                    stack.push(c);
                } else if (c == '}') {
                    stack.pop();
                }

                if (c == ',' && stack.isEmpty()) {
                    // If we are at a comma and the stack is empty, it's a separator for top-level parts
                    parts.add(buffer.toString());
                    buffer.setLength(0); // Clear the buffer for the next part
                } else {
                    buffer.append(c);
                }
            }

            // Add the last part
            if (!buffer.isEmpty()) {
                parts.add(buffer.toString());
            }

            if (parts.isEmpty()) {
                throw new IllegalArgumentException("Invalid operation: " + "\n"
                        +"Supported operations: "+ Arrays.toString(Operation.values()));
            }

            String functionName = parts.getFirst().trim().toUpperCase();
            parts.removeFirst();

            Stream<Object> listOfArg = Stream.of(parts.toArray()).map(argument ->  toExpression((String) argument));

            try {
                Operation.valueOf(functionName);
            } catch(IllegalArgumentException e){
                throw new IllegalArgumentException("Invalid operation: " + functionName + "\n"
                +"Supported operations: "+ Arrays.toString(Operation.values()));
            }

           return Operation.valueOf(functionName).create(listOfArg.toArray());

        } else {
            //number, string, boolean
            return primitiveParseToExpression(input);
        }

    }

    public static Set<Coordinate> findInfluenceFrom(String value, SheetGetters sheetGetters)
    {
        Set<Coordinate> cellDependence = new HashSet<>();
        Set<String> validFirstStrings = Set.of("sum", "average"); //should be in expression.

        value = value.toUpperCase();
        // Define the regex pattern to match the structure and capture the value after the comma
        Pattern pattern = Pattern.compile("\\{REF,\\s*([A-Z]\\d+)\\}");

        // Create a matcher for the input string
        Matcher matcher = pattern.matcher(value);

        // Find all matches in the string
        while (matcher.find()) {
            String extractedValue = matcher.group(1);
            cellDependence.add(CoordinateFactory.toCoordinate(extractedValue));
        }

        String regex = "\\{([a-zA-Z]+),(.+)\\}";// {sum,grades}

        // Compile the regex and match against the input
         pattern = Pattern.compile(regex);
         matcher = pattern.matcher(value);

        // If it matches the pattern
        if (matcher.matches()) {
            String firstString = matcher.group(1);
            String secondString = matcher.group(2);

            // Check if the first string is within the valid set
            if (validFirstStrings.contains(firstString.toLowerCase())) {
                Expression rangeName = toExpression(secondString);

                if(rangeName.getType() == DataType.STRING){

                    String value1 = (String) rangeName.evaluate().getValue();
                    RangeGetters r = sheetGetters.getRange(value1);
                    if (r != null) {
                        cellDependence.addAll(r.toCoordinateCollection());
                                //.stream()
                                //.map(sheetGetters::getCell)
                                //.filter(Objects::nonNull)
                               //.filter(cell -> cell.getEffectiveValue().getType() == DataType.NUMERIC)
                               // .map(CellGetters::getCoordinate)
                               // .collect(Collectors.toSet()));// Extract the second string

                    }
                }

            }
        }

        return cellDependence;
    }
}
