package org.processmining.logfiltering.Juan.algo.Juan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.MultiValuedMap;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinetsimulator.constants.LogConstants;
import org.processmining.petrinetsimulator.parameters.SimulationSettings;

import com.google.common.collect.Lists;

public abstract class LogSimulator {

	protected Petrinet petriNet;
	protected Marking initialMarking;
	protected SimulationSettings settings;
	protected XFactory factory;
	protected HashMap<String, String> ActivityCoder;
	protected HashMap<String, Double> probs;
	protected HashMap<String, Double> startProb;
	protected double uniqueness;

	protected HashMap<String, Integer> cVariants;
	protected HashMap<Transition, String> TauCoder;
	protected MultiValuedMap<Transition, Transition> visSucc;
	protected int n = 1000;

	public XLog simulate() {
		return null;
	}
	
	protected XEvent createEvent(Transition t, long time) {

		XAttributeMap atts = factory.createAttributeMap();
		atts.put(LogConstants.EVENTID, factory.createAttributeLiteral(LogConstants.EVENTID, t.getLabel(), null));
		atts.put(LogConstants.TIMESTAMP, factory.createAttributeTimestamp(LogConstants.TIMESTAMP, time, null));
		atts.put(LogConstants.LIFECYCLE, factory.createAttributeLiteral(LogConstants.LIFECYCLE, "complete", null));
		return factory.createEvent(atts);
	}
	
	private void adjustProb(transInfo c) {}
	
	private XTrace simulateTrace(long startTime, int traceID) {
		return null;
	}
	
	private Transition nextTransition(Set<Transition> enabledTransitions) {
		return null;
	}
	
	protected void assignProbs(Set<Transition> enabledTransitions, ArrayList<transInfo> visi,
			ArrayList<transInfo> invisi) {
		
		transInfo c;
		double total = 0.0;
		//double max = Double.MIN_VALUE;
		String prevVisibleFired = traceState.getPrevVisibleFired();

		for (Transition et : enabledTransitions) {

			if (et.isInvisible()) {					
				c = new transInfo(TauCoder.get(et), et, 0.0);
				invisi.add(c);
			} else {
				//Start, no previous transition
				if (prevVisibleFired.equals("")) {
					String codedLabel = ActivityCoder.get(et.getLabel());
					if (startProb.get(codedLabel) != null)
						c = new transInfo(codedLabel, et, startProb.get(codedLabel));
					else
						c = new transInfo(codedLabel, et, 0.0);
					total+= c.getProb();
				} else {
					String codedLabel = ActivityCoder.get(et.getLabel());
					String DF = prevVisibleFired + codedLabel;
					c = new transInfo(codedLabel, et, probs.get(DF));
			}
				
				//max = Math.max(max, c.getProb());
				
				total += c.getProb();
				visi.add(c);
			}
		}


		//compute invisible trans prob (distribute according to # of invis trans)
		if (!invisi.isEmpty()) {
//			final double invisProb =(1 - total)/invisi.size();
//			final double m = max;
//			double s = invisi.size();
			if (!visi.isEmpty()) 
				//getVisibleSuccessorsProb(x.getTrans(),invisi.size()+visi.size())
				invisi.forEach(x-> x.setProb(getVisibleSuccessorsProb(x.getTrans(),invisi.size()+visi.size())*Math.random()));
			else {
				
				//int inEdges = invisi.stream().map(x -> x.getTrans().getGraph().getInEdges(x.getTrans()).size()).reduce(0, Integer::sum);
				
				invisi.forEach(x ->  x.setProb(1.0/invisi.size()));
				
				//invisi.forEach(x -> x.setProb(1.0/invisi.size()));
				}
//			for(transInfo tf: invisi) 
//				//tf.setProb(invisProb*Math.random());
//				tf.setProb(getVisibleSuccessorsProb(tf.getTrans(),invisi.size())*Math.random());
		} 

			
	}
	
