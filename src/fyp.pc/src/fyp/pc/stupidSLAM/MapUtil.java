package fyp.pc.stupidSLAM;

/*
 * Author: Jinwu Li
 * Student number: D10120110
 * Process sensor readings, update then onto map 
 */
import java.util.*;
import lejos.geom.*;
import lejos.robotics.*;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;

public class MapUtil {
	
	/**
	 * @uml.property  name="pathFinder"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	PathFinder pathFinder;
	
	// need an initial pose;
	//Pose robotPose;
	/**
	 * @uml.property  name="lastWaypoint"
	 * @uml.associationEnd  readOnly="true"
	 */
	Waypoint lastWaypoint;
	
	/**
	 * @uml.property  name="lineMapFileName"
	 */
	final String lineMapFileName = "room.svg";
	/**
	 * @uml.property  name="mAXI_RANGE"
	 */
	final int MAXI_RANGE = 100;
	private static MapUtil mapUtilInstance;
	
	
	public static MapUtil getInstance(){
		if(mapUtilInstance == null){
			mapUtilInstance = new MapUtil();
		}
		return mapUtilInstance;
	}
	
	private MapUtil(){
		pathFinder = new PathFinder();
	}
	
	// assuming we know the current pose and current reading.
	public void processData(RangeReadings readings){
		
		Map map = Map.getInstance();
		
		Pose robotPose = map.getLastPoseIdeal();
		
		float robotAngle = robotPose.getHeading();
		
		Vector<Point> allPoints = new Vector<Point>();
		boolean [] isValid = new boolean[readings.size()];
		
		for(int i=0;i<isValid.length;++i){
			isValid[i] = true;
		}
		
		// do a lot of calculation here
		// convert the readings to the map coordinate
		for(int i=0;i<readings.size();++i){
			RangeReading reading = readings.get(i);
			
			// validate the reading
			if(!validateReading(reading)){
				isValid[i]=false;
			}
			
			// calculate the angle base on the robot pose 
			double angle = convertDegreeToRadian(reading.getAngle()) + robotAngle;
			
			//map.setText("reading: " + reading.getAngle() + " " + reading.getRange());
			
			// if it's the valid reading, calculate the point, otherwise, consider it as point at maximum range
			Point point = null;
			if(isValid[i])
				point = pointOnScreen(angle, robotPose, reading.getRange());
			else
				point = pointOnScreen(angle, robotPose, (float)MAXI_RANGE);
			
			allPoints.add(point);
		}
		
		
		/*		
		* perform naive mapping
		* keep it as reference
		*/
		/*
		// reviseUltrasonicBeam(allPoints, isValid);
		
		for(int i = 0;i<allPoints.size();++i){
			if(isValid[i]){
				Point p = allPoints.get(i);
				int x = map.convertToGridPosition(p.x);
				int y = map.convertToGridPosition(p.y);
				
				// at the moment, just update to map.threshhold
				map.updateOccupancyMapValue(map.convertToOccupancyMapPosition(x), map.convertToOccupancyMapPosition(y), map.thresh_hold+1);
			}
		}*/
		
		// perform the Bayes filter
		BayesFilter.execute(allPoints, isValid, robotPose);
		
		map.setEvent(true);
	}
	
	private boolean validateReading(RangeReading reading){
		
		// if the range is less than 0 or greater than max range, ignore 
		if(reading.getRange()<0||reading.getRange()>MAXI_RANGE){
			return false;
		}
		else{
			return true;
		}		
	}
	
	// conver the reading to the point on the map
	private Point pointOnScreen(double angleInRadian, Pose pose, Float range){
		float x = pose.getX() - (float)Math.sin(angleInRadian)*range;
		float y = pose.getY() - (float)Math.cos(angleInRadian)*range;
		return new Point(x,y);
	}	
	
	// use to revise data, for reference purpose
	/*
	private void reviseUltrasonicBeam(Vector<Point> tempData, boolean[] validList){
		int size = tempData.size();
		
		int startPos = size/2+1;
		// revise y, middle to left
		for(int i=startPos-2;i>-1;--i){
			if(validList[i] && validList[i+1] && validList[i+2])
				tempData.get(i).y = (tempData.get(i+1).y + tempData.get(i+2).y + tempData.get(i).y)/3;
		}
		
		// revise y, middle to right
		for(int i=startPos+2;i<size;++i){
			if(validList[i] && validList[i-1] && validList[i-2])
				tempData.get(i).y = (tempData.get(i-1).y + tempData.get(i-2).y + tempData.get(i).y)/3;
		}
		
		// revise x, left to middle
		// 90 degree to 0
		int middle = startPos;
		startPos = 0;
		for(int i = startPos + 2; i < middle;++i){
			if(validList[i] && validList[i-1] && validList[i-2])
				tempData.get(i).x = (tempData.get(i-1).x + tempData.get(i-2).x + tempData.get(i).x)/3;
		}
			
		// revise x, right to middle
		for(int i = size - 3;i>middle;--i){
			if(validList[i] && validList[i+1] && validList[i+2])
				tempData.get(i).x = (tempData.get(i+1).x + tempData.get(i+2).x + tempData.get(i).x)/3;
		}		
	}
	*/
	
	private float convertDegreeToRadian(float d) {
		return (float)(d * Math.PI/180);
	}
	
	public Pose convertWaypointToPose(Waypoint wp){
		return (new Pose((float)wp.getX(), (float)wp.getY(), (float)wp.getHeading()));
	}
	
	public Waypoint convertPoseToWaypoint(Pose pose){
		return (new Waypoint(pose.getX(), pose.getY(), pose.getHeading()));
	}
}
