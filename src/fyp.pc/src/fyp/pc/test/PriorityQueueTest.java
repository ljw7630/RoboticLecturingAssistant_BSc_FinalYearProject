package fyp.pc.test;

import java.util.PriorityQueue;
import java.util.Random;

public class PriorityQueueTest {
	
	public static void main(String[] args){
		PriorityQueue<Node> queue = new PriorityQueue<PriorityQueueTest.Node>();
		Random ranom = new Random(); 
		for(int i=0;i<10; ++i){
			Node node = new Node(ranom.nextDouble());
			queue.add(node);
		}
		
		while(queue.size()!=0){
			Node node = queue.remove();
			System.out.println(node.value);
		}
	}
	
	static class Node implements Comparable<Node>{
		public double value;
		
		public Node(double v){
			value = v;
		}
		
		public int compareTo(Node o){
			double d = ((Node)o).value;
			if(this.value < d)
				return -1;
			else if(this.value > d)
				return 1;
			return 0;
		}		
	}
}
