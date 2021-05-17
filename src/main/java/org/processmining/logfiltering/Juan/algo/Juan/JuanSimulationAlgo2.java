package org.processmining.logfiltering.Juan.algo.Juan;


import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.logfiltering.Juan.enumtypes.SimulationType;
import org.processmining.logfiltering.Juan.parameters.SimulationWizardParameters;
import org.processmining.logfiltering.Juan.trie.Trie;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public class JuanSimulationAlgo2 {
	
	public static String apply(XLog InputLog,Petrinet net, SimulationWizardParameters parameters) { 

		long totalTime = System.currentTimeMillis();
		long preProcessingTime = System.currentTimeMillis();
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		TransEvClassMapping mapping = constructMapping(net, InputLog, dummyEvClass, eventClassifier);
		XEventClassifier EventCol = parameters.getEventClassifier();
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		HashMap<String,String >ActivityCoder =new HashMap<String, String>();
		HashMap<String,String >ActivityDeCoder =new HashMap<String, String>();
		
		int charcounter=65;
		for (XEventClass clazz : logInfo.getNameClasses().getClasses()){			
			ActivityCoder.put(clazz.toString(), Character.toString((char)charcounter));
			ActivityDeCoder.put( Character.toString((char)charcounter),clazz.toString());
			charcounter++;
		}

		//Get markings
		PetriNetTools netTools = new PetriNetTools(net, ActivityCoder);
		Marking initialMarking = netTools.getInitialMarking();
		Marking finalMarking = netTools.getFinalMarking();
		
		String netActivities = netTools.getNetActivities();
		
		HashMap<String,Integer >codedVariants =new HashMap<String, Integer>();
		HashMap<String, Double> startProbs = new HashMap<String, Double>();
		HashMap<String, Double> probs = new HashMap<String, Double>();
		HashMap <String,Double> kPrefix = new HashMap <String,Double>();
		HashMap<String,Integer> count = new HashMap<String,Integer>();
		
		Pattern repeatingPattern = Pattern.compile("(.)\\1{2,}");
		Pattern special = Pattern.compile("[^A-Za-z0-9]");
		netActivities = special.matcher(netActivities).replaceAll("\\\\$0");
		
		int maxTraceLength= -1;
		for (XTrace trace : InputLog) {

			maxTraceLength = Math.max(maxTraceLength, trace.size());
					
			String codedTrace = "";
			for (XEvent event : trace)
				codedTrace += ActivityCoder.get(event.getAttributes()
										   .get(EventCol.getDefiningAttributeKeys()[0])
										   .toString());
			
			String startAct = Character.toString(codedTrace.charAt(0));
			if(startProbs.get(startAct)==null)	
				startProbs.put(startAct,1.0);
			else
				startProbs.put(startAct,startProbs.get(startAct)+1);
				

			if (!codedVariants.containsKey(codedTrace)){
				codedVariants.put(codedTrace, 1);
			}else
				codedVariants.put(codedTrace, codedVariants.get(codedTrace)+1);
				
		}
		
		HashMap<String,Double> lbCosts = new HashMap<String,Double>();
		double uniqueness = (double)codedVariants.size()/InputLog.size();
		int prefixSize = parameters.getPrefixSize();
		int rep = 2;
		
		for (String var: codedVariants.keySet()) {
			
			String DF = "";
			String codedVariant = var;
			double varFreq = codedVariants.get(var);
			
			//Keep only activities that are present in the Petri net
			codedVariant = codedVariant.replaceAll("[^" +netActivities+"]", "");
			lbCosts.put(var, (double)var.length()-codedVariant.length());			
			
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

			if(codedVariant.length()>=prefixSize) {
				String prefix = codedVariant.substring(0, prefixSize);
				for(int k = 1; k < prefix.length(); k++) {
						
					DF = prefix.substring(0, k + 1);
					if(probs.containsKey(DF))
						probs.put(DF, probs.get(DF)+varFreq);
					else 
						probs.put(DF, varFreq+1);
	
					String s = DF.substring(0, k);					
					if(kPrefix.containsKey(s)) 		
							kPrefix.put(s,kPrefix.get(s)+varFreq);
						else
							kPrefix.put(s,varFreq+netActivities.length());
				}
			}
			
			for (int j = 0; j < codedVariant.length()-prefixSize; j++) {
							
				DF = codedVariant.substring(j, prefixSize+j+1);
				if(probs.containsKey(DF)) 
					probs.put(DF, probs.get(DF)+varFreq);		
				else 
					probs.put(DF, varFreq+1);

				String s = DF.substring(0, prefixSize);		
				if(kPrefix.containsKey(s)) 		
					kPrefix.put(s,kPrefix.get(s)+varFreq);
				else
					kPrefix.put(s,varFreq+netActivities.length());
			}
			
		}
		
		//Settings for computing the alignment of an empty trace
		int nThreads = 4;
		int costUpperBound = Integer.MAX_VALUE;
		int timeoutMilliseconds = 10 * 1000;
		long preProcessTimeNanoseconds = 0;
		XLogInfo summary = XLogInfoFactory.createLogInfo(InputLog, eventClassifier);
		XEventClasses classes = summary.getEventClasses();

		
		final int total = InputLog.size();
		probs.replaceAll((k,v) -> v/kPrefix.get((k.substring(0, k.length()-1))));
		startProbs.replaceAll((k,v)-> v/total);			
		probs.putAll(startProbs);
		
		rep = Math.max(rep, count.values().stream().max(Comparator.comparing(Double::valueOf)).get())/ codedVariants.size();
		
		MixedSimulator2 lgSimulator = 
				new MixedSimulator2(parameters, probs, kPrefix, uniqueness, maxTraceLength, rep, netTools);	
		preProcessingTime = System.currentTimeMillis() - preProcessingTime;
		
		long simulationTime = System.currentTimeMillis();
		lgSimulator.simulate2();
		simulationTime = System.currentTimeMillis() - simulationTime;

		HashSet<String> cVariants = lgSimulator.getcompressedVariants();
		HashSet<String> Variants = lgSimulator.getVariants();
		Trie dicPrefix= lgSimulator.getDicPrefix();
		
		XLog TraceLog = factory.createLog();
		TraceLog.setAttributes(InputLog.getAttributes());
		XAttributeMapImpl case_map = new XAttributeMapImpl();
		String case_id = String.valueOf(charcounter);
		case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
		XTraceImpl trace = new XTraceImpl(case_map);
		TraceLog.add(trace);	
		Integer max = 2147483647;
		ReplayerParameters RepParameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
		//ReplayerParameters RepParameters = new ReplayerParameters.AStar();

		
		Replayer replayer = new Replayer(RepParameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);
		Future<TraceReplayTask>[] futures = new Future[TraceLog.size()];
		ExecutorService service = Executors.newFixedThreadPool(RepParameters.nThreads);

		//Compute the alignmet of an empty trace to get the SPM
		for (int i = 0; i < TraceLog.size(); i++) {
			// Setup the trace replay task
			TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog.get(i), i, timeoutMilliseconds,
					RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);

			// submit for execution
			futures[i] = service.submit(task);
		}
	
		int SPM = 0;
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
			SPM = (int)replayResult.getInfo().get("Raw Fitness Cost").longValue();
			List<Object> ModelBehavior = replayResult.getNodeInstance();
			List<StepTypes> TypeBehavior = replayResult.getStepTypes();
			String modelTrace="";
			for (int j=0;j<replayResult.getNodeInstance().size();j++) {

				if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
					if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  
						modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());	
					else {
						ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
						modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
						charcounter++;
					}
				}

			}

			//If SPM is not in L_S then add it
			if (!Variants.contains(modelTrace)) {
				Variants.add(modelTrace);
				dicPrefix.add(modelTrace);
			}
			

		}
		
		long approxTime = System.currentTimeMillis();
		String[] var = new String[Variants.size()];
		Variants.toArray(var);

		int n = Variants.size() + cVariants.size();
 		double totalLowerBoundCost = 0;
		double totalAppCost = 0;
		double totalUpperBoundCost = 0;
		double totalT = 0;
		HashMap<String, Integer> computed = new HashMap<String, Integer>();
		boolean compressed = parameters.getSimulationType() == SimulationType.CombinedLog;
		Map<String, Integer> x ;
		for (String LV: codedVariants.keySet()) {
			int minAlignmentCost=0;
			if(!dicPrefix.search(LV)) {
				while (true) {
					minAlignmentCost++;
					 x = dicPrefix.getSimilarityMap(LV,minAlignmentCost);
					if (x.size()>0)
						break;
				}
			}
			
			double appCost, lowerBoundCost, upperBoundCost;
			String LVc = "";
			
			//Raw upper bound cost
			upperBoundCost = Variants.parallelStream()
						.map(SV -> levenshteinDistanceCost(LV,SV))
						.reduce(Integer.MAX_VALUE, Integer::min);
			
			appCost = upperBoundCost;
			if (minAlignmentCost!= appCost) {
				n++;
				
				for (int i = 0; i < var.length; i++) {
					if(levenshteinDistanceCost(LV,var[i])==appCost )
						n++;
				}
			}
			if(compressed && lgSimulator.anyReapetitvePatterns()) {
					
				LVc = lgSimulator.removeRepetitivePatterns(LV);
				final String lvc = LVc;
				double appCostCompressed;
				
				if(!computed.containsKey(LVc)) {
					int m = (int) appCost;
					double appCost1 = Variants.parallelStream()
									.map(SV -> levenshteinDistanceCost(lvc,SV,m))
									.reduce(Integer.MAX_VALUE, Integer::min);
					
					double appCost2 = cVariants.parallelStream()
							.map(SV -> levenshteinDistanceCost(lvc,SV,(int)appCost1))
							.reduce(Integer.MAX_VALUE, Integer::min);	
					
					appCostCompressed = Math.min(appCost1, appCost2);
					computed.put(LVc, (int)appCostCompressed);
				}else
					appCostCompressed = computed.get(LVc);
				
				appCost = Math.min(appCost, appCostCompressed);

			}

			//Raw lower bound cost
			if(!lgSimulator.isTrieEmpty()) {
				lowerBoundCost = LV.length() < SPM ? Math.max(SPM - LV.length(), lbCosts.get(LV)): lbCosts.get(LV);	
				int t = Math.min(lgSimulator.getminPrefixLength(),LV.length());
				String head = LV.substring(0, t);
				totalT+=t;	
				HashSet<String> prfs = lgSimulator.getPrefixes().get(t-1);
				double lcs = prfs.parallelStream()
							.map(p -> longestCommonSubsequence(head,p))
							.reduce(0, Integer::max);
				lowerBoundCost = Math.max(lowerBoundCost, t - lcs);
				if(appCost<lowerBoundCost)
					appCost = Math.ceil((upperBoundCost+lowerBoundCost)/2.0);
			}else
				lowerBoundCost = upperBoundCost;
			
			//Variant frequency
			double freq = codedVariants.get(LV);
			
			//Compute the approximation cost
			double alignmentAppCost = appCost/(SPM + LV.length())*freq;
			totalAppCost +=  alignmentAppCost;
			
			//Compute the upper bound cost
			upperBoundCost = upperBoundCost/(SPM + LV.length())*freq;
			totalUpperBoundCost += upperBoundCost;
			
			//Compute the lower bound cost
			lowerBoundCost = lowerBoundCost/(SPM + LV.length())*freq;
			totalLowerBoundCost += lowerBoundCost;
		}
	
		//Compute the total fitness 
		double conformanceAppValue = 1 - (totalAppCost/InputLog.size());
		double lowerBoundFitness = 1 - (totalUpperBoundCost/InputLog.size());
		double upperBoundFitness = 1 - (totalLowerBoundCost/InputLog.size());
		totalT = totalT/codedVariants.size();
		approxTime = System.currentTimeMillis() - approxTime;
