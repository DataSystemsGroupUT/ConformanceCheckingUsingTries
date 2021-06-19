package ee.ut.cs.dsg.confcheck;

import com.sun.prism.shader.Solid_Color_AlphaTest_Loader;
import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

import java.util.*;

public class ConformanceChecker {
    protected final Trie trie;
    protected final int logMoveCost ;
    protected final int modelMoveCost ;
    protected PriorityQueue<State> nextChecks;

    protected int cntr=1;
    protected int maxStatesInQueue;
    private HashSet<State> seenBefore;
    private ArrayList<State> states;
    protected int traceSize;
    protected int maxModelTraceSize;
    protected int leastCostSoFar  = Integer.MAX_VALUE;

    protected int cleanseFrequency = 100;
    protected int maxTrials=500000;

    protected Trie inspectedLogTraces;
    protected Random rnd;
    public ConformanceChecker(Trie trie)
    {
       this(trie, 1, 1);

    }
    public ConformanceChecker(Trie trie, int logCost, int modelCost)
    {
        this(trie, logCost, modelCost, 10000);

    }
    public ConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue)
    {
        this.trie = trie;
        this.logMoveCost = logCost;
        this.modelMoveCost = modelCost;
        nextChecks = new PriorityQueue<>();
        states = new ArrayList<>();
        this.maxStatesInQueue = maxStatesInQueue;
        this.seenBefore = new HashSet<>();
    }

    public Alignment check(List<String> trace)
    {
        nextChecks.clear();
        seenBefore.clear();
        states.clear();

        cntr=0;
        traceSize = trace.size();
        maxModelTraceSize = trie.getRoot().getMaxPathLengthToEnd();
        TrieNode node;
        Alignment alg = new Alignment();
        List<String> traceSuffix;
        State state = new State(alg,trace,trie.getRoot(),0);
        nextChecks.add(state);
        states.add(state);
        State candidateState = null;
        String event;
        int numTrials=0;
        while(nextChecks.size() >0 && numTrials < maxTrials)
        {
//            System.out.println("Queue size is "+nextChecks.size());
//            if (cntr %100 ==0)
//            {
//                state = states.get((int)Math.random()*states.size());
//                nextChecks.remove(state);
//                cntr=1;
//            }
//            else {
                state = nextChecks.poll();
//                states.remove(state);
//            }

//            System.out.println("Cost so far "+state.getCostSoFar());
//            System.out.println("Alignment cost so far "+state.getAlignment().getTotalCost());
            numTrials++;


            event = null;
            traceSuffix = state.getTracePostfix();

            // we have to check what is remaining
            if (traceSuffix.size() == 0 && state.getNode().isEndOfTrace())// We're done
            {
                //return state.getAlignment();

                if (candidateState == null)
                {
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
//                    System.out.println("Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    cntr=0;
                }
                else if (state.getAlignment().getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
//                    System.out.println("Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());

                    cntr=0;
                }

               // candidates.add(candidateState);
//                System.out.println("Remaining alignments to check " +nextChecks.size());
//                System.out.println("Best alignment so far " +candidateState.getCostSoFar());
//                return candidateState.getAlignment();
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
                    node = node.getChildOnShortestPathToTheEnd();
                }
//                System.out.println("Alignment found costs "+alg.getTotalCost());
                if (candidateState == null)
                {
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
//                    System.out.println("Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());

                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
//                    System.out.println("Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());

                    cntr=0;
                }
                else
                {
//                    System.out.println("Current alignment is more expensive "+alg.getTotalCost());
                }
                continue;
            }
            else if (state.getNode().isEndOfTrace())
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
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
//                    System.out.println("Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
//                    System.out.println("Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());

                    cntr=0;
                }
                continue;
            }
            else {
                event = traceSuffix.remove(0);
                node = state.getNode().getChild(event);
            }
            if (node != null) // we found a match => synchronous move
            {

                Move syncMove = new Move(event,event,0);
                alg = state.getAlignment();
                alg.appendMove(syncMove);
//                if (node.isEndOfTrace() &&  traceSuffix.size()==0) // we should stop
//                    return alg;
//                state = new State(alg,traceSuffix,node, state.getCostSoFar());
//                state = new State(alg,traceSuffix,node,computeCostV2(node.getMinPathLengthToEnd(),traceSuffix.size(),0,false));
//                state = new State(alg,traceSuffix,node, maxModelTraceSize+traceSize - (node.getLevel() + (traceSize-traceSuffix.size())));//Math.abs(node.getLevel() - traceSuffix.size())-2);

                int cost = maxModelTraceSize + traceSize -(state.getNode().getLevel() + (traceSize-traceSuffix.size()) ) - (alg.getMoves().size() - alg.getTotalCost());
                State syncMoveState;
                syncMoveState = new State(alg,traceSuffix,node,cost);
                addStateToTheQueue(syncMoveState, candidateState);
//
                handleLogMove(traceSuffix, state, candidateState, event);
                handleModelMoves(traceSuffix, state, candidateState);

            }
            // On 27th of May 2021. we need to give the option to a log move as well as a model move
            else // there is no match, we have to make the model move and the log move
            {
                // let make the log move if there are still more moves
                handleLogMove(traceSuffix, state, candidateState, event);
                handleModelMoves(traceSuffix, state, candidateState);

            }
        }
        return candidateState != null? candidateState.getAlignment():null;
        //return alg;
    }

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
            int cost = 0;
            //int cost = (maxModelTraceSize - nd.getLevel()) + traceSuffix.size();//- (alg.getMoves().size() - alg.getTotalCost());// - Math.min((nd.getLevel()) , traceSuffix.size()));//+alg.getTotalCost();
            cost += nd.getMinPathLengthToEnd();//+traceSuffix.size() ;
