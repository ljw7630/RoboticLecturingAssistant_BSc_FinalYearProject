package fyp.pc.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 	
 * 	Find a path from current position to destination, using A star.
 */

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;
import lejos.geom.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;

public class PathFinder {
	
	private static final int directionSize = 8;
	
	// eight direction
	private static final double direction[][] = 
		{
			{-1,-1,0},{-1,1,0},{1,1,0},{1,-1,0},{-1,0,0},{0,-1,0},{1,0,0},{0,1,0}
		};
	
	public PathFinder(){
		for(int i=0;i<directionSize;++i){
			direction[i][2] = getDistance(direction[i][0], direction[i][1]);
		}
	}
	
	// first, we convert coordinate to occupancy index, full description see the final report, chapter 4
	public Vector<Waypoint> findPath(Pose startPose, Waypoint dest){
		Map map = Map.getInstance();
		
		Pose occupancyStartPose = new Pose(
				map.convertToOccupancyMapPosition(startPose.getX())
				,map.convertToOccupancyMapPosition(startPose.getY())
				, startPose.getHeading());
		Waypoint occupancyWaypoint = new Waypoint(
				map.convertToOccupancyMapPosition(dest.getX())
				, map.convertToOccupancyMapPosition(dest.getY())
				, dest.getHeading());
		
		
		HashMap<Node,Node> hashMap = new HashMap<Node,Node>();
		
		Vector<Waypoint> vec = new Vector<Waypoint>();
		
		double dist = getDistance(occupancyStartPose.getX(), occupancyStartPose.getY(), occupancyWaypoint);
		
		Node startNode = new Node(new Point(occupancyStartPose.getX(), occupancyStartPose.getY()),null,-1,dist, 0 , dist);
		
		PriorityQueue <Node> queue = new PriorityQueue <Node>();
		
		queue.add(startNode);
		hashMap.put(startNode, startNode);
		Node res = null;
		
		// implement a start algorithm
		while(queue.size()!=0){
			Node node = queue.remove();
			if((int)node.point.getX() == (int)occupancyWaypoint.getX() && (int)node.point.getY() == (int)occupancyWaypoint.getY()){
				res = node;
				break;
			}
			for(int i=0;i<directionSize;++i){
				int x = (int)node.point.getX() + (int)direction[i][0];
				int y = (int)node.point.getY() + (int)direction[i][1];
				
				if(x<0 || x > map.getMaxWidthIndex() || y<0|| y>map.getMaxHeightIndex()){
					continue;
				}
				
				if(map.getOccupancyMapValue(x, y)<=map.thresh_hold){
					
					double h_n = getDistance(x, y, occupancyWaypoint); // h(n)
					
					double f_n = node.f_n - node.h_n + direction[i][2] + h_n;
					
					// sum of the distance of start to current, current to end
					Node newNode = new Node(new Point(x, y),node,i,f_n,f_n-h_n, h_n);
					if(hashMap.get(newNode)!=null){
						
						// in map, but replace with a good node
						if(hashMap.get(newNode).f_n > newNode.f_n){
							hashMap.put(newNode, newNode);
						}
						else{
							continue;
						}
					}
					else{
						// not in map before
						hashMap.put(newNode, newNode);
					}
					
					queue.add(newNode);
				}
			}
		}
		
		
		// add the destination to the path
		if(res!=null){
			vec.add(new Waypoint(res.point));
		}
		
		while(res!=null){
			int directionIndex = res.directionIndex;
			while(res.prevNode!=null && res.prevNode.directionIndex == directionIndex){
				res = res.prevNode;
			}
			
			// change direction
			if(res.prevNode!=null){
				Waypoint wp = new Waypoint(res.prevNode.point);
				vec.add(wp);
			}
			else{ // if res.prevNode == null, then res is source point, we don't need to store it

			}
			res = res.prevNode;
		}
		
		// convert waypoint for occupancy index to real number
		for(int i=0;i<vec.size();++i){
			Waypoint wp = vec.get(i);
			vec.set(i, new Waypoint(wp.getX() * map.getGridLen(),wp.getY() * map.getGridLen(), wp.getHeading()));
		}
		
		return vec;
	}
	
	public double getDistance(double x, double y, Node node){
		return Math.sqrt(Math.pow(x - node.point.getX(),2) + Math.pow(y - node.point.getY(), 2)); 
	}
	
	public double getDistance(double x,double y){
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	public double getDistance(double x, double y, Waypoint wp){
		Node node = new Node(new Point((float)wp.getX(), (float)wp.getY()));
		return getDistance(x, y, node);
	}
	
	/**
	 * @author  Jinwu
	 */
	// internal data structure used in a star algorithm
	class Node implements Comparable<Node>{
		
		public Node(){
			
		}
		
		public Node(Point p){
			point = p;
		}
		
		public Node(Point p, Node prevNode, int directionIndex,double f_n, double g_n, double h_n){
			this.point = p;
			this.prevNode = prevNode;
			this.directionIndex = directionIndex;
			this.f_n = f_n;
			this.g_n = g_n;
			this.h_n = h_n;
		}
		
		public Point point;
		/**
		 * @uml.property  name="prevNode"
		 * @uml.associationEnd  
		 */
		public Node prevNode;
		
		public int directionIndex;
		
		public double f_n;
		public double g_n;
		public double h_n;
		
		
		// use in hash map
		public int hashCode(){
			return ((int)point.getX())*10000 + ((int)point.getY());
		}
		
		// use in hash map
		public boolean equals(Object object){
			if(object == null)
				return false;
			if(object == this)
				return true;
			if(this.getClass() != object.getClass())
				return false;
			Node node = (Node)object;
			if(this.hashCode() == node.hashCode()){
				if(this.f_n < node.f_n) // less than, we need to keep it
					return true;
			}
			return false;
		}
		
		// use in priority queue
		public int compareTo(Node node){
			//return (int)(this.f_n - node.f_n);
			if(this.f_n<node.f_n)
				return -1;
			else if(this.f_n > node.f_n)
				return 1;
			return 0;
		}
	}
}
