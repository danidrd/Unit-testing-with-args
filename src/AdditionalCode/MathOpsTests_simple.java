package AdditionalCode;
import Testable.*;

public class MathOpsTests_simple{

	public static String insertPrompt(Object obj){
    	String line = obj.toString();
 		return String.format("==> %s", line);
	}

	@Testable
	@Specification
    public void hello(){
		System.out.println(insertPrompt("Tests ready to run"));
    }

	/* Non va invocato */
	@Specification
	public void wrong0(){
		System.out.println(insertPrompt("***WARNING***: I'm not Testable!"));
    }
	
	@Testable
	@Specification(argTypes={}, argValues = {}, resType = "", resVal = "")
    public void testRound(){
	/* Va invocato: no args no result */
    }

	// correct result	
	@Testable
	@Specification(argTypes={"int","int"}, argValues = {"7","3"}, resType = "int", resVal = "3")
    public int testMin(int a, int b){
	return (a<b) ? a : b;
    }

	// private: should not be called	
	@Testable
	@Specification(argTypes={"int","int"}, argValues = {"7","3"}, resType = "int", resVal = "3")
	private int wrong3(int a, int b){
		System.out.println(insertPrompt("***WARNING***: I'm not public!"));
	return (a<b) ? a : b;
    }

	/* Non va invocato */
	// @Testable
	@Specification(argTypes={}, argValues = {}, resType = "", resVal = "")
	public void wrong1(){
		System.out.println(insertPrompt("***WARNING***: I'm not Testable!"));
    }
	
	/* Non va invocato */
	// @Testable
	@Specification(argTypes={"double"}, argValues = {"3.14"}, resType = "int", resVal = "3")
	public double wrong2(double d){
		System.out.println(insertPrompt("***WARNING***: I'm not Testable!"));
		return d;
    }

	// wrong result
	@Testable
	@Specification(argTypes={"int","int"}, argValues = {"7","3"}, resType = "int", resVal = "7")
    public int testMin1(int a, int b){
	return (a<b) ? a : b;
    }
	
	@Testable
	@Specification(argTypes={"int","int"}, argValues = {"7","3"}, resType = "bool", resVal = "false")
    public boolean isLarger(int a, int b){
	return (a > b);
    }
	
	@Testable
	@Specification(argTypes={"int","int"}, argValues = {"7","3"}, resType = "bool", resVal = "true")
    public boolean isLarger1(int a, int b){
	return (a > b);
    }
	
	@Testable
	@Specification(argTypes={"double"}, argValues = {"3.14"}, resType = "double", resVal = "3.14")
	public double idDouble(double d){
	return d;
}

    private static String testResult(double result, double expected){
	return (result == expected ? "ok" : "no");
    }

    public static void main(String[] args){
    }
}




