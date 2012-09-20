package fyp.robot.test;

import fyp.robot.test.DataExchange.CMD;
import lejos.robotics.subsumption.*;
import lejos.nxt.*;

class LineTrackingBehavior implements Behavior{

	private DataExchange dataExchange;
	private boolean suppressed;
	private LightSensor lightSensor;
	private final int blackThreshold = 35;
	private final int redThreshold = 50;
	
	public LineTrackingBehavior(DataExchange dataExchange){
		this.dataExchange = dataExchange;
		lightSensor = new LightSensor(SensorPort.S3);
	}
	
	@Override
	public void action() {
		suppressed = false;
		
		while(true){
			int light = lightSensor.getLightValue();
			
			if(light<blackThreshold){
				setMotorMove();
			}
			else if(light<redThreshold){
				
			}
			else{
				setMotorRotate();
			}
			
			if(suppressed){
				resetMotor();
				break;
			}
		}
	}

	@Override
	public void suppress() {
		
		suppressed = true;
	}

	@Override
	public boolean takeControl() {
		
		if(dataExchange.getCommand()==CMD.LINETRACKING){
			return true;
		}
		else {
			return false;
		}
	}
	
	private void setMotorRotate(){
		int initDegree = 10; // the increment degree each rotation
		int light;
		resetMotor();
		while(Motor.B.isMoving()||Motor.C.isMoving());
		while(true){
			
			LCD.drawInt(initDegree, 4, 0, 0);
			
			Motor.B.rotateTo(initDegree);
			
			LCD.drawInt(Motor.B.getTachoCount(), 4, 0, 1);
			
			light = lightSensor.getLightValue();
			
			LCD.drawInt(light, 4, 0, 3);
			
			
			if(light<blackThreshold){
				break;
			}
			
			Motor.B.rotateTo(0);
			
			LCD.drawInt(Motor.B.getTachoCount(), 4, 0, 1);
			
			Motor.C.rotateTo(initDegree);
			
			LCD.drawInt(Motor.C.getTachoCount(), 4, 0, 1);
			
			light = lightSensor.getLightValue();
			
			LCD.drawInt(light, 4, 0, 3);
			
			if(light<blackThreshold){
				break;
			}
			Motor.C.rotateTo(0);
			
			LCD.drawInt(Motor.C.getTachoCount(), 4, 0, 1);
			
			initDegree += 10;
			
			if(initDegree > 180)  // in case of turing around
				initDegree = 0;
		}
	}

	private void resetMotor() {
		Motor.B.resetTachoCount();
		Motor.C.resetTachoCount();
		Motor.B.stop();
		Motor.C.stop();
	}
	
	private void setMotorMove(){
		Motor.B.setSpeed(100);
		Motor.C.setSpeed(100);
		Motor.B.forward();
		Motor.C.forward();
	}	
}

class SpinAroundBehavior implements Behavior{

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean takeControl() {
		// TODO Auto-generated method stub
		return false;
	}
	
}