	protected Transition naiveChoice(Set<Transition> enabledTransitions) {
		return Lists.newArrayList(enabledTransitions).get((int) Math.floor(Math.random() * enabledTransitions.size()));
	}
	
	protected transInfo choseRandom(ArrayList<transInfo> trans) {

		transInfo result = null;
		TreeMap<Double, transInfo> map = new TreeMap<>();

		double total = 0.0;

		for (int i = 0; i < trans.size(); i++) {
			if(trans.get(i).getProb()>0.0)
				map.put(total += trans.get(i).getProb(), trans.get(i));
			}

		if(map.isEmpty())
			return trans.get((int) Math.floor(Math.random() * trans.size()));
		
		Random generator = new Random();
		// Generate a random value between 0 and 1
		double value = generator.nextDouble();
		value *= total;
		// Get the object that matches with the generated number
		//System.out.println(map.keySet() + " " +value);
		result = map.ceilingEntry(value).getValue();

		return result;
	}

	protected double getVisibleSuccessorsProb(Transition t, int n) {
		
		double max = 0.0;	

		String prevVisibleFired = traceState.getPrevVisibleFired();
		String prevFired = traceState.getPrevFired();
		
			if (prevFired.equals("") && !visSucc.get(t).isEmpty()) {

				for (Transition tr : visSucc.get(t)) {
					String codedLabel = ActivityCoder.get(tr.getLabel());

					if (startProb.get(codedLabel) != null) 
						max = Math.max(max,startProb.get(codedLabel));

				}			
				
			}else if(!visSucc.get(t).isEmpty()){

				max = visSucc.get(t).stream()
						.map(x-> probs.get(prevVisibleFired + ActivityCoder.get(x.getLabel()))).reduce(0.0, Double::max);
				
			}
			else{
//					String prevVisibleDF = traceState.getPrevVisibleDF();
//					
//					if(prevVisibleDF.length()==1)
//						max = startProb.get(prevVisibleDF);
//					else if (prevVisibleDF.length()==2)
//						max = probs.get(prevVisibleDF);
				max = Math.random()/n;

			}
			
			return max;
	}
	
	protected static class traceState {


		private static String trace = "";
		private static String codedTrace = "";
		public static String getCodedTrace() {
			return codedTrace;
		}

		public static void setCodedTrace(String codedTrace) {
			traceState.codedTrace = codedTrace;
		}

		private static String codedTrieTrace = "";
		private static String prevVisibleFired = "";
		private static String prevFired = "";
		private static String prevVisibleDF = "";
		private static String prevDF = "";
		private static Transition prevTrans = null;
		private static Transition prevVisibleTrans = null;
		public static Transition getPrevVisibleTrans() {
			return prevVisibleTrans;
		}

		public static void setPrevVisibleTrans(Transition prevVisibleTrans) {
			traceState.prevVisibleTrans = prevVisibleTrans;
		}

		private static double probability = 1.0;
		private static double prevProb = 1.0;
		private static boolean hasLoops = false;
		private static HashSet<String> selfLoops = new HashSet<String>();
		private static HashMap<String, Integer> loops = new HashMap<String, Integer>();
		private static HashMap<String, Integer> loopsT = new HashMap<String, Integer>();
		private static HashSet<String> fired = new HashSet<String>();
		
		public static boolean hasSelfLoops(String t) {
			
			if(selfLoops.contains(t))
				return true;
			else
				return false;
		}
		
		public static void setSelfLoops(Set<Transition> enabledTransitions) {
			
			if(enabledTransitions.contains(prevVisibleTrans))
				selfLoops.add(prevVisibleFired);
		}
		
		public static void clearSelfLoops() {
			selfLoops.clear();
		}
		
		public static HashSet<String> getSelfLoops() {
			return selfLoops;
		}
		
 		public static void hasLoops(boolean val) {
			traceState.hasLoops = val;
		}
		
		public static boolean hasLoops() {
			return hasLoops;
		}

		public static double getPrevProb() {
			return prevProb;
		}

