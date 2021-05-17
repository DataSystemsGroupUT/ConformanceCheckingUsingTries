package org.processmining.logfiltering.algorithms;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.Juan.algo.Juan.PetriNetTools;
import org.processmining.logfiltering.algorithms.ICC.AlignmentReplayResult;
import org.processmining.logfiltering.algorithms.ICC.AlignmentReplayer;
import org.processmining.logfiltering.algorithms.ICC.ApproxAlignmentReplayer;
import org.processmining.logfiltering.algorithms.ICC.ApproxFitnessReplayer;
import org.processmining.logfiltering.algorithms.ICC.FitnessReplayer;
import org.processmining.logfiltering.algorithms.ICC.IccParameters;
import org.processmining.logfiltering.algorithms.ICC.IccResult;
import org.processmining.logfiltering.algorithms.ICC.IncrementalConformanceChecker;
import org.processmining.logfiltering.algorithms.ICC.IncrementalReplayer;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
public class ConformanceBoundApproximationAlgo {

	public static String apply(XLog inputLog,Petrinet net, MatrixFilterParameter parameters,TransEvClassMapping mapping2) {
			long time = System.currentTimeMillis();
			long time0 = System.currentTimeMillis();
			XLog InputLog= (XLog) inputLog.clone();
			XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
			XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
			TransEvClassMapping mapping = constructMapping(net, InputLog, dummyEvClass, eventClassifier);
			
			XEventClassifier EventCol = parameters.getEventClassifier();
			
		    /*	if (parameters.getPrototypeType()==PrototypeType.Simulation) {
				mapping=mapping2;
			}*/
			
			
			
			 ///////////////////////////Compute the Replay Values///////////////////
			Multiset<String> asynchronousMoveBag=TreeMultiset.create();
			Marking initialMarking = getInitialMarking(net);
			Marking finalMarking = getFinalMarking(net);
			int nThreads = 10;
			
			int costUpperBound = Integer.MAX_VALUE;
			// timeout per trace in milliseconds
			int timeoutMilliseconds = 10 * 1000;
			// preprocessing time to be added to the statistics if necessary
			long preProcessTimeNanoseconds = 0;
			XLogInfo summary = XLogInfoFactory.createLogInfo(InputLog, eventClassifier);
			XEventClasses classes = summary.getEventClasses();
			
			
			
			
			
			XFactory factory = XFactoryRegistry.instance().currentDefault();
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
			HashMap<String,String >ActivityCoder =new HashMap<String, String>();
			HashMap<String,String >ActivityDeCoder =new HashMap<String, String>();
			HashMap<String, Integer> AsyncrousMoves= new HashMap<String, Integer>();
			HashMap<String, Double> AsyncrousDistribution= new HashMap<String, Double>();
			HashMap<String, Integer> SyncrousMoves= new HashMap<String, Integer>();
			HashMap<String, Integer> ModelMoves= new HashMap<String, Integer>();
			HashMap<String, Integer> LogMoves= new HashMap<String, Integer>();
			int LogSize = 0;
			PriorityQueue<Integer> pickedVariant=new PriorityQueue<>();
			SortedSet<String> eventAttributeSet = new TreeSet<String>();
			XAttributeMap eventAttributeMap;
			int KLength =parameters.getSubsequenceLength(); 
			int charcounter=65;
			ActivityCoder.put("ArtStart", Character.toString((char)charcounter));
			ActivityDeCoder.put(Character.toString((char)charcounter), "ArtStart");
			Set<String> ActivitySet = new HashSet<String>();
			 HashMap<String,Integer >ActivityCounter =new HashMap<String, Integer>();
			
			for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
				charcounter++;
				ActivitySet.add(clazz.toString());
				ActivityCoder.put(clazz.toString(), Character.toString((char)charcounter));
				ActivityDeCoder.put( Character.toString((char)charcounter),clazz.toString());
				ActivityCounter.put( Character.toString((char)charcounter),0);

				SyncrousMoves.put ( clazz.toString(), 0);
				AsyncrousMoves.put ( clazz.toString(), 0);
				ModelMoves.put ( clazz.toString(), 0);
				LogMoves.put ( clazz.toString(), 0);
			}
			charcounter++;
			ActivityCoder.put("ArtEnd", Character.toString((char)charcounter));
			ActivityDeCoder.put( Character.toString((char)charcounter),"ArtEnd");
			int ActivitiesSize = ActivitySet.size();
			//Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
			String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
			List<String> ActivityList = java.util.Arrays.asList(Activities);
			int[] ActivityCount = new int[ActivitiesSize];
			FilterLevel FilteringMethod = parameters.getFilterLevel();
			FilterSelection FilteringSelection =parameters.getFilteringSelection();
			HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
			HashMap<String,String >TraceHash =new HashMap<String, String>();
			HashMap<String,String >TraceHashReverse =new HashMap<String, String>();
			HashMap<Integer,String >tempMapper =new HashMap<Integer,String>();
			HashMap<String,Integer >HashTraceCounter =new HashMap<String, Integer>();
			HashMap<String,Integer >FilterHashMaper =new HashMap<String, Integer>();
			HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
			HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
			HashMap<String, Integer> DFrelationMapper= new HashMap<String, Integer>();
			HashMap<Integer, String> DFrelationReverseMapper =new HashMap<Integer, String>();
			HashMap<Integer, XTrace> VariantMapper =new HashMap<Integer, XTrace>();
			int chCount=0;
			int dfCount=0;
			HashMap<Integer,List<Integer> >Clusters =new HashMap<Integer,List<Integer>>();
			HashMap<Integer,Integer>SelectedList =new HashMap<Integer,Integer>();
			HashMap<Integer,Integer>SelectedListTemp =new HashMap<Integer,Integer>();
			HashMap<String,Integer >ModelBehaviorSim =new HashMap<String, Integer>();
			HashMap<Integer,String >ModelBehaviorSimReverse =new HashMap< Integer,String>();
			HashMap<String,String >ReducedVariants =new HashMap<String,String>();
			HashMap<Integer,String >charecterTraces =new HashMap<Integer,String>(); 
			 int avgTraceLength= 0;
			 int maxTraceLength=-1;
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
					String tr= Trace[0];
					String TraceinChar=ActivityCoder.get(tr);
					ActivityCounter.put(ActivityCoder.get(Trace[0]), ActivityCounter.get(ActivityCoder.get(Trace[0]))+1);
					for (int i =1; i < Trace.length; i++){
						tr= tr.concat("=>"+Trace[i]);
						TraceinChar= TraceinChar.concat(ActivityCoder.get(Trace[i]));
						ActivityCounter.put(ActivityCoder.get(Trace[i]), ActivityCounter.get(ActivityCoder.get(Trace[i]))+1);
					}
					//TraceinChar= TraceinChar.concat(ActivityCoder.get("ArtEnd"));
					//= tr.concat("=>"+"ArtEnd");
					if (ActionMaper.get(tr)==null ){
						ActionMaper.put(tr,1);
						ReverseMapper.put(chCount, tr);
						HashMaper.put(tr, chCount);
						HashTraceCounter.put(TraceinChar, 1);
						TraceHash.put(tr, TraceinChar);
						TraceHashReverse.put(TraceinChar, tr);
						VariantMapper.put(chCount, trace);
						charecterTraces.put(chCount, TraceinChar);
						chCount++;
						avgTraceLength=avgTraceLength+ TraceinChar.length();
						if(TraceinChar.length()>maxTraceLength)
							maxTraceLength=TraceinChar.length();
						}else{
						ActionMaper.put(tr, ActionMaper.get(tr)+1);
						HashTraceCounter.put(TraceinChar, HashTraceCounter.get(TraceinChar)+1);
					}
			 }
			 PetriNetTools netTools = new PetriNetTools(net, ActivityCoder);
			 String netActivities = netTools.getNetActivities();
			 Pattern repeatingPattern = Pattern.compile("(.)\\1{2,}");
			 Pattern special = Pattern.compile("[^A-Za-z0-9]");
			 netActivities = special.matcher(netActivities).replaceAll("\\\\$0");
			 HashMap<String,Integer> lbCosts = new HashMap<String,Integer>();
			 double uniqueness = (double)charecterTraces.size()/InputLog.size();
		

			 long logScanningTime= System.currentTimeMillis() - time;
			 
			 float [] VariantFreq= new float[HashMaper.size()];
			 float [] VariantLength= new float[HashMaper.size()];
			 int[] VariantInd= new int[HashMaper.size()];
			 for (int i = 0; i < VariantFreq.length; i++) {
				 VariantFreq[i]= ActionMaper.get(ReverseMapper.get(i)); 
				 VariantLength[i]= charecterTraces.get(i).length();
				 VariantInd[i]=i;
				 String TraceinChar= TraceHash.get(ReverseMapper.get(i));
				 char[] DFRelations=TraceinChar.toCharArray();
					String DF= "";
					for (int j = 0; j < DFRelations.length-1; j++) {
						DF= Character.toString(DFRelations[j])+Character.toString(DFRelations[j+1]);
						if(DFrelationMapper.get(DF)==null) {
							DFrelationMapper.put(DF,dfCount);
							DFrelationReverseMapper.put(dfCount,DF);
							dfCount++;
						}
					}
			}
			 HashMap<String,Integer> count = new HashMap<String,Integer>();
			 for (int i = 0; i < VariantFreq.length; i++) {
				String var = 	charecterTraces.get(i);
		
					String DF = "";
					String codedVariant = var;					
					//Keep only activities that are present in the Petri net
					codedVariant = codedVariant.replaceAll("[^" +netActivities+"]", "");
					lbCosts.put(var, var.length()-codedVariant.length());			
					
					//Remove single loop repetitions, replace them by only 2 repetitions
					Matcher matcher = repeatingPattern.matcher(codedVariant);
					codedVariant = matcher.replaceAll("$1$1");
					
					for(char a: codedVariant.toCharArray()) {
						String str = Character.toString(a);
						if(!count.containsKey(str))
							count.put(str, 1);
						else
							count.put(str, count.get(str)+1);
					}
			 
			 }
			 
			 int[][] VariantProfile = new int [HashMaper.size()][DFrelationMapper.size()];
			 String[] VariantChar = new String[HashMaper.size()];
			 for (int i = 0; i < VariantFreq.length; i++) {
				 String TraceinChar= TraceHash.get(ReverseMapper.get(i));
				 VariantChar[i]=TraceinChar;
				 char[] DFRelations=TraceinChar.toCharArray();
					String DF= "";
					for (int j = 0; j < DFRelations.length-1; j++) {
						DF= Character.toString(DFRelations[j])+Character.toString(DFRelations[j+1]);
						VariantProfile[i][DFrelationMapper.get(DF)]= DFrelationMapper.get(DF)+1; 
					}
			 }
			 
			 
			 double Threshold =1- parameters.getSubsequenceLength()*1.0/100;

				
			 avgTraceLength= avgTraceLength/ReverseMapper.size();
			
			 double[] alignmentComputedCosts= new double [VariantChar.length];
			 double INTRA= 0;
			 Variant[] VariantSimilarity = new Variant[HashMaper.size()];
			 double[] [] distanceMatrix =new double[HashMaper.size()][HashMaper.size()];
			 if(parameters.getPrototypeType()==PrototypeType.KCenterApprox||parameters.getPrototypeType()==PrototypeType.Frequency|| parameters.getPrototypeType()==PrototypeType.KMeansClusteringApprox||parameters.getPrototypeType()== PrototypeType.FastAlignment||parameters.getPrototypeType()== PrototypeType.Length||parameters.getPrototypeType()== PrototypeType.Random) {
				 switch (parameters.getSimilarityMeasure()) {
						case Levenstein :
						
							for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
								 double minTemp= 1000;
									for (int j = 0; j < VariantProfile.length; j++) {
										double x = -1;// levenshteinDistanceCost (VariantChar[i],VariantChar[j]) ;
										if (i==j)
											x=0;
										temp= temp+ x;
										distanceMatrix[i][j]= x;
										if (minTemp> x && x>0)
											minTemp=x;
									}
									VariantSimilarity[i]= new Variant(i, temp );
									INTRA= INTRA+minTemp;
									//INTRA= INTRA+(temp*1.0/(VariantProfile.length-1));
								}
							INTRA=INTRA/(VariantProfile.length-1);
							break;
						/*case Incremental :
							for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
									for (int j = 0; j < VariantProfile.length; j++) {
										temp= temp+ levenshteinDistance (VariantChar[i],VariantChar[j]) ;
										distanceMatrix[i][j]= levenshteinDistance (VariantChar[i],VariantChar[j]);
									}
									VariantSimilarity[i]= new Variant(i, temp );
								}
							break;
						case Jacard:
							 for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
									for (int j = 0; j < VariantProfile.length; j++) {
										temp= temp+ jaccardSimilarity (VariantProfile[i],VariantProfile[j])  ;
										
									}
									VariantSimilarity[i]= new Variant(i, temp  );
								}
							break;
						case Difference:
							 for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
									for (int j = 0; j < VariantProfile.length; j++) {
										temp= temp+  DiffSimilarity (VariantProfile[i],VariantProfile[j]) ;
										
									}
									VariantSimilarity[i]= new Variant(i, temp  );
								}
							break;
							*/
					}
			 }
			 
			 
			 
			 List<Integer> SampledList=  new ArrayList<>();
			 List<Integer> SelectedUnfittedList=  new ArrayList<>();
			 int[] MostSimilarVariant= new int [HashMaper.size()];
			 
			 int counter= 0;
			
			  // reducedHashTraceCounter = HashTraceCounter.clone();
			 
			 
			 
				///////////////////////////////////////////////// measring variables///////////////////
			 double[] approximatedAlignmentCost= new double [VariantFreq.length];
			 double[] approximatedFitness= new double [VariantFreq.length];
			 double[] actualAlignmentCost= new double [VariantFreq.length];
			 double[] actualFitness= new double [VariantFreq.length];
			 double[] alignmentCostError= new double [VariantFreq.length];
			 double[] fitnessError=new double [VariantFreq.length];
			 double actualFitnessValue=0.0;
			 double approximatedFitnessValue=0.0;
			 double FitnessError=0.0;	 
			 
			 
		
	///////////////////////////Selection Phase///////////////////////////////// 
			//// Initialization 
			 long PreprocessTime = System.currentTimeMillis();
			 PrototypeType  SelectiuonType=parameters.getPrototypeType(); 
			 switch (SelectiuonType) {
				 case Normal:
					 for (int i = 0; i < VariantInd.length; i++) {
							
								SelectedList.put(i,VariantInd[i]);
									nThreads=6;
						}
					 break;
				 case First:
					 
					 for (int i = 0; i < VariantInd.length; i++) {
							if (i< (1- Threshold)* VariantInd.length) {
								SelectedList.put(i,VariantInd[i]);
							}else {	
								SampledList.add(VariantInd[i]);
							}
						}
					 break;
				 
				case Frequency :
					quicksort (VariantFreq.clone(), VariantInd);
					 counter=0;
					for (int i = 0; i < VariantInd.length; i++) {
						if (i< (Threshold)* VariantInd.length) {
							SampledList.add(VariantInd[i]);
						}else {
							SelectedList.put(counter,VariantInd[i]);
							counter++;
						}
						
					}
					break;

				case Random:
					 Random generator = new Random(System.currentTimeMillis());
					 chCount=0;
					 ArrayList<Integer> VariantNumber = new ArrayList<Integer>();
					 XLog outputLog2 = factory.createLog();
						outputLog2.setAttributes(InputLog.getAttributes());	
						for (int i = 0; i < VariantMapper.size(); i++) {
							VariantNumber.add(i);
						}
					 while (chCount < (1-Threshold) * VariantInd.length )  {
							int index =ThreadLocalRandom.current().nextInt(0, VariantNumber.size());
							//outputLog2.add(InputLog.get(VariantNumber.get(index)));
							SelectedList.put(chCount,VariantNumber.get(index));
							pickedVariant.add(VariantNumber.get(index));
							VariantNumber.remove(VariantNumber.get(index));// Do not select this trace again 
							
								
								
								chCount++;
							}//while
				  counter=0;
					 for (int i = 0; i < VariantInd.length; i++) {
							if (!pickedVariant.contains(i)) {
								SampledList.add(i);
							}
							
						}
					break;
					
				case KCenterApprox:		
			 for (int i = 0; i < VariantInd.length; i++) {
					SampledList.add(VariantInd[i]);
			}
	     	SelectedList.put(0,VariantInd[0]);
	     	SampledList.remove(VariantInd[0]);
	     	for (int i = 0; i <  (1-Threshold)* VariantInd.length; i++) {
	     		int TempDistance= -1;
	     		int TempIndex = -1;
	     		counter++;
	     		for (int j = 0; j < SampledList.size(); j++) {
	     			
	     			int computedDistance= 0;
						Integer tempNode = SampledList.get(j);
						
						for (int k = 0; k < SelectedList.size(); k++) {
							if(distanceMatrix [tempNode][k]<0) {
								distanceMatrix [tempNode][k]=levenshteinDistanceCost (VariantChar[tempNode],VariantChar[k]);
								distanceMatrix [k][tempNode]=distanceMatrix [tempNode][k];
							}
							computedDistance=(int) +  (distanceMatrix [tempNode][k] *VariantFreq[tempNode] ) ;
						}
						if (computedDistance >TempDistance ) {
							TempDistance= computedDistance;
							TempIndex=tempNode;
						}	
				}
	     		
	     		 SelectedList.put(counter,VariantInd[TempIndex]);	
	 	     	SampledList.remove(SampledList.indexOf(VariantInd[TempIndex]));
	     		
	     		
	     	}
	     	break;
				case KMeansClusteringApprox:
					 int K= (int) ((1-Threshold)* VariantInd.length);
					 
					 int iterations=2;
					 for (int i = 0; i < K ; i++) {
						 List<Integer> tempList = new ArrayList<>();
						 Clusters.put(i, tempList);
						 SelectedList.put(i,i);
					}////KMEDOIDS
					 for (int i = 0; i < iterations; i++) {
						 Clusters=  FindCluster(distanceMatrix,SelectedList,VariantChar);
						 SelectedList= UpdateKMedoids(SelectedList, distanceMatrix,VariantFreq,Clusters,VariantChar);
					}
					 for (int i = 0; i < SelectedList.size(); i++) {
						pickedVariant.add(SelectedList.get(i));
					}
					 for (int i = 0; i < VariantInd.length; i++) {
						 if (!pickedVariant.contains(i)) {
							 SampledList.add(i);
						 }
					 }
					 break;
				case FastAlignment:
				{
					XLog variantLog = factory.createLog();
					long PreprocessTime2 = System.currentTimeMillis() - time ;

					
					ReplayerParameters RepParameters2 = new ReplayerParameters.Default(nThreads*2, Math.round(parameters.getSubsequenceLength() / 10 ), Debug.NONE);
					 Replayer replayer = new Replayer(RepParameters2, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);
					 Future<TraceReplayTask>[] futures2 = new Future[VariantMapper.size()];
					 ExecutorService service2 = Executors.newFixedThreadPool(RepParameters2.nThreads);
					   
				
					 for (int i = 0; i < VariantMapper.size(); i++) {
						 variantLog.add(VariantMapper.get(i));
						 TraceReplayTask task = new TraceReplayTask(replayer, RepParameters2, VariantMapper.get(i), i, 2000,
						 RepParameters2.maximumNumberOfStates, preProcessTimeNanoseconds);
						 // submit for execution
						 futures2[i] = service2.submit(task);
					}
					
						chCount=0;
						int chCount2=0;
					 	for (int i = 0; i < variantLog.size(); i++) {
					 		TraceReplayTask result;
					 		try {
					 			
					 			result = futures2[i].get();
						 		SyncReplayResult replayResult = result.getSuccesfulResult();
						 		SelectedList.put(chCount2,i);
						 		alignmentComputedCosts[i]=replayResult.getInfo().get("Raw Fitness Cost");
						 		chCount2++;
						 		if(alignmentComputedCosts[i] ==0) {
						 		ModelBehaviorSim.put(charecterTraces.get(i), chCount);
				 				ModelBehaviorSimReverse.put(chCount, charecterTraces.get(i));
				 				approximatedFitness[i]=1;
				 				approximatedFitnessValue= approximatedFitnessValue+VariantFreq[i];
				 				chCount++;
				 				}else {
				 					String modelTrace2="";
				 					List<Object> ModelBehavior = replayResult.getNodeInstance();
							 		 List<StepTypes> TypeBehavior = replayResult.getStepTypes();
							 	
							 		 for (int j=0;j<replayResult.getNodeInstance().size();j++) {
							 			/*if(TypeBehavior.get(j).equals(StepTypes.L)||TypeBehavior.get(j).equals(StepTypes.MREAL)) {
							 				AsyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (AsyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
							 				if(TypeBehavior.get(j).equals(StepTypes.L))
							 					LogMoves.put(ModelBehavior.get(j).toString(), (int) (LogMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
							 				else
							 					ModelMoves.put(ModelBehavior.get(j).toString(), (int) (ModelMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
							 			}else if(TypeBehavior.get(j).equals(StepTypes.LMGOOD))
							 				SyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (SyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
							 		*/
							 		
							 		 
							 		if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
						 				
						 				if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
						 					modelTrace2=modelTrace2 + ActivityCoder.get(ModelBehavior.get(j).toString());
						 				}
						 				else {
						 					charcounter++;
						 					ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
						 					modelTrace2=modelTrace2 + ActivityCoder.get(ModelBehavior.get(j).toString());
						 				}
						 			}
							 		 }
							 		if (ModelBehaviorSim.get(modelTrace2)==null) {
							 			ModelBehaviorSim.put(modelTrace2, chCount);
							 			ModelBehaviorSimReverse.put(chCount, modelTrace2);
							 			chCount++;
							 		}  
							 		SelectedUnfittedList.add(i);
							 		
						 		}
						 		
						 		
				 				 
						 		
						 		/*binaryAlignmentCost[i]= replayResult.getInfo().get("Raw Fitness Cost");
						 			if(binaryAlignmentCost[i]==0){
						 				
						 		 }*/
					 		} catch (Exception e) {
					 			// execution os the service has terminated.
					 			SampledList.add(i);
					 			//throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
					 		}
					 		service2.shutdown();
					 		}
					 		
					 		System.out.println(variantLog.size());
					 		System.out.println(SelectedList.size());
					 		System.out.println(SampledList.size());
					 	}
				
				break;
				case Length:
					/*for (int i = 0; i <VariantChar.length; i++) {
						if (maxTraceLength*(parameters.getSubsequenceLength()*1.0/100)<VariantChar[i].length()) {
						//if (avgTraceLength*(parameters.getSubsequenceLength()*1.0/100)<VariantChar[i].length()){
							SampledList.add(VariantInd[i]);
						}else {
							SelectedList.put(counter,VariantInd[i]);
							counter++;
						}

					}*/
					quicksort (VariantLength.clone(), VariantInd);
					 counter=0;
					for (int i = 0; i < VariantInd.length; i++) {
						if (i> (1- Threshold)* VariantInd.length) {
							SampledList.add(VariantInd[i]);
						}else {
							SelectedList.put(counter,VariantInd[i]);
							counter++;
						}
					}
	     	}/// switch
		/*	 HashMap<Integer, String> clusters= new HashMap<Integer, String>();
			 for (int k = 0; k < SelectedList.size(); k++) {
				 clusters.put(k, VariantChar[SelectedList.get(k)]);
			 }
			 for (int j = 0; j < SampledList.size(); j++) {
		     		int alignmentLowerboundCost=0;
		     		int index = -1;
		     		double MinDistance=10000;
		     		for (int k = 0; k < SelectedList.size(); k++) {
		     			double dist = distanceMatrix [SelectedList.get(k)][SampledList.get(j)];
		     			if (MinDistance>distanceMatrix [SelectedList.get(k)][SampledList.get(j)]) {
		     				MinDistance= dist;
		     				index=k;
		     			}
		     			
		     		}
		     		 String x = clusters.get(index);
	     			 x= lcs(x, VariantChar[SampledList.get(j)]);
	     			clusters.put(index, x);
		     		
		     	}
			 
			 String m = lcs(VariantChar[0],VariantChar[1]);
			 for (int j = 0; j < VariantChar.length; j++) {
				 m=lcs(m,VariantChar[j]);
			}*/
			 
			 PreprocessTime = System.currentTimeMillis() - PreprocessTime ;

			 long MBTime = System.currentTimeMillis();
				
			 XLog TraceLog = factory.createLog();
			 TraceLog.setAttributes(InputLog.getAttributes());
			 XAttributeMapImpl case_map = new XAttributeMapImpl();
			 String case_id = String.valueOf(charcounter);
			 case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
			 XTraceImpl trace = new XTraceImpl(case_map);
			 TraceLog.add(trace);	
			 //double ShortestPath=replayTraceOnNet( TraceLog,  net,  mapping);
			 double[] AlignmentCosts= new double [SelectedList.size()+1];
			 double[] FitnessValues= new double [SelectedList.size()];
			 
				

					
		   	 //////////////////////////////////////////////////////////////////////////////////
			 
	////// Except for Binary alignment method
		double AlignFit=0;
		double AlignCost=0;			
		ReplayerParameters RepParameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
		Replayer replayer = new Replayer(RepParameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);
		Future<TraceReplayTask>[] futures = new Future[TraceLog.size()];
		ExecutorService service = Executors.newFixedThreadPool(RepParameters.nThreads);
		if(SelectiuonType!= PrototypeType.FastAlignment) {			 ///// Compute the actual alignment
			 for (int i = 0; i < SelectedList.size(); i++) {
				 TraceLog.add(VariantMapper.get(SelectedList.get(i)));
				 VariantMapper.get(SelectedList.get(i)).toArray();
				// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
			}//
		}//// for Binary vs all 
			 futures = new Future[TraceLog.size()];
			   
			 for (int i = 0; i < TraceLog.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog.get(i), i, timeoutMilliseconds,
			 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
			 		
			 		// submit for execution
			 		futures[i] = service.submit(task);
			 	}
			
			 
			 
			 	// obtain the results one by one.
			chCount=0;
			 	for (int i = 0; i < TraceLog.size(); i++) {

			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		 AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 		List<Object> ModelBehavior = replayResult.getNodeInstance();
			 		 List<StepTypes> TypeBehavior = replayResult.getStepTypes();
			 		 String modelTrace="";
			 		 if(i>0) {
			 			alignmentComputedCosts[i-1]=AlignmentCosts[i];
			 			approximatedAlignmentCost[SelectedList.get(i-1)]= AlignmentCosts[i];
			 			approximatedFitness[SelectedList.get(i-1)]= 1- (AlignmentCosts[i]/(VariantChar[SelectedList.get(i-1)].length() +AlignmentCosts[0]));
			 			approximatedFitnessValue= approximatedFitnessValue+(approximatedFitness[SelectedList.get(i-1)] *VariantFreq[SelectedList.get(i-1)]);
			 		 }
			 		service.shutdown();
			 		for (int j=0;j<replayResult.getNodeInstance().size();j++) {
			 			if(i>0) {
			 				
				 			if(TypeBehavior.get(j).equals(StepTypes.L)||TypeBehavior.get(j).equals(StepTypes.MREAL)) {
				 				AsyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (AsyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
				 				if(TypeBehavior.get(j).equals(StepTypes.L))
				 					LogMoves.put(ModelBehavior.get(j).toString(), (int) (LogMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
				 				else
				 					ModelMoves.put(ModelBehavior.get(j).toString(), (int) (ModelMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
				 			}else if(TypeBehavior.get(j).equals(StepTypes.LMGOOD))
				 				SyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (SyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
			 			
				 			
			 			}
			 			
			 		if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
			 				
			 				if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
			 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
			 				}
			 				else {
			 					charcounter++;
			 					ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
			 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
			 				}
			 			}
			 			 
			 		}
			 		
			 		if (ModelBehaviorSim.get(modelTrace)==null) {
			 			ModelBehaviorSim.put(modelTrace, chCount);
			 			ModelBehaviorSimReverse.put(chCount, modelTrace);
			 			chCount++;
			 		}
			 		
			 		
			 		/*for (int j=0;j<replayResult.getStepTypes().size();j++) {
			 			if(replayResult.getStepTypes().get(j).toString().equals("Log move") || replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 				//System.out.println(replayResult.getNodeInstance().get(j).toString());
			 				if(replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 					if(mapping.containsKey(replayResult.getNodeInstance().get(j))) {
			 						asynchronousMoveBag.add(mapping.get(replayResult.getNodeInstance().get(j)).toString());
			 					}
			 					else {
			 						asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 					}
			 				}
			 				if(replayResult.getStepTypes().get(j).toString().equals("Log move")) {
			 					asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 				}
			 			}
			 		}*/
			 	}
			 	
			 	MBTime = System.currentTimeMillis() - MBTime ;

		double SPM= AlignmentCosts[0];
			 	/////  for unfitted selected traces 
				for (int k = 0; k < SelectedUnfittedList.size(); k++) {
					approximatedFitness[SelectedUnfittedList.get(k)]=1-(alignmentComputedCosts[SelectedUnfittedList.get(k)]/(VariantChar[SelectedUnfittedList.get(k)].length()+SPM));
	 				approximatedFitnessValue= approximatedFitnessValue+(VariantFreq[SelectedUnfittedList.get(k)]*approximatedFitness[SelectedUnfittedList.get(k)]);
				}
		
		
				 //MBTime = System.currentTimeMillis()- MBTime;
				 ////////////////////////.............No Model Distance computation//////////////////////////
				double MaximumPossibleSumError=1;
				double MaximumalignmentCostError =0;
				double MaximumalignmentCostAvgError = 0;
				double MSQError=0;
				
			if(SelectiuonType!=PrototypeType.Normal) {	
				double distance=0;
				 int tempMax=0;
				 double tempFitness=0;
				 double msqTempFitness=0;
				 for (int i = 0; i < VariantChar.length; i++) {
					 String currentVarinat= VariantChar[i];
					 int tempDist=(int)(AlignmentCosts[0]+currentVarinat.length()*1.0);
					for (int k = 0; k < SelectedList.size(); k++) {
						if(distanceMatrix [i][SelectedList.get(k)]<0) {
							distanceMatrix [i][SelectedList.get(k)]=levenshteinDistanceCost (currentVarinat,VariantChar[SelectedList.get(k)]);
							distanceMatrix [SelectedList.get(k)][i]=distanceMatrix [i][SelectedList.get(k)];
						}
						if(tempDist>distanceMatrix [i][SelectedList.get(k)])
							tempDist=(int)distanceMatrix [i][SelectedList.get(k)];
						//tempDist =(int) distanceMatrix [i][SelectedList.get(k)];
						
					}
					if (tempDist>AlignmentCosts[0]+currentVarinat.length()) {
						tempDist=(int) (AlignmentCosts[0]+currentVarinat.length());
					}
					if (tempMax<tempDist) {
						tempMax=tempDist;
					}

					tempFitness= tempFitness + ((tempDist *VariantFreq[i]*1.0)/ (AlignmentCosts[0]+currentVarinat.length()*1.0));
					msqTempFitness=msqTempFitness+ (Math.pow(tempDist,2) *VariantFreq[i]);
					distance=distance+(tempDist *VariantFreq[i]);
					
				}
				MaximumPossibleSumError= tempFitness/LogSize;
				MaximumalignmentCostError = tempMax;
				MaximumalignmentCostAvgError = distance/LogSize;
				MSQError= msqTempFitness/LogSize;
				}

			
			
				 //////////////////////////////////////////////////
			 	
				
				
		int chCount2=0;
			 for (int i = 0; i < FitnessValues.length; i++) {
				 chCount2= chCount2+ (int)VariantFreq[SelectedList.get(i)];
				 FitnessValues[i]= 1- ( AlignmentCosts[i+1]*1.0/ (AlignmentCosts[0]+VariantChar[SelectedList.get(i)].length() ));
				 AlignFit= AlignFit+FitnessValues[i];
				 AlignCost= AlignCost+ (1- FitnessValues[i]);
			}
			 AlignFit=AlignFit/FitnessValues.length;
			 AlignCost=AlignCost/FitnessValues.length;
			
			 
			 
			 int[] sapmledVariantsLoweverBoundcost = new int[SampledList.size()];
			 int[] sapmledVariantsUpperBoundcost = new int[SampledList.size()];
			 double MaxDistance = -2;
		     double dist=0;
		     	for (int j = 0; j < SampledList.size(); j++) {
		     		int alignmentLowerboundCost=0;
		     		int alignmentUpperboundCost=9999;
		     		 if(VariantChar[SampledList.get(j)].length() < SPM) {
		     			alignmentLowerboundCost=  (int)SPM-VariantChar[SampledList.get(j)].length() ;
		     		 }
		     		double MinDistance=1000;
		     		for (int k = 0; k < SelectedList.size(); k++) {
		     			if(distanceMatrix [SampledList.get(j)][SelectedList.get(k)]<0) {
							distanceMatrix [SampledList.get(j)][SelectedList.get(k)]=levenshteinDistanceCost (VariantChar[SampledList.get(j)],VariantChar[SelectedList.get(k)]);
							distanceMatrix [SelectedList.get(k)][SampledList.get(j)]=distanceMatrix [SampledList.get(j)][SelectedList.get(k)];
						}
		     			dist = distanceMatrix [SelectedList.get(k)][SampledList.get(j)];
		     			if (MinDistance>distanceMatrix [SelectedList.get(k)][SampledList.get(j)]) {
		     				MinDistance= dist;
		     			}
		     			if(alignmentComputedCosts[k]- dist > alignmentLowerboundCost)
		     				alignmentLowerboundCost= (int) (alignmentComputedCosts[k]- dist);
		     			if(alignmentComputedCosts[k]+ dist < alignmentUpperboundCost)
		     				alignmentUpperboundCost= (int) (alignmentComputedCosts[k]+ dist);
		     		}
		     		sapmledVariantsLoweverBoundcost[j]=alignmentLowerboundCost;
		     		sapmledVariantsUpperBoundcost[j]=alignmentUpperboundCost;
		     		if (MaxDistance < MinDistance)
		     			MaxDistance=MinDistance;
		     	}
			 
			 ///////////////////////Compute the approximation Cost /////////
		     double lowerBound=approximatedFitnessValue; 
			 double upperBound=approximatedFitnessValue;
			 double approximation=approximatedFitnessValue;
			 double[] sapmledVariantsUBFitness = new double[SampledList.size()];
			 chCount2=0;

	
		
			 int sampledFreq=0;
			
			 long STSimilarityTime = System.currentTimeMillis();
			 
			 double lb1=0;double lb2=0;double lb3=0;
			 
			 
			 
		if (SampledList.size()>0) {
			
			
			for (int i = 0; i < SampledList.size(); i++) {
				int  variantFreq=  (int) VariantFreq[SampledList.get(i)];
				 sampledFreq =(int) (sampledFreq+VariantFreq[SampledList.get(i)]);
				 int alignmentUpperboundCost =100000;
				 double alignmentLowerboundCost =-100000;
				 ///////// Computing the uppperbound of the alignment cost
				 int tempIndex=0;
				 int tempDist=0;
				 double tempLB=0;
				 String alignment="";
				 String currentVariant = VariantChar[SampledList.get(i)];
				 
				 //////////////////////////////Parallel////////////////////////
				 final String LV = VariantChar[SampledList.get(i)];
				SimpleImmutableEntry<String, Integer> pair = ModelBehaviorSimReverse.values().parallelStream()
				 .map(SV -> new SimpleImmutableEntry<>(SV,levenshteinDistanceCost(LV,SV)))
				 .min(Entry.comparingByValue()).get();
				 alignmentUpperboundCost = pair.getValue();
				 String SV = pair.getKey();
				 ///////////////////////////////////////////////
				 
				 
				 ////////////////////////////////  Single Core/////////////
				 
			/*	SimilarLoop:	 for (int j = 0; j < ModelBehaviorSimReverse.size(); j++) {
					tempDist=levenshteinDistanceCost(currentVariant,  ModelBehaviorSimReverse.get(j), alignmentUpperboundCost );
						 if( tempDist  < alignmentUpperboundCost) {
								alignmentUpperboundCost=tempDist  ;
								tempIndex=j;
								if(SelectiuonType!= PrototypeType.FastAlignment) {
								if (alignmentUpperboundCost==0)
									break SimilarLoop;
								}else {
									if (alignmentUpperboundCost==1)
										break SimilarLoop;
								}
							}
						 
					}*/
			/*	 for (int j = 0; j < SelectedList.size(); j++) {
					tempLB= alignmentComputedCosts[SelectedList.get(j)]-levenshteinDistanceCost(currentVariant, VariantChar[SelectedList.get(j)]);
					if (tempLB>alignmentLowerboundCost)
						alignmentLowerboundCost=tempLB;
				}*/
				 //////////////////////////////////////////
			///////// Approximating the alignment sync and Async moves
				/*//AlignObj temp=levenshteinDistancewithAlignment(currentVariant,  ModelBehaviorSimReverse.get(tempIndex) );
				  AlignObj temp=levenshteinDistancewithAlignment(currentVariant,  SV );
				 alignment=temp.Alignment;
				 String[] Moves = alignment.split(">");
				for (int j = 0; j < Moves.length; j++) {
					if(Moves[j].contains("Sync"))
						SyncrousMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (SyncrousMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
					else {
						AsyncrousMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (AsyncrousMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
						if(Moves[j].contains("Deletion"))
							ModelMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (ModelMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
						else
							LogMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (LogMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
					}
				}
				*/
		
		 		 
				 if (alignmentUpperboundCost> AlignmentCosts[0]+currentVariant.length())
					 alignmentUpperboundCost= (int)  AlignmentCosts[0]+currentVariant.length();
				 if(alignmentUpperboundCost >  sapmledVariantsUpperBoundcost[i])
					 alignmentUpperboundCost =Math.min(alignmentUpperboundCost, sapmledVariantsUpperBoundcost[i]);
				 double fitnessLowerbound= 1-(alignmentUpperboundCost*1.0/ (AlignmentCosts[0]+currentVariant.length() ));
				 lowerBound=lowerBound+(fitnessLowerbound*variantFreq); 
				 sapmledVariantsUBFitness[i]=fitnessLowerbound;
						 
				double fitnessUpperbound = 1.0- ( sapmledVariantsLoweverBoundcost[i] / ( AlignmentCosts[0]+ currentVariant.length()));
				
				double  lowerBoundCost = LV.length() < SPM ? Math.max(SPM - LV.length(), lbCosts.get(LV)): lbCosts.get(LV);
				//////////Which lower bound is selected????//////////////////////////
				
				if (sapmledVariantsLoweverBoundcost[i]  >=lowerBoundCost) {
					lb3++;
					lowerBoundCost=sapmledVariantsLoweverBoundcost[i] ;
				}
				if (lbCosts.get(LV) ==lowerBoundCost)
					lb2++;
				if ( (SPM - LV.length() ) ==lowerBoundCost)
					lb1++;
				
				
				
				
				////////////////////////////////////////////////////
				fitnessUpperbound= Math.min(fitnessUpperbound, ( 1- (lowerBoundCost / ( AlignmentCosts[0]+ currentVariant.length())) ));
				double fitnessApproximation = (fitnessUpperbound+fitnessLowerbound)/2;
					///////// Computing the lowerbound of the alignment cost		 
				upperBound= upperBound+ (fitnessUpperbound  *variantFreq);
				approximation= approximation + (fitnessApproximation  *variantFreq);
				
				
				approximatedAlignmentCost[SampledList.get(i)]= (alignmentUpperboundCost+sapmledVariantsLoweverBoundcost[i])*1.0;
		 		approximatedFitness[SampledList.get(i)]= 1-( ((alignmentUpperboundCost+sapmledVariantsLoweverBoundcost[i])*1.0)/(VariantChar[SampledList.get(i)].length() +AlignmentCosts[0]));
		 		approximatedFitnessValue= approximatedFitnessValue+(approximatedFitness[SampledList.get(i)] *VariantFreq[SampledList.get(i)]);
				 
			}
		}
		
		lb1=lb1/SampledList.size(); 
		lb2=lb2/SampledList.size();
		lb3=lb3/SampledList.size();
		STSimilarityTime = System.currentTimeMillis()-STSimilarityTime;

				
			
			long time2 = System.currentTimeMillis();
			long approximationMethodTime = System.currentTimeMillis() - time0 ;
			
			
//////////////////////////////////Normal Fitness/////////////////////////////////////////////	 


/*
AlignFit=0;
double  LogSize2=0;
XLog TraceLog2 = factory.createLog();
TraceLog2.setAttributes(InputLog.getAttributes());
TraceLog2.setAttributes(InputLog.getAttributes());

case_id = String.valueOf(charcounter+2000000);
case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
XTraceImpl trace2 = new XTraceImpl(case_map);
//TraceLog2.add(trace2);	
double avgAlignmentCostError=0;
double maxAlignmentCostError=0;
for (int i = 0; i < VariantFreq.length; i++) {

TraceLog2.add(VariantMapper.get(i));

// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
}//


AlignmentCosts=	 new double [TraceLog2.size()];
FitnessValues= new double [TraceLog2.size()-1];
Future<TraceReplayTask>[] futures3 = new Future[TraceLog2.size()]; 
ExecutorService service3 = Executors.newFixedThreadPool(RepParameters.nThreads);

for (int i = 0; i < TraceLog2.size(); i++) {
// Setup the trace replay task
TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog2.get(i), i, timeoutMilliseconds,
RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);

futures3[i] = service3.submit(task);
}
for (int i = 0; i < TraceLog2.size(); i++) {

TraceReplayTask result;
try {
	result = futures3[i].get();
} catch (Exception e) {
	// execution os the service has terminated.
	assert false;
	throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
}
SyncReplayResult replayResult = result.getSuccesfulResult();
AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
actualAlignmentCost[i]=replayResult.getInfo().get("Raw Fitness Cost");
alignmentCostError[i]= Math.abs(actualAlignmentCost[i]- approximatedAlignmentCost[i]);
avgAlignmentCostError=avgAlignmentCostError+(alignmentCostError[i]*VariantFreq[i]);
if(maxAlignmentCostError< alignmentCostError[i])
	maxAlignmentCostError=alignmentCostError[i];
actualFitness[i]= 1 - ((actualAlignmentCost[i]*1.00)/ (SPM+VariantChar[i].length() ));
fitnessError[i]= Math.abs(actualFitness[i]-approximatedFitness[i]);
actualFitnessValue= actualFitnessValue+ (actualFitness[i]*VariantFreq[i]);
service3.shutdown();
for (int j=0;j<replayResult.getStepTypes().size();j++) {
	if(replayResult.getStepTypes().get(j).toString().equals("Log move") || replayResult.getStepTypes().get(j).toString().equals("Model move")) {
		//System.out.println(replayResult.getNodeInstance().get(j).toString());
		if(replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			if(mapping.containsKey(replayResult.getNodeInstance().get(j))) {
				asynchronousMoveBag.add(mapping.get(replayResult.getNodeInstance().get(j)).toString());
			}
			else {
				asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			}
		}
		if(replayResult.getStepTypes().get(j).toString().equals("Log move")) {
			asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
		}
	}
} 	
}


FitnessError= Math.abs((actualFitnessValue/LogSize) - (approximatedFitnessValue/LogSize) );
avgAlignmentCostError=avgAlignmentCostError/LogSize;		
*/	
			///////////////////////////Normal Fitness /////////////////////////////
					
				double computedFitness=0; int computedFreq=0;
				double computedAlignCost=0;
			for (int i = 0; i < SelectedList.size(); i++) {
				computedFitness= computedFitness+(FitnessValues[i]*VariantFreq[SelectedList.get(i)]);
				computedAlignCost= computedAlignCost+((1-FitnessValues[i])*VariantFreq[SelectedList.get(i)]);
				computedFreq=(int) (computedFreq+VariantFreq[SelectedList.get(i)]);
			}
			
			double computedFitnessValue=0;
			 
			 if (parameters.getPrototypeType()!=PrototypeType.Simulation) {
					computedFitnessValue=computedFitness/computedFreq;
			 }
			
			

			 double LowerBoundFitness = ( computedFitness+ lowerBound)*1.0 / (sampledFreq+computedFreq);
			 double ApproximatedFitness= ( computedFitness+ approximation)*1.0 / (sampledFreq+computedFreq);
			 double UpperBoundFitness= ( computedFitness+ upperBound)*1.0 / (sampledFreq+computedFreq);
			 
			 long Totaltime = System.currentTimeMillis() - time ;
			 time= System.currentTimeMillis();
			
			
//			if (parameters.getPrototypeType()==PrototypeType.Simulation){
//					ApproximatedFitness=  (LowerBoundFitness + UpperBound )/2;
//					
//					}
			Set<String> T = SyncrousMoves.keySet();
			for (Iterator t = T.iterator(); t.hasNext();) {
				String string = (String) t.next();
				AsyncrousDistribution.put(string, (double) (AsyncrousMoves.get(string)*1.0/(SyncrousMoves.get(string)+AsyncrousMoves.get(string))));		
			}
			
			
			 long normalConformanceTime = System.currentTimeMillis() - time2 + logScanningTime;
			 String outp1= (approximation/LogSize) +">>" + (upperBound/LogSize)+ ">>"+ (lowerBound/LogSize)+">>"+
					 MaximumPossibleSumError+ ">>"+MaximumalignmentCostAvgError+">>" +MaximumalignmentCostError+">>"+approximationMethodTime+">>"+(SelectedList.size()*1.0/ (VariantFreq.length))+ 
					 ">>" +ModelBehaviorSim.size()+">>" + PreprocessTime + ">>" + MBTime + ">>" + MSQError+">>"+STSimilarityTime +
					 ">>" + lb1 +">>" +lb2 + ">>" + lb3;
			 
			// String outp=( LowerBoundFitness +"==>>"+ UpperBoundFitness+"==>>"+ ApproximatedFitness+"==>>"+ LowerBoundFitness +"==>>" + Totaltime+"==>>" + MBTime+"==>>" +PreprocessTime +"==>>" +ApproximatedFitness +"==>>"+ModelBehaviorSim.size());// +"==>>"+ (double)(chCount2+SelectedList.size())/VariantMapper.size()+"==>>"+ AsyncrousDistribution);
			 String htmlDeviation="<tr>\n" +"<td style=\"width: 200px; font-size: 1.2em;\">Activities</td>\n" + "<td style=\"width: 200px; font-size: 1.2em;color: green;\">Synchronous Moves</td>\n" + "<td style=\"width: 200px; font-size: 1.2em; color: red;\">Asynchronous Moves</td>\n" +"<tr>\n"
					 				+"<tr>\n" +"<tr>\n";
			 for (Iterator t = T.iterator(); t.hasNext();) {
					String string = (String) t.next();
					AsyncrousDistribution.put(string, (double) (AsyncrousMoves.get(string)*1.0/(SyncrousMoves.get(string)+AsyncrousMoves.get(string))));	
					htmlDeviation=htmlDeviation +"<tr>\n" + 
							"<td style=\"font-size: 1.2em;\">"+string+"</td>\n"  +
							"<td style=\"font-size: 1.2em;color: green;\">"+SyncrousMoves.get(string)+"</td>\n"+
							"<td style=\"font-size: 1.2em;color: red;\">"+AsyncrousMoves.get(string)+"</td>\n"
							+"<tr>\n" ; 
				}
			 
			 
			 String outp2 = "<html><table>\n" + 
						"<tbody>\n" + 
						/*"<tr>\n" + 
						"<td style=\"width: 200px; font-size: 1.2em;\">Real Fitness:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+(actualFitnessValue/LogSize)+"</td>\n" +
						"</tr>\n" +*/

						"<tr>\n" +
						"<td style=\"width: 300px; font-size: 1.2em;\">approximated Fitness:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+(approximation/LogSize)+"</td>\n" + 
						"</tr>\n" +
						
						"<tr>\n" +
						"<td style=\"width: 300px; font-size: 1.2em;\">Upper bound Fitness:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+(upperBound/LogSize)+"</td>\n" + 
						"</tr>\n" +
						
						"<tr>\n" +
						"<td style=\"width: 300px; font-size: 1.2em;\">Lower bound Fitness:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+(lowerBound/LogSize)+"</td>\n" + 
						"</tr>\n" + 
						/*"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Fitness Coomolative Error</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+(Math.abs(actualFitnessValue-approximation)/LogSize)+"</td>\n" + 
						"</tr>\n" + 
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Fitness Error</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+FitnessError+"</td>\n" + 
						"</tr>\n" + */
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Aproximated Fitness Error without model:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+MaximumPossibleSumError+"</td>\n" + 
						"</tr>\n" + 
						/*"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Avergae Alignment Cost Error:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+avgAlignmentCostError+"</td>\n" + 
						"</tr>\n" + */
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Avergae Alignment Cost Error without Model:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+MaximumalignmentCostAvgError+"</td>\n" + 
						"</tr>\n" + 
						/*"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Maximum Alignment Cost Error:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+maxAlignmentCostError+"</td>\n" + 
						"</tr>\n" + */
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Maximum Alignment Cost Error without Model:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+MaximumalignmentCostError+"</td>\n" + 
						"</tr>\n" + 
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Approximation Time:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+approximationMethodTime+"</td>\n" + 
						/*"</tr>\n" + 
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Normal Conformance Time:</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+normalConformanceTime+"</td>\n" + 
						"</tr>\n" + */
						"<tr>\n" +
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Aligned Percentage</td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+(SelectedList.size()*1.0/ (VariantFreq.length))+"</td>\n" + 
						"</tr>\n" + 
					/*	"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Performance Improvement </td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+(normalConformanceTime*1.0/ approximationMethodTime)+"</td>\n" + 
						"</tr>\n" + */
						"<tr>\n" + 
						"<td style=\"width: 400px; font-size: 1.2em;\">Size of Model Subset </td>\n" + 
						"<td style=\"font-size: 1.2em;\">"+ModelBehaviorSim.size()+"</td>\n" + 
						"</tr>\n" + 
						"<tr>\n" +
						"<td style=\"width: 400px; font-size: 1.2em;height: 300px\">&nbsp;</td>\n" + 	
						"</tr>\n" +
						//htmlDeviation+
						"</tbody>\n" + 
						"</table></html>";
		//////////////////////////////////Actual Fitness/////////////////////////////////////////////	 
/*		double[] AlignmentCosts2= new double [InputLog.size()];
			 futures = new Future[InputLog.size()];
			 service = Executors.newFixedThreadPool(RepParameters.nThreads);
			 for (int i = 0; i < InputLog.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, InputLog.get(i), i, timeoutMilliseconds,
			 				RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
			 		// submit for execution
			 		futures[i] = service.submit(task);
			 	}
			 for (int i = 0; i < InputLog.size(); i++) {
			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		AlignmentCosts2[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 	}
			 double ActualFitness=0;
			 for (int i = 0; i < AlignmentCosts2.length; i++) {
				 ActualFitness= ActualFitness + (1-(AlignmentCosts2[i]*1.0/ (AlignmentCosts[0]+InputLog.get(i).size() )  ));
			}
			 double ActualFitnessValue=ActualFitness/InputLog.size();

			 long timer3= System.currentTimeMillis() - time ;
			 double PerformanceImprovement= timer2 *1.0 / timer2;  
			 double AccuracyClosness= Math.abs(ApproximatedFitness -  ActualFitnessValue ); 
		///////////////////////////////////////////////////////////////////////////////////////////
			 
			 
			 AlignFit=0;
			double  LogSize2=0;
			 XLog TraceLog2 = factory.createLog();
			 TraceLog2.setAttributes(InputLog.getAttributes());
			 TraceLog2.setAttributes(InputLog.getAttributes());
			 
			  case_id = String.valueOf(charcounter+2000000);
			 case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
			 XTraceImpl trace2 = new XTraceImpl(case_map);
			 TraceLog2.add(trace2);	
 for (int i = 0; i < VariantFreq.length; i++) {
				 
				 TraceLog2.add(VariantMapper.get(i));
				
				// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
			}//
 			AlignmentCosts=	 new double [TraceLog2.size()];
 			FitnessValues= new double [TraceLog2.size()-1];
 				futures = new Future[TraceLog2.size()];
			 for (int i = 0; i < TraceLog2.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog2.get(i), i, timeoutMilliseconds,
			 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);

			 		futures[i] = service.submit(task);
			 	}
			 for (int i = 0; i < TraceLog2.size(); i++) {

			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		 AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 		for (int j=0;j<replayResult.getStepTypes().size();j++) {
			 			if(replayResult.getStepTypes().get(j).toString().equals("Log move") || replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 				//System.out.println(replayResult.getNodeInstance().get(j).toString());
			 				if(replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 					if(mapping.containsKey(replayResult.getNodeInstance().get(j))) {
			 						asynchronousMoveBag.add(mapping.get(replayResult.getNodeInstance().get(j)).toString());
			 					}
			 					else {
			 						asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 					}
			 				}
			 				if(replayResult.getStepTypes().get(j).toString().equals("Log move")) {
			 					asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 				}
			 			}
			 		} 	
			 	}
			 for (int i = 0; i < FitnessValues.length; i++) {
				 FitnessValues[i]= 1- ( AlignmentCosts[i+1]*1.0/ (AlignmentCosts[0]+VariantChar[i].length() ));
				 AlignFit= AlignFit+ (FitnessValues[i]* VariantFreq[i]);
				 
				 LogSize2=LogSize2+VariantFreq[i]; 
			}
			 
			 double AlignFit2=AlignFit/LogSize2;
			
			 
			 boolean flag= false;
			 for (int i = 0; i < FitnessValues.length; i++) {
				if (FitnessValues[i] < SampledFitness[i]) {
					flag=true;
				}
					
			} */
			 
				return outp1;
		
		
			
			
		
		
	}

	public static String apply2(PluginContext context,XLog log, Petrinet net, MatrixFilterParameter parameters,
			TransEvClassMapping mapping) {
		// TODO Auto-generated method stub 
		//Martin Bauer
		long time = System.currentTimeMillis();
		System.out.println(	System.currentTimeMillis() - time);
		XLog log2= (XLog) log.clone();
		
		double delta=parameters.getSubsequenceLength()*0.001;//0.01
		double alpha=0.99;
		double epsilon=parameters.getSubsequenceLength()*0.001;//0.01
		double k=0.5;
		int initialSize=20;
		String goal="fitness";
		boolean approximate=false;
		IccParameters iccParameters=new IccParameters(delta, alpha, epsilon, k, initialSize, goal, approximate);

		IncrementalReplayer replayer = null;
		if (goal.equals("fitness")&& !iccParameters.isApproximate()) {
			replayer=new FitnessReplayer(iccParameters);
		}
		if (goal.equals("fitness")&&iccParameters.isApproximate()) {
			replayer=new ApproxFitnessReplayer(iccParameters);
		}
		if (iccParameters.getGoal().equals("alignment")&& !iccParameters.isApproximate())
			replayer=new AlignmentReplayer(iccParameters);
		if (iccParameters.getGoal().equals("alignment") && iccParameters.isApproximate()) {
			replayer= new ApproxAlignmentReplayer(iccParameters);
			//replayer.init(context, net, log);
		}
				
		//make own parameter function for alignment/fitness
		AlignmentReplayResult result= calculateAlignmentWithICC(context, replayer, net, log2, iccParameters, mapping);
		
		result.setTime(System.currentTimeMillis()-time);
		System.out.println("Fitness         : "+result.getFitness());
		System.out.println("Time(ms)        : "+result.getTime());
		System.out.println("Log Size        : "+result.getLogSize());
		System.out.println("No AsynchMoves  : "+result.getTotalNoAsynchMoves());
		System.out.println("AsynchMoves abs : "+result.getAsynchMovesAbs().toString());
		System.out.println("AsynchMoves rel : "+result.getAsynchMovesRel().toString());
		long timer2 = System.currentTimeMillis() - time ;
		String outp=( 0 +"==>>"+ 1+"==>>"+ result.getFitness()+"==>>"+ result.getLogSize() +"==>>" + timer2+"==>>" +0+"==>>"+ result.getFitness())+"==>>"+ result.getLogSize() ;
		
		return outp;
		
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
 

 public static HashMap<Integer, List<Integer>> FindCluster (double[][] distanceMatrix,HashMap<Integer,Integer> SelectedList, String[] VariantChar) {
	 int[] belongCluster = new int[distanceMatrix.length];
	 HashMap<Integer,List<Integer> >Clusters =new HashMap<Integer,List<Integer>>();
	 int K = SelectedList.size();
	 for (int i = 0; i < K ; i++) {
		 List<Integer> tempList = new ArrayList<>();
		 Clusters.put(i, tempList);
	}

	 for (int i = 0; i < belongCluster.length; i++) {
		
			 double cost=1000;
			 int counter =0; int index=0;
			for (int j = 0; j < K; j++) {
				
				if(distanceMatrix [i][SelectedList.get(j)]<0) {
					distanceMatrix [i][SelectedList.get(j)]=levenshteinDistanceCost (VariantChar[SelectedList.get(j)],VariantChar[i]);
					distanceMatrix [SelectedList.get(j)][i]=distanceMatrix [i][SelectedList.get(j)];
				}
				
					if (distanceMatrix[i][SelectedList.get(j)]< cost) {
						cost =distanceMatrix[i][SelectedList.get(j)];
						counter= SelectedList.get(j);
						index=j;
					}
				
				
			}
			belongCluster[i]= index; 
			List<Integer> List = Clusters.get(index);
	 		List.add(i);
			Clusters.put(index,List);
		
	}
	 return Clusters;
 }
 
 
 public static HashMap<Integer, Integer> UpdateKMedoids(HashMap<Integer,Integer> SelectedList,double[][] distanceMatrix, float[] variantFreq, HashMap<Integer,List<Integer> > Clusters, String[] VariantChar) {
	 int K = SelectedList.size();
	 for (int i = 0; i < K; i++) {
		
			 List<Integer> List= Clusters.get(i);
			 double distance = 980000000;

				 for (int j = 0; j < List.size(); j++) {
					 double cost = 0;
					 int counter=0;
					 
					 for (int j2 = 0; j2 < List.size(); j2++) {
						 if(distanceMatrix [List.get(j)][List.get(j2)]<0) {
								distanceMatrix [List.get(j)][List.get(j2)]=levenshteinDistanceCost (VariantChar[List.get(j)],VariantChar[List.get(j2)]);
								distanceMatrix [List.get(j2)][List.get(j)]=distanceMatrix [List.get(j)][List.get(j2)];
							}
						 cost=cost+ (distanceMatrix[List.get(j)][List.get(j2)] * variantFreq[List.get(j2)]);
					}
					 if( cost <distance) {
							distance = cost;
							SelectedList.put(i,List.get(j));
						}
				}
			
			 
		 
		
	}
	 
	 return SelectedList;
	 
 }
 
 
 
 public static int levenshteinDistanceCost (CharSequence lhs, CharSequence rhs) {    
		
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	     int maxLen= 0;
	     if (len0>len1) {
	    	 maxLen=len0;
	     }
	     else {
	    	  	maxLen= len1;
	     }
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) 
	    	cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; 
	        cost = newcost;
	        newcost = swap;                          
	    }                                                                               
	                                                                          
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1] ;                                                          
	}
	private static int levenshteinDistanceCost(CharSequence  left, CharSequence  right, int threshold) {
		
		if (left == null || right == null) {
			throw new IllegalArgumentException("CharSequences must not be null");
		}
		if (threshold < 0) {
			throw new IllegalArgumentException("Threshold must not be negative");
		}
		
		
		int n = left.length(); // length of left
        int m = right.length(); // length of right

        // if one string is empty, the edit distance is necessarily the length
        // of the other
        if (n == 0) {
            return m <= threshold ? m : threshold;
        } else if (m == 0) {
            return n <= threshold ? n : threshold;
        }

        if (n > m) {
            // swap the two strings to consume less memory
            final CharSequence tmp = left;
            left = right;
            right = tmp;
            n = m;
            m = right.length();
        }

        // the edit distance cannot be less than the length difference
        if (m - n > threshold) {
            return threshold;
        }

        int[] p = new int[n + 1]; // 'previous' cost array, horizontally
        int[] d = new int[n + 1]; // cost array, horizontally
        int[] tempD; // placeholder to assist in swapping p and d

        // fill in starting table values
        final int boundary = Math.min(n, threshold) + 1;
        for (int i = 0; i < boundary; i++) {
            p[i] = i;
        }
        // these fills ensure that the value above the rightmost entry of our
        // stripe will be ignored in following loop iterations
        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
        Arrays.fill(d, Integer.MAX_VALUE);

        // iterates through t
        for (int j = 1; j <= m; j++) {
            final char rightJ = right.charAt(j - 1); // jth character of right
            d[0] = j;

            // compute stripe indices, constrain to array size
            final int min = Math.max(1, j - threshold);
            final int max = j > Integer.MAX_VALUE - threshold ? n : Math.min(
                    n, j + threshold);

            // ignore entry left of leftmost
            if (min > 1) {
                d[min - 1] = Integer.MAX_VALUE;
            }

            // iterates through [min, max] in s
            for (int i = min; i <= max; i++) {
            	
            	
            	int match = left.charAt(i - 1) == rightJ ? 0 : 2;
            	int cost_replace = p[i - 1]==Integer.MAX_VALUE? p[i - 1] : p[i - 1]+ match;	
            	int cost_insert = p[i]==Integer.MAX_VALUE? p[i] : p[i]+ 1;	;
                int cost_delete = d[i - 1]==Integer.MAX_VALUE? d[i - 1] : d[i - 1]+ 1;	
            	d[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
                
            }

            // copy current distance counts to 'previous row' distance counts
            tempD = p;
            p = d;
            d = tempD;
        }

        // if p[n] is greater than the threshold, there's no guarantee on it
        // being the correct
        // distance
        if (p[n] <= threshold) {
            return p[n];
        }
        return threshold;
	}
 public static double levenshteinDistance (CharSequence lhs, CharSequence rhs) {    
	
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	     int maxLen= 0;
	     if (len0>len1) {
	    	 maxLen=len0;
	     }
	     else {
	    	  	maxLen= len1;
	     }
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) 
	    	cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j-1;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; 
	        cost = newcost;
	        newcost = swap;                          
	    }                                                                               
	                                                                          
	    // the distance is the cost for transforming all letters in both strings        
	    return (cost[len0 - 1]*1.0 )/ (len1+len0) ;                                                          
	}
 
 
 public static AlignObj levenshteinDistancewithAlignment (CharSequence lhs, CharSequence rhs) {    
		
   
			
		    int len0 = lhs.length() + 1;                                                     
		    int len1 = rhs.length() + 1;                                                     
		     int maxLen= 0;
		     if (len0>len1) {
		    	 maxLen=len0;
		     }
		     else {
		    	  	maxLen= len1;
		     }
		    // the array of distances                                                       
		    int[] cost = new int[len0];                                                     
		    int[] newcost = new int[len0];    
		    String[] costString = new String[len0];                                                     
		    String[] newcostString = new String[len0];    
		     String[][] alignment= new String [len1][len0];                                                                              
		    // initial cost of skipping prefix in String s0                                 
		    for (int i = 0; i < len0; i++) 
		    	cost[i] = i;                                     
		    alignment[0][0] ="";newcostString[0] ="";
		    for (int i = 1; i < len0; i++) 
		    	alignment[0][i] =alignment[0][i-1]+ "> Deletion " + lhs.charAt(i-1);
		    for (int i = 1; i < len1; i++) 
		    	alignment[i][0] =alignment[i-1][0]+ "> Insertion " + rhs.charAt(i-1);
		    // dynamically computing the array of distances                                  
		    boolean deleted= true;                                                                                
		    // transformation cost for each letter in s1                                    
		    for (int j = 1; j < len1; j++) {                                                
		        // initial cost of skipping prefix in String s1                             
		        newcost[0] = j-1;                                                             
		        // transformation cost for each letter in s0                                
		        for(int i = 1; i < len0; i++) {                                             
		            // matching current letters in both strings                             
		            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;             
		                                                                                    
		            // computing cost for each transformation                               
		            int cost_replace = cost[i - 1] + match;                                 
		            int cost_insert  = cost[i] + 1;                                         
		            int cost_delete  = newcost[i - 1] + 1;                                  
		                                                                                    
		            // keep minimum cost                                                    
		            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
		            if (match==0)
		            	alignment[j][i]= alignment[j-1][i-1]+"> Sync "+ rhs.charAt(j - 1);
		            else { if (Math.min(cost_insert, cost_delete) < cost_replace ) 
		        	  	{
		            		if(cost_insert>cost_delete)
		            			alignment[j][i]= alignment[j][i-1]+"> Deletion " + lhs.charAt(i-1);
		            		else
		            			alignment[j][i]= alignment[j-1][i]+ "> Insertion " + rhs.charAt(j-1);
		        	  	}
		            else
		            	alignment[j][i]= alignment[j-1][i-1]+ "> Insertion " + rhs.charAt(j-1) +"> Deletion " + lhs.charAt(i-1);

		            }      
		        }
		                                                                                  
		        // swap cost/newcost arrays                                                 
		        int[] swap = cost; 
		        cost = newcost;
		        newcost = swap;         

		       
		    }                                         
		    String align = alignment[len1-1][len0-1].substring(1);
		    AlignObj alignObj = new AlignObj(align, (cost[len0 - 1]*1.0 ) / (len1+len0)) ;                                                                    
		    // the distance is the cost for transforming all letters in both strings        
		    return alignObj;//alignment[len0][len0-1] ;                                                          
		}

 
 
 public static void quicksort(float[] main, int[] index) {
	    quicksort(main, index, 0, index.length - 1);
	}

	// quicksort a[left] to a[right]
	public static void quicksort(float[] a, int[] index, int left, int right) {
	    if (right <= left) return;
	    int i = partition(a, index, left, right);
	    quicksort(a, index, left, i-1);
	    quicksort(a, index, i+1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition(float[] a, int[] index, 
	int left, int right) {
	    int i = left - 1;
	    int j = right;
	    while (true) {
	        while (less(a[++i], a[right]))      // find item on left to swap
	            ;                               // a[right] acts as sentinel
	        while (less(a[right], a[--j]))      // find item on right to swap
	            if (j == left) break;           // don't go out-of-bounds
	        if (i >= j) break;                  // check if pointers cross
	        exch(a, index, i, j);               // swap two elements into place
	    }
	    exch(a, index, i, right);               // swap with partition element
	    return i;
	}

	// is x < y ?
	private static boolean less(float x, float y) {
	    return (x < y);
	}

	// exchange a[i] and a[j]
	private static void exch(float[] a, int[] index, int i, int j) {
	    float swap = a[i];
	    a[i] = a[j];
	    a[j] = swap;
	    int b = index[i];
	    index[i] = index[j];
	    index[j] = b;
	}
 
 
 
	
	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	
	
	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}
 
	
	
	private static TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClass dummyEvClass,
			XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (Transition t : net.getTransitions()) {
			boolean mapped = false;

			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();
				String label = t.getLabel();
												
				if (label.equals(id)) {
					mapping.put(t, evClass);
					mapped = true;
					break;
				}
			}
		}

		return mapping;
		}



	
	
	public static AlignmentReplayResult calculateAlignmentWithICC(final PluginContext context, IncrementalReplayer replayer, PetrinetGraph net, XLog log, IccParameters parameters, TransEvClassMapping mapping) 
	{	

		IncrementalConformanceChecker icc =new IncrementalConformanceChecker(context, replayer, parameters, log, net);
		IccResult iccresult = icc.apply(context, log, net, mapping);
		Map<String, Integer> asynchMoveAbs=new TreeMap<String, Integer>();
		Map<String, Double> asynchMoveRel=new TreeMap<String, Double>();
		
		if(parameters.getGoal().equals("alignment")) {
			int asynchMovesSize=iccresult.getAlignmentContainer().getAsynchMoves().size();

			for (String key:iccresult.getAlignmentContainer().getAsynchMoves().elementSet()) {
				int absValue=iccresult.getAlignmentContainer().getAsynchMoves().count(key);
				double relValue=(double)absValue/(double)asynchMovesSize;
				asynchMoveAbs.put(key, absValue);
				asynchMoveRel.put(key, relValue);
			}
			AlignmentReplayResult result = new AlignmentReplayResult(iccresult.getFitness(), iccresult.getTime(), iccresult.getTraces(), asynchMovesSize, asynchMoveAbs, asynchMoveRel);
			return result;
		}
		else {
			AlignmentReplayResult result = new AlignmentReplayResult(iccresult.getFitness(), iccresult.getTime(), iccresult.getTraces(), -1, asynchMoveAbs, asynchMoveRel);
			return result;		
			}
		
		}

	
	
	public static class AlignObj {
		  public final String Alignment;
		  public final double cost;

		  public AlignObj(String Alignment, double d) {
		    this.Alignment = Alignment;
		    this.cost = d;
		  }
		}
	
    public static String lcs(String a, String b) {
        String x;
        String y;

        int alen = a.length();
        int blen = b.length();
        if (alen == 0 || blen == 0) {
            return "";
        } else if (a.charAt(alen-1)==b.charAt(blen-1)){
        	  return lcs(a.substring(0,alen-1),b.substring(0,blen-1)) + a.charAt(alen-1);
        } else {
            x = lcs(a, b.substring(0, blen - 1));
            y = lcs(a.substring(0, alen - 1), b);
        }
        return (x.length() > y.length()) ? x : y;
    }
	

	
}
	



	
	
  



    
    
    
    
    
    

    
    
    

    
    

    

	