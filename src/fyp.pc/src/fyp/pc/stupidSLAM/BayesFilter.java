package fyp.pc.stupidSLAM;

/*
 * 	Author: Jinwu Li
 * 	Student number: D10120110
 * 	The Bayes Filter algorithm is used to determine the belief state
 * 	of a place base on the measurement and sensor noise probability.
 * 	For more information about Bayes Filter, see <Probabilistic Robotics>
 * 	And there's some discussion in my dissertation. 
 */

import java.util.Vector;
import lejos.geom.Point;
import lejos.robotics.navigation.Pose;

public class BayesFilter {
	
	// probability of sensor say yes, there's an obstacle; and there's an obstacle in real world.
	private static final double prob_SensorYes_RealYes = 0.8;
	
	// probability of sensor say no, there's no obstacle; and there's an obstacle in real world.
	private static final double prob_SensorNo_RealYes = 0.2;
	
	// probability of sensor say yes, there's an obstacle; and there's no obstacle in real world.
	private static final double prob_SensorYes_RealNo = 0.3;
	
	// probability of sensor say no, there's no obstacle; and there's no obstacle in real world.
	private static final double prob_SensorNo_RealNo = 0.7;
	
	
	// update belief state based on last belief state
	private static double calculateProbablity(boolean sensorYes, double lastValue){
		double prob_yes;
		double prob_no;
		if(sensorYes){
			
			// p(xn) = p(y|y) * p(xn-1), x is yes.
			prob_yes = prob_SensorYes_RealYes * lastValue;
			
			// p(xn) = p(y|n) * p(xn-1), x is no. 
			prob_no = prob_SensorYes_RealNo * (1.0-lastValue);
		}
		else{
			
			// p(xn) = p(n|y) * p(xn-1), x is yes.
			prob_yes = prob_SensorNo_RealYes * lastValue;
			
			// p(xn) = p(n|n) * p(xn-1), x is no.
			prob_no = prob_SensorNo_RealNo * (1.0- lastValue);
		}
		
		
		// Normalise the probability to 1.
		return (1.0f/(prob_yes+prob_no)) * prob_yes;
	}
	
	
	// use floodfill algorithm to update all relative nodes.
	public static void execute(Vector<Point> allPoints, boolean isValid[], Pose robotPose){
		Map map = Map.getInstance();
		int [] bounds = Node.init(allPoints, isValid);
		int x = map.convertToOccupancyMapPosition(robotPose.getX());
		int y = map.convertToOccupancyMapPosition(robotPose.getY());
		Node.floodFill(x, y, bounds[0], bounds[1], bounds[2], bounds[3]);
		
		Map.getInstance().setText("process finished");
	}
	
	// use floodfill to calculate bayes filter result
	/**
	 * @author  Jinwu
	 */
	static class Node{
		public boolean isVisited;
		//public double value;
		
		/**
		 * @uml.property  name="nodes"
		 * @uml.associationEnd  multiplicity="(0 -1)"
		 */
		public static Node nodes[][];
		
		public static final int direction_size = 4;
		
		//public static HashSet<Integer> hashSet;
		
		public static int direction[][] = {
			{-1,0},
			{0,1},
			{1,0},
			{0,-1}
		};
		
		// update all "edge" result. 
		public static int[] init(Vector<Point> allPoints, boolean isValid[]){
			int [] bound = new int[4]; // left most, right most, up, bottom
			Map map = Map.getInstance();
			nodes = new Node[map.getMaxHeightIndex()+1][map.getMaxWidthIndex()+1];
			
			for(int i=0;i<map.getMaxHeightIndex()+1;++i){
				for(int j=0;j<map.getMaxWidthIndex()+1;++j){
					nodes[i][j] = new Node();
					nodes[i][j].isVisited = false;
				}
			}
			
			bound[0] = Integer.MAX_VALUE;
			bound[1] = Integer.MIN_VALUE;
			bound[2] = Integer.MAX_VALUE;
			bound[3] = Integer.MIN_VALUE;
			for(int i=0;i<allPoints.size();++i){
				Point p = allPoints.get(i);
				
				int x = map.convertToOccupancyMapPosition(p.x);
				int y = map.convertToOccupancyMapPosition(p.y);
				//map.setText("x: " + x + " " + "y: " + y + " " + isValid[i]);
				// if this point has been visit before
				if(nodes[x][y].isVisited){
					continue;
				}
				
				boolean sensorResult;
				double d=0.0f;
				if(isValid[i]){
					sensorResult = true;
					
					d = BayesFilter.calculateProbablity(sensorResult, map.getOccupancyMapValue(x, y));
					map.updateOccupancyMapValue(x, y, d);
				}
				else{
					sensorResult = false;
				}
				
				nodes[x][y].isVisited = true;
				
				if(bound[0]>y){
					bound[0] = y;
				}
				if(bound[1]<y){
					bound[1] = y;
				}
				if(bound[2]>x){
					bound[2]=x;
				}
				if(bound[3]<x){
					bound[3]=x;
				}
			}
			
			return bound;
		}
		
		// all internal nodes, which means there's no obstacle in these nodes.
		public static void floodFill(int x,int y, int leftBound, int rightBound, int upBound, int downBound){
			Map map = Map.getInstance();
			
			for(int i=0;i<direction_size;++i){
				int xx = x + direction[i][0];
				int yy = y + direction[i][1];
				if(xx>=upBound&&xx<=downBound&&yy>=leftBound&&yy<=rightBound){
					if(nodes[xx][yy].isVisited){
						;
					}
					else{
						double d = 0.0f;
						
						// only update when greater than thresh_hold to void bias
						if(map.getOccupancyMapValue(xx, yy)>=map.thresh_hold){
							
							d = BayesFilter.calculateProbablity(false, map.getOccupancyMapValue(xx, yy));
							map.updateOccupancyMapValue(xx, yy, d);
						
						}
						nodes[xx][yy].isVisited = true;
						
						// recursive update
						floodFill(xx, yy, leftBound, rightBound, upBound, downBound);
					}
				}
			}
		}
	}
}
