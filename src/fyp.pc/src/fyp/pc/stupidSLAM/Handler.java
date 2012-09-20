/*
 * Author: Jinwu Li
 * Student number: D10120110
 * This class is originally from PCNavigationModel.java
 * in project pccomms, package: lejos.robotics.mapping
 * As it's couple with NavigationPanel, so I simplified it 
 * to meet my requirement.
 */

package fyp.pc.stupidSLAM;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import javax.xml.stream.XMLStreamException;

import fyp.pc.stupidSLAM.Map.Mode;

import lejos.geom.Point;
import lejos.pc.comm.*;
import lejos.robotics.RangeReadings;
import lejos.robotics.mapping.*;
import lejos.robotics.navigation.*;

public class Handler extends NavigationModel{

	/**
	 * @uml.property  name="moves"
	 */
	protected ArrayList<Move> moves = new ArrayList<Move>();
	/**
	 * @uml.property  name="poses"
	 */
	protected ArrayList<Pose> poses = new ArrayList<Pose>();
	/**
	 * @uml.property  name="features"
	 */
	protected ArrayList<Point> features = new ArrayList<Point>();
	/**
	 * @uml.property  name="waypoints"
	 */
	protected ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	/**
	 * @uml.property  name="reached"
	 * @uml.associationEnd  readOnly="true"
	 */
	protected Waypoint reached;	
	/**
	 * @uml.property  name="receiver"
	 */
	private Thread receiver;
	
	/**
	 * @uml.property  name="nextWaypointFlag"
	 */
	private boolean nextWaypointFlag = true;
	/**
	 * @uml.property  name="isCSocketFinished"
	 */
	private boolean isCSocketFinished = false;
	/**
	 * @uml.property  name="isReturn"
	 */
	//private boolean isReturn = false;
	/**
	 * @uml.property  name="isAtPosition"
	 */
	private boolean isAtPosition = false;
	/**
	 * @uml.property  name="pathFinishedFlag"
	 */
	private boolean pathFinishedFlag = false;
	
	/**
	 * @uml.property  name="serverSocket"
	 */
	private ServerSocket serverSocket;
	/**
	 * @uml.property  name="clientSocket"
	 */
	private Socket clientSocket;
	/**
	 * @uml.property  name="bufferedReader"
	 */
	private BufferedReader bufferedReader;
	/**
	 * @uml.property  name="port"
	 */
	final int port = 12345;
	/**
	 * @uml.property  name="len"
	 */
	final int len = 3;
	
	private Process markerRecognitionProcess;
	
	private boolean isConnected = false;
	
	private static Handler handler;
	
	
	// the Singleton Design Pattern
	private Handler(){
	}
	
	public static Handler getInstance(){
		if(handler == null){
			handler = new Handler();
		}
		return handler;
	}
	
	
	// get connection status
	public boolean getIsConnected(){
		return isConnected;
	}
	
	// entry point
	public static void main(String[] args){
		
		Handler handler = Handler.getInstance();
		
		Map map = Map.getInstance();
		MapUtil mapUtil = MapUtil.getInstance();
		
		handler.connect();
	}

	// connect to the robot
	public void connect() {
		if(isConnected)
			return;
		NXTConnector conn = new NXTConnector();

		if (!conn.connectTo("btspp://")) {
			System.out.println("error when connecting");
			return;
		}
		isConnected = true;
		System.out.println("finish connect");
		dis = new DataInputStream(conn.getInputStream());
		dos = new DataOutputStream(conn.getOutputStream());
		
		// Start the receiver thread
		receiver = new Thread(new Receiver());
		receiver.setDaemon(true);
		receiver.start();
	}	
	
