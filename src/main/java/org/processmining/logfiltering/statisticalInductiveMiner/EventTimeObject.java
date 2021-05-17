package org.processmining.logfiltering.statisticalInductiveMiner;

public class EventTimeObject {
	String eventName;
	double appearances;
	double avgEventTime;
	double epsilon;
	
	EventTimeObject(double epsilon){
		appearances=0;
		avgEventTime=0;
		this.epsilon=epsilon*1000*60;
		
	}
	
	public boolean addNewEventTime(double time){
		double oldEventTime=this.avgEventTime;
		double totalEventTime=this.avgEventTime*appearances;
		appearances++;
		this.avgEventTime=(totalEventTime+time)/appearances;
		if(Math.abs(oldEventTime-this.avgEventTime)>this.epsilon){
			return true;
		}
		return false;
	}
}
