package org.processmining.logfiltering.algorithms.KCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
public class Network<V>{
	private Map<V, List<Node<V>>> adjacencyList;
 	private Set<V> vertices;
 	private static final int DEFAULT_WEIGHT = Integer.MAX_VALUE;
 	private static int storageServer;
 	private static int processServer;
 	
	private static Network<String> graph = new Network<String>();
        Scanner sc = new Scanner(System.in);
	public Network(){
  		this.adjacencyList = new HashMap<>();
  		vertices = new HashSet<>();
 	}
 	private static class Node<V> {
  		private V name;
  		private int weight;
  		public Node(V name, int weight) {
   			this.name = name;
   			this.weight = weight;
  		}
  		public V getName() {
   			return name;
  		}
  		public int getWeight() {
   			return weight;
  		}
 	}	
 	public Set<V> getAllVertices() {
  		return Collections.unmodifiableSet(this.vertices);
 	}
 	private void addEdge(V src, Node<V> destNode) {
  		List<Node<V>> adjacentVertices = adjacencyList.get(src);
  		
		if (adjacentVertices == null || adjacentVertices.isEmpty()) {
   			adjacentVertices = new ArrayList<Node<V>>();
   			adjacentVertices.add(destNode);
  			} else {
   				adjacentVertices.add(destNode);
  			}
  		adjacencyList.put(src, adjacentVertices);
 	}
 	public void addEdge(V src, V dest, int weight) {
  		Objects.requireNonNull(src);
  		Objects.requireNonNull(dest);
  		this.addEdge(src, new Node<>(dest, weight));
		this.vertices.add(src);
  		this.vertices.add(dest);
 	}
 	public int getWeight(V src, V dest) {
  		int weight = DEFAULT_WEIGHT;
   		List<Node<V>> adjacentNodes = this.adjacencyList.get(src);
   
		for (Node<V> node : adjacentNodes) {
    			if (node.getName().equals(dest)) {
     				weight = node.getWeight();
    			}
   		}
  		return weight;
 	}
 	public String toString() {
  		StringBuilder sb = new StringBuilder();
  		sb.append("Set of Edges :\n");
  		for (V v : this.adjacencyList.keySet()) {
   			List<Node<V>> neighbour = this.adjacencyList.get(v);
   			for (Node<V> vertex : neighbour) {
    				if (vertex.getWeight() != DEFAULT_WEIGHT) {
     					sb.append(v + " -- (" + vertex.getWeight() + ")--->"
       					+ vertex.getName() + "\n");
    				} else {
     					sb.append(v + " ------->" + vertex.getName() + "\n");
    				}
			}
  		}
  		return sb.toString();
 	}
 	public void readServerInput(){
     		System.out.print("Enter the number of storage intensive servers:\t");
     		storageServer = sc.nextInt();
     		System.out.print("Enter the number of process intensive servers:\t");
     		processServer = sc.nextInt();
	}
 	public Set<V> getAdjacentVertices(V vertex) {
  		List<Node<V>> adjacentNodes = this.adjacencyList.get(vertex);
  		Set<V> neighborVertex = new HashSet<V>();
  		if ((adjacentNodes != null) && !adjacentNodes.isEmpty()) {
   			for (Node<V> v : adjacentNodes) {
    				neighborVertex.add(v.getName());
   			}
  		}
  		return neighborVertex;
 	}
 	public Set<V> filterCities(V city, Set<V> Cities, float rad){
   		Set<V>filteredSet = new HashSet<>();
   		for(V vertex : Cities){
			if((getWeight(city,vertex))>rad){
				filteredSet.add(vertex);
			}
		}
		return filteredSet;
	}
    	
 	public V findNextCity(Set<V>mCities,List<V>optCities){
    		V cityToAdd = null;
    		float weight,min = Float.MAX_VALUE,newMax = Float.MIN_VALUE,previousMax=(-1);
    		Iterator<V> itrVertices = mCities.iterator();
    
		while(itrVertices.hasNext()){
			V nextCity = itrVertices.next();
        		for(V selectedCentre : optCities){
                		weight = getWeight(nextCity,selectedCentre);
                		min = Math.min(min,weight);
                	}
        
		newMax = Math.max(min,newMax);
        	if(previousMax != newMax){
                	cityToAdd = nextCity;
                	previousMax = newMax;
        		}
    		}
		return cityToAdd;
	}
   
	public void selectCentre(){
    		Set<V> mCities = this.getAllVertices();
    		Set<V> neighbourCities = new HashSet<V>();
    		List<V> optCities = new ArrayList<V>();
    		float serverRadius;
    		int total;
    		total = storageServer + processServer;
	
    		System.out.print("Enter the serverRadius:\t");
    		serverRadius = sc.nextFloat();
    		Iterator<V> itrVertices = mCities.iterator();
    		V firstCity = itrVertices.next();
    		V cityToAdd = null;
    		optCities.add(firstCity);
    		neighbourCities = getAdjacentVertices(firstCity);
    		mCities = filterCities(firstCity,neighbourCities,serverRadius);	
    		int flag=0,len = mCities.size();
    		if(len>0){
    			while(len>0){
				cityToAdd = findNextCity(mCities,optCities);	
				optCities.add(cityToAdd);
				mCities.remove(cityToAdd);
				len = mCities.size();
			
				if(len==0){
                			break;
				}
	
				mCities = filterCities(cityToAdd,mCities,serverRadius);
				len = mCities.size();
			}
		}
		if(optCities.size() <= total){
			System.out.println("The optimal cities to place the servers:");
                	System.out.println(optCities);
		}else{
			System.out.println("Only few cities can be served. Change the radius!");
		}
	}
 	public static void main(String args[]){
		graph.addEdge("0", "1", 47);
  		graph.addEdge("1", "0", 47);
  		graph.addEdge("0", "2", 28);
  		graph.addEdge("2", "0", 28);
  		graph.addEdge("0", "3",200);
  		graph.addEdge("3", "0",200);
		graph.addEdge("0","4",100);
		graph.addEdge("4","0",100);

		graph.addEdge("1", "2",200);
  		graph.addEdge("2", "1",200);
  		graph.addEdge("2", "1",200);					// HARD-CODED

  		graph.addEdge("1", "3",56);
  		graph.addEdge("3", "1",56);
		graph.addEdge("1","4",87);
		graph.addEdge("4","1",87);
		graph.addEdge("2", "3",90);
  		graph.addEdge("3", "2",90);
		
		graph.addEdge("2","4",50);
		graph.addEdge("4","2",50);
		graph.addEdge("3","4",70);
		graph.addEdge("4","3",70);
  		System.out.print(graph.toString());
  		graph.readServerInput();
  		graph.selectCentre();
 	}
}