package org.processmining.logfiltering.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SamplingType;
import org.processmining.logfiltering.parameters.AdjustingType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.ProbabilityType;
import org.processmining.logfiltering.parameters.SamplingReturnType;

public class SampleVariants2ForPrediction {

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
		HashMap<Integer,ArrayList<XTrace>> VariantMapperList =new HashMap<Integer, ArrayList<XTrace>>();
		HashMap<Integer, Integer> SelectedList= new HashMap<Integer, Integer>();
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
		/* Random generator2 = new Random(System.currentTimeMillis());
		 int randomnumber=generator2.nextInt(100);
		 for (double i = randomnumber; i < InputLog.size(); i+=((double)100/parameters.getSubsequenceLength())) {
			 pickedTraces.add((int)i);
			}
		 while(!pickedTraces.isEmpty()){
				//build new log with same order of traces as in original log
				outputLog2.add(InputLog.get(pickedTraces.poll()));
			}
			 
			 System.out.println(	System.currentTimeMillis() - time);
			 if(InputLog.size()>5)
				 return outputLog2;	*/ 
		 
		 Random generator = new Random(System.currentTimeMillis());
		 int InputLogSize= InputLog.size();
		
		
		 
		 
		//int ActivitiesSize = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]).size();
		int ActivitiesSize = ActivitySet.size();
		//Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
		String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
		List<String> ActivityList = java.util.Arrays.asList(Activities);
		int[] ActivityCount = new int[ActivitiesSize];
		String temp1 = new String();
		String temp2 = new String();
		String temp3 = new String();
		FilterLevel FilteringMethod = parameters.getFilterLevel();
		FilterSelection FilteringSelection =parameters.getFilteringSelection();
		HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
		HashMap<String,Integer >FilterHashMaper =new HashMap<String, Integer>();
		HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
		
