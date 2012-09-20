package fyp.robot.test;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.util.Vector;

import lejos.nxt.*;
import lejos.robotics.*;

import lejos.robotics.mapping.NavigationModel.NavEvent;
import lejos.robotics.navigation.DifferentialPilot;

import lejos.robotics.navigation.Waypoint;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;


public class RobotMovement {

	/**
	 * @uml.property  name="newMessage"
	 */
	private boolean newMessage;
	/**
	 * @uml.property  name="running"
	 */
	private boolean running;
	/**
	 * @uml.property  name="receiver"
	 */
	private Thread receiver;
	/**
	 * @uml.property  name="navEvent"
	 * @uml.associationEnd  
	 */
	private NavEvent navEvent;
	/**
	 * @uml.property  name="waypoint"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Waypoint waypoint = new Waypoint(0, 0);
	/**
	 * @uml.property  name="epsilon"
	 */
	private double epsilon = 1; 
	/**
	 * @uml.property  name="dis"
	 */
	private DataInputStream dis;
	/**
	 * @uml.property  name="dos"
	 */
	private DataOutputStream dos;
	/**
	 * @uml.property  name="connected"
	 */
	private boolean connected = false;
	/**
	 * @uml.property  name="eventVector"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="lejos.robotics.mapping.NavigationModel$NavEvent"
	 */
	private Vector<NavEvent> eventVector;
	
	/**
	 * @uml.property  name="startAngle"
	 */
	private int startAngle;
	/**
	 * @uml.property  name="endAngle"
	 */
	private int endAngle;
	/**
	 * @uml.property  name="incAngle"
	 */
	private int incAngle;
	
	/**
	 * @uml.property  name="pilot"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	DifferentialPilot pilot;
	/**
	 * @uml.property  name="sonic"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	RangeFinder sonic;
	/**
	 * @uml.property  name="scanner"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	RangeScanner scanner;
	/**
	 * @uml.property  name="angles" multiplicity="(0 -1)" dimension="1"
	 */
	float []angles;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RobotMovement robot = new RobotMovement();
		robot.execute();
	}
	
	public RobotMovement(){
		pilot = new DifferentialPilot(5.5f, 12.4f, Motor.C, Motor.B);
		sonic = new UltrasonicSensor(SensorPort.S4);
		pilot.setTravelSpeed(20);
		pilot.setRotateSpeed(30);
		scanner = new FixedRangeScanner(pilot, sonic);
		startAngle = -90;
		endAngle = 90;
		incAngle = 5;
		/*angles = getAngles(-90, 90, 5);
		scanner.setAngles(angles);*/
		eventVector = new Vector<NavEvent>();
		running = true;
		
		receiver = new Thread( new Receiver());
		
		receiver.setDaemon(true);
		receiver.start();
	}
	
	public void execute(){
		while(true){
			if(!running){
				break;
			}
			while(eventVector.isEmpty()){  // if the msg haven't being used by main thread, wait.
				Thread.yield();
			}
			
			synchronized (eventVector) {
				
				for(int i=0;i<eventVector.size();++i){
					NavEvent event = eventVector.elementAt(i);
					switch (event) {
					case GOTO:
						System.out.println("perform goto");
						performGoTO();
						break;
					case TAKE_READINGS:
						System.out.println("perform reading");
						performReadings();
						break;
					default:
						break;
					}
				}
				
				eventVector.clear();
				//setNewMessage(false);
			}
		}
		
		//Button.ENTER.waitForPress();		
	}
	
	private void performReadings(){
		
		angles = getAngles(startAngle, endAngle, incAngle);
		scanner.setAngles(angles);
		
		RangeReadings readings = scanner.getRangeValues();
		sendRangeReadings(NavEvent.RANGE_READINGS, readings);
		
		int tmpAngle = startAngle;
		startAngle = endAngle;
		endAngle = tmpAngle;
		incAngle = -incAngle;
	}
	
	private void performGoTO(){
		double degree = getDegree(waypoint);
		double distance = getDistance(waypoint);
		pilot.rotate(degree);
		pilot.travel(distance);
		pilot.rotate(-degree);
		
		
		// finished, send event
		sendEvent(NavEvent.WAYPOINT_REACHED);
	}
	
	private void sendEvent(NavEvent navEvent){
		try{
			dos.write(navEvent.ordinal());
			dos.flush();
		}catch (Exception e) {
			System.out.println("exception occurred in sendEvent");
			System.out.println(e.getMessage());
		}
	}
	
	private void sendRangeReadings(NavEvent navEvent, RangeReadings readings){
		try{
			dos.write(navEvent.ordinal());
			readings.dumpObject(dos);
			dos.flush();
		}
		catch (Exception e) {
			System.out.println("exception occurred in sendRangeReadings");
			System.out.println(e.getMessage());
		}
	}
	
	private double getDistance(Waypoint waypoint){
		double distance = Math.sqrt(waypoint.x * waypoint.x + waypoint.y * waypoint.y);
		return distance;
	}
	
	
	// maybe we should revert y
	private double getDegree(Waypoint waypoint) {
		
		System.out.println("wp: " + waypoint.x + " " + waypoint.y);
		if(Math.abs(waypoint.y)<epsilon){
			if(waypoint.x > 0){
				return -90;
			}
			else {
				return 90;
			}
		}
		if(waypoint.y>0){
			if(waypoint.x<0){
				return 180 - getDegree(Math.atan(-waypoint.x/waypoint.y));
			}
			else {
				return -180 + getDegree(Math.atan(waypoint.x/waypoint.y)); 
			}
		}
		else
		{
			return -getDegree(Math.atan(waypoint.x/(-waypoint.y)));
		}
	}
	
	private double getDegree(double radians) {
		return radians * 180 / Math.PI;
	}
	
	/**
	 * @param b
	 * @uml.property  name="newMessage"
	 */
	private synchronized void setNewMessage(boolean b){
		newMessage = b;
	}
	
	private boolean getNewMessage(){
		return newMessage;
	}
	
	public static float[] getAngles(float min, float max, float gap){
		
		float []angles = new float[(int) (( (max - min) / gap) + 1)];
		float angle = max;
		for(int i=0;i<angles.length;++i){
			angles[i] = angle;
			angle -= gap;
		}
		return angles;
	}
	
	
	// potential problem: input stream was full of unread msgs. we need to disable "send command"
	// on pc until robot send "finish"
	class Receiver implements Runnable{
		public Receiver(){
			newMessage = false;
		}
		public void run(){
			System.out.println("waiting");
			BTConnection conn = Bluetooth.waitForConnection();
			LCD.refresh();
			connected = true;
			System.out.println("connected");
			dis = conn.openDataInputStream();
			dos = conn.openDataOutputStream();
			while(running){
				try {
					while(!eventVector.isEmpty()){  // if the msg haven't being used by main thread, wait.
						Thread.yield();
					}
					byte event = dis.readByte();
					navEvent = NavEvent.values()[event];
					synchronized (eventVector) {
						switch (navEvent) {
						case GOTO: // go to but not need take readings
							waypoint.loadObject(dis);
							//System.out.println("receive a waypoint");
							break;
						case TAKE_READINGS:
							break;
						default:
							break;
						}
						
						eventVector.addElement(navEvent);
						//setNewMessage(true);						
					}
					System.out.println("receive an event");
				} catch (Exception e) {
					System.out.println("exception in receiver");
					System.exit(0);
				}
			}
		}
	}
}
