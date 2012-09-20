package fyp.robot.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fyp.pc.test.CommObject;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.FixedRangeScanner;
import lejos.robotics.RangeFinder;
import lejos.robotics.RangeReadings;
import lejos.robotics.RangeScanner;
import lejos.robotics.RotatingRangeScanner;
import lejos.robotics.navigation.DifferentialPilot;


public class USClientTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		LCD.drawString("waiting...", 0, 0);
		BTConnection connection = Bluetooth.waitForConnection();
		LCD.refresh();
		LCD.drawString("connected", 0, 0);
		DataOutputStream outputStream = connection.openDataOutputStream();
		DifferentialPilot pilot = new DifferentialPilot(5.5f, 12.4f, Motor.C, Motor.B);
		RangeFinder sonic = new UltrasonicSensor(SensorPort.S4);
		pilot.setTravelSpeed(20);
		pilot.setRotateSpeed(30);
		//Motor.A.setSpeed(30);
		RangeScanner scanner = new FixedRangeScanner(pilot, sonic);
		float []angles = getAngles(-90, 90, 5);

		scanner.setAngles(angles);
		int c = 0;
		while(true){
			RangeReadings readings = scanner.getRangeValues();
		
			CommObject commObject = new CommObject(0, c, readings);
			commObject.dumpObject(outputStream);
			
			float val1 = readings.get(angles.length/2+1).getRange();
			float val2 = readings.get(angles.length/2-1).getRange();
			float val3 = readings.get(angles.length/2).getRange();
			
			// if the value is less that 30, which means there's object there.
			if( (val1 < 0 || val1>30) 
					&& (val2 < 0 || val2>30) 
					&& (val3 < 0 || val3>30)){
				c = 10;
				pilot.travel(c);
			}
			else {
				break;
			}
		}
		//Button.ENTER.waitForPress();
		outputStream.close();
		connection.close();
	}
	private static float[] getAngles(float min, float max, float gap){
		
		float []angles = new float[(int) (( (max - min) / gap) + 1)];
		float angle = max;
		for(int i=0;i<angles.length;++i){
			angles[i] = angle;
			angle -= gap;
		}
		return angles;
	}
}
