package fyp.pc.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 	The GUI class of the program
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.VolatileImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;

public class Map extends JFrame implements ActionListener{

	/**
	 * @uml.property  name="mAX_WIDTH"
	 */
	public final int MAX_WIDTH = 4000;
	/**
	 * @uml.property  name="mAX_HEIGHT"
	 */
	public final int MAX_HEIGHT = 4000;
	/**
	 * @uml.property  name="thresh_hold"
	 */
	// if the probability higher that this value, the grid is considered occupied.
	public final double thresh_hold = 0.5;  
	
	/**
	 * @uml.property  name="isEnable"
	 */
	private boolean isEnable=true; 
	
	/**
	 * @uml.property  name="contentPane"
	 * @uml.associationEnd  
	 */
	private JPanel contentPane;
	/**
	 * @uml.property  name="textArea"
	 * @uml.associationEnd  
	 */
	private JTextArea textArea;
	/**
	 * @uml.property  name="scrollPane"
	 * @uml.associationEnd  
	 */
	private JScrollPane scrollPane;

	
	/**
	 * @uml.property  name="map" multiplicity="(0 -1)" dimension="2"
	 */
	double map[][];
	
	/**
	 * @uml.property  name="posesIdealVector"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="lejos.robotics.navigation.Pose"
	 */
	Vector<Pose> posesIdealVector;
	/**
	 * @uml.property  name="posesRealVector"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="lejos.robotics.navigation.Pose"
	 */
	Vector<Pose> posesRealVector;
	/**
	 * @uml.property  name="targetsVector"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="lejos.robotics.navigation.Pose"
	 */
	Vector<Pose> targetsVector; 
	/**
	 * @uml.property  name="waypointVector"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="lejos.robotics.navigation.Waypoint"
	 */
	Vector<Waypoint> waypointVector;
	/**
	 * @uml.property  name="initialPose"
	 * @uml.associationEnd  
	 */
	Pose initialPose = null; // in case we need the initial position
	
	/**
	 * @uml.property  name="panelCenter"
	 * @uml.associationEnd  inverse="map:fyp.pc.stupidSLAM.MapPanel"
	 */
	MapPanel panelCenter;
	
	private static Map mapInstance;
	/**
	 * @uml.property  name="commandList"
	 * @uml.associationEnd  
	 */
	private JComboBox commandList;
	/**
	 * @uml.property  name="btnClearAllTargets"
	 * @uml.associationEnd  
	 */
	private JButton btnClearAllTargets;
	/**
	 * @uml.property  name="targetsList"
	 * @uml.associationEnd  
	 */
	private JComboBox targetsList;
	/**
	 * @uml.property  name="headingList"
	 * @uml.associationEnd  
	 */
	private JComboBox headingList;
	public JComboBox getHeadingList() {
		return headingList;
	}

	/**
	 * @uml.property  name="btnGoToTarget"
	 * @uml.associationEnd  
	 */
	private JButton btnGoToTarget;
	/**
	 * @uml.property  name="btnSaveMap"
	 * @uml.associationEnd  
	 */
	private JButton btnSaveMap;
	/**
	 * @uml.property  name="fileChooser"
	 * @uml.associationEnd  
	 */
	JFileChooser fileChooser;
	/**
	 * @uml.property  name="btnLoadMap"
	 * @uml.associationEnd  
	 */
	private JButton btnLoadMap;
	/**
	 * @uml.property  name="btnPlanPath"
	 * @uml.associationEnd  
	 */
	private JButton btnPlanPath;
	/**
	 * @uml.property  name="btnReturn"
	 * @uml.associationEnd  
	 */
	private JButton btnReturn;
	private JButton btnConnect;
	
	/**
	 * @author   Jinwu
	 */
	public enum Mode {/**
	 * @uml.property  name="bUILDMAP"
	 * @uml.associationEnd  
	 */
	BUILDMAP, /**
	 * @uml.property  name="gOTOTARGETS"
	 * @uml.associationEnd  
	 */
	GOTOTARGETS}
	
