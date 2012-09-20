package fyp.pc.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.AbstractMap.SimpleEntry;

import lejos.geom.Point;

/*
 * Arthor: Jinwu Li
 * This implementation of Ransac algorithm is mainly getting for "SLAM for Dummies",
 * which is originally written in C# by Søren Riisgaard and Morten Rufus Blas. 
 * I adopt their code in order to fit into my own data structure.
 */

public class Ransac {
	
	final int MINPOINTS = 10;
	final int MAXTRYS = 1000;
	final int MAXSAMPLE = 3;
	final double RANSAC_TOLERANCE = 5;
	final int RANSAC_CONSENSUS = 50;
	
	int screenWidth, screenHeight;
	
	public Ransac(int screenWidth, int screenHeight){
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}
	
	public Vector<Point> extractLine(Vector<Point> ultrasonicData){
		
		Vector<Point> lineFound = new Vector<Point>();
		Vector<Integer> linePoints = new Vector<Integer>();
		int tryTimes = 0;
		
		Random random = new Random();
		
		// initialize all line points
		for(int i=0;i<ultrasonicData.size();++i){
			linePoints.add(i);
		}		
		
		while(tryTimes<MAXTRYS && linePoints.size() > MINPOINTS){
			//System.out.println(tryTimes);
			Vector<Integer> randomSelectedPoints = new Vector<Integer>();
			
			int temp =0;
			boolean newPoint;
			
			// reviewed, should be fine
			int centerPoint = random.nextInt(ultrasonicData.size()-1 - MAXSAMPLE*2) + MAXSAMPLE;
			randomSelectedPoints.add(centerPoint);
			for(int i=1;i<MAXSAMPLE;++i){
				newPoint = false;
				
				while(!newPoint){
					
					// reviewed, should be fine.
					// temp is a random select point
					int int1 = random.nextInt(2)-1;
					int int2 = random.nextInt(MAXSAMPLE);
					//temp = centerPoint + (random.nextInt(2)-1)*random.nextInt(MAXSAMPLE);
					temp = centerPoint + int1 * int2;
					for(int j=0;j<randomSelectedPoints.size();j++){
						if(randomSelectedPoints.get(j).intValue()==temp)
							break;
						if(j>=i-1)
							newPoint = true;
					}
				}
				randomSelectedPoints.add(temp);
			}
			
			// y = ax + b
			Point point = LeastSquaresLineEstimate(ultrasonicData, randomSelectedPoints);
			double a = point.x;
			double b = point.y;
			Vector<Integer> consensusPoints = new Vector<Integer>();
			Vector<Integer> newLinePoints = new Vector<Integer>();
			
			double x = 0, y=0,d=0;
			for(int i=0;i<linePoints.size();i++){
				// need review
				x = ultrasonicData.get(linePoints.get(i)).x * screenWidth;
				y = ultrasonicData.get(linePoints.get(i)).y * screenHeight;
				d = DistanceToLine(x, y, a, b);
				if(d<RANSAC_TOLERANCE){
					consensusPoints.add(linePoints.get(i));
				}
				else {
					newLinePoints.add(linePoints.get(i));
				}
			}
			if(consensusPoints.size() > RANSAC_CONSENSUS){
				point = LeastSquaresLineEstimate(ultrasonicData, consensusPoints);
				a = point.x;
				b = point.y;
				
				linePoints = newLinePoints;
				
				lineFound.add(point);
				
				tryTimes = 0;
			}
			else{
				tryTimes++;
			}
		}
		
		return lineFound;
	}
	
	private Point LeastSquaresLineEstimate(Vector<Point> ultrasonicData, Vector<Integer> selectedPoints){
		double y=0;
		double x=0;
		double sumY=0;
		double sumYY=0;
		double sumX = 0;
		double sumXX=0;
		double sumYX=0;
		double a,b;
		
		for(int i=0;i<selectedPoints.size();++i){
			
			x=ultrasonicData.get(selectedPoints.get(i)).x * screenWidth;
			y=ultrasonicData.get(selectedPoints.get(i)).y * screenHeight;
			
			sumY+=y;
			sumYY+=Math.pow(y, 2);
			sumX+=x;
			sumXX+=Math.pow(x, 2);
			sumYX+=y*x;
		}
		
		b=(sumY*sumXX-sumX*sumYX)/(selectedPoints.size()*sumXX-Math.pow(sumX , 2));
		a=(selectedPoints.size()*sumYX-sumX*sumY)/(selectedPoints.size()*sumXX-Math.pow(sumX, 2));
		Point point=  new Point((float)a, (float)b);
		
		return point;
	}
	
	private double DistanceToLine(double x,double y,double a,double b){
		return Math.abs(a*x-y+b)/Math.sqrt(a*a+1);
	} 
}