package fyp.robot.test;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;

public class MotorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			DifferentialPilot pilot = new DifferentialPilot(5.5f, 12.4f, Motor.C, Motor.B);
			//pilot.setTravelSpeed(400);
			//pilot.setRotateSpeed(50);
			//pilot.rotate(90);
			//pilot.travel(100);
			//pilot.rotate(-90);
			//pilot.rotate(360);
			pilot.setTravelSpeed(15);
			pilot.setRotateSpeed(20);
			//pilot.forward();
			pilot.rotateRight();
			
			Button.waitForAnyPress();
		} catch (Exception e) {
			// TODO: handle exception
			LCD.drawString("test", 0, 0);
			LCD.drawString(e.getMessage(), 0, 1);
		}
	}

}
