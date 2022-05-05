package ee.ut.cs.dsg.confcheck;


import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Configuration;
import ee.ut.cs.dsg.confcheck.util.Utils;

import java.util.*;

/**
 * This class adds the logic of benefiting from previously computed alignments of earlier log traces.
 * For this, another trie is constructed where each node matches an event in a trace and points to the respective alignment object.
 * Rather than starting the alignment from scratch, we can search the traces trie and exit at the latest common prefix
 */
public class StatefulRandomConformanceChecker extends RandomConformanceChecker {
   // private final Trie tracesTrie;

    public StatefulRandomConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue, int maxTrials) {
        super(trie, logCost, modelCost, maxStatesInQueue, maxTrials);
        searchSpace = new HashMap<>();

    }

    private int trialsPerEvent;
    private final Map<State, PriorityQueue<State>> searchSpace;
    private final boolean reuseSearchSpace =false;

    public void setTrialsPerEvent(int tpe)
    {
        trialsPerEvent = tpe;
    }
    private State getStateFromTracesTrie(List<String> trace)
    {
        if (trace.size() == 0)
            return null;
        TrieNode node = inspectedLogTraces.match(trace);
        if (node!=null) {
            List<String> tracePostfix = trace.subList(node.getLevel(),trace.size());
            List<String> tracePrefix = trace.subList(0,node.getLevel());
            // fill the queue based on the state
            if (reuseSearchSpace) {
                PriorityQueue<State> previousSearchSpace = searchSpace.get(node.getAlignmentState());
                if (previousSearchSpace != null) {
                    previousSearchSpace.stream().filter(ps -> isAValidState(ps, trace)).forEach(vs -> nextChecks.add(vs));
//                    previousSearchSpace.stream().filter(ps -> isAValidState(ps, trace)).forEach(vs -> nextChecks.add(
//                            new State(trace.subList(vs.getNode().getLevel(),trace.size()),vs.getNode(),vs.getCostSoFar())));
                    if (verbose)
                        System.out.printf("Loading previous search space to resume from. Kept %d states from original %d states%n", nextChecks.size(), previousSearchSpace.size());
                }
            }
            return new State(node.getAlignmentState().getAlignment(),tracePostfix,node.getAlignmentState().getNode(),node.getAlignmentState().getCostSoFar());
        }

        return null;
    }

    private boolean isAValidState(State s, List<String> trace)
    {
        String traceString = trace.toString().replace("[","").replace("]","").replace(",","").replace(" ","");
        //return traceString.indexOf(s.getAlignment().logProjection()) == 0;
        return traceString.equals(s.getAlignment().logProjection());
    }
    public Alignment check(List<String> trace)
    {

        nextChecks.clear();
        states.clear();
        cntr=1;
        traceSize = trace.size();
        maxModelTraceSize = modelTrie.getRoot().getMaxPathLengthToEnd();
        TrieNode node;


        List<String> traceSuffix;
        State state;
        state = getStateFromTracesTrie(trace);
        if (state == null) {
            state = new State(new Alignment(), trace, modelTrie.getRoot(), 0);

        }

        nextChecks.add(state);

        State candidateState = null;
        String event;
        numTrials = 1;
        //        cleanseFrequency = Math.max(maxTrials/10, 100000);
     //   exploitVersusExploreFrequency = 1001 ;
        Utils.resetPrimeIndex();
        while(nextChecks.size() >0  && numTrials < maxTrials)
        {

//            adaptiveExploreExploit();
            // as we are using old search spaces, there might be some invalid states from other prefixes.
            if (candidateState!= null && candidateState.getCostSoFar() == 0)
                break;
            state = pickRandom(candidateState);
            Alignment alg;
//            while (stateRetrieved && !isAValidState(state,trace))
//            {
//                state = pickRandom(candidateState);
//            }
            if (state==null)
                continue;
            numTrials++;

            if (numTrials % 100000 == 0 && verbose) {
                System.out.println("Trials so far " + numTrials);
                System.out.println("Queue size "+nextChecks.size());
            }

            traceSuffix = state.getTracePostfix();

            // we have to check what is remaining
            if (traceSuffix.size() == 0 && state.getNode().isEndOfTrace())// We're done
            {
                //return state.getAlignment();

                if (candidateState == null)
                {

                    if(verbose)
                        System.out.println("1-Better alignment reached with cost "+state.getAlignment().getTotalCost());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
//                    cntr=0;
                }
                else if (state.getAlignment().getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());

                    if (verbose)
                        System.out.println("2-Better alignment reached with cost "+state.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());

//                    cntr=0;
                }
                candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost(), state);
                newCandidateStateFoundSinceLastEpoc=true;
                // candidates.add(candidateState);
//                System.out.println("Remaining alignments to check " +toCheck.size());
//                System.out.println("Best alignment so far " +candidateState.getCostSoFar());
//                return candidateState.getAlignment();
                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;
            }
            else if (traceSuffix.size() ==0)
            {
                // we still have model moves to do
                // we should pick the shortest path to an end node
//                System.out.println("Log trace ended! We can only follow the shortest path of the model behavior to the end");
                alg = state.getAlignment();

                node = state.getNode();
                node = node.getChildOnShortestPathToTheEnd();
                while (node != null)
                {
                    Move modelMove = new Move(">>", node.getContent(),1);
                    alg.appendMove(modelMove);
                    if (node.isEndOfTrace())
                        break;
                    node = node.getChildOnShortestPathToTheEnd();
                }
//                System.out.println("Alignment found costs "+alg.getTotalCost());
                if (candidateState == null)
                {
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost(), state);
                    newCandidateStateFoundSinceLastEpoc=true;
                    if(verbose)
                        System.out.println("3-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost(), state);
                    newCandidateStateFoundSinceLastEpoc=true;
                    if (verbose)
                        System.out.println("4-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }

                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;

            }
            else if (state.getNode().isEndOfTrace() & !state.getNode().hasChildren()) // and no more children
            {
//                System.out.println("Model trace ended! We can only follow the remaining log trace to the end");
                alg = state.getAlignment();
                for (String ev: state.getTracePostfix())
                {
                    Move logMove = new Move(ev, ">>",1);
                    alg.appendMove(logMove);
                }
                if (candidateState == null)
                {
                    candidateState = new State(alg, new ArrayList<>(),null, alg.getTotalCost(),state);
                    newCandidateStateFoundSinceLastEpoc=true;
                    if(verbose)
                        System.out.println("5-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
//                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, new ArrayList<>(),null, alg.getTotalCost(), state);
                    newCandidateStateFoundSinceLastEpoc=true;
                    if (verbose)
                        System.out.println("6-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }
                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;
            }
            else {
                event = traceSuffix.remove(0);
                node = state.getNode().getChild(event);
            }

            List<State> newStates = new ArrayList<>();
            if (node != null) // we found a match => synchronous    move
            {
                alg = state.getAlignment();
                State syncState;
//                do {
//
                    if(!onMatchFollowPrefixOnly)
                    {

                        List<String> trSuffix = new LinkedList<>(traceSuffix);
                        State nonSyncState = new State(new Alignment(alg), trSuffix, node.getParent(),0, state);
                        addStateToTheQueue(handleLogMove(trSuffix, nonSyncState, event), candidateState);
                  //      trSuffix.add(0, event);
                        nonSyncState = new State(new Alignment(alg), trSuffix, node.getParent(),0, state);
                        for (State s: handleModelMoves(trSuffix, nonSyncState, candidateState))
                            addStateToTheQueue(s, candidateState);
                    }

                    Move syncMove = new Move(event,event,0);

                    alg.appendMove(syncMove); // <== something wrong in this object alg it is referenced by other states
//                    prev = node;
//                    if (traceSuffix.size() > 0)
//                    {
//                        event = traceSuffix.remove(0);
//
//                        node = node.getChild(event);
//                    }
//                    else
//                    {
//                        event = null;
//                        node = null;
//                    }
//                }
//                while(node != null);
                // put the event back that caused non sync move
//                if (event != null)
//                    traceSuffix.add(0,event);


                int cost = 0;

                syncState = new State(alg,traceSuffix, node,cost, state);
                addStateToTheQueue(syncState, candidateState);



            }
            // On 27th of May 2021. we need to give the option to a log move as well as a model move
            else // there is no match, we have to make the model move and the log move
            {
                // let make the log move if there are still more moves


                newStates.add(handleLogMove(traceSuffix, state, event));
                newStates.addAll(handleModelMoves(traceSuffix, state, candidateState));
            }

            //Now randomly add those states to the queue so that if they have the same cost we can pick them differently
            for (State s :newStates)
            {
                if (s !=null)
                    addStateToTheQueue(s, candidateState);
            }

        }

        if(verbose)
            System.out.printf("Queue Size %d and num trials %d%n", nextChecks.size(),numTrials);
        if (candidateState!=null)
            updateTracesTrie(candidateState);
        return candidateState != null? candidateState.getAlignment():null;
        //return alg;
    }

    private void adaptiveExploreExploit() {
        if (numTrials% 50000 == 0 )
        {
            if (!newCandidateStateFoundSinceLastEpoc) // we have to exploit more
            {
                if (exploitVersusExploreFrequency > 29) {
                    exploitVersusExploreFrequency = Utils.getPreviousPrimeFromList();
                    if (verbose)
                        System.out.printf("Exploit frequency has been increased to %d%n", exploitVersusExploreFrequency);
                }
            }
            else
            {
                newCandidateStateFoundSinceLastEpoc = false;
                exploitVersusExploreFrequency = Utils.getNextPrimeFromList();
            }
        }
    }

    @Override
    protected State handleLogMove(List<String> traceSuffix, State state, String event) {
        Alignment alg;
        State logMoveState;
        if (event != null) {
            Move logMove = new Move(event, ">>",1);// logMoveCost);
            alg = state.getAlignment();
            alg.appendMove(logMove);
            int cost = 0;
            cost += (state.getNode().isEndOfTrace()? 0: state.getNode().getMinPathLengthToEnd()) + traceSuffix.size() ;
            for (TrieNode nd: state.getNode().getAllChildren()) {
                if (nd.getChild(event) != null)// If we make a model move, we can reach a sync move. So, log move is not the best move
                {
                    cost += 1;
                    break;
                }
            }
            logMoveState = new State(alg, traceSuffix, state.getNode(), costFunction.computeCost(state,traceSuffix,event, Configuration.MoveType.LOG_MOVE, this), state);
            traceSuffix.add(0, event);
            return logMoveState;
        }
        else
            return null;
    }

    protected List<State> handleModelMoves(List<String> traceSuffix, State state, State candidateState) {
        Alignment alg;
        List<State> result;
        // Let us make the model move
        List<TrieNode> nodes = state.getNode().getAllChildren();
        result = new ArrayList<>(nodes.size());
        Move modelMove;
        for (TrieNode nd : nodes)
        {
            modelMove = new Move(">>", nd.getContent(),1);//modelMoveCost);
            alg = state.getAlignment();
            alg.appendMove(modelMove);
            State dummyState = new State(alg, traceSuffix,nd,-1);
            // Cost = worst case - what has been processed in both the log and the model
            int cost = 0;
            cost += (nd.isEndOfTrace()? 0: nd.getMinPathLengthToEnd()) +traceSuffix.size();
            if (traceSuffix.size() > 0 && nd.getChild(traceSuffix.get(0))!= null) // we can find a next sync move this path
                cost-=1;
            State modelMoveState = new State(alg, traceSuffix,nd, costFunction.computeCost(dummyState,traceSuffix,null, Configuration.MoveType.MODEL_MOVE, this), state);
            result.add(modelMoveState);
        }
        return  result;
    }

    private void updateTracesTrie(final State candidateState)
    {
        int stateSize = nextChecks.size();



        Stack<State> statesBack = new Stack<State>();
        State currentState = candidateState.getParentState();
        int alignmentSize = currentState.getAlignment().getMoves().size();

        while(currentState !=null)
        {
            if (reuseSearchSpace && stateSize > 0)
            {
                PriorityQueue<State> currentSearchSpace = new PriorityQueue<>();
                currentSearchSpace.addAll(nextChecks);
//                PriorityQueue<State> currentSearchSpace = nextChecks;
                //   currentSearchSpace.addAll(prevStates);
                currentSearchSpace.add(currentState);
                searchSpace.put(currentState,currentSearchSpace);

            }

            statesBack.push(currentState);
            currentState = currentState.getParentState();
        }
        // now keep popping from the stack and adding to the traces trie
        TrieNode currentNode = inspectedLogTraces.getRoot();

        int i = 0;
        while(!statesBack.isEmpty())
        {

            currentState = statesBack.pop();
            if (currentState.getAlignment().getMoves().size()==0)// this has not found any matches before as this a trace with a unique prefix so far.
            {
    //            i = statesBack.size();
                continue;
            }
            // If this is a sync move, we might have many entries in the alignment with cost 0

            List<Move> moves = currentState.getAlignment().getMoves();
            if (i >= moves.size())
                continue;

            do {
                String move = moves.get(i).getLogMove();
                if (move.equalsIgnoreCase(">>"))// Not interested in non-log moves
                {
                    i++;
                    continue;
                }

                TrieNode child = currentNode.getChild(move);
                if (child == null) {
                    child = new TrieNode(move, inspectedLogTraces.getMaxChildren(), -1, -1, false, currentNode);
                    child = currentNode.addChild(child);
                    child.setAlignmentState(currentState);
                }
//                else if (child.getAlignmentState().getCostSoFar() > currentState.getCostSoFar())
//                {
//                    child.setAlignmentState(currentState);
//                }
                currentNode = child;
                i++;
            }
            while(i < alignmentSize && i < moves.size());




        }
    }

}