	// save map to file
	private void saveOccupancyGridMapToFile(String fileName){
		try{
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(fileName));
			oos.writeObject(map);  // save map
			oos.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// load map from file;
	@SuppressWarnings("unchecked")
	private void loadOccupancyGridMapFromFile(String fileName){
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(
					new FileInputStream(fileName));
			map = (double[][])ois.readObject(); // load map
			ois.close();
		} catch (Exception e) {
			map = new double[MAX_WIDTH][MAX_HEIGHT];
			setText("load map fail");
		}
	}
	
	// return the occupancy map
	public double[][] getOccupancyMap(){
		return map;
	}
	
	// return the mode, at the moment, only have "build map", "go to targets".
	public Mode getMode(){
		if(commandList.getSelectedItem().toString() == "buildMap"){
			setText("Build map mode");
			return Mode.BUILDMAP;
		}
		else if (commandList.getSelectedItem().toString() == "gotoTargets") {
			setText("Go to targets mode");
			return Mode.GOTOTARGETS;
		}
		else {
			return null;
		}
	}
	
	// display debug text and useful info
	public void setText(String string){
		textArea.append(string+"\n");
	}
	
	// get a list of robot poses that the robot has been to
	public Vector<Pose> getPosesIdealVector(){
		return posesIdealVector;
	}
	
	// get the robot's current pose
	public Pose getLastPoseIdeal(){
		return posesIdealVector.lastElement();
	}
	
	// not used, for global localisation
	public Vector<Pose> getPosesRealVector(){
		return posesRealVector;
	}

	// not used, for global localisation
	public Pose getLastPoseReal(){
		return posesRealVector.lastElement();
	}
	
	// set target classroom
	public void setTargetPose(Pose p, int gridX, int gridY){
		targetsVector.add(p);
		
		targetsList.addItem("target"+targetsVector.size() + ": " + gridX + " " + gridY);
		
		panelCenter.repaint();
	}
	
	// return a list of target
	public Vector<Pose> getTargetsVector(){
		return targetsVector;
	}
	
	// the Singleton pattern
	public static Map getInstance(){
		if(mapInstance==null){
			mapInstance = new Map();
		}
		return mapInstance;
	}

	/**
	 * Create the frame.
	 */
	private Map() {
	
		frameUIInit();
		memberInit();
	}
	
	// initialize all members
	public void memberInit(){
		posesIdealVector = new Vector<Pose>();
		posesRealVector = new Vector<Pose>();
		targetsVector = new Vector<Pose>();
		waypointVector = new Vector<Waypoint>();
		
		int startX = convertToGridPosition(panelCenter.getWidth()/2);
		int startY = convertToGridPosition(panelCenter.getHeight()-2*panelCenter.getGridLen()); 
		initialPose = new Pose(startX, startY, 0);
		
		Pose startPose = new Pose(initialPose.getX(),initialPose.getY(),initialPose.getHeading());
		
		posesIdealVector.add(startPose);
		posesRealVector.add(startPose);
		
		map = new double[MAX_WIDTH][MAX_HEIGHT];
		
		for(int i=0;i<MAX_HEIGHT;++i){
			for(int j = 0;j<MAX_WIDTH;++j){
				map[i][j] = 0.5;
			}
		}
	}
	
	// initialize all swing
	public void frameUIInit(){
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		panelCenter = new MapPanel(this);
		contentPane.add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panelEast = new JPanel();
		contentPane.add(panelEast, BorderLayout.WEST);
		panelEast.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panelEast.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setAutoscrolls(true);
		textArea.setColumns(20);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		scrollPane.setAutoscrolls(true);
		
		JPanel panelControl = new JPanel();
		panelEast.add(panelControl, BorderLayout.NORTH);
		panelControl.setLayout(new GridLayout(0, 2, 0, 0));
		
		btnConnect = new JButton("Connect");
		panelControl.add(btnConnect);
		btnConnect.addActionListener(this);
		
		targetsList = new JComboBox();
		panelControl.add(targetsList);
		
		commandList = new JComboBox();
		commandList.setModel(new DefaultComboBoxModel(new String[] {"buildMap", "gotoTargets"}));
		panelControl.add(commandList);
		
		headingList = new JComboBox();
		headingList.setModel(new DefaultComboBoxModel(new String[] {"0", "180", "90", "-90"}));
		panelControl.add(headingList);
		
		btnSaveMap = new JButton("Save map");
		panelControl.add(btnSaveMap);
		btnSaveMap.addActionListener(this);
		
		fileChooser = new JFileChooser(".");
		
		btnPlanPath = new JButton("Plan path");
		panelControl.add(btnPlanPath);
		btnPlanPath.addActionListener(this);
		
		btnLoadMap = new JButton("Load map");
		panelControl.add(btnLoadMap);
		btnLoadMap.addActionListener(this);
		
		btnGoToTarget = new JButton("Go to target");
		panelControl.add(btnGoToTarget);
		btnGoToTarget.addActionListener(this);
		
		btnReturn = new JButton("Return");
		panelControl.add(btnReturn);
		
		btnClearAllTargets = new JButton("Clear targets");
		panelControl.add(btnClearAllTargets);
		btnClearAllTargets.addActionListener(this);
		btnReturn.addActionListener(this);		
		
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		dimension.height = dimension.height - 40;
		setPreferredSize(dimension);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);
		pack();	
		
