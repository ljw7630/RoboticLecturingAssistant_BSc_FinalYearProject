package fyp.robot.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.RangeFinder;
import lejos.robotics.RangeScanner;
import lejos.robotics.RotatingRangeScanner;
import lejos.robotics.localization.MCLPoseProvider;
import lejos.robotics.mapping.NXTNavigationModel;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;


public class MappingClientTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		mclTest();
	}

	private static void mclTest(){
		BTConnection connection = Bluetooth.waitForConnection();
		DataOutputStream outputStream = connection.openDataOutputStream();
		DataInputStream inputStream = connection.openDataInputStream();
		DifferentialPilot pilot = new DifferentialPilot(2.6f, 5.5f, Motor.C, Motor.B);
		RangeFinder sonic = new UltrasonicSensor(SensorPort.S1); 
		RangeScanner scanner = new RotatingRangeScanner(Motor.A, sonic);
		MCLPoseProvider mclPoseProvider = new MCLPoseProvider(pilot,scanner,null,0,0);
		Navigator navigator = new Navigator(pilot, mclPoseProvider);
		NXTNavigationModel model = new NXTNavigationModel();
		model.setDebug(true);
		model.setRandomMoveParameters(40f, 20f);
		model.addNavigator(navigator);
		model.setAutoSendPose(true);
	}
}
