package org.processmining.logfiltering.Juan.algo.Juan;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.logfiltering.Juan.enumtypes.SimulationType;
import org.processmining.logfiltering.Juan.parameters.SimulationWizardParameters;

//import weka.core.Trie;

import org.processmining.logfiltering.Juan.trie.Trie;
import org.processmining.logfiltering.algorithms.PetrinetUtilsCombo;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class MixedSimulator2 extends LogSimulator2{

	private Trie trie;
	private boolean removed = true;
	private HashSet<String> patterns;
	private ArrayList<String> sortedPatterns;
	private Pattern scapeSpecial = Pattern.compile("[^A-Za-z0-9]");
	private Pattern repeatingPattern = Pattern.compile("(.+?)\\1+");
	private boolean compressed;
	private ArrayList<HashSet<String>> PX; /// all the model prefixes 
	private ArrayList<HashSet<String>> tempPX;//  not extended 
	private HashSet<String> tempRemoved;  /// 
	private int minPrefixLength;
	
	public MixedSimulator2(SimulationWizardParameters parameters, HashMap<String, Double> probs, 
		   HashMap<String, Double> kPrefix, double uniqueness, int maxTraceLength, int maxRep, PetriNetTools netTools) {
		
		//Parameters from Wizzard
		compressed = parameters.getSimulationType() == SimulationType.CombinedLog;
		this.numOfTraces = parameters.getNumOfTraces();
		this.batchSize =  parameters.getBatchSize();
		this.diffThreshold = parameters.getRepRatioThreshold();
		this.timeOut = parameters.getTimeOut()*1000;
		
		//Petri Net tools
		this.petriNet = netTools.getNet();
		this.initialMarking = netTools.getInitialMarking();
		this.ActivityCoder = netTools.getActivityCoder();
		this.TauCoder = netTools.getTauCoder();
		this.visSucc = netTools.getVisSucc();
		this.invisProb = new HashMap<String, Double>();
		this.netActivites = netTools.getNetActivities();
		
		//Simulation variables
		this.traceState = new TraceState();
		this.traceState.setPrefixSize(parameters.getPrefixSize());
		this.probs = probs;
		this.kPrefix = kPrefix;
		this.uniqueness = uniqueness;
		this.maxTraceLength = maxTraceLength;
		this.maxRep = maxRep;
		this.Variants = new HashSet<String>();
		this.trieDic= new Trie(true, StandardCharsets.UTF_8);
		this.cVariants = new HashSet<String>();
		this.patterns = new HashSet<String>();
		this.sortedPatterns = new ArrayList<String>();
		this.trie = new Trie(true, StandardCharsets.UTF_8);;
		this.tempPX = new ArrayList<HashSet<String>>(maxTraceLength);
		this.PX = new ArrayList<HashSet<String>>(maxTraceLength);
		this.tempRemoved = new HashSet<String>();

		//Default configuration
		if(batchSize == 0) {   //// it retunrs the defaul setting 
			diffThreshold = 0.001;
			
			if(uniqueness<0.10) 
				batchSize = 200;
			else if(uniqueness<0.25) 
				batchSize = 500;
			else if(uniqueness<0.50)
				batchSize = 1000;
			else
				batchSize = 2000;
		}
		
	}
	
	public void simulate2() {

		int loopCount = 0;
		double previousRepRatio = 1.0;
		double currentRepRatio = 0.0;
		int checkPoint = batchSize;
		
		long initialTime = System.currentTimeMillis();
		while (true) {

			//Simulate trace
			String codedTrace = simulateTrace2();

			//Keep only unique traces, i.e., variants by using a HashSet
			if (codedTrace != null) {
				Variants.add(codedTrace);
				//////////// ************* add the prefix tree here ***************
				trieDic.add(codedTrace);
				//If the trace has a repeated element &&
				//parameters asked for compression, then
				//collapse the trace to only one repetition
				if(traceState.anyRepeatedActivity() && compressed) {
					String compressedTrace = compressTrace(codedTrace);			
					cVariants.add(compressedTrace);
				}
				
			}
			
			//Compute difference in Repetition Ration
			loopCount++;
			if(checkPoint == loopCount) {
				
				//Repetition Ratio
				currentRepRatio = (double)Variants.size()/checkPoint;
				//Delta
				deltaRepRatio = Math.abs(previousRepRatio-currentRepRatio);
				previousRepRatio = currentRepRatio;
				checkPoint += batchSize;		
			}

			//Reset traceState and set removed to false to avoid
			//adding more partial sequences to the Trie once the
			//next iteration begins
			removed = false;
			traceState.resetTraceState();
	
			boolean timeExceeded = (System.currentTimeMillis() - initialTime) > timeOut;
			
			//Stop the simulation if n is reached, Trie is empty, deltaR < threshold or time is exceeded
			if (Variants.size() == numOfTraces || trie.getNumberOfWords()==0 || deltaRepRatio<diffThreshold ||timeExceeded) {
				break;
				
			}
		}

		//Sort Repetitive Patterns according to their length, longer first. 
		sortedPatterns.addAll(patterns);
		sortedPatterns.sort(Comparator.comparingInt(String::length));
		minPrefixLength = minPrefixLength();
	}
			
	private String simulateTrace2() {

		//initialize markings
		Map<Place, Integer> markings = new HashMap<Place, Integer>();
		for (Place p : petriNet.getPlaces())
			markings.put(p, 0);

		//update with initial marking
		for (Place p : initialMarking.baseSet())
			markings.put(p, 1);

		//for a given max number of events
		int i = 0;

		while (i <= maxTraceLength) {

			//detect enabled transitions and fire a random one
			Set<Place> placesWithTokens = new HashSet<Place>();
			for (Place p : markings.keySet())
				if (markings.get(p) > 0)
					placesWithTokens.add(p);

			Set<Transition> nextTransitions = new HashSet<Transition>();
			//first add all
			for (Place p : placesWithTokens)
				nextTransitions.addAll(PetrinetUtilsCombo.getNextTransitions(p, petriNet));
			
			//now keep only the enabled ones
			Set<Transition> enabledTransitions = new HashSet<Transition>();
			for (Transition t : nextTransitions)
				if (PetrinetUtilsCombo.isTransitionEnabled(t, markings, petriNet))
					enabledTransitions.add(t);

			//if there are enabled transitions, fire one according to their prob.
			Transition t = null;
			if (!enabledTransitions.isEmpty()) {
				
				t = nextTransition(enabledTransitions);
				
				//if transition its not invisible, increase activity counter
				if (!t.isInvisible()) 	
					i++;
				
			} else //there is nothing else enabled, so return.
				return traceState.getCodedTrace();

			//update markup
			PetrinetUtilsCombo.fireTransition(t, markings, petriNet);

		}

		return null;
	}
	
	private Transition nextTransition(Set<Transition> enabledTransitions) {

		Transition trans;
	
		ArrayList<TransObject> visi = new ArrayList<TransObject>();
		ArrayList<TransObject> invisi = new ArrayList<TransObject>();
		ArrayList<TransObject> candidates = new ArrayList<TransObject>();

		//Assign probabilities
		assignProbs(enabledTransitions, visi, invisi);
		candidates.addAll(visi);
		candidates.addAll(invisi);
				
		if(traceState.anyRepeatedActivity()) 
			trans = standardSelection(candidates);
		else 
			trans = prefixSelection(candidates);
			
		return trans;
	}
	
	private Transition prefixSelection(ArrayList <TransObject> candidates) {
		
		String codedTrieTrace = traceState.getCodedTrieTrace();

		for(TransObject c: candidates) {
			c.setPrefix(codedTrieTrace + c.getCodedLabel());
			addToTempPX(c);
			addToPX(c);
			
			if (removed)
				trie.add(c.getPrefix());
			
			if (!trie.startsWith(c.getPrefix())) 
				c.setProb(0.0);	
		}
		
		Collections.sort(candidates);
		TransObject r = choseRandom(candidates);
		String pref = r.getPrefix();
		removeFromTempPX(r);
		traceState.updateTraceState(r);
		
		if(!traceState.anyRepeatedActivity())
			removed = trie.remove(pref);
		

		return r.getTrans();
	}
	
	private Transition standardSelection(ArrayList <TransObject> candidates) {

		for(TransObject c: candidates) { 
			addToTempPX(c);
			addToPX(c);
			adjustProb(c);
		}
	
		Collections.sort(candidates);	
		TransObject r = choseRandom(candidates);
		removeFromTempPX(r);
		traceState.updateTraceState(r);
		
		return r.getTrans();
	}
	
	private void adjustProb(TransObject c) {
		
		int count = traceState.getTransCount(c.getCodedLabel());
		if(count>maxRep)
			c.setProb(0.0);				
	}
	
	public  String removeRepetitivePatterns(String trace) {
		
		String temp = trace;
		ArrayList<String> candidates = new ArrayList<String>(sortedPatterns);

		for(String c: candidates) {
		c = scapeSpecial.matcher(c).replaceAll("\\\\$0");
		String reg = "("+c+")"+"{2,}";
		temp = temp.replaceAll(reg, c);
		}
		
		String str = temp;
		if(candidates.stream().anyMatch(x -> str.contains(x+x)))
			temp = removeRepetitivePatterns(temp);
		
		return temp;
	}
	
	public  String removeRepetitivePatterns(String trace, ArrayList<String>sortedPatterns) {
		
		String temp = trace;
		ArrayList<String> candidates = new ArrayList<String>(sortedPatterns);

		for(String c: candidates) {
		c = scapeSpecial.matcher(c).replaceAll("\\\\$0");
		String reg = "("+c+")"+"{2,}";
		temp = temp.replaceAll(reg, c);
		}
		
		String str = temp;
		if(candidates.stream().anyMatch(x -> str.contains(x+x)))
			temp = removeRepetitivePatterns(temp);
		
		return temp;
	}
		
	private String compressTrace(String trace) {
		
		int i = 0;
		HashSet<String> patterns = new HashSet<String>();
		ArrayList<String> sortedPatterns = new ArrayList<String>();
		
		while(i<trace.length()-1) {
			int j = i;
			String s1 = trace.substring(i, j+1);
			while(j<trace.length() - s1.length()) {	
				String s2 = trace.substring(j+1, j+s1.length()+1);
				j++;
				if(s1.equals(s2)) { 	
					String patt = s1.length()>2?compressPattern(s1):s1;
					patterns.add(s1);
					this.patterns.add(patt);
				}else 
					s1 = trace.substring(i, j+1);
					
			}
		i++;
		}
		
		sortedPatterns.addAll(patterns);
		sortedPatterns.sort(Comparator.comparingInt(String::length));
		
		return removeRepetitivePatterns(trace,sortedPatterns);
		}
	
	private String compressPattern(String patt) {
		Matcher matcher = repeatingPattern.matcher(patt);
		return matcher.replaceAll("$1");
	}
	
	private void addToTempPX(TransObject c) {
		
		String codedTrace = traceState.getCodedTrace();
		HashSet<String> prefs =  new HashSet<String>();
		
		//if(codedTrace.length()<minTraceLength) {
			if(!c.getTrans().isInvisible())
				if(!tempRemoved.contains(codedTrace+c.getCodedLabel()))
					prefs.add(codedTrace+c.getCodedLabel());
			else
				for(String act: visSucc.get(c.getCodedLabel())) 
					if(!tempRemoved.contains(codedTrace+act))
						prefs.add(codedTrace+act);	
			
			if(!prefs.isEmpty())
				if(tempPX.size()>codedTrace.length()) 
					tempPX.get(codedTrace.length()).addAll(prefs);
				else 
					tempPX.add(codedTrace.length(), prefs);
		//}
	}
	
	private void removeFromTempPX(TransObject c) {

		String codedTrace = traceState.getCodedTrace();
		//if(codedTrace .length()<minTraceLength) {
			if(!c.getTrans().isInvisible()) {
				String visiPrefix = codedTrace + c.getCodedLabel();
				tempPX.get(visiPrefix.length()-1).remove(visiPrefix);
				tempRemoved.add(visiPrefix);
			//}
		}
		
	}
	
	private void addToPX(TransObject c){
		
		String codedTrace = traceState.getCodedTrace();
		HashSet<String> prefs =  new HashSet<String>();
		
//		if(codedTrace.length()<minTraceLength) {
			if(!c.getTrans().isInvisible())
				prefs.add(codedTrace+c.getCodedLabel());
			else
				for(String act: visSucc.get(c.getCodedLabel())) 
					prefs.add(codedTrace+act);	
			
			if(PX.size()>codedTrace.length()) 
				PX.get(codedTrace.length()).addAll(prefs);
			else 
				PX.add(codedTrace.length(), prefs);
//		}
	}
	
	public ArrayList<HashSet<String>> getPrefixes(){
		//int min = Math.min(minPrefixLength, minTraceLength);
		//return new ArrayList<HashSet<String>>(PX.subList(0, minPrefixLength));
		return PX;
	}
	
	private int minPrefixLength() {
		int i;
		for(i = 0; i<tempPX.size()-1;i++) 
			if(!tempPX.get(i).isEmpty())
				break;
		return i+1;
	}
	
	public int getminPrefixLength() {
		return minPrefixLength;
	}
	
	public HashSet<String> getPatterns(){
		return patterns;
	}
	
	public boolean anyReapetitvePatterns() {
		
		return !sortedPatterns.isEmpty();
	}

	public boolean isTrieEmpty() {
		if(trie.getNumberOfWords()==0)
		return true;
		else
		return false;
	}
	
	
}
