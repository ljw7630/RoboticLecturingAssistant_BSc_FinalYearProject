package fyp.robot.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 
 *	Go to a waypoint and take readings 	
 */

import lejos.robotics.RangeReadings;
import lejos.robotics.navigation.Waypoint;

public class SLAMController {
	
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
	 * @uml.property  name="angles" multiplicity="(0 -1)" dimension="1"
	 */
	float []angles;
	/**
	 * @uml.property  name="epsilon"
	 */
	private double epsilon = 1; 
	
	public SLAMController(){
		startAngle = -90;
		endAngle = 90;
		incAngle = 5;		
	}
	
	private static float[] getAngles(float min, float max, float gap){
		
		float []angles = new float[(int) (( (max - min) / gap) + 1)];
		float angle = max;
		for(int i=0;i<angles.length;++i){
			angles[i] = angle;
			angle -= gap;
		}
		return angles;
	}
	
	public RangeReadings performReadings(){
		
		Robot robot = Robot.getInstance();
		
		angles = getAngles(startAngle, endAngle, incAngle);
		robot.getScanner().setAngles(angles);
		
		RangeReadings readings = robot.getScanner().getRangeValues();
		//sendRangeReadings(NavEvent.RANGE_READINGS, readings);
		
		int tmpAngle = startAngle;
		startAngle = endAngle;
		endAngle = tmpAngle;
		incAngle = -incAngle;
		
		return readings;
	}
	
	// go to a waypoint
	public void performGoTO(Waypoint waypoint){
		
		Robot robot = Robot.getInstance();
		
		double degree = getDegree(waypoint);
		double distance = getDistance(waypoint);
		robot.getPilot().rotate(degree);
		robot.getPilot().travel(distance);
		robot.getPilot().rotate(-degree);
	}
	
	// calculate the degree the robot need to rotate
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
				return 180 - getDegree( Math.atan( (-waypoint.x) /waypoint.y ) );			
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
	
	private double getDistance(Waypoint waypoint){
		double distance = Math.sqrt(waypoint.x * waypoint.x + waypoint.y * waypoint.y);
		return distance;
	}	
}
