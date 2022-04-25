package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import org.processmining.logfiltering.algorithms.ProtoTypeSelectionAlgo;

import java.util.*;

public class PrefixConformanceChecker  extends ConformanceChecker {


    protected boolean lookForInfixMatches;
    protected Map<Integer, ProtoTypeSelectionAlgo.AlignObj> prevAlignments;
    protected Trie inverseTrie;
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
        inverseTrie = new Trie(trie.getMaxChildren());
        List<String> traceAsList ;
        for (int i : trie.getRoot().getLinkedTraces())
        {
            String trace = trie.getTrace(i);
            traceAsList = new ArrayList<>();
            for (int j = trace.length()-1; j>=0; j--)
            {
                traceAsList.add(Character.toString(trace.charAt(j)));
            }
            inverseTrie.addTrace(traceAsList,i);

        }
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
            if(match.getLevel() == 0)
            {
                // root node
                match = modelTrie.getRoot();
                boolean lastNodeReached = false;
                while (!lastNodeReached)
                {
                    TrieNode shortest = match.getChildOnShortestPathToTheEnd();
                    if (shortest.isEndOfTrace())
                    {
                        lastNodeReached = true;
                    }
                    match = shortest;

                }
                int shortestTraceIndex = match.getLinkedTraces().get(0);
                String shortestTraceAsString = modelTrie.getTrace(shortestTraceIndex);
                if (shortestTraceAsString.length() > match.getLevel())
                {
                    shortestTraceAsString = shortestTraceAsString.substring(0, match.getLevel());
                }

                ProtoTypeSelectionAlgo.AlignObj obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(traceAsString, shortestTraceAsString);
                minCost = obj.cost;
                System.out.println("Empty trace, returning minimum trie length");
            }
            else
            {
                ProtoTypeSelectionAlgo.AlignObj obj = prevAlignments.get(match.getLinkedTraces().get(0)); //we only have one linked trace here.
                linkedTraces.addAll(match.getLinkedTraces());
                minCost = obj.cost;
                bestTrace = modelTrie.getTrace(match.getLinkedTraces().get(0));
                System.out.println("An identical trace has been seen before, just getting the previous result");
            }
        }

        else {

            // Let start by walking through the log
            match = matchPrefix(trace, traceSuffix, this.modelTrie);

            // try a little trick to run the state-search based approach


            linkedTraces.addAll(match.getLinkedTraces());

            // Now let's walk through the model
            //match = trie.match(trace,trie.getRoot());
//            match=null;
            if (lookForInfixMatches )
            {

                List<TrieNode> toCheck = new LinkedList<>();
                toCheck.addAll(modelTrie.getRoot().getAllChildren());
                TrieNode next;
                while (toCheck.size()> 0 )
                {
                    next = toCheck.remove(0);
                    match = modelTrie.match(trace, next);
                    if (match==null)
                    {
                        toCheck.addAll(next.getAllChildren());
                    }
                    else
                        linkedTraces.addAll(match.getLinkedTraces());

                }

            }

//            List<String> revTrace = reverseTrace(trace);
//            match = matchPrefix(revTrace,new LinkedList<>(revTrace),this.inverseTrie);
//            linkedTraces.addAll(match.getLinkedTraces());

//        System.out.println("Matched node level "+match.getLevel());
            ProtoTypeSelectionAlgo.AlignObj obj=null;
            ProtoTypeSelectionAlgo.AlignObj bestObj = null;
            int traceIndex=0;
            for (Integer i : linkedTraces) {


//                if (match.getLevel() == trace.size()) // this is an exact match, we do not need to compute any distance
//                {
//                    minCost = 0;
//                    bestAlignment = traceAsString;
//                    bestTrace = traceAsString;
//                    break;
//                } else if (match.getLevel() > 0 && !lookForInfixMatches)
//                    obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(traceAsString.substring(match.getLevel() - 1), trie.getTrace(i).substring(match.getLevel()));
//
//                else
                    obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(traceAsString, modelTrie.getTrace(i));
                if (obj.cost < minCost) {
                    minCost = obj.cost;
                    bestAlignment = obj.Alignment;
                    bestTrace = modelTrie.getTrace(i);
                    traceIndex = i;
                    bestObj = obj;
                    if (obj.cost==0)
                        break;
                }
            }
            if (bestObj != null) {
                prevAlignments.put(traceIndex, bestObj);
                inspectedLogTraces.addTrace(trace, traceIndex);
            }

        }
