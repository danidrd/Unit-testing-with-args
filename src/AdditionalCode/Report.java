package AdditionalCode;

import Testable.Specification;
import java.util.Arrays;

public class Report{

    public enum TEST_RESULT{WrongArgs, WrongResultType,TestFailed,TestSucceeded}

    public static String insertPrompt(Object obj){
        String line = obj.toString();
 	    return String.format("=> %s", line);
    }

    public static void report(TEST_RESULT tr, String methodName, Specification s){
		System.out.println(insertPrompt("Testing \"" + methodName +"\" with args " + Arrays.toString(s.argValues())  + " of type " + Arrays.toString(s.argTypes()) + " and result " + s.resVal() + ":" + s.resType() + "..."));
		String res = "";
		switch (tr){
			case WrongArgs: res = "\"" + methodName + "\" not tested: wrong argument list"; break;
			case WrongResultType: res = "\"" + methodName + "\" not tested: wrong result type"; break;
			case TestFailed: res = "\"" + methodName + "\" tested but FAILED"; break;
			case TestSucceeded: res = "\"" + methodName + "\" tested with SUCCESS"; break;
		}
		System.out.println(insertPrompt(res));
	} 
}