		HashMap<String, Integer> DirectlyFollowMaper= new HashMap<String, Integer>();
		HashMap<String, Integer> DirectlyFollowMaperCounter= new HashMap<String, Integer>();
		HashMap<String, Integer> DirectlyFollowMapperCounter= new HashMap<String, Integer>();
		HashMap<Integer, String> RevereseDirectlyFollowMaper= new HashMap<Integer, String>();
		ArrayList<int[]> action = new ArrayList<int[]>();
		ArrayList<int[]> WinAction = new ArrayList<int []>();
		AdjustingType adjustingType = parameters.getAdjustingThresholdMethod();
		ProbabilityType probabilityType= parameters.getProbabilitycomutingMethod();
		int SimilarityWindow=3;
		for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
			ActivitySet.add(clazz.toString());
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
		 
		

		
		for (XTrace trace : InputLog) { // for each trace
				 LogSize++;
				 /// Put trace to array
				 String[] Trace = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) { 
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Trace = templist.toArray(new String[trace.size()]);
					String tr= "";
					int WSCounter=0;
					for (int i =0; i < Trace.length; i++){
						tr= tr.concat(Trace[i]).concat("=>");
					}
					int SubPatternLength=2;
					String Subpattern="";
					if (parameters.getSamplingType()==SamplingType.Penalty ) {//|| parameters.getSamplingType()==SamplingType.Penalty2) {
					for (int l = 0; l <= SubPatternLength; l++) {
						String temp;
						for (int i = 0; i < Trace.length-l; i++) {
							Subpattern="";
							for(int k=0; k<l;k++) {
								Subpattern=Subpattern.concat(Trace[i+k].concat("=>"));
							}
							if(WindowsSequenceCounter.get(Subpattern)==null) {
								WindowsSequenceCounter.put(Subpattern, 1);
								WindowsSequenceMaper.put(Subpattern, WSCounter);
								RevereseWindowsSequenceMaper.put(WSCounter, Subpattern);
								WSCounter++;
							}else {
								WindowsSequenceCounter.put(Subpattern, WindowsSequenceCounter.get(Subpattern)+1);
							}
							String LContext="";
							String RContext="";
							if(i==0) {
								LContext="ArtificialStart";
							}else {
								LContext=Trace[i-1];
							}
							if(i+l==Trace.length-1) {
								RContext="ArtificialEnd";
							}else {
								RContext=Trace[i+l];
							}
							String Context= LContext .concat("++").concat(RContext);
							if(ContextCounter.get(Context)==null) {
								ContextCounter.put(Context, 1);
								ContextMaper.put(Context, contextCounter);
								RevereseContextMaper.put(contextCounter, Context);
								contextCounter++;
							}else {
								ContextCounter.put(Context, ContextCounter.get(Context)+1);
							}
							String Structure = Context.concat("77").concat(Subpattern);
							if(StractureCounter.get(Structure)==null) {
								StractureCounter.put(Structure, 1);
								StractureMaper.put(Structure, structureCounter);
								RevereseStractureMaper.put(structureCounter, Structure);
								structureCounter++;
							}else {
								StractureCounter.put(Structure, StractureCounter.get(Structure)+1);
							}
						} // for length of trace
					}//for subpattern length 
					}// end if SamplingType.Penalty
					if (ActionMaper.get(tr)==null ){
						ActionMaper.put(tr,1);
						ReverseMapper.put(chCount, tr);
						HashMaper.put(tr, chCount);
						VariantMapper.put(chCount, trace);
						 ArrayList<XTrace> tempList = new ArrayList<XTrace>();
						 tempList.add(trace);
						VariantMapperList.put(chCount, tempList);
						chCount++;
						
						
					
						
						if (parameters.getSamplingType()==SamplingType.Similar||parameters.getSamplingType()==SamplingType.Hybrid ||parameters.getSamplingType()==SamplingType.ProtoTyping   ) {	
						for (int i = 0; i < Trace.length; i++) {
							String temp;
							
							if (i==0) {
								 temp= "ArtStart" + Trace[i];
							}else {
								 temp= Trace[i-1]+ Trace[i];
							}
							
							 if (DirectlyFollowMaper.get(temp)==null ) {
								 DirectlyFollowMaperCounter.put(temp, DFcounter);
								 DirectlyFollowMaper.put(temp, 1);
								 DirectlyFollowMapperCounter.put(temp, directCount);
								 RevereseDirectlyFollowMaper.put(directCount, temp);
								 directCount++;
								 DFcounter++;
							 }
							 else {
								DirectlyFollowMaper.put(temp, DirectlyFollowMaper.get(temp)+1);
							}
							if (i==Trace.length-1) {
								temp=   Trace[i]+ "ArtEnd";
								if (DirectlyFollowMaper.get(temp)==null ) {
									 DirectlyFollowMaper.put(temp, 1);
									 DirectlyFollowMaperCounter.put(temp, DFcounter);
									 DirectlyFollowMapperCounter.put(temp, directCount);
									 RevereseDirectlyFollowMaper.put(directCount, temp);
									 directCount++;
									 DFcounter++;
								 }
								 else {
									DirectlyFollowMaper.put(temp, DirectlyFollowMaper.get(temp)+1);
								}
							}
						}
						}// if Similarity Type
						
					}else{
						ActionMaper.put(tr, ActionMaper.get(tr)+1);
						ArrayList<XTrace> tempList = VariantMapperList.get(HashMaper.get(tr));
						 tempList.add(trace);
						VariantMapperList.put(HashMaper.get(tr), tempList);
					}
					
			 }
			 
		
		
		 int VariantSize = ActionMaper.size();
		 int Threshold = (int) (VariantSize * (double)(80*1.0)/100) ;  


		 double [] VariantConsideration = new double [VariantSize];
		 double [] VariantHybridScore = new double [VariantSize]; 
		 int [] VariantLength= new int [VariantSize];
		 VariantFreq= new int[VariantSize];
		 int [] VariantsPenalty= new int [VariantSize];
		 Variant[] Variants = new Variant[VariantSize];
		 double[] VariantSimilarity= new double[VariantSize];
		