//        System.out.println("Total proxy traces "+this.trie.getRoot().getLinkedTraces().size());
//        System.out.println("Total candidate traces to inspect "+linkedTraces.size());
        System.out.println("Alignment cost "+minCost);
        if(minCost==6){
            System.out.println("Stop");
        }
//       System.out.println(bestAlignment);
//        System.out.println("Log trace "+traceAsString);
//        System.out.println("Aligned trace "+bestTrace);



        return null;

    }

    private TrieNode matchTrieSubtree(List<String> trace, TrieNode startFromHere)
    {
        return modelTrie.match(trace,startFromHere);
    }

    private TrieNode matchPrefix(List<String> trace, List<String> traceSuffix, Trie t) {
        TrieNode prevMatch=t.getRoot();
        TrieNode match=null;
        match = t.match(trace);
//        if (match == null)
//            match = t.getRoot();
        if (match != null && match.isEndOfTrace())
            return match;

        if (lookForInfixMatches) {

            while (traceSuffix.size() > 0 && match == null) {
                match = t.match(traceSuffix);
                traceSuffix.remove(0);
            }

        }


        if (match == null)// we could not find any partial match, let's fall back to scanning all the traces
        {

            match = t.getRoot();
//                System.out.println("We could not fine any common prefix. Let's try the search based approach");
//                Alignment alg = super.check(trace);
//                System.out.println(alg.toString());
//                return alg;
        }
        return match;
    }

    @Override
    protected List<State> handleModelMoves(List<String> traceSuffix, State state, State candidateState) {
        Alignment alg;
        List<State> result;

        // Let us make the model move

        List<TrieNode> nodes = state.getNode().getAllChildren();
        result = new ArrayList<>(nodes.size());
//                if (nodes.size() > 1)
//                    System.out.println("We have multiple children in the trie "+nodes.size());
        Move modelMove;
//        State minModelMoveState = null ;
        for (TrieNode nd : nodes)
        {


            modelMove = new Move(">>", nd.getContent(),1);//modelMoveCost);
            alg = state.getAlignment();
            alg.appendMove(modelMove);



            // Cost = worst case - what has been processed in both the log and the model
            int cost = alg.getTotalCost();

            cost += (nd.isEndOfTrace()? 0: nd.getMinPathLengthToEnd()) +traceSuffix.size();
//            cost += (nd.isEndOfTrace()? 0+traceSuffix.size(): nd.getMinPathLengthToEnd()) +traceSuffix.size();
//            cost += nd.isEndOfTrace()? 0+traceSuffix.size(): Math.min(Math.abs(nd.getMinPathLengthToEnd() - traceSuffix.size()), Math.abs(nd.getMaxPathLengthToEnd()-traceSuffix.size())) ;
            if (traceSuffix.size() > 0 && nd.getChild(traceSuffix.get(0))!= null) // we can find a next sync move this path
                cost-=1;


            State modelMoveState = new State(alg, traceSuffix,nd, cost);

            result.add(modelMoveState);
        }
        return  result;
    }

    @Override
    protected State handleLogMove(List<String> traceSuffix, State state, String event) {
        Alignment alg;
        State logMoveState=null;
        if (event != null) {

            Move logMove = new Move(event, ">>",1);// logMoveCost);
            alg = state.getAlignment();
            alg.appendMove(logMove);


            int cost = alg.getTotalCost();


             cost += (state.getNode().isEndOfTrace()? 0: state.getNode().getMinPathLengthToEnd()) + traceSuffix.size() ;
//            cost += (state.getNode().isEndOfTrace()? 0+traceSuffix.size(): state.getNode().getMinPathLengthToEnd()) +traceSuffix.size();
//            cost += state.getNode().isEndOfTrace()? 0+traceSuffix.size(): Math.min(Math.abs(state.getNode().getMinPathLengthToEnd() - traceSuffix.size()), Math.abs(state.getNode().getMaxPathLengthToEnd()-traceSuffix.size())) ;
//            cost += );
            for (TrieNode nd: state.getNode().getAllChildren()) {
                if (nd.getChild(event) != null)// If we make a model move, we can reach a sync move. So, log move is not the best move
                {
                    cost += 1;
                    break;
                }
            }

            logMoveState = new State(alg, traceSuffix, state.getNode(), cost);


            // let's put the event back in the trace postfix to see how it check for model moves
            traceSuffix.add(0, event);
            return logMoveState;
        }
        else
            return null;
    }
}