		MouseHandler mouseHandler = new MouseHandler();
		panelCenter.addMouseListener(mouseHandler);
		panelCenter.addMouseMotionListener(mouseHandler);		
	}
	
	public JPanel getPanel(){
		return contentPane;
	}
	
	// update map value 
	public void updateOccupancyMapValue(int x,int y, double value){

		map[x][y] = value;
	}
	
	// get the map value
	public double getOccupancyMapValue(int x,int y){
		return map[x][y];
	}
	
	// set the value to the closest grid position
	public int convertToGridPosition(double z){
		return panelCenter.convertToGridPosition(z);
	}
	
	// set the value to the occupancy map position
	public int convertToOccupancyMapPosition(double x){
		return (int)(x/panelCenter.getGridLen());
	}
	
	public void updateMap(Pose robotProbablePose){	
		repaint();
	}
	
	public void paint(Graphics g){
		super.paintComponents(g);
	}	

	// send the pose to the new waypoint
	public void updatePoseAfterCommand(Pose pose){
		posesIdealVector.add(pose);
		
		panelCenter.repaint();
	}
	
	// get the grid length
	public int getGridLen(){
		return panelCenter.getGridLen();
	}
	
	// see is the GUI disabled
	public boolean getEvent(){
		return isEnable;
	}
	
	// enable or disable GUI
	public synchronized void setEvent(boolean isEnable){
		this.isEnable = isEnable;
		
		panelCenter.repaint();
	}
	
	// handle button click event
	public void actionPerformed (ActionEvent e){
		
		// clear all current targets
		if(e.getSource() == btnClearAllTargets){
			((DefaultComboBoxModel)(targetsList.getModel())).removeAllElements();
			targetsVector.clear();
			waypointVector.clear();
		}
		
		// plan a path
		else if(e.getSource() == btnPlanPath){
			commandList.setSelectedIndex(1);
			if(targetsList.getItemCount()==0){
				setText("target not set");
			}
			else{
				
				Pose pose = targetsVector.get(targetsList.getSelectedIndex());
				pose.setHeading(Integer.parseInt(headingList.getSelectedItem().toString()));
				setText("plan Path to target: " + pose.getX() + " " + pose.getY() + " " + headingList.getSelectedItem().toString());
				Handler.getInstance().planPath(posesIdealVector.lastElement(), MapUtil.getInstance().convertPoseToWaypoint(pose));
			}
		}
		
		// go to the target
		else if(e.getSource() == btnGoToTarget){
			if(waypointVector == null || waypointVector.size() == 0){
				setText("no path available, need to plan path first");
			}
			else{
				//commandList.setSelectedIndex(1);
				Handler.getInstance().followPath(false);
			}
		}
		
		// save the map to disk
		else if(e.getSource() == btnSaveMap){
			fileChooser.setApproveButtonText("Save");
			int value = fileChooser.showOpenDialog(this);
			
			if(value == JFileChooser.APPROVE_OPTION){
				saveOccupancyGridMapToFile(fileChooser.getSelectedFile().getAbsolutePath());
			}
		}
		
		// load a map from disk
		else if(e.getSource() == btnLoadMap){
			fileChooser.setApproveButtonText("Load");
			int value = fileChooser.showOpenDialog(this);
			
			if(value == JFileChooser.APPROVE_OPTION){
				loadOccupancyGridMapFromFile(fileChooser.getSelectedFile().getAbsolutePath());
			}
		}
		
		// call the robot to return
		else if(e.getSource() == btnReturn){
			Handler.getInstance().callReturn();
		}
		
		// connect to the robot
		else if(e.getSource() == btnConnect){
			Handler.getInstance().connect();
		}
		panelCenter.repaint();
	}
	
	public int getMaxWidthIndex(){
		return panelCenter.getMaxWidthIndex();
	}
	
	public int getMaxHeightIndex(){
		return panelCenter.getMaxHeightIndex();
	}
	
	// set the path
	public void setWaypointVector(Vector<Waypoint> waypoints){
		this.waypointVector = waypoints;
		panelCenter.repaint();
	}
	
	// get the path
	public Vector<Waypoint> getWaypointVector(){
		return this.waypointVector;
	}
}

