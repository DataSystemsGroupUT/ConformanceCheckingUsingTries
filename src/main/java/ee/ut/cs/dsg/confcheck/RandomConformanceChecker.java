package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.cost.*;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Configuration;

import java.util.*;

public class RandomConformanceChecker extends ConformanceChecker{



    protected int exploitVersusExploreFrequency = 100;
    protected int numEpochs;
    protected boolean onMatchFollowPrefixOnly = false;
    protected boolean verbose = false;
    protected int numTrials=0;
    //protected int execCounter=0;
    protected boolean newCandidateStateFoundSinceLastEpoc=false;
    protected int whichDirection=1; // 1 means the upper half of the queue, 0 means the lower half of the queue
    protected final CostFunction costFunction;

    // Streaming variables

    protected boolean replayWithLogMoves = true; // if set to false the performance is faster but result is less precise
    protected int minLookAheadLimit=0;


    public RandomConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue, int maxTrials, CostFunction costFunction)
    {
        super(trie, logCost, modelCost, maxStatesInQueue);
        rnd = new Random(19);
        numEpochs = maxTrials/10000;
        this.maxTrials = maxTrials;
        inspectedLogTraces = new Trie(trie.getMaxChildren());
        this.costFunction = costFunction;
    }
    public RandomConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue) {
        this(trie,logCost,modelCost,maxStatesInQueue, 10000);

    }

    public RandomConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue, int maxTrials) {
        this(trie, logCost, modelCost, maxStatesInQueue, maxTrials, new DualProgressiveCostFunction());
    }
    protected State pickRandom(State candidateState)
    {

//        toCheck.sort(new Comparator<State>() {
//            @Override
//            public int compare(State o1, State o2) {
//                return o1.compareTo(o2);
//            }
//        });

        //= (State[]) nextChecks.toArray();
//        if (numTrials % cleanseFrequency == 0)
//            successiveHalving();
        int index;
        State s;
        if (cntr % exploitVersusExploreFrequency== 0) {

//            long start = System.currentTimeMillis();
            State[] elements =  new State[nextChecks.size()];
            nextChecks.toArray(elements);






            int upperBound = nextChecks.size();
            int lowerBound = Math.max(upperBound-  (nextChecks.size()/2)-1,1);
            index = rnd.nextInt( lowerBound);

            s = elements[(lowerBound*whichDirection)+index >= upperBound? 0:(lowerBound*whichDirection)+index];
//            s = states.remove((lowerBound*whichDirection)+index >= upperBound? 0:(lowerBound*whichDirection)+index);
            whichDirection =  whichDirection == 0 ? 1:0;

            nextChecks.remove(s);

            cntr = 0;
        }
        else {
            s = nextChecks.poll();
            states.remove(s);
        }


        return s;
    }
    protected void successiveHalving()
    {
//        if (nextChecks.size() < 100000)
//            return  ;
        List<State> result = new ArrayList<>(nextChecks.size()/2);
        State[] elements = new State[nextChecks.size()];
        nextChecks.toArray(elements);

        Arrays.sort(elements);
        int quantile = nextChecks.size()/4;
        nextChecks.clear();
        for(int i = 0; i < quantile*4; i+=2)
        {
            nextChecks.add(elements[i]);
        }
//        for (int i = quantile*2; i < quantile*3; i++)
//        {
//            nextChecks.add(elements[i]);
//        }

    }
    public Alignment prefix_check(List<String> trace, String caseId)
    {
        // check tracesInBuffer if caseId exists
        String event;
        State state;
        TrieNode node;
        TrieNode prev;
        Alignment alg;
        int cost;
        boolean followWithSyncMove;
        boolean precedeWithLogMove;
        /*if (tracesInBuffer == null)
        {
            alg = new Alignment();
            state = new State(alg,trace, modelTrie.getRoot(),0);
            tracesInBuffer.put(caseId, state);
        }
        else */
        if (tracesInBuffer.containsKey(caseId))
        {
            // case exists, fetch last state
            state = tracesInBuffer.get(caseId);
        }
        else
        {

            // create dummy state, create key in tracesInBuffer
            alg = new Alignment();
            state = new State(alg,trace, modelTrie.getRoot(),0);
            tracesInBuffer.put(caseId, state);
        }

        while (trace.size()>0)
        {
            event = trace.remove(0);
            node = state.getNode().getChild(event);
            if (node != null) // we found a match => synchronous    move
            {
                // EXAMPLE trie: abcde. trace seen so far: ab. We now see event c --> sync move
                alg = state.getAlignment();
                State syncState;
                Move syncMove = new Move(event, event, 0);
                alg.appendMove(syncMove);
                prev = node;
                cost = alg.getTotalCost();
                state = new State(alg, trace, prev, cost);
            }
            else
            {
                // if model move would trigger a sync move, go for model move
                followWithSyncMove = false;
                precedeWithLogMove = false;
                prev = null;
                List<TrieNode> nodes = state.getNode().getAllChildren();
                for(TrieNode nd : nodes)
                {
                    node = nd.getChild(event);
                    if (node != null)
                    {
                        // EXAMPLE trace so far ab, we now see d. We mae a model move + sync move
                        prev = nd;
                        followWithSyncMove = true; // we should make a model move and follow it with sync move
                        break;
                    }
                }
                if (node == null && trace.size()>0) // model+sync move does not work. If there are events left in trace, try for a
                {
                    String nextEvent = trace.get(0);
                    node = state.getNode().getChild(nextEvent);
                    if (node != null)
                    {
                        // EXAMPLE trace so far ab, we now see x and c. We make a log move + sync move
                        precedeWithLogMove = true; // we should make a log move and then we have a match from the trace suffix
                    }
                }


                if (node != null)
                {
                    //model move
                    if (followWithSyncMove)
                    {
                        Move modelMove = new Move(">>", prev.getContent(), 1);
                        Move syncMove = new Move(event, event, 0);
                        alg = state.getAlignment();
                        alg.appendMove(modelMove);
                        alg.appendMove(syncMove);
                        cost = alg.getTotalCost()+1;
                        state = new State(alg, trace,node,cost);
                    }
                    else if (precedeWithLogMove)
                    {
                        Move logMove = new Move(event, ">>", 1);
                        Move syncMove = new Move(node.getContent(), node.getContent(), 0);
                        alg = state.getAlignment();
                        alg.appendMove(logMove);
                        alg.appendMove(syncMove);
                        cost = alg.getTotalCost()+1;
                        trace.remove(0); // Remove the "nextEvent"
                        state = new State(alg,trace,node,cost);

                    }
                    else
                    {
                        Move modelMove = new Move(">>", node.getContent(),1);
                        alg = state.getAlignment();
                        alg.appendMove(modelMove);

                        cost = alg.getTotalCost()+1;
                        state = new State(alg, trace,node,cost);
                    }
                }
                else
                {
                    // log move
                    Move logMove = new Move(event, ">>",1);
                    alg = state.getAlignment();
                    alg.appendMove(logMove);
                    cost = alg.getTotalCost();
                    state = new State(alg, trace,state.getNode(),cost);
                }

            }

        }

        // put new state to hashmap
        tracesInBuffer.put(caseId, state);


        return state.getAlignment();
    }

    public Alignment check(List<String> trace)
    {

        nextChecks.clear();
        states.clear();
        cntr=1;
        traceSize = trace.size();
        maxModelTraceSize = modelTrie.getRoot().getMaxPathLengthToEnd();
        TrieNode node;
        Alignment alg = new Alignment();
        // Speed up by looking up identical log traces first before computing the alignment
//        node = inspectedLogTraces.match(trace);
//        if (node !=null && node.getLevel() == trace.size())
//        {
//            int cost = node.getLinkedTraces().get(0);
//            alg = new Alignment(cost);
//            System.out.println("An identical trace has been seen before, just getting the previous result");
//            return alg;
//        }

        List<String> traceSuffix;
        State state = new State(alg,trace, modelTrie.getRoot(),0);
        nextChecks.add(state);

        State candidateState = null;
        String event;
        numTrials = 1;
//        cleanseFrequency = Math.max(maxTrials/10, 100000);
        cleanseFrequency = maxTrials/20;
        while(nextChecks.size() >0  && numTrials < maxTrials)
        {
            if (candidateState!= null && candidateState.getCostSoFar() == 0)
                break;
            state = pickRandom(candidateState);
            if (state==null)
                continue;
            numTrials++;

            if (numTrials % 100000 == 0 && verbose) {
                System.out.println("Trials so far " + numTrials);
                System.out.println("Queue size "+nextChecks.size());
            }

            event = null;
            traceSuffix = state.getTracePostfix();

            // we have to check what is remaining
            if (traceSuffix.size() == 0 && state.getNode().isEndOfTrace())// We're done
            {
                //return state.getAlignment();

                if (candidateState == null)
                {
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
                    if(verbose)
                        System.out.println("1-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
//                    cntr=0;
                }
                else if (state.getAlignment().getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
                    if (verbose)
                        System.out.println("2-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());

//                    cntr=0;
                }

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
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
                    if(verbose)
                        System.out.println("3-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
                    if (verbose)
                        System.out.println("4-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }
                else
                {
//                    System.out.println("Current alignment is more expensive "+alg.getTotalCost());
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
                    candidateState = new State(alg, new ArrayList<>(),null, alg.getTotalCost());
                    if(verbose)
                        System.out.println("5-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
//                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, new ArrayList<>(),null, alg.getTotalCost());
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
                TrieNode prev=node;
                State syncState;
                do {
//
                    if(!onMatchFollowPrefixOnly)
                    {

                        List<String> trSuffix = new LinkedList<>();
                        trSuffix.addAll(traceSuffix);
                        State nonSyncState = new State(new Alignment(alg), trSuffix, node.getParent(),0);
                        addStateToTheQueue(handleLogMove(trSuffix, nonSyncState, event), candidateState);
                        nonSyncState = new State(new Alignment(alg), trSuffix, node.getParent(),0, state);
                        for (State s: handleModelMoves(trSuffix, nonSyncState, candidateState))
                            addStateToTheQueue(s, candidateState);
                    }

                    Move syncMove = new Move(event,event,0);

                    alg.appendMove(syncMove);
                    prev = node;
                    if (traceSuffix.size() > 0)
                    {
                        event = traceSuffix.remove(0);

                        node = node.getChild(event);

                    }
                    else
                    {
                        event = null;
                        node = null;
                    }
                }
                while(node != null);
                // put the event back that caused non sync move
                if (event != null)
                    traceSuffix.add(0,event);


                int cost = 0;

                syncState = new State(alg,traceSuffix,prev,cost);
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
//            int size = newStates.size();
//            for (int i = size; i >0;i--)
//            {
//                State s = newStates.get(rnd.nextInt(i));
//                if (s != null)
//                    addStateToTheQueue(s, candidateState);
//            }
        }
//        if (candidateState != null)
//            inspectedLogTraces.addTrace(trace, candidateState.getAlignment().getTotalCost());
        if(verbose)
            System.out.println(String.format("Queue Size %d and num trials %d", nextChecks.size(),numTrials));
       // System.out.println(execCounter);
        return candidateState != null? candidateState.getAlignment():null;
        //return alg;
    }

/*
    public Alignment check2(List<String> trace, boolean prefixBased, String caseId)
    {

        nextChecks.clear();
        states.clear();
        cntr=1;
        traceSize = trace.size();
        State state;
        State previousState;
        StatesBuffer caseStatesInBuffer;
        Alignment alg;
        TrieNode node;
        List<String> traceSuffix;


        if (statesInBuffer.containsKey(caseId))
        {
            // case exists, fetch last state
            caseStatesInBuffer = statesInBuffer.get(caseId);
            previousState = caseStatesInBuffer.getMatchingPrefixState();
            state = new State(previousState.getAlignment(), trace, previousState.getNode(), previousState.getCostSoFar());
            nextChecks = caseStatesInBuffer.getNextChecks();
            for (State c : nextChecks)
            {
                c.addTracePostfix(trace);
            }
            nextChecks.add(state);
            // are the other nextChecks missing the correct trace suffix?
        }
        else
        {

            // create dummy state, create key in tracesInBuffer
            state = new State(new Alignment(),trace, modelTrie.getRoot(),0);
            caseStatesInBuffer = new StatesBuffer(maxStatesInQueue, modelTrie.getRoot(),trace);
            statesInBuffer.put(caseId, caseStatesInBuffer);
            nextChecks.add(state);
        }


        State candidateState = null;
        String event;
        numTrials = 1;
        cleanseFrequency = maxTrials/20;

        while(nextChecks.size() >0  && numTrials < maxTrials)
        {
            if (candidateState!= null && candidateState.getCostSoFar() == 0)
                break;
            state = pickRandom(candidateState);
            if (state==null)
                continue;
            numTrials++;

            if (numTrials % 100000 == 0 && verbose) {
                System.out.println("Trials so far " + numTrials);
                System.out.println("Queue size "+nextChecks.size());
            }

            event = null;
            traceSuffix = state.getTracePostfix();

            if (traceSuffix.size() == 0 && (state.getNode().isEndOfTrace() || prefixBased))// We're done
            {

                if (candidateState == null)
                {
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());

                }
                else if (state.getAlignment().getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());

                }


                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;
            }
            else if (traceSuffix.size() ==0)
            {
                alg = state.getAlignment();

                node = state.getNode();
                node = node.getChildOnShortestPathToTheEnd();
                while (node != null)
                {
                    Move modelMove = new Move(">>", node.getContent(),1);
                    alg.appendMove(modelMove);
                    node = node.getChildOnShortestPathToTheEnd();
                }
                if (candidateState == null)
                {
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());

                }
                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;

            }
            else if (state.getNode().isEndOfTrace() & !state.getNode().hasChildren()) // and no more children
            {
                alg = state.getAlignment();
                for (String ev: state.getTracePostfix())
                {
                    Move logMove = new Move(ev, ">>",1);
                    alg.appendMove(logMove);
                }
                if (candidateState == null)
                {
                    candidateState = new State(alg, new ArrayList<>(),null, alg.getTotalCost());

                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, new ArrayList<>(),null, alg.getTotalCost());

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
                TrieNode prev=node;
                State syncState;
                do {
//
                    if(!onMatchFollowPrefixOnly)
                    {

                        List<String> trSuffix = new LinkedList<>();
                        trSuffix.addAll(traceSuffix);
                        State nonSyncState = new State(alg, trSuffix, prev.getParent(),0);
                        addStateToTheQueue(handleLogMove(trSuffix, nonSyncState, event), candidateState);
                        nonSyncState = new State(alg, trSuffix, prev.getParent(),0, state);
                        for (State s: handleModelMoves(trSuffix, nonSyncState, candidateState))
                            addStateToTheQueue(s, candidateState);
                    }

                    Move syncMove = new Move(event,event,0);

                    alg.appendMove(syncMove);
                    prev = node;
                    if (traceSuffix.size() > 0)
                    {
                        event = traceSuffix.remove(0);

                        node = node.getChild(event);
                    }
                    else
                    {
                        event = null;
                        node = null;
                    }
                }
                while(node != null);
                if (event != null)
                    traceSuffix.add(0,event);


                int cost = 0;

                syncState = new State(alg,traceSuffix,prev,cost);
                addStateToTheQueue(syncState, candidateState);



            }
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

        caseStatesInBuffer.updateBuffer(candidateState, new PriorityQueue<>(nextChecks));

        return candidateState != null? candidateState.getAlignment():null;
    }
*/
    protected void addStateToTheQueue(State state, State candidateState) {


        cntr++;
        if (nextChecks.size() == maxStatesInQueue)
        {
//            if (verbose)
//                System.out.println("Max queue size reached. New state is not added!");
           if (state.getCostSoFar() < nextChecks.peek().getCostSoFar())
            // if (state.getAlignment().getTotalCost() < nextChecks.peek().getAlignment().getTotalCost())
            {
//                System.out.println(String.format("Adding a good candidate whose cost is %d which is less that the least cost so far %d", state.getAlignment().getTotalCost(), nextChecks.peek().getAlignment().getTotalCost()));
//                System.out.println(String.format("Replacement state suffix length %d, number of model moves %d", state.getTracePostfix().size(), state.getNode().getLevel()));
                nextChecks.poll();
                nextChecks.add(state);
//                states.add(state);
            }
            return;
        }
        if (candidateState != null) {
            if ((state.getAlignment().getTotalCost()+Math.min(Math.abs(state.getTracePostfix().size() - state.getNode().getMinPathLengthToEnd()),Math.abs(state.getTracePostfix().size() - state.getNode().getMaxPathLengthToEnd())))  < candidateState.getAlignment().getTotalCost())// && state.getNode().getLevel() > candidateState.getNode().getLevel())
            {

                nextChecks.add(state);
//                states.add(state);
            }
            else if (verbose) {

          //      System.out.println(String.format("State is not promising cost %d is greater than the best solution so far %d",(state.getAlignment().getTotalCost()+Math.min(Math.abs(state.getTracePostfix().size() - state.getNode().getMinPathLengthToEnd()),Math.abs(state.getTracePostfix().size() - state.getNode().getMaxPathLengthToEnd()))),candidateState.getAlignment().getTotalCost()) );

//                System.out.println("Least cost to check next "+nextChecks.peek().getCostSoFar());
            }
        }
        else //if (state.getCostSoFar()< (nextChecks.size() == 0? Integer.MAX_VALUE: nextChecks.peek().getCostSoFar()))
//        {
            nextChecks.add(state);
//            states.add(state);
//        }

    }

    @Override
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
 //           int cost = alg.getTotalCost();
//            cost += (nd.isEndOfTrace()? 0: nd.getMinPathLengthToEnd()) +traceSuffix.size();
//            if (traceSuffix.size() > 0 && nd.getChild(traceSuffix.get(0))!= null) // we can find a next sync move this path
//                cost-=1;
            State modelMoveState = new State(alg, traceSuffix,nd, costFunction.computeCost(dummyState,traceSuffix,null, Configuration.MoveType.MODEL_MOVE, this));
            result.add(modelMoveState);
        }
        return  result;
    }

    @Override
    protected State handleLogMove(List<String> traceSuffix, State state, String event) {
        Alignment alg;
        State logMoveState;
        if (event != null) {
            Move logMove = new Move(event, ">>",1);// logMoveCost);
            alg = state.getAlignment();
            alg.appendMove(logMove);
            int cost = alg.getTotalCost();
            cost += (state.getNode().isEndOfTrace()? 0: state.getNode().getMinPathLengthToEnd()) + traceSuffix.size() ;
            for (TrieNode nd: state.getNode().getAllChildren()) {
                if (nd.getChild(event) != null)// If we make a model move, we can reach a sync move. So, log move is not the best move
                {
                    cost += 1;
                    break;
                }
            }

            logMoveState = new State(alg, traceSuffix, state.getNode(), costFunction.computeCost(state,traceSuffix,event, Configuration.MoveType.LOG_MOVE, this));


            // let's put the event back in the trace postfix to see how it check for model moves
            traceSuffix.add(0, event);
            return logMoveState;
        }
        else
            return null;
    }
}
