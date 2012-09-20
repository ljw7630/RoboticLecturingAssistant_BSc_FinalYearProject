package fyp.robot.test;

import lejos.nxt.Button;
import lejos.nxt.LCD;


public class EntryPoint {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			DataExchange dataExchange = new DataExchange();
			LineTracking lineTracking = new LineTracking(dataExchange);
			BTReceiver receiver = new BTReceiver(dataExchange);
			AdjustPosition controller = new AdjustPosition(dataExchange);
			//PositionMonitor positionMonitor = new PositionMonitor(dataExchange, controller);
			lineTracking.execute();
			receiver.start();
			controller.start();
			//positionMonitor.start();
		}
		catch (Exception e) {
			// TODO: handle exception
			LCD.drawString(e.getMessage(), 6, 0);
		}
		Button.ENTER.waitForPress();
	}
}

class DataExchange{
	
	public boolean hasModified;
	
	public enum CMD{
		STOP, AdjustPosition, SPIN, LINETRACKING
	}
	
	private CMD command = CMD.LINETRACKING;
	
	public double d[] = new double[3];
	
	public synchronized void setCommand(CMD cmd){
		command = cmd;
	}
	
	public CMD getCommand(){
		return command;
	}
}