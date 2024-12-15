# Report on the `RunTests` Class

## Overview
The `RunTests` class is designed to automatically execute tests defined in a specified class using annotations (`@Testable` and `@Specification`). It leverages **Java reflection** to identify and invoke annotated methods, and compares the results obtained with the expected outcomes.

## Key Features

### 1. **Dynamic Class Loading**
- The specified class is loaded dynamically using `Class.forName`.
- Methods annotated with `@Testable` are identified using `getDeclaredMethods`.

### 2. **Test Execution**
- Methods annotated with `@Testable` are executed.
- The `@Specification` annotation defines the argument types, argument values, and expected results for each test.

### 3. **Result Validation**
- The method results are validated against the expected values using the `verifyResult` method.
- Supports data types such as `int`, `double`, `boolean`, and `string`.

### 4. **Output Comparison**
- Test outputs are saved to a temporary file (`temp_output.txt`).
- The temporary file is compared against a reference file (`RunTests_<ClassName>.output`) using `Files.readAllLines`.

### 5. **Temporary File Cleanup**
- After execution, the temporary file is deleted to maintain a clean environment.

## Example Usage
To execute tests for a class, compile the necessary files and run the `RunTests` program:
```bash
javac RunTests.java MathOpsTests.java
java RunTests MathOpsTests
