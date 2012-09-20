package fyp.pc.test;

import java.io.DataInputStream;
import java.io.IOException;


import lejos.pc.comm.NXTConnector;

public class CommServerTest {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		NXTConnector connector = new NXTConnector();
		connector.connectTo("btspp://");
		DataInputStream dis = new DataInputStream(connector.getInputStream());
		try
		{
			CommObject object = new CommObject();
			
			object.loadObject(dis);
			System.out.println(object.direction + " " + object.distance + " ");
			for(int i=0;i<object.readings.getNumReadings();++i){
				System.out.println(object.readings.getAngle(i) + " " +object.readings.getRange(i));
			}
			System.out.println(object.readings.getNumReadings() + " " + object.readings.size());
		}
		catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		dis.close();
		connector.close();
	}

}