/*
		long fitnessTime = System.currentTimeMillis();
		//////////////////////////////////Actual Fitness/////////////////////////////////////////////	 
		double[] AlignmentCosts2= new double [InputLog.size()];
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
			 fitnessTime =  System.currentTimeMillis() - fitnessTime;
			 long timer3= System.currentTimeMillis() - time ;
			 double PerformanceImprovement= timer2 *1.0 / timer2;  
			 double AccuracyClosness= Math.abs(appLowerBound -  ActualFitnessValue ); 
		///////////////////////////////////////////////////////////////////////////////////////////
/*				 
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

			 double AlignFit2=AlignFit/LogSize2;s
*/
		//String outp=(n + "\t" +ApproximatedFitness2 +"\t\t" + time1 +"\t" + time2 +"\t" + UpperBound+"\t" + LowerBoundFitness+"\t" + te);
		//String out2 = "Approximation ==> " + conformanceAppValue + "    " + "\nUpperBound ==> " + upperBoundValue + "    " + "LowerBound ==> " + lowerBoundValue;
		totalTime = System.currentTimeMillis()- totalTime;
		String outP = (Variants.size() + "==>>" +conformanceAppValue +"==>>" +upperBoundFitness+"==>>" +lowerBoundFitness+"==>>"+ preProcessingTime +"==>>" + simulationTime +"==>>" + approxTime +"==>>" + totalTime);
		//System.out.println(outPut + "\t" + lowerBoundValue);
