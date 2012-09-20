package fyp.robot.test;

import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class LightSensorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LightSensor lightSensor = new LightSensor(SensorPort.S3);
		while(true){
			int light = lightSensor.getLightValue();
			LCD.drawInt(light, 0, 0);
		}
	}

}
