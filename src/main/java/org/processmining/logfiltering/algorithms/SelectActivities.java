package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.logfiltering.algorithms.SelectActivities.Activity;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SamplingType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.SamplingReturnType;

public class SelectActivities {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) {
		//XLog OutputLog = (XLog) InputLog.clone();
		/////////////////Initialization/////////////////////////////////////////////
	//	long time = System.currentTimeMillis();
		
		XEventClassifier EventCol = parameters.getEventClassifier();
		

		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		int LogSize = 0;
		int[] VariantFreq = null;
		SortedSet<String> eventAttributeSet = new TreeSet<String>();
		XAttributeMap eventAttributeMap;
		int KLength =parameters.getSubsequenceLength(); 
		XLog outputLog2 = factory.createLog();
		outputLog2.setAttributes(InputLog.getAttributes());	
		Set<String> ActivitySet = new HashSet<String>();
		HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
		HashMap<Integer, XTrace> VariantMapper =new HashMap<Integer, XTrace>();
		HashMap<Integer, Integer> SelectedList= new HashMap<Integer, Integer>();
		FilterLevel FilteringMethod = parameters.getFilterLevel();
		FilterSelection FilteringSelection =parameters.getFilteringSelection();
		HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
		HashMap<String,Integer >ActivityMaper =new HashMap<String, Integer>();
		HashMap<String,Integer >ActivityContextMaper =new HashMap<String, Integer>();
		HashMap<String,Integer >FilterHashMaper =new HashMap<String, Integer>();
		HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
		
		HashMap<String, Integer> DirectlyFollowMaper= new HashMap<String, Integer>();
		HashMap<String, Integer> DirectlyFollowMaperCounter= new HashMap<String, Integer>();
		HashMap<String, Integer> DirectlyFollowMapperCounter= new HashMap<String, Integer>();
		HashMap<Integer, String> RevereseDirectlyFollowMaper= new HashMap<Integer, String>();
		//////////////////////////////// Computing Activities Frequency /////////////////////
		
		SamplingType samplingType = parameters.getSamplingType();
		SamplingReturnType returnType = parameters.getReturnType();
		int structureCounter=0;
		int contextCounter=0;
		int chCount=0;
		int directCount=0;
		int DFcounter=0;
		long time = System.currentTimeMillis();
		ArrayList<Integer> TraceNumber = new ArrayList<Integer>();
		for (int i = 0; i < InputLog.size(); i++) {
			TraceNumber.add(i);
		}
		
