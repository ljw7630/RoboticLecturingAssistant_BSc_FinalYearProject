package fyp.pc.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.VolatileImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Vector;
import java.util.AbstractMap.SimpleEntry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lejos.geom.Point;
import lejos.robotics.RangeReading;
import lejos.robotics.RangeReadings;

public class TestRansac {
	public static void main(String[] args) throws FileNotFoundException {
		MapFrameTest frame = new MapFrameTest();
		Ransac ransac = new Ransac(frame.getWidth(), frame.getHeight());
		frame.loadDataFromFile("testransac");
/*		for (int i = 0; i < ultrasonicData.size(); i++) {
			System.out.println("ultrasonic: " + ultrasonicData.get(i).x + " " + ultrasonicData.get(i).y);
		}*/
		Vector<Point> vector = ransac.extractLine(frame.getUltrasonicData());
		/*System.out.println(vector.size());
		for(int i=0;i<vector.size();++i){
			//System.out.println(vector.get(i).x + " " + vector.get(i).y);
		}*/
		//frame.setUltrasonicData(ultrasonicData);
		frame.setLines(vector);
		frame.repaint();
	}
}

class MapFrameTest extends JFrame{
	JPanel drawPanel;
	Vector<SimpleEntry<RangeReadings,Point>> originVec;
	Vector<SimpleEntry<ArrayList<Point>,Point>> pointVec;
	Vector<Point> ultrasonicData;
	Vector<Double> ultrasonicDataAngle;
	Point robotStartPos;
	Point robotCurrentPos;
	Dimension dimension;
	VolatileImage osiImage;
	final int MAXI_RANGE = 100;
	float distanceFromReceiverToPivot;
	Vector<Point> lines;
	
	public MapFrameTest() throws FileNotFoundException {
		
		// keep track of robot pos
		robotStartPos = new Point(1.0f/2, 1.0f/2);
		robotCurrentPos = robotStartPos;
		
		// data structure for later painting & calculation
		originVec = new Vector<SimpleEntry<RangeReadings,Point>>();
		pointVec = new Vector<SimpleEntry<ArrayList<Point>,Point>>();
		ultrasonicData = new Vector<Point>();
		ultrasonicDataAngle = new Vector<Double>();
		lines = new Vector<Point>();
		
		// 4cm between ultrasonic receiver and ultrasonic rotate pivot
		distanceFromReceiverToPivot = convertMetric(4);
		
		
		// panel, listener, configuration
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		drawPanel = new JPanel();
		setContentPane(drawPanel);

		dimension = Toolkit.getDefaultToolkit().getScreenSize();		
		
		drawPanel.setPreferredSize(dimension);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		
		setVisible(true);
	}
	
	public void setLines(Vector<Point> lines){
		this.lines = lines;
	}
	
