package fyp.robot.test;

import fyp.robot.test.DataExchange.CMD;
import lejos.nxt.*;

public class LineTracking {
	private DataExchange dataExchange;
	private LightSensor lightSensor;
	private final int blackThreshold = 35;
	private final int whiteThreshold = 44;
	private final int initialDegree = 10;
	private final int inc = 10;
	private static final NXTRegulatedMotor leftMotor = Motor.C;
	private static final NXTRegulatedMotor rigtMotor = Motor.B;
	private static final ADSensorPort lineSensorPort = SensorPort.S3;
	
	public LineTracking(DataExchange dataExchange){
		this.dataExchange = dataExchange;
		lightSensor = new LightSensor(lineSensorPort);
	}
	
	public void execute() {
		
		if(dataExchange.getCommand() == CMD.LINETRACKING){
			while(true){
				int light = lightSensor.getLightValue();
				System.out.println("light reading : " + light);
				if(light<blackThreshold){
					setMotorMove();
				}
				else if(light>whiteThreshold){
					System.out.println("find white note");
					resetMotor();
					dataExchange.setCommand(CMD.SPIN);
					LCD.clear();
					break;
				}
				else{
					System.out.println("rotate");
					setMotorRotate();
				}
			}
		}
	}
	
	private void setMotorRotate(){
		int degree = initialDegree; // the increment degree each rotation
		resetMotor();
		while(rigtMotor.isMoving()||leftMotor.isMoving());
		while(true){
			
			LCD.drawInt(degree, 4, 0, 0);
			
			if(rotateAndRead(rigtMotor, degree)){
				return;
			}
			
			if(rotateAndRead(leftMotor, degree)){
				return;
			}
			
			degree += inc;
			
			if(degree > 180){  // in case of turing around
				degree = 0;
				return;
			}
		}
	}
	
	private boolean rotateAndRead(NXTRegulatedMotor motor, int degree) {
		
		motor.rotateTo(degree,true);
		
		//LCD.drawInt(rigtMotor.getTachoCount(), 4, 0, 1);
		
		while(motor.isMoving()){
			int light = lightSensor.getLightValue();
			
			//LCD.drawInt(light, 4, 0, 3);
			
			if(light<blackThreshold || light>whiteThreshold){
				motor.stop();
				return true;
			}
		}
		motor.rotateTo(0);
		
		LCD.drawInt(motor.getTachoCount(), 4, 0, 1);
		return false;
	}

	private void resetMotor() {
		rigtMotor.resetTachoCount();
		leftMotor.resetTachoCount();
		rigtMotor.stop();
		leftMotor.stop();
	}
	
	private void setMotorMove(){
		rigtMotor.setSpeed(100);
		leftMotor.setSpeed(100);
		rigtMotor.forward();
		leftMotor.forward();
	}		
}