//		String color = colorPicker(lowerBoundValue,upperBoundValue,lgSimulator.isTrieEmpty());
		
		String outPut = "<html><table>\n" + 
				"<tbody>\n" + 
				"<tr>\n" + 
				"<td style=\"width: 100px; font-size: 1.2em;\">Upper Bound:</td>\n" + 
				"<td style=\"font-size: 1.2em;\">"+upperBoundFitness+"</td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td style=\"width: 100px; font-size: 1.2em;\">Approximation:</td>\n" + 
				"<td style=\"font-size: 1.2em; color: "+"red"+ ";\">"+conformanceAppValue+"</td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td style=\"width: 100px; font-size: 1.2em;\">Lower Bound:</td>\n" + 
				"<td style=\"font-size: 1.2em;\">"+lowerBoundFitness+"</td>\n" + 
				"</tr>\n" + 
				"<tr>\n" +
				"<td style=\"width: 100px; font-size: 1.2em;height: 300px\">&nbsp;</td>\n" + 	
				"</tr>\n" +
				"</tbody>\n" + 
				"</table></html>";
		
		
		return outPut;

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

	private static String colorPicker(double lb, double ub, boolean empty) {
		String color = "";
		
		//#0ffc03 - green, #fc9803 - orange, #fc0303 - red
		if(ub-lb<=0.15 || empty)
			color = "#20ab00";
		else if (ub-lb<=0.3)
			color = "#cc7518";
		else
			color = "#ab0000";
		
		return color;
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
	
	public static int levenshteinDistanceCost(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

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
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }
	
	private static int longestCommonSubsequence(CharSequence lhs, CharSequence rhs) {
		int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = 0;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = 0;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {

            	if(lhs.charAt(i - 1) == rhs.charAt(j - 1))
            		newcost[i] = 1 + cost[i-1];
            	else
            		newcost[i] = Math.max(newcost[i-1], cost[i]);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
		
	}
	
}


























