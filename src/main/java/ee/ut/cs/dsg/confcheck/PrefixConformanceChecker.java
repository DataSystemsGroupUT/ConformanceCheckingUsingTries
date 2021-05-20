package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import org.processmining.logfiltering.algorithms.ProtoTypeSelectionAlgo;

import java.util.*;

public class PrefixConformanceChecker  extends ConformanceChecker {

    protected Trie inspectedLogTraces;
    protected boolean lookForInfixMatches;
    protected Map<Integer, ProtoTypeSelectionAlgo.AlignObj> prevAlignments;
    public PrefixConformanceChecker(Trie trie, int logCost, int modelCost) {
        super(trie, logCost, modelCost);
        inspectedLogTraces = new Trie(trie.getMaxChildren());
        prevAlignments = new HashMap<>();
    }

    public PrefixConformanceChecker(Trie trie, int logCost, int modelCost, boolean lookForInfixMatches) {
        super(trie, logCost, modelCost);
        inspectedLogTraces = new Trie(trie.getMaxChildren());
        this.lookForInfixMatches = lookForInfixMatches;
        prevAlignments = new HashMap<>();
//        inverseTrie = new Trie(trie.getMaxChildren());
//        List<String> traceAsList ;
//        for (int i : trie.getRoot().getLinkedTraces())
//        {
//            String trace = trie.getTrace(i);
//            traceAsList = new ArrayList<>();
//            for (int j = trace.length()-1; j>=0; j--)
//            {
//                traceAsList.add(Character.toString(trace.charAt(j)));
//            }
//            inverseTrie.addTrace(traceAsList,i);
//
//        }
    }

    private List<String> reverseTrace(List<String> trace)
    {
        List<String> reveresedTrace = new ArrayList<>(trace.size());
        for (int i = trace.size()-1; i>=0;i --)
            reveresedTrace.add(trace.get(i));
        return reveresedTrace;
    }
    public Alignment check(List<String> trace)
    {
        // Check if we have seen a similar trace before

        List<String> traceSuffix = new LinkedList<>(trace);
        TrieNode match=null;
        String bestAlignment="";
        double minCost = Double.MAX_VALUE;
        String bestTrace="";
        String traceAsString = String.valueOf(trace.stream().reduce((s, s2) -> s + s2));
        traceAsString = traceAsString.substring(9, traceAsString.length() - 1);
        Set<Integer> linkedTraces = new HashSet<>();
        match = inspectedLogTraces.match(trace);
        if(match != null && match.getLevel() == trace.size())// identical match
        {
            ProtoTypeSelectionAlgo.AlignObj obj = prevAlignments.get(match.getLinkedTraces().get(0)); //we only have one linked trace here.
            linkedTraces.addAll(match.getLinkedTraces());
            minCost = obj.cost;
            bestTrace = trie.getTrace(match.getLinkedTraces().get(0));
            System.out.println("An identical trace has been seen before, just getting the previous result");
        }
        else {
            if (lookForInfixMatches) {
                while (traceSuffix.size() > 0 && match == null) {
                    match = this.trie.match(traceSuffix);
                    traceSuffix.remove(0);
                }
            } else
                match = this.trie.match(trace);


            if (match == null)// we could not find any partial match, let's fall back to scanning all the traces
                match = trie.getRoot();



            linkedTraces.addAll(match.getLinkedTraces());


//        System.out.println("Matched node level "+match.getLevel());
            ProtoTypeSelectionAlgo.AlignObj obj=null;
            ProtoTypeSelectionAlgo.AlignObj bestObj = null;
            int traceIndex=0;
            for (Integer i : linkedTraces) {


                if (match.getLevel() == trace.size()) // this is an exact match, wwe do not need to compute any distance
                {
                    minCost = 0;
                    bestAlignment = traceAsString;
                    bestTrace = traceAsString;
                    break;
                } else if (match.getLevel() > 0)
                    obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(traceAsString.substring(match.getLevel() - 1), trie.getTrace(i).substring(match.getLevel()));

                else
                    obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(traceAsString, trie.getTrace(i));
                if (obj.cost < minCost) {
                    minCost = obj.cost;
                    bestAlignment = obj.Alignment;
                    bestTrace = trie.getTrace(i);
                    traceIndex = i;
                    bestObj = obj;
                }
            }
            if (bestObj != null) {
                prevAlignments.put(traceIndex, bestObj);
                inspectedLogTraces.addTrace(trace, traceIndex);
            }

        }
        System.out.println("Total proxy traces "+this.trie.getRoot().getLinkedTraces().size());
        System.out.println("Total candidate traces to inspect "+linkedTraces.size());
        System.out.println("Alignment cost "+minCost);
//       System.out.println(bestAlignment);
        System.out.println("Log trace "+traceAsString);
        System.out.println("Aligned trace "+bestTrace);



        return null;

    }
}
