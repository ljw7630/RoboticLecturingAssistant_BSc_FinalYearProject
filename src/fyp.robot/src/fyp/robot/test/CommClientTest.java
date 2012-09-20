package fyp.robot.test;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.RangeReading;
import lejos.robotics.RangeReadings;
import fyp.pc.test.CommObject;;

public class CommClientTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		LCD.drawString("wait", 0, 0);
		BTConnection connection = Bluetooth.waitForConnection();
		DataOutputStream dos = connection.openDataOutputStream();
		try {
			LCD.refresh();
			LCD.drawString("connceted", 0, 0);
			
			RangeReadings  readings= new RangeReadings(1);
			readings.set(0,new RangeReading(55, 55));
			CommObject communicationObject = new CommObject(20, 30, readings);
			LCD.drawInt(readings.getNumReadings(), 0, 1);
			LCD.drawString(readings.get(0).getAngle() + " " + readings.get(0).getRange(), 0, 2);
			communicationObject.dumpObject(dos);			
		} catch (Exception e) {
			LCD.drawString(e.getMessage(), 0, 3);
		}

		dos.close();
		connection.close();
		Button.ENTER.waitForPress();
	}

}
