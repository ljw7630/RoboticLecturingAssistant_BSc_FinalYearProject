package fyp.pc.test;

import lejos.pc.comm.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class BTInitiator {
	
	final int port = 12345;
	final int len = 3;
	ServerSocket serverSocket;
	Socket clientSocket;
	BufferedReader bufferedReader;
	double d[];
	NXTConnector connector;
	DataOutputStream dos;
	boolean isFinished = false;
	
	/**
	 * @param args
	 * @throws NXTCommException 
	 */
	public static void main(String[] args) throws NXTCommException {
		// TODO Auto-generated method stub
		
		try {
			BTInitiator initiator = new BTInitiator();
			initiator.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public BTInitiator() throws IOException{
		
		socketInit();
		
		bluetoothInit();
	}

	private void bluetoothInit() {
		// bt init
		connector = new NXTConnector();
		connector.addLogListener(new NXTCommLogListener() {
			
			@Override
			public void logEvent(Throwable trowable) {
				// TODO Auto-generated method stub
				System.out.println("BlueToothInitiator Log.Listener - stack trace: ");
				trowable.printStackTrace();
			}
			
			@Override
			public void logEvent(String message) {
				// TODO Auto-generated method stub
				System.out.println("BlueToothInitiator Log.Listener: " + message);
			}
		});
		
		boolean connected = connector.connectTo("btspp://");
		
		if(!connected){
			System.err.println("Fail to connect to NXT...");
			System.exit(1);
		}
		System.out.println("Bluetooth Connection established!");
		dos = new DataOutputStream(connector.getOutputStream());
	}

	private void socketInit(){
		// socket init
		try{
			serverSocket = new ServerSocket(port);
			
			Runtime.getRuntime().exec("marker_recognition.exe");
			
			clientSocket = serverSocket.accept();
			
			bufferedReader = new BufferedReader(
					new InputStreamReader(
							clientSocket.getInputStream()));
			
			System.out.println("Socket Connection established!");
			
			d = new double[len];
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void execute(){
		while(true){
			receivedFromCSocket();
			
			if(d!=null){
				sendToNXT();
			}
			if(isFinished)
			{
				break;
			}
		}
		close();
	}
	
	public void receivedFromCSocket() {
		try{
			System.out.println("Waiting");
			String string = bufferedReader.readLine();
			System.out.println(string==null);
			if(string == null){
				d=null;
				return;
			}
			Scanner scanner = new Scanner(string);
			for (int i = 0; i < d.length; i++) {
				d[i] = scanner.nextDouble();
				System.out.println(d[i]);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			isFinished = true;
		}
	}
	
	public void sendToNXT(){
		try {
			for (int i = 0; i < d.length; i++) {
				dos.writeDouble(d[i]);
			}
			dos.flush();			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			bufferedReader.close();
			clientSocket.close();
			serverSocket.close();
			dos.close();
			connector.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	}
}
