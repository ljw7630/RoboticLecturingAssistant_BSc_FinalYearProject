package fyp.robot.test;

import fyp.robot.test.DataExchange.CMD;

public class PositionMonitor extends Thread{
	
	private DataExchange dataExchange;
	private AdjustPosition adjustPosition;
	
	public PositionMonitor(DataExchange dataExchange, AdjustPosition adjustPosition){
		this.dataExchange = dataExchange;
		this.adjustPosition = adjustPosition;
	}
	
	public void run(){
		double x,z,xx,zz;
		while(dataExchange.getCommand()==CMD.AdjustPosition){
			xx = AdjustPosition.xx;
			zz = AdjustPosition.zz;
			
			synchronized (dataExchange) {
				x = dataExchange.d[0];
				z = dataExchange.d[2];
			}
			
			if(Math.abs(x-xx)<AdjustPosition.precision&&Math.abs(z-zz)<AdjustPosition.precision){
				adjustPosition.threadStop = true;
				break;
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				
			}
		}
	}
}
