package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
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
import org.deckfour.xes.extension.XExtension;
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

public class SampleVariants4CrossValidation {

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
		 switch (samplingType) {
			 case RandomSamplingWithoutLooking:
				 
				 switch (returnType) {
					case Traces :
						while (chCount < parameters.getSubsequenceLength() * InputLogSize * 0.01)  {
							/*int index =ThreadLocalRandom.current().nextInt(0, TraceNumber.size());
							outputLog2.add(InputLog.get(TraceNumber.get(index)));
							TraceNumber.remove(TraceNumber.get(index));// Do not select this trace again 
*/							int randomTrace=generator.nextInt(InputLogSize-chCount);
							pickedTraces.add(randomTrace);
							 chCount++;
							 
							}//while
						
						break;

					case Variants :
						while (chCount < parameters.getSubsequenceLength() * InputLogSize * 0.01)  {
							//int index =ThreadLocalRandom.current().nextInt(0, TraceNumber.size());
							int randomTrace=generator.nextInt(InputLogSize-chCount);
							XTrace tempTrace =  InputLog.get(randomTrace);
							String[] Trace = new String[tempTrace.size()];
							List<String> templist = new ArrayList<String>();
							for (XEvent event : tempTrace) { 
								eventAttributeMap = event.getAttributes();
								templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
							}
							Trace = templist.toArray(new String[tempTrace.size()]);
							String tr= "";
							for (int i =0; i < Trace.length; i++){
								tr= tr.concat(Trace[i]);
							}
							
									if (ActionMaper.get(tr)==null ){
										ActionMaper.put(tr, 1);
										pickedTraces.add(randomTrace);
										//outputLog2.add(tempTrace);
									}
								
									
									//TraceNumber.remove(TraceNumber.get(index));//Do not select this trace again!
								 chCount++;// in any case (even if we had that variant) we are counting 
							}//while
						break;
				}  //switch
				 
				
		
				 while(!pickedTraces.isEmpty()){
						//build new log with same order of traces as in original log
						outputLog2.add(InputLog.get(pickedTraces.poll()));
					}
					 System.out.println(	System.currentTimeMillis() - time);	
					return outputLog2;	 
	}
		
		
		 
		 
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
		 
		
		if(samplingType!= SamplingType.RandomSamplingWithoutLooking) {
		
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
					}
					
			 }
			 
		
		
		 int VariantSize = ActionMaper.size();
		 int Threshold = (int) (VariantSize * (double)(80*1.0)/100) ;  
		 int [][] VariantVectors = new int[VariantSize] [DirectlyFollowMaper.size()];
		 int[] VariantAggrigation = new int[VariantSize];
		 double [] VariantSimlartyScore = new double [VariantSize];
		 double [] VariantHybridScore = new double [VariantSize]; 
		 int [] VariantLength= new int [VariantSize];
		 VariantFreq= new int[VariantSize];
		 int [] VariantsPenalty= new int [VariantSize];
		 Variant[] Variants = new Variant[VariantSize];
		 double[] VariantSimilarity= new double[VariantSize];
 
			 
			 
			
			 
		 String logName="BPIC2012";
		 int crossValidationK=5; 
		 int batch = InputLog.size()/crossValidationK;
		 
		 for (int j = 0; j < crossValidationK; j++) {
			 int counter=0;
			 int tcounter=0;
			 int tscounter=0;
			 XLog trainLogs=factory.createLog();
			 trainLogs.setAttributes(InputLog.getAttributes());
			 XLog testLogs=factory.createLog();
			 testLogs.setAttributes(InputLog.getAttributes());
			 for (XTrace trace : InputLog) { // for each trace
				 if(counter>=j*batch && counter<(j+1)*batch  ) {
					 trainLogs.add(trace); 
					 tcounter++;
				 }else {
					 testLogs.add(trace); 
					 tscounter++;
				 }
				 
				 counter++;
			 }
			 MatrixFilterParameter  mp= new MatrixFilterParameter();
			 mp.setDicoveryMethod("D:\\PHD\\papers\\2021\\predict\\cross\\"+logName+"_train_"+j+"_Log3Distribution"+".csv");
			 XLog alaki = SampleVariants2ForPrediction.apply(trainLogs, mp);
			 mp.setDicoveryMethod("D:\\PHD\\papers\\2021\\predict\\cross\\"+logName+"_test_"+j+".csv");
			// alaki = SampleVariants2ForPrediction.apply(testLogs, mp);
				//out = new FileOutputStream(new File("D:\\PHD\\papers\\2021\\predict\\"+logName+"_train_"+j+".xes"));

			 

		 }
	
			 /////////////////Sampling ///////////////
			 int sum=0;
			 int checker=0;
			
			switch (returnType) {						
				case Traces:
					for (int i=0; i< chCount; i++){
						 if (sum<= LogSize*((double)(parameters.getSubsequenceLength()*1.0)/100)  && checker ==0  ){
							 sum = sum+ ActionMaper.get(ReverseMapper.get(Variants[i].getIndex()));
							 FilterHashMaper.put(ReverseMapper.get(Variants[i].getIndex()), i);
						 }else if(sum  >  LogSize*((double)(parameters.getSubsequenceLength()*1.0)/100)){							 
							 checker++;
						 }
					 }
					
					break;
			}
		}	 			 

			for (XExtension extension : InputLog.getExtensions()){
				outputLog2.getExtensions().add(extension);
				}
			int counterTrace=0;
			for (XTrace trace : InputLog) { // for each trace
				if (counterTrace< LogSize *parameters.getSubsequenceLength()*1.0/100 ) {
					outputLog2.add(trace);
				}
				counterTrace++;
			}
			
		
			 ///////////////////////////Filtering //////////

			int sum=0;
				
			switch (returnType) {
			
				case Traces:
					for (XExtension extension : InputLog.getExtensions()){
						outputLog2.getExtensions().add(extension);
						}
					for (XTrace trace : InputLog) { // for each trace
						 /// Put trace to array
						 String[] Trace = new String[trace.size()];
							List<String> templist = new ArrayList<String>();
							for (XEvent event : trace) { 
								eventAttributeMap = event.getAttributes();
								templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
							}
							Trace = templist.toArray(new String[trace.size()]);
							String tr= "";
							for (int i =0; i < Trace.length; i++){
								tr= tr.concat(Trace[i]).concat("=>");
							}
							if (FilterHashMaper.get(tr)!=null && sum <= LogSize*((double)(parameters.getSubsequenceLength()*1.0)/100) ){
								outputLog2.add(trace);
								sum++;
							}
					 }
					break;
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


    

    

	