package fyp.robot.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 
 * 	Tracking a line
 */

import lejos.robotics.navigation.DifferentialPilot;

public class LineTracker {
	/**
	 * @uml.property  name="blackThreshold"
	 */
	private final int blackThreshold = 32;
	/**
	 * @uml.property  name="whiteThreshold"
	 */
	private final int whiteThreshold = 40;
	/**
	 * @uml.property  name="initialDegree"
	 */
	private final int initialDegree = 10;
	/**
	 * @uml.property  name="inc"
	 */
	private final int inc = 5;
	
	public void execute() {
		
		Robot robot = Robot.getInstance();
		resetMotor();
		
		robot.getPilot().setTravelSpeed(3);
		
		while(true){
			int light = robot.getLightSensor().getLightValue();
			System.out.println("light reading : " + light);
			
			// find the black tape
			if(light<blackThreshold){
				
				// move forward
				setMotorMove();
			}
			// find the white tag
			else if(light>whiteThreshold){
				System.out.println("find white tag");
				
				// stop motor
				resetMotor();
				break;
			}
			//  not find black or white, rotate to find a black tape
			else{
				System.out.println("rotate");
				// rotate
				setMotorRotate();
			}
		}
		
		robot.resetMotor();
		robot.resetMotorSpeed();
	}	
	
/*	public void execute() {
		
		Robot robot = Robot.getInstance();
		resetMotor();
		
		robot.getPilot().setTravelSpeed(3);
		
		while(true){
			int light = robot.getLightSensor().getLightValue();
			System.out.println("light reading : " + light);
			if(light>39){
				setMotorMove();
			}
			else if(light<30){
				System.out.println("find white note");
				resetMotor();
				break;
			}
			else{
				System.out.println("rotate");
				setMotorRotate();
			}
		}
	}	*/
	
	
	private void setMotorRotate(){
		
		Robot robot = Robot.getInstance();
		
		int degree = initialDegree; // the increment degree each rotation
		resetMotor();
		
		// if the motor is rotating, wait until it stop
		while(robot.getRightMotor().isMoving()
				||robot.getLeftMotor().isMoving())
			;
		while(true){
			
			System.out.println("line tracking degree: " + degree);
			
			if(rotateAndRead(degree)){
				return;
			}
			
			if(rotateAndRead(-degree)){
				return;
			}
			
			degree += inc;
			
			if(degree > 180){  // in case of turing around
				degree = 0;
				return;
			}
		}
	}	
	
	private boolean rotateAndRead(int degree) {
		
		Robot robot = Robot.getInstance();
		DifferentialPilot pilot = robot.getPilot();
		
		pilot.rotate(degree, true);
		
		while(pilot.isMoving()){
			int light = robot.getLightSensor().getLightValue();
			
			if(light<blackThreshold || light>whiteThreshold){
				pilot.stop();
				return true;
			}
		}
		pilot.rotate(-degree);
	
		return false;
	}	
	
/*	private boolean rotateAndRead(int degree) {
		
		Robot robot = Robot.getInstance();
		DifferentialPilot pilot = robot.getPilot();
		
		pilot.rotate(degree, true);
		
		while(pilot.isMoving()){
			int light = robot.getLightSensor().getLightValue();
			
			if(light>39 || light<30){
				pilot.stop();
				return true;
			}
		}
		pilot.rotate(-degree);
	
		return false;
	}		
*/	
	
	private void resetMotor() {
		Robot.getInstance().resetMotor();
	}
	
	private void setMotorMove(){
		Robot.getInstance().getPilot().forward();
	}		
}
