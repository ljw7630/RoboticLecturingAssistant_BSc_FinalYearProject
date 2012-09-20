package fyp.pc.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 
 * 	handler the mouse event
 */

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import fyp.pc.stupidSLAM.Map.Mode;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;

public class MouseHandler extends MouseAdapter implements MouseMotionListener{
	/**
	 * @uml.property  name="status"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	MouseStatus status;
	/**
	 * @author   Jinwu
	 */
	enum MouseStatus{
		/**
		 * @uml.property  name="gOTO"
		 * @uml.associationEnd  
		 */
		GOTO,/**
		 * @uml.property  name="tARGET"
		 * @uml.associationEnd  
		 */
		TARGET
	}
	
	public MouseHandler(){

		status = MouseStatus.GOTO;
	}
	
	public void mouseDragged(MouseEvent e){
		
	}
	
	
	// use to debug the Bayes filter, not really useful
	public void mouseMoved(MouseEvent e){
		
		if(Handler.getInstance().getIsConnected())
		{
			Map map = Map.getInstance();
			
			int x = map.convertToOccupancyMapPosition(e.getX());
			int y = map.convertToOccupancyMapPosition(e.getY());
	
			System.out.println("occupancy value: " + x + " " + y + " " + map.getOccupancyMapValue(x, y));
			map.repaint();
		}
	}
	
	public void mouseReleased(MouseEvent e){

		Map map = Map.getInstance();
		
		int mouseX = map.convertToGridPosition(e.getX());
		int mouseY = map.convertToGridPosition(e.getY());
		
		// right click, ask the robot go to a point
		if(e.getButton()==MouseEvent.BUTTON3){
			
			if(map.getMode() == Mode.BUILDMAP){	
				if(map.getEvent()){
					Pose pose = map.getLastPoseIdeal();
					
					Waypoint waypoint = new Waypoint(mouseX, mouseY, pose.getHeading());
					
					map.setText("pose value: " + pose.getX() +" " + pose.getY() + " " + pose.getHeading());
					map.setText("e value: " + mouseX + " "+ mouseY);
					map.setText("Waypoint value: " + waypoint.x + " " + waypoint.y + " " + waypoint.getHeading());

					map.setEvent(false);
					Handler.getInstance().goTo(waypoint);
				}
			}
		}
		// left click, set a target
		else if(e.getButton() == MouseEvent.BUTTON1){
			Pose newPose = new Pose(mouseX,mouseY,0);
			
			int gridX = map.convertToOccupancyMapPosition(mouseX);
			int gridY = map.convertToOccupancyMapPosition(mouseY);
			
			// if the place is occupied
			if(map.getOccupancyMapValue(gridX, gridY)>=map.thresh_hold){
				map.setText("the targe is either never explored or invalid");
				return;
			}
			
			// if the pose was originally existed, return
			for (int i = 0; i < map.getTargetsVector().size(); i++) {
				Pose pose = map.getTargetsVector().get(i);
				
				if((int)newPose.getX() == (int)pose.getX()
						&& (int) newPose.getY() == (int)pose.getY()){
					return;
				}
			}
			
			// set a target
			map.setTargetPose( newPose, gridX, gridY );
		}
	}
}
