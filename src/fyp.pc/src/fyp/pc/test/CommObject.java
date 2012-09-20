package fyp.pc.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.robotics.RangeReadings;

public class CommObject {
	public float direction;
	public float distance;
	public RangeReadings readings;
	
	public CommObject(){
		direction = 0;
		distance = 0;
		readings = new RangeReadings(0);
	}
	
	public CommObject(float direction, float distance, RangeReadings readings){
		this.direction = direction;
		this.distance = distance;
		this.readings = readings;
	}
	
	public void dumpObject(DataOutputStream dos) throws IOException{
		dos.writeFloat(direction);
		dos.writeFloat(distance);
		readings.dumpObject(dos);
		/*dos.writeInt(readings.getNumReadings());
		for(int i=0;i<readings.getNumReadings();++i){
			dos.writeFloat(readings.get(i).getAngle());
			dos.writeFloat(readings.get(i).getRange());
		}
		*/
		//dos.flush();
	}
	
	public void loadObject(DataInputStream dis) throws IOException{
		direction = dis.readFloat();
		distance = dis.readFloat();
		readings.loadObject(dis);
		/*int num = dis.readInt();
		System.out.println(num);
		readings = new RangeReadings(num);
		for(int i=0;i<num;++i){
			float angle = dis.readFloat();
			float range = dis.readFloat();
			readings.setRange(i, angle, range);
			System.out.println(angle +  " " + range);
		}*/
	}
}