	/*
	 * request type:
	 * rotate, travel, goto, get_reading
	 */
	public void sendEvent(NavEvent navEvent) {
		if(!isConnected)
			return;
		try{
			synchronized (receiver) {
				dos.writeByte(navEvent.ordinal());
				dos.flush();
			}
		}
		catch (Exception e) {
			System.out.println("exception occurred in " + navEvent.name());
			e.printStackTrace();
		}
	}
	
	
	// send marker recognition parameters to the robot
	public void sendDoubleArray(NavEvent navEvent, double[] d){
		if(!isConnected)
			return;
		try{
			synchronized (receiver) {
				dos.writeByte(navEvent.ordinal());
				for(int i = 0;i<d.length;++i){
					dos.writeDouble(d[i]);
				}
				dos.flush();
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	// send double value to the robot
	public void sendDouble(NavEvent navEvent,double d){
		if(!isConnected)
			return;
		try{
			synchronized (receiver) {
				dos.writeByte(navEvent.ordinal());
				dos.writeDouble(d);
				dos.flush();
			}
		}
		catch (Exception e) {
			System.out.println("exception occurred in " + navEvent.name());
			e.printStackTrace();
		}
	}
	
	// send next waypoint to the robot
	public void sendWaypoint(NavEvent navEvent, Waypoint waypoint){
		if(!isConnected)
			return;
		try{
			synchronized (receiver) {
				dos.writeByte(navEvent.ordinal());
				waypoint.dumpObject(dos);
			}
		}
		catch (Exception e) {
			System.out.println("exception occurred in " + navEvent.name());
			e.printStackTrace();
		}
	}
	
	
	// call the path finder to find a path
	public void planPath(Pose startPose, Waypoint endWaypoint) {
		Vector<Waypoint> waypoints = MapUtil.getInstance().pathFinder.findPath(startPose, endWaypoint);
		
		Map.getInstance().setText("waypoints: " + waypoints.size());
		Map.getInstance().setText("path: ");
		Map.getInstance().setText("start: " + waypoints.lastElement().getX() +" " +waypoints.lastElement().getY());
		Map.getInstance().setText("end: " + waypoints.get(0).getX() +" " +waypoints.get(0).getY());
		Map.getInstance().setWaypointVector(waypoints);
	}
	
	// follow path might perform twice, one for robot goes to classroom, another for going back.
	public void followPath(boolean isReturn){
		
		Map map = Map.getInstance();
		
		Vector<Waypoint> waypoints = Map.getInstance().getWaypointVector();
		
		if(isReturn){
			
			int angle = Integer.parseInt((String)map.getHeadingList().getSelectedItem());
			
			sendDouble(NavEvent.ROTATE, angle);
			
			int index = 1;
			
			while(index<waypoints.size()){
				while(!nextWaypointFlag){
					Thread.yield();
				}
				
				Pose pose = MapUtil.getInstance().convertWaypointToPose(waypoints.get(index));
				
				map.setText("going: ");
				map.setText("robot pose: " + map.getLastPoseIdeal().getX() + " " + map.getLastPoseIdeal().getY());
				map.setText("next waypoint: " + pose.getX() + " " + pose.getY());
				
				nextWaypointFlag = false;
				goTo(waypoints.get(index));
				index++;	
			}
			
			sendEvent(NavEvent.EXIT);
		}
		else{
			int index = waypoints.size()-2;
			while(index>-1){
				while(!nextWaypointFlag){
					Thread.yield();
				}
				
				Pose pose = MapUtil.getInstance().convertWaypointToPose(waypoints.get(index));
				map.setText("returning: ");
				map.setText("robot pose: " + map.getLastPoseIdeal().getX() + " " + map.getLastPoseIdeal().getY());
				map.setText("next waypoint: " + pose.getX() + " " + pose.getY());
				nextWaypointFlag = false;
				goTo(waypoints.get(index));
				index --;
			}
			
			int angle = Integer.parseInt((String)map.getHeadingList().getSelectedItem());
			
			sendDouble(NavEvent.ROTATE, angle);
			
			sendEvent(NavEvent.PATH_COMPLETE);
		}
		
		// follow path finish, change to "follow line mode"
		if(!isReturn){
			performFollowLineMode();
		}
	}
	
	// tell the robot return to the start position
	public void callReturn(){
		
		// if the robot not at the end position, return
		if(!isAtPosition){
			Map.getInstance().setText("Robot still not at position yet");
			return;
		}
		else{
			
			isAtPosition = false;
			// move started, again
			sendEvent(NavEvent.MOVE_STARTED);		
		}
		while(!pathFinishedFlag){
			Thread.yield();
		}
		
		// when the color tracking finished, start follow path mode
		followPath(true);
	}
	
	// following line and find the marker
	private void performFollowLineMode(){
		
		// start the marker recognition program, initialise socket to receive data 
		followLineModeSocketInit();
		
		// receive data from the marker recognition program, send it to NXT robot 
		followLineModeExecute();
	}
	
	public void goTo(Waypoint waypoint){
	
		Map map = Map.getInstance();
		
		// convert waypoint to relatively waypoint
		Pose pose = map.getLastPoseIdeal();
		Waypoint relativeWaypoint = new Waypoint(
				waypoint.getX() - pose.getX()
				, waypoint.getY() - pose.getY()
				, waypoint.getHeading());
		
		// add new waypoint to next robot pose
		map.updatePoseAfterCommand(MapUtil.getInstance().convertWaypointToPose(waypoint));
		
		// disable the GUI
		map.setEvent(false);
		
		sendWaypoint(NavEvent.GOTO, relativeWaypoint);
		
		// at the moment, we only send range reading when build map
		if(Map.getInstance().getMode() == Mode.BUILDMAP){
			takeReadings();
		}
	}
	
	
	// ask the robot to take readings
	public void takeReadings(){
		sendEvent(NavEvent.TAKE_READINGS);
	}
	
	// ask the robot exit
	public void sendExit(){
		sendEvent(NavEvent.EXIT);
	}
	
	
	// ask the robot rotate to a specific angle
	public void rotateTo(double angle){
		sendDouble(NavEvent.ROTATE_TO, angle);
	}
	
	// run the marker recognition program
	private void runMarkerRecognition(){
		Runtime runtime = Runtime.getRuntime();
		try {
			markerRecognitionProcess = runtime.exec("marker_recognition.exe");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void followLineModeSocketInit(){
		try{
			// create a server socket
			serverSocket = new ServerSocket(port);
			
			// call marker recognition program here!
			runMarkerRecognition();
			
			// accept the marker recognition program connection
			clientSocket = serverSocket.accept();
			
			// get the input stream
			bufferedReader = new BufferedReader(
					new InputStreamReader(
							clientSocket.getInputStream()));
			
			System.out.println("Socket Connection Established!");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// receive data from the marker recognition program, send to the robot
	private void followLineModeExecute(){
		while (!isAtPosition) {
			double d [] = receivedFromCSocket();
			
			if(d!=null){
				sendDoubleArray(NavEvent.FEATURE_DETECTED, d);
			}
			if(isCSocketFinished){
				break;
			}
		}
		// isAtPosition = false;
		try{
			clientSocket.close();
			
			serverSocket.close();
			
			markerRecognitionProcess.destroy();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	// read data from the marke recognition program
	private double[] receivedFromCSocket(){
		double [] d = new double[len];
		try{
			System.out.println("Waiting");
			String string = bufferedReader.readLine();
			System.out.println(string==null);
			if(string == null){
				return null;
			}
			Scanner scanner = new Scanner(string);
			for (int i = 0; i < d.length; i++) {
				d[i] = scanner.nextDouble();
				System.out.println(d[i]);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			isCSocketFinished = true;
			return null;
		}
		return d;
	}
	
	
	// a thread used to receive event from the robot
	class Receiver implements Runnable {
	
		public void run() {
			
			try {
				MapUtil mapUtil = MapUtil.getInstance();
			
				while(true) {
					try {
						RangeReadings rangeReadings = new RangeReadings(0);
						byte event = dis.readByte();
						NavEvent navEvent = NavEvent.values()[event];
						synchronized(this) {
							switch (navEvent) {
							
							// tell mapUtil to process reading
							case RANGE_READINGS:
								rangeReadings.loadObject(dis);
								mapUtil.processData(rangeReadings);
								break;
							
							// nextWaypoint
							case WAYPOINT_REACHED:
								nextWaypointFlag = true; // for the path in gototarget mode
								break;
							case MOVE_STOPPED: 
								// the robot find the whiteboard position and place itself correctly
								isAtPosition = true;
							case PATH:
								// finish the path
								pathFinishedFlag = true;
							}
						}
					} catch (IOException ioe) {
						
					}
				}
			}
			catch (Exception e) {
				System.out.println("exeption in receiver, run, maputil");
				e.printStackTrace();
			}
		}	
	}	
}
