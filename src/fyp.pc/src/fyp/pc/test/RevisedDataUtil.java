package fyp.pc.test;

import java.util.Vector;

import lejos.geom.Point;

public class RevisedDataUtil {
	
	public static void RevisedUltrasonicBeam(Vector<Point> tempData, boolean[] validList, int size){
		
		int startPos = size/2+1;
		// revise x, middle to left
		for(int i=startPos-2;i>-1;--i){
			if(validList[i] && validList[i+1] && validList[i+2])
				tempData.get(i).y = (tempData.get(i+1).y + tempData.get(i+2).y + tempData.get(i).y)/3;
		}
		
		// revise x, middle to right
		for(int i=startPos+2;i<size;++i){
			if(validList[i] && validList[i-1] && validList[i-2])
				tempData.get(i).y = (tempData.get(i-1).y + tempData.get(i-2).y + tempData.get(i).y)/3;
		}
		
		// revise y, left to middle
		// 90 degree to 0
		int middle = startPos;
		startPos = 0;
		for(int i = startPos + 2; i < middle;++i){
			if(validList[i] && validList[i-1] && validList[i-2])
				tempData.get(i).x = (tempData.get(i-1).x + tempData.get(i-2).x + tempData.get(i).x)/3;
		}
			
		// revise y, right to middle
		for(int i = size - 3;i>middle;--i){
			if(validList[i] && validList[i+1] && validList[i+2])
				tempData.get(i).x = (tempData.get(i+1).x + tempData.get(i+2).x + tempData.get(i).x)/3;
		}
	}
}
