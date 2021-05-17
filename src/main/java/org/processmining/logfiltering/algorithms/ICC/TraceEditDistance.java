package org.processmining.logfiltering.algorithms.ICC;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class TraceEditDistance {

	int distance;
	
	public TraceEditDistance(XTrace trace1, XTrace trace2) {
		this.distance=calculateTED(trace1, trace2);
	}
	
	public int getDistance() {
		return this.distance;
	}
	
	public int calculateTED(XTrace trace1, XTrace trace2) {
		String[] trace1Events=new String[trace1.size()+1];
		trace1Events[0]="#";
		String[] trace2Events=new String[trace2.size()+1];
		trace2Events[0]="#";
		
		//init trace Lists
		for(int i=0;i<trace1.size();i++) {
			XEvent current=trace1.get(i);
			trace1Events[i+1]=current.getAttributes().get("concept:name").toString();
		}
		for(int j=0;j<trace2.size();j++) {
			XEvent current=trace2.get(j);
			trace2Events[j+1]=current.getAttributes().get("concept:name").toString();
		}
		
		//init calculation matrix
		int[][] TEDMatrix=new int[trace1Events.length][trace2Events.length];
		for (int i=0;i<trace1Events.length;i++) {
			TEDMatrix[i][0]=i;
		}
		for (int j=0;j<trace2Events.length;j++) {
			TEDMatrix[0][j]=j;
		}
		
		//calculate edit distance
		for(int i=1;i<trace1Events.length;i++) {
			for(int j=1;j<trace2Events.length;j++) {
				int left=TEDMatrix[i][j-1]+1;
				int bot=TEDMatrix[i-1][j]+1;
				int botleft=TEDMatrix[i-1][j-1];
				if(!trace1Events[i].equals(trace2Events[j])) {
					botleft+=2;
				}
				int min=Math.min(left, bot);
				min=Math.min(min, botleft);
				TEDMatrix[i][j]=min;
			}
		}
		return TEDMatrix[trace1Events.length-1][trace2Events.length-1];
	}
	
	
	
}
