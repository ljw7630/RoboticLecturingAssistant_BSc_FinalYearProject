package fyp.robot.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 
 * 	encapsulate all robot control method
 */

import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.FixedRangeScanner;
import lejos.robotics.RangeFinder;
import lejos.robotics.RangeScanner;
import lejos.robotics.navigation.DifferentialPilot;


public class Robot {
	/**
	 * @uml.property  name="pilot"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private DifferentialPilot pilot;
	/**
	 * @uml.property  name="sonic"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private RangeFinder sonic;
	/**
	 * @uml.property  name="scanner"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private RangeScanner scanner;
	/**
	 * @uml.property  name="leftMotor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private NXTRegulatedMotor leftMotor = Motor.C;
	/**
	 * @uml.property  name="rightMotor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private NXTRegulatedMotor rightMotor = Motor.B;
	/**
	 * @uml.property  name="armMotor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private NXTRegulatedMotor armMotor = Motor.A;
	/**
	 * @uml.property  name="ultrasonicSensorPort"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private SensorPort ultrasonicSensorPort = SensorPort.S4;
	/**
	 * @uml.property  name="lineSensorPort"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private SensorPort lineSensorPort = SensorPort.S3;
	/**
	 * @uml.property  name="lightSensor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private LightSensor lightSensor;
	
	private double originTravelSpeed;
	private double originRotateSpeed;
	
	private static Robot robotInstance;
	
	public static Robot getInstance(){
		if(robotInstance == null){
			robotInstance = new Robot();
		}
		return robotInstance;
	}
	
	// setup params
	private Robot(){
		pilot = new DifferentialPilot(5.5f, 12.4f, leftMotor, rightMotor);
		sonic = new UltrasonicSensor(ultrasonicSensorPort);
		originTravelSpeed = 15;
		originRotateSpeed = 20;
		pilot.setTravelSpeed(originTravelSpeed);
		pilot.setRotateSpeed(originRotateSpeed);
		scanner = new FixedRangeScanner(pilot, sonic);		
		lightSensor = new LightSensor(lineSensorPort);
	}	
	
	public void resetMotorSpeed() {
		pilot.setTravelSpeed(originTravelSpeed);
		pilot.setRotateSpeed(originRotateSpeed);
	}
	
	public void resetMotor() {
		robotInstance.getRightMotor().resetTachoCount();
		robotInstance.getLeftMotor().resetTachoCount();
		robotInstance.getRightMotor().stop();
		robotInstance.getLeftMotor().stop();
	}
	
	public void setAngles(float[] angles){
		scanner.setAngles(angles);
	}
	
	/**
	 * @return
	 * @uml.property  name="pilot"
	 */
	public DifferentialPilot getPilot() {
		//System.out.println("get pilot is called");
		return pilot;
	}


	/**
	 * @return
	 * @uml.property  name="sonic"
	 */
	public RangeFinder getSonic() {
		return sonic;
	}


	/**
	 * @return
	 * @uml.property  name="scanner"
	 */
	public RangeScanner getScanner() {
		return scanner;
	}


	/**
	 * @return
	 * @uml.property  name="leftMotor"
	 */
	public NXTRegulatedMotor getLeftMotor() {
		return leftMotor;
	}


	/**
	 * @return
	 * @uml.property  name="rightMotor"
	 */
	public NXTRegulatedMotor getRightMotor() {
		return rightMotor;
	}

	/**
	 * @return
	 * @uml.property  name="armMotor"
	 */
	public NXTRegulatedMotor getArmMotor() {
		return armMotor;
	}

	/**
	 * @return
	 * @uml.property  name="lightSensor"
	 */
	public LightSensor getLightSensor() {
		return lightSensor;
	}

}
