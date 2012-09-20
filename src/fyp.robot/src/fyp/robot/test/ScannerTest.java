package fyp.robot.test;

import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.FixedRangeScanner;
import lejos.robotics.RangeReadings;
import lejos.robotics.RangeScanner;
import lejos.robotics.RotatingRangeScanner;
import lejos.robotics.navigation.DifferentialPilot;
import fyp.robot.stupidSLAM.*;

public class ScannerTest {
	public static void main(String[] args){
		DifferentialPilot pilot = new DifferentialPilot(2.75f, 6.2f, Motor.C, Motor.B);
		UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S4);
		pilot.setTravelSpeed(20);
		pilot.setRotateSpeed(40);
		Motor.A.setSpeed(30);
		RangeScanner scanner = new FixedRangeScanner(pilot, sonic);
		float []angles = RobotMovement.getAngles(-90, 90, 5);
		scanner.setAngles(angles);
		
		for(int i=0;i<4;++i){
			RangeReadings readings = scanner.getRangeValues();
			//pilot.travel(5);
			//pilot.rotate(Math.PI/2,false);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		 
		
/*		try {
			pilot.rotate(90);
			Thread.sleep(500);
			pilot.rotate(90);
			Thread.sleep(500);
			pilot.rotate(90);
			Thread.sleep(500);
			pilot.rotate(90);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
