package fyp.robot.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 
 * 	Main entry point, borrow the idea from LEJOS NXTNavigation model
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.RangeReadings;
import lejos.robotics.mapping.NavigationModel.NavEvent;
import lejos.robotics.navigation.Waypoint;

public class Handler {
	/**
	 * @uml.property  name="slamController"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private SLAMController slamController;
	/**
	 * @uml.property  name="positionController"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private PositionController positionController;
	//private PositionMonitor positionMonitor;
	/**
	 * @uml.property  name="lineTracker"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private LineTracker lineTracker;
	/**
	 * @uml.property  name="dis"
	 */
	private DataInputStream dis;
	/**
	 * @uml.property  name="dos"
	 */
	private DataOutputStream dos;
	/**
	 * @uml.property  name="receiver"
	 */
	private Thread receiver;
	
	/**
	 * @uml.property  name="eventVector"
	 */
	private NavEvent currentEvent;
	
	private boolean connected;
	private Waypoint nextWaypoint;
	private double [] doubleArray;
	
	private static Handler handler;
	private double angle;
	
	public static void main(String[] args){
		Handler.getInstance().execute();
	}
	
	public static Handler getInstance(){
		if(handler == null){
			handler = new Handler();
		}
		
		return handler;
	}
	
	private Handler(){

		Robot.getInstance();
		
		slamController = new SLAMController();
		
		lineTracker = new LineTracker();
		
		positionController = new PositionController();
		
		connected = false;
		
		receiver = new Thread(new Receiver());
		
		//receiver.setDaemon(true);
		
		receiver.start();
	}
	
	private void execute(){
		while(!connected){
			Thread.yield();
		}
		while(true){
			while(currentEvent==null){
				Thread.yield();
			}
			synchronized (receiver) {
				
				switch (currentEvent) {
				
				// go to a waypoint
				case GOTO:
					slamController.performGoTO(nextWaypoint);
					sendEvent(NavEvent.WAYPOINT_REACHED);
					break;
					
				// take readings
				case TAKE_READINGS:
					RangeReadings readings = slamController.performReadings();
					sendRangeReadings(NavEvent.RANGE_READINGS, readings);
					break;
					
				// complete the path, follow line and find marker
				case PATH_COMPLETE:
					// change to another mode
					
					// tracking a line
					System.out.println("execute line tracker!");
					lineTracker.execute();
					System.out.println("position controller started");
					positionController.start();		
					break;
					
				// rotate a certain degree
				case ROTATE:
					
					Robot.getInstance().getPilot().rotate(angle);
					
					break;
					
				// return
				case MOVE_STARTED:
					// return mode
					// rotate a degree
					Robot.getInstance().getPilot().rotate(90-positionController.getDegreeRotated());
					
					// tracking the line
					lineTracker.execute();
					
					// send event
					sendEvent(NavEvent.PATH);
					break;
				
				// set the x,y,z parameters to the positionController
				case FEATURE_DETECTED:
					positionController.setDoubleArray(doubleArray);
				default:
					break;
				}
				currentEvent = null;
			}
		}
	}
	
	// send event to the pc
	public void sendEvent(NavEvent navEvent){
		try{
			synchronized (this) {
				dos.write(navEvent.ordinal());
				dos.flush();	
			}
		}catch (Exception e) {
			System.out.println("exception occurred in sendEvent");
			System.out.println(e.getMessage());
		}
	}	
	
	// send reading to the pc
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
	
	
	// receive event from the pc
	class Receiver implements Runnable{
		public Receiver(){
			
		}
		public void run(){
			System.out.println("waiting");
			BTConnection conn = Bluetooth.waitForConnection();
			LCD.refresh();
			connected = true;
			System.out.println("connected");
			dis = conn.openDataInputStream();
			dos = conn.openDataOutputStream();
			while(true){
				try {
					while(currentEvent!=null){  // if the msg haven't being used by main thread, wait.
						Thread.yield();
					}
					byte event = dis.readByte();
					NavEvent navEvent = NavEvent.values()[event];
					synchronized (this) {
						switch (navEvent) {
						case GOTO: 
							// go to but not need take readings
							// call slamController
							// get the next waypoint
							nextWaypoint = new Waypoint(0,0);
							nextWaypoint.loadObject(dis);
							
							break;
						case TAKE_READINGS:
							
							break;
						case PATH_COMPLETE:
							
							break;
						
						case ROTATE:
							angle = dis.readDouble();
							
							break;
							
						case FEATURE_DETECTED:
							// get the x,y,z coordinate
							doubleArray = new double[PositionController.len];
							for(int i = 0;i<doubleArray.length;++i){
								doubleArray[i] = dis.readDouble();
							}
							
							break;
							
						case MOVE_STARTED:
							
							break;
						
						case EXIT:
							// finish, shutdown
							System.exit(0);
						default:
							break;
						}
						currentEvent = navEvent;
												
					}
					System.out.println("receive an event: " + navEvent.toString());
				} catch (Exception e) {
					System.out.println("exception in receiver");
					System.exit(0);
				}
			}
		}
	}	
}
