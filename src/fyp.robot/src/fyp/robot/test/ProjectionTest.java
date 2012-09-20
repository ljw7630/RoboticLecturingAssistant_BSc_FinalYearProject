package fyp.robot.test;

import java.io.DataInputStream;

import com.sun.org.apache.bcel.internal.generic.IXOR;

import lejos.nxt.Motor;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.navigation.DifferentialPilot;

public class ProjectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("waiting..");
		BTConnection btConnection = Bluetooth.waitForConnection();
		System.out.println("connected");
		DataInputStream dis = btConnection.openDataInputStream();
		
		Robot robot = new Robot();
		
		robot.start();
		
		double d[] =  new double[3];
		
		try{
			while(true){
				for(int i=0;i<3;++i){
					d[i] = dis.readDouble();
				}
				
				robot.setDoubleArray(d);
				
				//System.out.println("set double arr finished");
				
				Thread.sleep(100);
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

}

class Robot extends Thread{
	NXTRegulatedMotor arm;
	
	double doubleArray[];
	
	Robot(){
		arm = Motor.A;
		arm.setSpeed(10);
	}
	
	public void setDoubleArray(double[] d){
		synchronized (this) {
			doubleArray= d;
		}
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			// TODO: handle exception
			
		}
	}
	
	public void run(){
		while(true){
			double xx,yy;
			while(doubleArray == null){
				Thread.yield();
			}
			synchronized (this) {
				xx = doubleArray[0];
				yy = doubleArray[1];
			}
			
			//double angle = Math.atan(yy/(xx/5));
			double angle = Math.atan((yy-200)/xx);
			
			angle = Math.toDegrees(angle);
			System.out.println("before rotate");
			System.out.println("the angle: " + angle);
			arm.rotate((int)angle);
			break;
			/*try{
				Thread.sleep(500);
			}
			catch (Exception e) {
				// TODO: handle exception
			}*/
		}
		System.out.println("end run");
	}
}