		public static void setPrevProb(double prevProb) {
			traceState.prevProb = prevProb;
		}

		public static String getPrevDF() {
			return prevDF;
		}

		public static void setPrevDF(String prevDF) {
			traceState.prevDF = prevDF;
		}

		public static void updateTrace(String activity, String codedActivity, Transition trans, double prob) {	
			probability = prob;
			
			if (trans.isInvisible())
				activity = "";
			else {
				prevVisibleDF = prevVisibleFired + codedActivity;
				prevVisibleFired = codedActivity;
				prevVisibleTrans = trans;
				codedTrace += codedActivity;
			}
			
			fired.add(codedActivity);
			prevDF = prevFired + codedActivity;
			prevFired = codedActivity;

			String tempDF = prevFired + codedActivity;
			if (!tempDF.equals("")) {
				if (loops.get(tempDF) == null)
					loops.put(tempDF, 0);
				else
					loops.put(tempDF, loops.get(tempDF) + 2);

			}
				
			
			if (loopsT.get(codedActivity) == null)
				loopsT.put(codedActivity, 1);
			else
				loopsT.put(codedActivity, loopsT.get(codedActivity) + 1);

			if(!hasLoops)
			if(getFiringCount(codedActivity)>1)
				hasLoops = true;
			
			trace += activity + "";
			codedTrieTrace += codedActivity + "-";
			prevTrans = trans;
			
		}
	
		public static HashSet<String> getFired() {
			return fired;
		}

		public static void setFired(HashSet<String> fired) {
			traceState.fired = fired;
		}

		public static int firedCount() {
			return fired.size();
		}
		
		public static Set<String> firedTransitions() {
			return fired;
		}
		
		public static void updateState(transInfo c) {

			traceState.updateTrace(c.getTrans().getLabel(), c.getCodedLabel(), c.getTrans(), c.getProb());

		}

		public static void clear() {
			trace = "";
			codedTrace="";
			codedTrieTrace = "";
			prevDF = "";
			prevVisibleDF = "";
			prevFired = "";
			prevVisibleFired = "";
			prevTrans = null;
			prevVisibleTrans = null;
			probability = 1.0;
			prevProb = 1.0;
			loops.clear();
			loopsT.clear();
			fired.clear();

		}

		public static int getFiringCount(String t) {
			if (loopsT.containsKey(t))
				return loopsT.get(t);
			else
				return 0;
		}
		
		public static HashMap<String, Integer> getLoopsT() {
			return loopsT;
		}

		public static void setLoopsT(HashMap<String, Integer> loopsT) {
			traceState.loopsT = loopsT;
		}

		public static void resetLoopCount(String t) {
			
			if(loopsT.containsKey(t))
				loopsT.put(t, 0);
			
		}

		public static double getProb() {
			return probability;
		}

		public static void setProb(double prob) {
			traceState.probability = prob;
		}

		public static String getTrace() {
			return trace;
		}

		public static void setTrace(String trace) {
			traceState.trace = trace;
		}

		public static String getCodedTrieTrace() {
			return codedTrieTrace;
		}

		public static void setCodedTrieTrace(String codedTrace) {
			traceState.codedTrieTrace = codedTrace;
		}

		public static String getPrevFired() {
			return prevFired;
		}

		public static void setPrevFired(String prevFired) {
			traceState.prevFired = prevFired;
		}

		public static Transition getPrevTrans() {
			return prevTrans;
		}

		public static void setPrevTrans(Transition prevTrans) {
			traceState.prevTrans = prevTrans;
		}

		public static String getPrevVisibleFired() {
			return prevVisibleFired;
		}

		public static void setPrevVisibleFired(String prevVisibleFired) {
			traceState.prevVisibleFired = prevVisibleFired;
		}

		public static String getPrevVisibleDF() {
			return prevVisibleDF;
		}

		public static void setPrevVisibleDF(String prevVisibleDF) {
			traceState.prevVisibleDF = prevVisibleDF;
		}

		
	}
	
	
}