			int samplingUnit=90000000;
			 for (int i = 0; i < VariantFreq.length; i++) {
				 VariantFreq[i]= ActionMaper.get(ReverseMapper.get(i));
				if(VariantFreq[i] > 0) {//LogSize*0.001) {
					VariantConsideration[i]=1;
					if(samplingUnit> VariantFreq[i])
						samplingUnit=VariantFreq[i]; 
				}else {
					VariantConsideration[i]=0;
				}
			}
		
		
			

			
			
		
			
	
			 /////////////////Sampling ///////////////
	
			


			 ///////////////////////////Filtering //////////
			 for (int i = 0; i < VariantSimilarity.length; i++) {
				int samplingSize=0; 
				if(VariantConsideration[i]==1) {
					//samplingSize= Math.round(VariantFreq[i]/samplingUnit); 
					samplingSize= 1; 
					//samplingSize= (int) Math.round(Math.log10(VariantFreq[i])); 
					for (int j = 0; j < samplingSize; j++) {
						Random rand = new Random();
					    outputLog2.add(VariantMapperList.get(i).get(rand.nextInt(VariantMapperList.get(i).size())));
		
					}
				}
			}
		
				
			 
			 
			 
			 
			 //////////////////////////////Conversion 
			 Map<String, String> liveCycleMap = new HashMap<String, String>(); ////// new structure for saving time of starts 
				Map<String, Integer>  activityCaseHappenStart = new HashMap<String, Integer>();/// contain the activity or not 
				Map<String, Integer>  activityCaseHappenComplete = new HashMap<String, Integer>();/// contain the activity or not 
				String csvName=InputLog.getAttributes().get("concept:name").toString();
				PrintWriter writer = null;
				try {
					writer = new PrintWriter(new File("D:\\PHD\\papers\\2021\\Predict\\BPIC_2012_unique_Variant_Training.csv"));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				StringBuilder sb = new StringBuilder();
			      sb.append("CASE_ID");
			      sb.append(',');
			      sb.append("Activity");
			      sb.append(',');
			      sb.append("Resource");
			      sb.append(',');
			      sb.append("StartTimestamp");
			      sb.append(',');
			      sb.append("Amount");
			      sb.append(',');
			      sb.append("CompleteTimestamp");
			      sb.append(',');
			      sb.append("Duration");
			      sb.append('\n');

			     


			      SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			      TimeUnit timeUnit = TimeUnit.MINUTES;
			      long duration; 
				for(XTrace trace : outputLog2){
					XAttributeMap attributes = trace.getAttributes();
					LogSize++;
				      
					int activityCounter= 0;
					
					//ReverseSequenecDatabase=-2 + "\r\n"+ ReverseSequenecDatabase ;
					for (XEvent event : trace){
						String activity= event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
						String  activityCase= LogSize+ activity;
						if (event.getAttributes().get("lifecycle:transition").toString().equals("START")){
							
							 
							
							if(activityCaseHappenStart.get(activityCase)==null) {
								activityCaseHappenStart.put(activityCase, 0);
								liveCycleMap.put(activityCase+activityCaseHappenStart.get(activityCase), event.getAttributes().get("time:timestamp").toString());
							}else {
								activityCaseHappenStart.put(activityCase, activityCaseHappenStart.get(activityCase)+1);
								liveCycleMap.put(activityCase+activityCaseHappenStart.get(activityCase), event.getAttributes().get("time:timestamp").toString());

							}
						}
						
						if (event.getAttributes().get("lifecycle:transition").toString().equals("COMPLETE")
								&&   activity.substring(0,1).equals("W") ){ 
							if (activityCaseHappenComplete.get(LogSize+activity)==null) {
								activityCaseHappenComplete.put(LogSize+activity, 0);
								
							}else {
								activityCaseHappenComplete.put(LogSize+activity, activityCaseHappenComplete.get(LogSize+activity)+ 1);
							}
						
						  sb.append(LogSize);
					      sb.append(',');
					      sb.append(activity);
					      sb.append(','); 
					      String tempTime="";
					      if(event.getAttributes().containsKey("org:resource"))
					    	  sb.append(event.getAttributes().get("org:resource").toString());
					      else
					    	  sb.append("no resuource");
					      sb.append(',');
					    
					      if(liveCycleMap.get(activityCase+activityCaseHappenStart.get(activityCase))==null) { ///// we do not have any start time
					      if(event.getAttributes().containsKey("time:timestamp")) {
					    	  tempTime= event.getAttributes().get("time:timestamp").toString().substring(0,19).replace('T', ' ').replace('-', '.');
					    	  sb.append(tempTime);
					      }
					      else
					    	  sb.append("");
					      sb.append(',');
					      if(trace.getAttributes().containsKey("AMOUNT_REQ")) //////////////////Just for BPIC 2012
					    	  sb.append(trace.getAttributes().get("AMOUNT_REQ").toString());
					      else
					    	  sb.append("");
					      sb.append(',');
					      if(event.getAttributes().containsKey("time:timestamp")) {
					    	  tempTime= event.getAttributes().get("time:timestamp").toString().substring(0,19).replace('T', ' ').replace('-', '.');
					    	  sb.append(tempTime);
					      }
					      else
					    	  sb.append("");
					      
					      sb.append(',');
					      sb.append(0);
					      
					      }else { ////if we have start time 
					    	  Date dateStart = null;
					    	  Date dateComplete = null;
					    	  if(event.getAttributes().containsKey("time:timestamp")) {
					    		  tempTime= liveCycleMap.get(activityCase+activityCaseHappenStart.get(activityCase)).toString().substring(0,19).replace('T', ' ').replace('-', '.');
					    		  sb.append(tempTime);
					    		  try {
									dateStart=format.parse(tempTime);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						      }
						      else
						    	  sb.append("");
						      sb.append(',');
						      if(trace.getAttributes().containsKey("AMOUNT_REQ")) //////////////////Just for BPIC 2012
						    	  sb.append(trace.getAttributes().get("AMOUNT_REQ").toString());
						      else
						    	  sb.append("");
						      sb.append(',');
						      if(event.getAttributes().containsKey("time:timestamp")) {
						    	  tempTime= event.getAttributes().get("time:timestamp").toString().substring(0,19).replace('T', ' ').replace('-', '.');
						    	  sb.append(tempTime);
						    	  try {
									dateComplete=format.parse(tempTime);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						      }
						      else
						    	  sb.append("");
						      
						      sb.append(',');

						      long difference = dateComplete.getTime() - dateStart.getTime();
						      
						      if (difference>=0) {
						    	  duration=	timeUnit.convert(difference,TimeUnit.MILLISECONDS);
						      }else {
						    	  duration= timeUnit.convert(difference,TimeUnit.MILLISECONDS);
						      }
						      sb.append(duration);
					      }
					      
					     
					      sb.append('\n');
					      
					    	 
						 }
						
						}
					
		 			
				}
				
					
				  writer.write(sb.toString());
				  writer.close();
			      System.out.println("done!");


				
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

	static class Variant
	{
	    private int VariantIndex;
	    private int IntScore;
	    private double DoubleScore;
	    
	    public Variant()
	    {
	    	
	    	 	this.VariantIndex = 0;
		        this.IntScore = 0;
		        this.DoubleScore=0.0;
	    }
	    public Variant(int ind, int intScore)
	    {
	       
	        this.VariantIndex = ind;
	        this.IntScore = intScore;
	        this.DoubleScore=0.0;
	    } 
	    public Variant(int ind, double doubleScore)
	    {
	       
	        this.VariantIndex = ind;
	        this.IntScore = 0;
	        this.DoubleScore=doubleScore;
	    } 
	    public Variant(int ind, int intScore,double doubleScore)
	    {
	       
	        this.VariantIndex = ind;
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
			return VariantIndex;
		} 
	}
}


	