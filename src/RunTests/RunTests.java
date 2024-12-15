package RunTests;
import Testable.*;
import AdditionalCode.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.lang.annotation.*;

public class RunTests {

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

    private static Object parseValue(String type, String value) {
        switch (type) {
            case "int": return Integer.parseInt(value);
            case "double": return Double.parseDouble(value);
            case "bool": return Boolean.parseBoolean(value);
            case "string": return value;
            default: return null;
        }
    }

    private static Class<?> getClassForType(String type) {
        switch (type) {
            case "int": return Integer.class;
            case "double": return Double.class;
            case "bool": return Boolean.class;
            case "string": return String.class;
            default: throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }


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

    private static boolean verifyType(String resType, Object result) {
        if(result == null && resType.isEmpty()) return true;
        return getClassForType(resType).isInstance(result);
    }


    private static boolean compareFiles(String file1, String file2) throws IOException {
        List<String> file1Lines = Files.readAllLines(Path.of(file1));
        List<String> file2Lines = Files.readAllLines(Path.of(file2));
        return file1Lines.equals(file2Lines);
    }
}
