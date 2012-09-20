package fyp.pc.test;

import java.util.HashMap;


public class HashMapTest {
	
	public static void main(String args[]){
		Node node = new Node(3.1f, 4.2f, 5f);
		Node node2 = new Node(4.1f, 4.2f, 6f);
		Node node3 = new Node(3.1f, 4.5f, 4f);
		Node node4 = new Node(4.2f, 4.5f, 7f);
		
		HashMap<Node, Node> hashMap = new HashMap<Node, Node>();
		
		hashMap.put(node, node);
		hashMap.put(node2, node2);
		hashMap.put(node3, node3);
		hashMap.put(node4, node4);
		
		System.out.println(hashMap.size());
	}
	
	
	
	
	static class Node implements Comparable<Node>{
		public double f_n;
		
		float x;
		float y;
		
		public Node(float x, float y, double f_n){
			this.f_n = f_n;
			this.x = x;
			this.y = y;
		}
		
		public int compareTo(Node o){
			double d = ((Node)o).f_n;
			if(this.f_n < d)
				return -1;
			else if(this.f_n > d)
				return 1;
			return 0;
		}
		
		public int hashCode(){
			return ((int)x)*10000 + ((int)y);
		}		
		
		public boolean equals(Object object){
			if(object == null)
				return false;
			if(object == this)
				return true;
			if(this.getClass() != object.getClass())
				return false;
			Node node = (Node)object;
			if(this.hashCode() == node.hashCode()){
				//if(this.f_n > node.f_n) // new value is better, we need to keep it
					return true;
			}
			return false;
		}		
	}
}