//            int cost = nd.getMaxPathLengthToEnd()+traceSuffix.size();
            // I need to add to the cost the more steps needed in the best case alignment
//            cost+= Math.min(Math.abs(nd.getMinPathLengthToEnd() - traceSuffix.size()), Math.abs(nd.getMaxPathLengthToEnd()-traceSuffix.size()));
//            cost+= Math.abs(nd.getMinPathLengthToEnd() - traceSuffix.size());
            // we penalize model move if it causes the the difference between total trace length and model trace length to increase
//            if (nd.getLevel() -1> (traceSize - traceSuffix.size()))
//                cost+=1;
//            cost-=10;
            if (traceSuffix.size() > 0 && nd.getChild(traceSuffix.get(0))!= null) // we can find a next sync move this path
                cost-=1;

//            else
//                cost+=1;
//            cost*=-1;
//            if (Math.random() > 0.5)
//                cost-=1;
//            else
//                cost+=10;
            //cost--;
//            int cost = maxModelTraceSize-nd.getLevel();
            State modelMoveState = new State(alg, traceSuffix,nd, /*computeCostV2(nd.getMinPathLengthToEnd(), traceSuffix.size(), lookAhead,false)*/cost);
//                    nextChecks.add(modelMoveState);
//            if(modelMoveState.getCostSoFar()< state.getCostSoFar() || state.getCostSoFar() ==0)
//                addStateToTheQueue(modelMoveState, candidateState);
            result.add(modelMoveState);
        }
        return  result;
    }

    protected State handleLogMove(List<String> traceSuffix, State state, State candidateState, String event) {
        Alignment alg;
        State logMoveState=null;
        if (event != null) {

            Move logMove = new Move(event, ">>",1);// logMoveCost);
            alg = state.getAlignment();
            alg.appendMove(logMove);
//            int lookAhead=0;

            int cost = 0;
            cost +=  traceSuffix.size();
//            int cost = state.getNode().getMaxPathLengthToEnd() + traceSuffix.size() ;
//            cost += Math.min(Math.abs(state.getNode().getMinPathLengthToEnd() - traceSuffix.size()), Math.abs(state.getNode().getMaxPathLengthToEnd()-traceSuffix.size()));
            for (TrieNode nd: state.getNode().getAllChildren()) {
                if (nd.getChild(event) != null)// If we make a model move, we can reach a sync move. So, log move is not the best move
                {
                    cost += 1;
                   // break;
                }
            }

            logMoveState = new State(alg, traceSuffix, state.getNode(), /*computeCostV2(state.getNode().getMinPathLengthToEnd() , traceSuffix.size(), lookAhead,true)*/ cost);

//            //nextChecks.add(logMoveState);
////            if(logMoveState.getCostSoFar() < state.getCostSoFar() || state.getCostSoFar() ==0)
//                addStateToTheQueue(logMoveState, candidateState);
            // let's put the event back in the trace postfix to see how it check for model moves
            traceSuffix.add(0, event);
            return logMoveState;
        }
        else
            return null;
    }

    protected void addStateToTheQueue(State state, State candidateState) {

//        if (seenBefore.contains(state)) {
//            System.out.println("This state has been seen before, skipping it...");
//            return;
//        }
//        else
//            seenBefore.add(state);
//        if (state.getCostSoFar() < 0)
//            return;
        if (cntr==maxStatesInQueue) {
//            System.out.println("Max queue size reached. New state is not added!");
            return;
        }
        cntr++;
        if (nextChecks.size() == maxStatesInQueue)
        {
//            System.out.println("Max queue size reached. New state is not added!");
//           if (state.getCostSoFar() < nextChecks.peek().getCostSoFar())
//            // if (state.getAlignment().getTotalCost() < nextChecks.peek().getAlignment().getTotalCost())
//            {
//                System.out.println(String.format("Adding a good candidate whose cost is %d which is less that the least cost so far %d", state.getAlignment().getTotalCost(), nextChecks.peek().getAlignment().getTotalCost()));
//                System.out.println(String.format("Replacement state suffix length %d, number of model moves %d", state.getTracePostfix().size(), state.getNode().getLevel()));
//                nextChecks.poll();
//                nextChecks.add(state);
//            }
            return;
        }
        if (candidateState != null) {
            if ((state.getAlignment().getTotalCost() + Math.min(Math.abs(state.getTracePostfix().size() - state.getNode().getMinPathLengthToEnd()),Math.abs(state.getTracePostfix().size() - state.getNode().getMaxPathLengthToEnd())))< candidateState.getAlignment().getTotalCost())// && state.getNode().getLevel() > candidateState.getNode().getLevel())
            {

                nextChecks.add(state);
//                states.add(state);
            }
            else {
//                System.out.println(String.format("State is not promising cost %d is greater than the best solution so far %d",(state.getAlignment().getTotalCost()+Math.abs(state.getTracePostfix().size() - state.getNode().getMinPathLengthToEnd())),candidateState.getAlignment().getTotalCost()) );
//                System.out.println("Queue size "+nextChecks.size());
//                System.out.println("Least cost to check next "+nextChecks.peek().getCostSoFar());
            }
        }
        else //if (state.getCostSoFar()< (nextChecks.size() == 0? Integer.MAX_VALUE: nextChecks.peek().getCostSoFar()))
        {
            nextChecks.add(state);
//            states.add(state);
        }
        if (cntr % cleanseFrequency == 0)
        {
            cleanState(candidateState);
            cntr=1;
        }
    }
    private void cleanState(State candidateState)
    {
        int coundDown=cleanseFrequency;
        State current;
        while (nextChecks.size() > cleanseFrequency & coundDown > 0) {
            current = nextChecks.poll();
            if (candidateState != null)
            {
                if ((current.getAlignment().getTotalCost() + Math.abs(current.getTracePostfix().size() - current.getNode().getMinPathLengthToEnd())) >= candidateState.getAlignment().getTotalCost())// && state.getNode().getLevel() > candidateState.getNode().getLevel())
                {
//                    System.out.println(String.format("Removing an old expensive state with cost %d, which is greater than the best solution so far %d",(current.getAlignment().getTotalCost()+Math.abs(current.getTracePostfix().size() - current.getNode().getMinPathLengthToEnd())),candidateState.getAlignment().getTotalCost()) );
//                    System.out.println("Queue size "+nextChecks.size());
                    continue;

                }
            }
            else {
                nextChecks.add(new State(current.getAlignment(), current.getTracePostfix(), current.getNode(), (int) (current.getCostSoFar() + (1 + 10))));
                coundDown--;
            }

        }
        //adjust the frequency of state cleaning

        if(candidateState != null)
        {
            if (leastCostSoFar > candidateState.getAlignment().getTotalCost()) // we couldn't find a better solution since last time, we need to decrease the frequency
            {
                cleanseFrequency = Math.min(100, (cleanseFrequency/10)+100);
            }
            else
                cleanseFrequency *=10;
    //        System.out.println("State cleansing frequency changed to " +cleanseFrequency);
        }

    }

    private int computeCost(int minPathLengthToEnd, int traceSuffixLength, int cumulativeCost, boolean isLogMove)
    {
        int cost = isLogMove? logMoveCost: modelMoveCost;

        // If this is a log move, we have to add 1 to the trie length to end as we have not moved yet from the current node
        // in the trie.
        cost += cumulativeCost + Math.abs( (/*(isLogMove? 1:0) +*/ minPathLengthToEnd) -  traceSuffixLength);
        return cost;
    }

    private int computeCostV2(int minPathLengthToEnd, int traceSuffixLength, int cumulativeCost, boolean isLogMove)
    {
        int cost =0;//isLogMove? logMoveCost: modelMoveCost;


        // If this is a log move, we have to add 1 to the trie length to end as we have not moved yet from the current node
        // in the trie.
//        cost += cumulativeCost + Math.abs( (/*(isLogMove? 1:0) +*/ minPathLengthToEnd) -  traceSuffixLength)+minPathLengthToEnd+traceSuffixLength;
//        cost += Math.max(minPathLengthToEnd+traceSuffixLength - cumulativeCost - Math.abs( (/*(isLogMove? 1:0) +*/ minPathLengthToEnd) -  traceSuffixLength),0);
//

        // Description of the cost: worst case is no alignment at all we have to do a model trace followed by the log trace
        // Then we subtract how far we went into the model model trace which is represented by the misleading name of cumulative cost
        // We also have to subtract how far did we go in the log trace
        cost += maxModelTraceSize+traceSize -(cumulativeCost + (traceSize- traceSuffixLength));//- Math.abs( /*(isLogMove? 1:0) +*/ minPathLengthToEnd -  traceSuffixLength);
        if (cost < 0)
            System.out.println("Cost is negative "+cost +" worst case cost is "+(maxModelTraceSize+traceSize) + "cumulative cost is "+cumulativeCost);
        return cost;

    }

}