	public void saveDataToFile(String fileName){
		try {
			System.out.println("data size: " + ultrasonicData.size());
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
			outputStream.writeObject(pointVec);
			outputStream.writeObject(ultrasonicData);
			outputStream.writeObject(ultrasonicDataAngle);
			outputStream.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public void loadDataFromFile(String fileName){
		ObjectInputStream inputStream =null;
		try{
			inputStream = new ObjectInputStream(new FileInputStream(fileName));
			
			pointVec = (Vector<AbstractMap.SimpleEntry<ArrayList<Point>,Point>>) inputStream.readObject();
			ultrasonicData = (Vector<Point>) inputStream.readObject();
			ultrasonicDataAngle = (Vector<Double>) inputStream.readObject(); 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			
			try {
				//System.out.println("load data size: " + vector.size());
				inputStream.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}	
	
	public Vector<Point> getUltrasonicData() {
		return ultrasonicData;
	}

	public Vector<Double> getUltrasonicDataAngle() {
		return ultrasonicDataAngle;
	}

	public void paint(Graphics g){
		super.paintComponents(g);
		if(osiImage == null 
				|| osiImage.getWidth() != getWidth() 
				|| osiImage.getHeight() != getHeight()){
			osiImage = createVolatileImage(getWidth(), getHeight());
		}
		Graphics osiGraphics = osiImage.getGraphics();
		osiGraphics.clearRect(0, 0, osiImage.getWidth(), osiImage.getHeight());
		
/*		Point lastPoint = null;
		
		for(int i = 0;i<pointVec.size();++i){
			SimpleEntry<ArrayList<Point>, Point> entry = pointVec.get(i);
			ArrayList<Point> arrayList = entry.getKey();
			Point robotPos = entry.getValue();
			
			// draw oval
			for(int j=0;j<arrayList.size();++j){
				Point point = arrayList.get(j);
				osiGraphics.drawOval((int)(point.x*getWidth()), (int)(point.y*getHeight()), 4, 4);
				//System.out.println((int)point.x*getWidth() + " " + (int)point.y*getHeight());
			}
			
			Color color = g.getColor();
			osiGraphics.setColor(Color.blue);
			osiGraphics.drawOval((int)(robotPos.x*getWidth()), (int)(robotPos.y*getHeight()), 8, 8);
			//System.out.println("robot.x: " + robotPos.x +", robot.y: " + robotPos.y);
			osiGraphics.setColor(color);
			
			if(lastPoint!=null){
				osiGraphics.drawLine((int)(lastPoint.x*getWidth()+4), (int)(lastPoint.y*getHeight()+4), (int)(robotPos.x*getWidth()+4), (int)(robotPos.y*getHeight()+4));
			}
			
			lastPoint = robotPos;
		}*/
		
		//System.out.println("paint");
		for(int i=0;i<ultrasonicData.size();++i){
			int x1 = (int)(ultrasonicData.get(i).x * getWidth());
			int y1 =(int)(ultrasonicData.get(i).y * getHeight());
			osiGraphics.drawOval(x1, y1, 4, 4);
			//System.out.println("p: " + x1 + " " + y1);
		}
		
		for(int i=0;i<lines.size();++i){
			int x1 = -5,x2 = getWidth() + 5;
			int y1 = (int)(lines.get(i).x * x1 + lines.get(i).y);
			int y2 = (int)(lines.get(i).x * x2 + lines.get(i).y);
			osiGraphics.drawLine(x1, y1, x2, y2);
		}
		
		g.drawImage(osiImage, 0, 0, null);
	}
	
	public void addCommObject(CommObject commObject){
		commObject.distance = convertMetric(commObject.distance);
		double angle = convertDegreeToRadian(commObject.direction);
		robotCurrentPos = percentagePointOnScreen(angle, robotCurrentPos, commObject.distance);
		originVec.add(new SimpleEntry<RangeReadings, Point>(commObject.readings, robotCurrentPos));
		processPointVector(commObject.readings);
	}
	
	private float convertMetric(float d){
		return d * 6 / 2.75f;
	}
	
	private void processPointVector(RangeReadings readings){
		ArrayList<Point> arrList = new ArrayList<Point>();
		
		// rangeReading of 0 degree;
		//float rangeOfZeroDegree = readings.get(readings.size()/2+1).getRange();
		for(int i=0;i<readings.size();++i){
			RangeReading reading = readings.get(i);
			if(!validateReading(reading)){
				continue;
			}
			double angle = convertDegreeToRadian(reading.getAngle());
			//float realRange = getRealReadingRange(reading.getRange(), angle);
			//Point point = percentagePointOnScreen(angle, robotCurrentPos, realRange);
			Point point = percentagePointOnScreen(angle, robotCurrentPos, reading.getRange());
			
			ultrasonicData.add(point);
			ultrasonicDataAngle.add(angle);
			arrList.add(point);
		}
		
		pointVec.add(new SimpleEntry<ArrayList<Point>, Point>(arrList, new Point(robotCurrentPos.x,robotCurrentPos.y)));
		repaint();
	}
	
	// due to the rotation of ultrasonic header, the reading range is not precise. 
	private float getRealReadingRange(float range, double angle){
		return
			(float)(range/Math.abs(Math.sin(angle)));
	}
	
	private double convertDegreeToRadian(double d) {
		return d * Math.PI/180;
	}
	
	// calculate the point pos on screen in percentage.
	private Point percentagePointOnScreen(double angleInRadian, Point startPoint, Float range){
		float x = startPoint.x + (float)Math.sin(angleInRadian)*range/getWidth();
		float y = startPoint.y - (float)Math.cos(angleInRadian)*range/getHeight();
		return new Point(x,y);
	}
	
	private boolean validateReading(RangeReading reading){
		if(reading.getRange()<0||reading.getRange()>MAXI_RANGE){
			return false;
		}
		else{
			return true;
		}
	}
}