class MapPanel extends JPanel{

	//public int width, height;
	/**
	 * @uml.property  name="gridLen"
	 */
	public final int gridLen = 10;
	/**
	 * @uml.property  name="image"
	 */
	private VolatileImage image;
	/**
	 * @uml.property  name="map"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="panelCenter:fyp.pc.stupidSLAM.Map"
	 */
	private Map map;
	
	public MapPanel(Map map){
		this.map = map;
	}
	
	// draw image
	public void paint(Graphics g){
		super.paintComponent(g);
		
		if(image==null 
				|| image.getWidth()!=getWidth()
				|| image.getHeight()!=getHeight()){
			image = createVolatileImage(getWidth(), getHeight());
		}
		
		
		// draw grid line
		Graphics graphics = image.getGraphics();
		
		graphics.clearRect(0, 0, getWidth(), getHeight());
		
		graphics.setColor(Color.green);
		for(int i = 0;i<=getHeight()/gridLen;++i){
			graphics.drawLine(0, i * gridLen, getWidth(),i * gridLen);
		}
		
		for(int i=0;i<=getWidth()/gridLen;++i){
			graphics.drawLine(i*gridLen, 0, i*gridLen, getHeight());
		}
		
		
		// draw occupancy map
		
		if(map.getOccupancyMap()!=null){
			graphics.setColor(Color.darkGray);
			for(int i=0;i<=getHeight()/gridLen;++i){   
				for(int j=0;j<=getWidth()/gridLen;++j){
					
					if(map.getOccupancyMapValue(i,j)>map.thresh_hold){
						graphics.fillRect(i*gridLen, j*gridLen, gridLen, gridLen);
					}
				}
			}
		}
		
		// draw waypoint
		if(map.getWaypointVector()!=null){
			graphics.setColor(Color.BLUE);
			
			for(int i=0;i<map.getWaypointVector().size();++i){
				Waypoint wp = map.getWaypointVector().get(i);
				graphics.fillOval(convertToGridPosition(wp.getX()), convertToGridPosition(wp.getY()), gridLen, gridLen);
				
			}
		}
		
		// draw target position
		if(map.getTargetsVector()!=null){
			graphics.setColor(Color.RED);
			
			for(int i=0;i<map.getTargetsVector().size();++i){
				Pose pose = map.getTargetsVector().get(i);
				graphics.fillOval(convertToGridPosition(pose.getX()), convertToGridPosition(pose.getY()), gridLen, gridLen);
			}
		}		
		
		// draw robot position
		if(map.getPosesIdealVector()!=null){
			graphics.setColor(Color.black);
		
			for(int i=0;i<map.getPosesIdealVector().size();++i){
				Pose pose = map.getPosesIdealVector().get(i);
				graphics.fillOval(convertToGridPosition(pose.getX()), convertToGridPosition(pose.getY()), gridLen, gridLen);
			}
		}
		g.drawImage(image, 0,0,null);
	}
	
	public int convertToGridPosition(double z){
		return (int)(z/gridLen)*gridLen;
	}
	
	public int getMaxHeightIndex(){
		return getHeight()/gridLen;
	}
	
	public int getMaxWidthIndex(){
		return getWidth()/gridLen;
	}
	
	public int getGridLen(){
		return gridLen;
	}
}