		 ArrayList<String> SampledVariantlist = new ArrayList<String>();
		 PriorityQueue<Integer> pickedTraces=new PriorityQueue<>();

		 
		 Random generator = new Random(System.currentTimeMillis());
		 int InputLogSize= InputLog.size();
		 for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
				ActivitySet.add(clazz.toString());
			}
		//int ActivitiesSize = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]).size();
		int ActivitiesSize = ActivitySet.size();
		//Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
		String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
		List<String> ActivityList = java.util.Arrays.asList(Activities);
		int[] ActivityCount = new int[ActivitiesSize];
		for (int i = 0; i < ActivityCount.length; i++) {
			ActivityMaper.put(ActivityList.get(i), 0);
			ActivityContextMaper.put(ActivityList.get(i), 0);
		}
	

		
		int [] [] VariantRelations= new int[ActivitiesSize] [ActivitiesSize];
		HashMap<String, Integer> WindowsSequenceMaper= new HashMap<String, Integer>();
		HashMap<String, Integer> WindowsSequenceCounter= new HashMap<String, Integer>();
		HashMap<Integer, String> RevereseWindowsSequenceMaper= new HashMap<Integer, String>();
		
		HashMap<String, Integer> ContextMaper= new HashMap<String, Integer>();
		HashMap<String, Integer> ContextCounter= new HashMap<String, Integer>();
		HashMap<Integer, String> RevereseContextMaper= new HashMap<Integer, String>();
		
		HashMap<String, Integer> StractureMaper= new HashMap<String, Integer>();
		HashMap<String, Integer> StractureCounter= new HashMap<String, Integer>();
		HashMap<Integer, String> RevereseStractureMaper= new HashMap<Integer, String>();
		 
		
		if(samplingType!= SamplingType.RandomSamplingWithoutLooking) 
		
		for (XTrace trace : InputLog) { // for each trace
				 LogSize++;
				 /// Put trace to array
				 String[] Trace2 = new String[trace.size()];
				 String[] Trace = new String[trace.size()+2];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) { 
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Trace2 = templist.toArray(new String[trace.size()]);
					Trace[0]="ArtStart"; Trace[trace.size()+1]="ArtEnd"; 
					for (int i = 0; i < Trace2.length; i++) {
						Trace[i+1]=Trace2[i];
					}
					String tr= "";
					int WSCounter=0;
					for (int i =0; i < Trace.length; i++){
						tr= tr.concat(Trace[i]).concat("=>");
					}
					int SubPatternLength=2;
					String[] tempPattern= new String[SubPatternLength+1];
					String Subpattern="";
				
						int l=SubPatternLength;
						String temp;
						for (int i = 0; i < Trace.length-l; i++) {
							Subpattern="";
							for(int k=0; k<=l;k++) {
								Subpattern=Subpattern.concat(Trace[i+k]);
								tempPattern[k]=Trace[i+k]; 
								if(k!=l)
									Subpattern=Subpattern.concat("=>");
							}
							ActivityMaper.put(tempPattern[1], ActivityMaper.get(tempPattern[1])+1);
							if(WindowsSequenceCounter.get(Subpattern)==null) {
								WindowsSequenceCounter.put(Subpattern, 1);
								WindowsSequenceMaper.put(Subpattern, WSCounter);
								RevereseWindowsSequenceMaper.put(WSCounter, Subpattern);
								WSCounter++;
								ActivityContextMaper.put(tempPattern[1], ActivityContextMaper.get(tempPattern[1])+1);
							}else {
								WindowsSequenceCounter.put(Subpattern, WindowsSequenceCounter.get(Subpattern)+1);
							}
						
						} // for length of trace

				
					if (ActionMaper.get(tr)==null ){
						ActionMaper.put(tr,1);
						ReverseMapper.put(chCount, tr);
						HashMaper.put(tr, chCount);
						VariantMapper.put(chCount, trace);
						
						chCount++;
						
						
					
						
						
						
					}else{
						ActionMaper.put(tr, ActionMaper.get(tr)+1);
					}
					
			 }
			 
		
		
	
		Activity[] activitiesArray = new Activity[ActivitiesSize];
		
		for (int i = 0; i < activitiesArray.length; i++) {
			Activity tempActivity= new Activity(0,0); 
			tempActivity= new Activity(i,ActivityMaper.get(Activities[i]),ActivityContextMaper.get(Activities[i]));
			activitiesArray[i]= tempActivity;
		}

		 
		
				
		
		 List<String> deletedActivityList = new ArrayList<String>();
			for (int i = 0; i < activitiesArray.length; i++) {
				if(parameters.getSamplingType()==SamplingType.Similar) {
					Arrays.sort(activitiesArray, new ActivityComparatorInt() );
					if ((i*1.0/activitiesArray.length  )>= parameters.getSubsequenceLength()* 0.01) {
						deletedActivityList.add(Activities[activitiesArray[i].getIndex()]);
					}
				}else	if(parameters.getSamplingType()==SamplingType.Frequency) {
					if(ActivityMaper.get(Activities[i])*1.0/InputLog.size() >=parameters.getSubsequenceLength()* 0.01 )
						deletedActivityList.add(Activities[i]);
				}
				else if (parameters.getSamplingType()==SamplingType.Penalty) {
					Arrays.sort(activitiesArray, new ActivityComparatorDoubleREverese() );
					if ((i*1.0/activitiesArray.length  )>= parameters.getSubsequenceLength()* 0.01) {
						deletedActivityList.add(Activities[activitiesArray[i].getIndex()]);
					}
				}
			}
			 /////////////////Sampling ///////////////
	
				int index=0;
			for (XTrace trace : InputLog) { // for each trace
				XAttributeMapImpl case_map = new XAttributeMapImpl();
				String case_id = String.valueOf(index++);
				case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
				XTraceImpl tempTrace = new XTraceImpl(case_map);
				 for (XEvent event : trace) { 
					 if(!deletedActivityList.contains(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString())){
						 tempTrace.add(event);
					 }
				 }
				 if(tempTrace.size()>0)
					 outputLog2.add(tempTrace);
			}
			return outputLog2;
			
			
			
	}
    static private double jaccardSimilarity(int[] a, int[] b) {

        Set<Integer> s1 = new HashSet<Integer>();
        for (int i = 0; i < a.length; i++) {
            s1.add(a[i]);
        }
        Set<Integer> s2 = new HashSet<Integer>();
        for (int i = 0; i < b.length; i++) {
            s2.add(b[i]);
        }

        final int sa = s1.size();
        final int sb = s2.size();
        s1.retainAll(s2);
        final int intersection = s1.size();
        return 1d / (sa + sb - intersection) * intersection;
    }

    
    static private double DiffSimilarity(int[] a, int[] b) {
    	int Tdf=0;
    	int Sdf=0;
        for (int i = 0; i < b.length; i++) {
			if (a[i]>0 || b[i]>0) {
				if (a[i]>0 && b[i]>0) {
				Sdf++;	
				}
				Tdf++;
			}
        
		}
        
        return  (double)(Sdf*1.0)/Tdf;
    }

	static class Activity
	{
	    private int ActivityIndex;
	    private int IntScore;
	    private double DoubleScore;
	    
	    public Activity()
	    {
	    	
	    	 	this.ActivityIndex = 0;
		        this.IntScore = 0;
		        this.DoubleScore=0.0;
	    }
	    public Activity(int ind, int intScore)
	    {
	       
	        this.ActivityIndex = ind;
	        this.IntScore = intScore;
	        this.DoubleScore=0.0;
	    } 
	    public Activity(int ind, double doubleScore)
	    {
	       
	        this.ActivityIndex = ind;
	        this.IntScore = 0;
	        this.DoubleScore=doubleScore;
	    } 
	    public Activity(int ind, int intScore,double doubleScore)
	    {
	       
	        this.ActivityIndex = ind;
	        this.IntScore = intScore;
	        this.DoubleScore=doubleScore;
	    }
		public int getIntScore() {
			// TODO Auto-generated method stub
			return IntScore;
		} 
		public double getDoubleScore() {
			// TODO Auto-generated method stub
			return DoubleScore;
		} 
		public int getIndex() {
			// TODO Auto-generated method stub
			return ActivityIndex;
		} 
	}
}

class ActivityComparatorDouble implements Comparator<Activity> {
	 
    @Override
    public int compare(Activity var1, Activity var2) {
        return (int) ((var2.getDoubleScore()*10000)- (var1.getDoubleScore() *10000));
    }
}
class ActivityComparatorDoubleREverese implements Comparator<Activity> {
	 
    @Override
    public int compare(Activity var1, Activity var2) {
        return (int) ((var1.getDoubleScore()*10000)- (var2.getDoubleScore() *10000));
    }
}
class ActivityComparatorInt implements Comparator<Activity> {
	 
    @Override
    public int compare(Activity var1, Activity var2) {
        return  var2.getIntScore() - var1.getIntScore() ;
    }

}
class ActivityComparatorIntREverese implements Comparator<Activity> {
  	 
    @Override
    public int compare(Activity var1, Activity var2) {
        return   var1.getIntScore()- var2.getIntScore();
    }
    
}

    

	