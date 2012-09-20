package fyp.robot.test;

import lejos.nxt.*;
import lejos.nxt.comm.*;
import java.io.*;

import fyp.robot.test.DataExchange.CMD;

public class BTReceiver extends Thread {
	
	BTConnection btConnection;
	String connected = "Connected";
	String waiting = "Waiting...";
	String closing = "Closing...";
	/**
	 * @param args
	 */
	private DataExchange dataExchange;
	
	public BTReceiver(DataExchange dataExchange){
		this.dataExchange = dataExchange;
		LCD.drawString(waiting, 0, 0);
		btConnection = Bluetooth.waitForConnection();
	}
	
	public void run() {
		
		//LCD.drawString(waiting, 0, 0);
		LCD.refresh();
		//LCD.drawString("before waiting", 0, 1);
		
		//LCD.drawString("after waiting", 0, 1);
		//LCD.clear();
		LCD.drawString(connected, 0, 0);
		LCD.refresh();
		
		DataInputStream dis = btConnection.openDataInputStream();
		double d[] = new double[3];
		try{
			while(true){
				LCD.drawString("in while true", 7, 0);
				
				if(dataExchange.getCommand()==CMD.STOP){
					break;
				}
				
				for(int i=0;i<3;++i){
					d[i] = dis.readDouble();
					
					LCD.drawString(Double.toString(d[i]),0, i+1);
				}
				synchronized (dataExchange) {
					dataExchange.hasModified = true;
					for (int i = 0; i < d.length; i++) {
						dataExchange.d[i] = d[i];
					}
					if(dataExchange.getCommand()==CMD.SPIN){
						dataExchange.setCommand(CMD.AdjustPosition);
						System.out.println("send adjust pos command");
					}
				}
			}
			System.out.println("receiver stop");
			dis.close();
			btConnection.close();
			LCD.clear();
		}catch (Exception e) {
			// TODO: handle exception
			LCD.drawString(e.getMessage(), 0, 7);
		}
	}

}
