package fyp.robot.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 
 * 	Rotate to the correct position for projection purpose
 */


import lejos.robotics.mapping.NavigationModel.NavEvent;

public class PositionController extends Thread{
	
	private static final double x_threshold = 655f;
	private static final double y_threshold = 0;
	private static final double z_threshold = 0;
	public static final double precision = 20f;
	public static final int stepLength = 2;
	/**
	 * @uml.property  name="isAtPosition"
	 */
	private boolean isAtPosition;
	/**
	 * @uml.property  name="d" multiplicity="(0 -1)" dimension="1"
	 */
	private double d[];
	public static final int len = 3;
	/**
	 * @uml.property  name="degreeRotated"
	 */
	private double degreeRotated;
	
	private double armMotorDegreeRotated;
	
	/**
	 * @uml.property  name="mode"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private ADJUST_POSITION_MODE mode;
	/**
	 * @uml.property  name="hasModified"
	 */
	private boolean hasModified;
	
	/**
	 * @author   Jinwu
	 */
	public enum ADJUST_POSITION_MODE{
		/**
		 * @uml.property  name="sPIN"
		 * @uml.associationEnd  
		 */
		SPIN, /**
		 * @uml.property  name="move"
		 * @uml.associationEnd  
		 */
		Move, /**
		 * @uml.property  name="sTOP"
		 * @uml.associationEnd  
		 */
		STOP
	}
	
	public PositionController(){
		
		mode = ADJUST_POSITION_MODE.SPIN;
		
		d = new double[3];
	}
	
	private void setRobotSpeed(){
		Robot robot = Robot.getInstance();
		
		robot.getPilot().setTravelSpeed(3);
		
		robot.getPilot().setRotateSpeed(20);		
	}
	
	public void restoreArmAngle(){
		Robot.getInstance().getArmMotor().rotate((int)(-armMotorDegreeRotated));
	}
	
	public double[] getDoubleArray(){
		return d;
	}
	
	public void setDoubleArray(double[] array){
		synchronized (this) {
			hasModified = true;
			d = array;
		}
		try{
			Thread.sleep(100);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * @param mode
	 * @uml.property  name="mode"
	 */
	public void setMode(ADJUST_POSITION_MODE mode){
		synchronized (this) {
			this.mode = mode;
		}
	}
	
	/**
	 * @return
	 * @uml.property  name="mode"
	 */
	public ADJUST_POSITION_MODE getMode(){
		return mode;
	}
	
	/**
	 * @return
	 * @uml.property  name="degreeRotated"
	 */
	public double getDegreeRotated(){
		return degreeRotated;
	}
	
	public void run(){
		setRobotSpeed();
		
		spin();
		double xx, yy, zz;
		while(true){
			if(hasModified == true){
				synchronized (this) {
					xx = d[0];
					yy = d[1];
					zz = d[2];
				}	
				System.out.println("x value: " + xx + " " + "z value: " + zz);
				
				if(Math.abs(x_threshold-xx)<precision 
						&& Math.abs(z_threshold-zz)<precision){
					break;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		rotateRobotArm(xx, yy);
		
		degreeRotated = Robot.getInstance().getPilot().getAngleIncrement();
		
		// must be called after function finished
		Robot.getInstance().resetMotorSpeed();
		
		// projection finished
		Handler.getInstance().sendEvent(NavEvent.MOVE_STOPPED);
	}
	
	// rotate the robot arm
	public void rotateRobotArm(double xx, double yy){
		double angle = Math.atan((yy-200)/xx);
		
		angle = Math.toDegrees(angle);
		System.out.println("before rotate");
		System.out.println("the angle: " + angle);
		
		armMotorDegreeRotated = angle;
		
		Robot.getInstance().getArmMotor().rotate((int)angle);		
	}
	
	// we might not need findCorrectiPosition
/*	public void run(){
		ADJUST_POSITION_MODE lastMode = null;
		
		while(true){
			ADJUST_POSITION_MODE currentMode = mode; 
			if(currentMode == ADJUST_POSITION_MODE.SPIN){
				if(lastMode == currentMode){
					continue;
				}
				spin();
			}
			else if(currentMode == ADJUST_POSITION_MODE.Move){
				isAtPosition = true;
				
				findCorrectPosition();
				
				if(isAtPosition){
					setMode(ADJUST_POSITION_MODE.STOP);
					break;
				}
				
				if(!dataExchange.hasModified){
					dataExchange.setCommand(CMD.SPIN);
					resetMotor();
				}
			}
			else if(currentMode == ADJUST_POSITION_MODE.STOP){
				break;
			}
		}
	}*/
	
	private void spin(){
		resetMotor();
		Robot robot = Robot.getInstance();
		robot.getPilot().setRotateSpeed(20);
		//robot.getPilot().rotate(90,true);
		robot.getPilot().rotateLeft(); // or rotate right. depends
		System.out.println("after set spin");
		//robot.getLeftMotor().setSpeed(30);
		//robot.getLeftMotor().forward();
	}	
	
/*	private void findCorrectPosition() {
		double x,z;
		
		synchronized (this) {
			x=d[0];
			//y=dataExchange.d[1];
			z=d[2];
			if(!hasModified){
				return;
			}
			hasModified = false;
		}
		
		resetMotor();
		
		driveTowards(x, z);
	}*/
	
	private void resetMotor() {
		Robot.getInstance().resetMotor();
	}	
	
/*	private void driveTowards(double x,double z){
		//double degree = Math.atan2(Math.abs(z - zz),Math.abs(x-xx));
		//double angle = degree * 180 / Math.PI;
		//angle = angle*4;
		
		Robot robot = Robot.getInstance();
		
		double angle;
		if(Math.abs(z-zz)>precision){
			isAtPosition=false;
			if(z>zz){
				angle = -90;
			}
			else{
				angle = 90;
			}
			robot.getPilot().rotate(angle);
			robot.getPilot().travel(stepLength);
			robot.getPilot().rotate(-angle);
		}
		else if(Math.abs(x-xx)>precision){
			isAtPosition=false;
			if(x>xx){
				robot.getPilot().travel(stepLength);
			}
			else {
				robot.getPilot().travel(-stepLength);
			}
		}
		try{
			Thread.sleep(500);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}	*/
}
