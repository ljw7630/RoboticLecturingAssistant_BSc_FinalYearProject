package fyp.robot.test;

import fyp.robot.test.DataExchange.CMD;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

public class AdjustPosition extends Thread {
	
	public static final double xx = 700;
	public static final double yy = 0;
	public static final double zz = 0;
	public static final double precision = 20;
	public static final int stepLength = 2;
	
	private boolean isAtPosition;
	public volatile boolean threadStop = false;
	private static final NXTRegulatedMotor leftMotor = Motor.C;
	private static final NXTRegulatedMotor rightMotor = Motor.B;
	
	// test pilot
	DifferentialPilot pilot;
	
	private DataExchange dataExchange;
	
	public AdjustPosition(DataExchange dataExchange) {
		this.dataExchange = dataExchange;
		
		// test algorithm
		pilot = new DifferentialPilot(5.5f, 12.4f, leftMotor, rightMotor);
		pilot.setTravelSpeed(3);
		pilot.setRotateSpeed(20);
	}
	
	public void run(){
		CMD lastStatus = null;
		while(true){
			CMD currentStatus = dataExchange.getCommand();
			if(currentStatus==CMD.SPIN){
				
				// debug
				LCD.drawString("in spin", 0, 4);
				
				if(lastStatus == currentStatus){
					continue;
				}
				spin();
			}
			else if(currentStatus==CMD.AdjustPosition){
				
				// debug
				LCD.drawString("in find pos", 7, 4);
				
				isAtPosition = true;
				
				findCorrectPosition();
				if(isAtPosition){
					dataExchange.setCommand(CMD.STOP);
					break;
				}
				if(!dataExchange.hasModified){
					dataExchange.setCommand(CMD.SPIN);
					resetMotor();
				}
			}
			else if(currentStatus==CMD.STOP){
				break;
			}
			lastStatus = currentStatus;
		}
		LCD.drawString("finally", 0, 0);
		for (int i = 0; i < dataExchange.d.length; i++) {
			LCD.drawString(Double.toString(dataExchange.d[i]), 0, i+4);
		}
		Button.ENTER.waitForPress();
	}
	
	private void resetMotor() {
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		leftMotor.stop();
		rightMotor.stop();
	}
	
	private void spin(){
		leftMotor.setSpeed(30);
		leftMotor.forward();
	}

	private void findCorrectPosition() {
		double x,z;
		
		synchronized (dataExchange) {
			x=dataExchange.d[0];
			//y=dataExchange.d[1];
			z=dataExchange.d[2];
			if(!dataExchange.hasModified){
				return;
			}
			dataExchange.hasModified = false;
		}
		
		resetMotor();
		
		//rightMotor.setSpeed(30);
		//leftMotor.setSpeed(30);
		
/*		if(Math.abs(x-xx)>precision){
			isAtPosition =false;
			
			// debug
			LCD.drawString("deal with x", 0, 5);
			
			// forward
			if(x>xx){
				//rightMotor.forward();
				//leftMotor.forward();
				rightMotor.rotate(30);
				leftMotor.rotate(30);
			}
			// backward
			else{
				//rightrightMotorackward();
				//leftMotor.backward();
				rightMotor.rotate(-30);
				leftMotor.rotate(-30);
			}
			
		}
		
		resetMotor();
		
		if(Math.abs(z-zz)>precision){
			
			// debug
			LCD.drawString("deal with z", 0, 5);
			
			isAtPosition =false;
			
			// from left to right
			if(z>zz){
				rotate(leftMotor, rightMotor);
			}
			// from right to left
			else {
				rotate(rightMotor, leftMotor);
			}
		}*/
		
		driveTowards(x, z);
	}
	
	private void driveTowards(double x,double z){
		//double degree = Math.atan2(Math.abs(z - zz),Math.abs(x-xx));
		//double angle = degree * 180 / Math.PI;
		//angle = angle*4;
		
		double angle;
		if(Math.abs(z-zz)>precision){
			isAtPosition=false;
			if(z>zz){
				angle = -90;
			}
			else{
				angle = 90;
			}
			pilot.rotate(angle);
			pilot.travel(stepLength);
			pilot.rotate(-angle);
		}
		else if(Math.abs(x-xx)>precision){
			isAtPosition=false;
			if(x>xx){
				pilot.travel(stepLength);
			}
			else {
				pilot.travel(-stepLength);
			}
		}
		try{
			Thread.sleep(500);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
/*	private void rotate(NXTRegulatedMotor m1,NXTRegulatedMotor m2) {
		
		LCD.drawString("in rotate", 0, 6);
		int degree = 180;
		try {
			m1.rotateTo(degree,true);
			Thread.yield();
			if(threadStop){
				resetMotor();
				return;
			}
			m2.stop();
			
			m1.rotate(30,true);
			m2.rotate(30,true);
			Thread.yield();
			
			if(threadStop){
				resetMotor();
				return;
			}			
		} catch (Exception e) {
		}
		m1.resetTachoCount();
		m1.rotateTo(-degree,true);
		m2.stop();
		
		LCD.drawString("out rotate", 0, 6);
	}*/
}
