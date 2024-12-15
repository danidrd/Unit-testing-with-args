package RunTests;
import Testable.*;
import AdditionalCode.*;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class RunTests {

    /**
     * Main entry point for running tests.
     * Usage: java RunTests <className>
     * where <className> is the name of the class containing the tests.
     * The output of the tests is compared against a reference file
     * "RunTests_<className>.output" and the result is printed to the console.
     * The reference file is expected to be in the same package as the class
     * being tested.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java RunTests <className>");
            return;
        }

        String className = args[0];
        String outputFileName = "RunTests_" + className + ".output";
        String tempOutputFile = "temp_output.txt";
        try (PrintStream originalOut = System.out;
             PrintStream fileOut = new PrintStream(new FileOutputStream(tempOutputFile))) {

            // Reindirizza l'output sul file temporaneo
            System.setOut(fileOut);

            // Carica la classe con Reflection


            Class<?> clazz = Class.forName("AdditionalCode." + className);
            Object instance = clazz.getConstructor().newInstance();

            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Testable.class)) {
                    Specification spec = method.getAnnotation(Specification.class);
                    if (spec != null) {
                        runTest(method, instance, spec);
                    }
                }
            }

            // Ripristina il System.out originale
            System.setOut(originalOut);

            // Confronta il file temporaneo con l'output di riferimento
            if (compareFiles(tempOutputFile, outputFileName)) {
                System.out.println("Output matches reference file: " + outputFileName);
            } else {
                System.out.println("Output differs from reference file: " + outputFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Elimina il file temporaneo
            try {
                Files.deleteIfExists(Paths.get(tempOutputFile));
            } catch (IOException e) {
                System.err.println("Failed to delete temporary output file: " + tempOutputFile);
            }
        }
    }
    /**
     * Runs a test method with the given arguments and expected result.
     *
     * The method is invoked with the parsed arguments and the result is compared
     * with the expected result. If the result matches, the test is reported as
     * succeeded, otherwise it is reported as failed.
     *
     * @param method the method to be tested
     * @param instance the instance to be used for invoking the method
     * @param spec the specification of the test
     */
    private static void runTest(Method method, Object instance, Specification spec) {
        try {
            // Parsing degli argomenti
            Object[] parsedArgs = parseArguments(spec.argTypes(), spec.argValues());
            if (parsedArgs == null) {
                Report.report(Report.TEST_RESULT.WrongArgs, method.getName(), spec);
                return;
            }

            // Invocazione del metodo
            Object result = method.invoke(instance, parsedArgs);

            // Controllo del risultato atteso

            if (!verifyType(spec.resType(), result)) {
                Report.report(Report.TEST_RESULT.WrongResultType, method.getName(), spec);
            } else if(verifyResult(result, spec.resType(), spec.resVal())) {
                Report.report(Report.TEST_RESULT.TestSucceeded, method.getName(), spec);
            } else {
                Report.report(Report.TEST_RESULT.TestFailed, method.getName(), spec);
            }

        } catch (IllegalArgumentException e) {
            Report.report(Report.TEST_RESULT.WrongArgs, method.getName(), spec);
        } catch (InvocationTargetException | IllegalAccessException e) {
            Report.report(Report.TEST_RESULT.TestFailed, method.getName(), spec);
        }
    }

    /**
     * Parses an array of arguments given as strings into an array of objects.
     * The types of the arguments are given as an array of strings, and the
     * parser will attempt to parse each value as an argument of the specified
     * type.
     * <p>
     * If the length of the types and values arrays do not match, or if the
     * parsing fails for any argument, the method returns null.
     * <p>
     * Supported types are: int, double, bool, string.
     * @param types the types of the arguments, given as an array of strings
     * @param values the values of the arguments, given as an array of strings
     * @return an array of object arguments, or null if the parsing fails
     */
    private static Object[] parseArguments(String[] types, String[] values) {
        if (types.length != values.length) return null;
        try {
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                args[i] = parseValue(types[i], values[i]);
            }
            return args;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a string value into an object of the specified type.
     *
     * Supported types are: int, double, bool, string.
     *
     * @param type the type to parse the value into, represented as a string
     * @param value the string value to be parsed
     * @return the parsed object corresponding to the given type, or null if the type is unsupported
     * @throws NumberFormatException if the value cannot be parsed to the specified numeric type
     */
    private static Object parseValue(String type, String value) {
        switch (type) {
            case "int": return Integer.parseInt(value);
            case "double": return Double.parseDouble(value);
            case "bool": return Boolean.parseBoolean(value);
            case "string": return value;
            default: return null;
        }
    }

    /**
     * Given a type name as a string, returns the corresponding class object.
     * <p>
     * Supported types are: int, double, bool, string.
     * <p>
     * If the type is not recognized, an {@link IllegalArgumentException} is thrown.
     * @param type the type name as a string
     * @return the corresponding class object
     * @throws IllegalArgumentException if the type is not recognized
     */
    private static Class<?> getClassForType(String type) {
        switch (type) {
            case "int": return Integer.class;
            case "double": return Double.class;
            case "bool": return Boolean.class;
            case "string": return String.class;
            default: throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }


    /**
     * Verifies if the given result matches the expected result, given as a string.
     *
     * @param result the object to be verified
     * @param resType the type of the expected result as a string (e.g., "int", "double", "bool", "string")
     * @param resVal the expected result as a string
     * @return true if the result is null and expected value is empty, or if the result is equal to the expected value; false otherwise
     */
    private static boolean verifyResult(Object result, String resType, String resVal) {
        if (result == null && resVal.isEmpty()) return true;
        if (result == null || resType.isEmpty()) return false;
        try {
            Object expected = parseValue(resType, resVal);
            return result.equals(expected);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifies if the given result's type matches the expected type.
     *
     * @param resType the expected type as a string (e.g., "int", "double", "bool", "string")
     * @param result the object whose type is to be verified
     * @return true if the result is null and expected type is empty, or if the result is an instance of the expected type; false otherwise
     */
    private static boolean verifyType(String resType, Object result) {
        if(result == null && resType.isEmpty()) return true;
        return getClassForType(resType).isInstance(result);
    }


    /**
     * Compares the contents of two files, line by line.
     * @param file1 the first file to compare
     * @param file2 the second file to compare
     * @return true if the files are equal, false otherwise
     * @throws IOException if there is an error reading either file
     */
    private static boolean compareFiles(String file1, String file2) throws IOException {
        List<String> file1Lines = Files.readAllLines(Path.of(file1));
        List<String> file2Lines = Files.readAllLines(Path.of(file2));
        return file1Lines.equals(file2Lines);
    }
}
