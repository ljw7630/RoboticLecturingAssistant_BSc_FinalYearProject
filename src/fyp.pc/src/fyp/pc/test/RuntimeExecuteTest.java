package fyp.pc.test;

import java.io.IOException;

public class RuntimeExecuteTest {
	
	public static void main(String[] args){
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec("./SimpleTest.exe");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
