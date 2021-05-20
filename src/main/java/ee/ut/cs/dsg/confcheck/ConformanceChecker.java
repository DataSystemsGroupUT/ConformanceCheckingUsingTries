package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class ConformanceChecker {
    protected final Trie trie;
    protected final int logMoveCost ;
    protected final int modelMoveCost ;
    private PriorityQueue<State> nextChecks;

    private int maxStatesInQueue;
    public ConformanceChecker(Trie trie)
    {
       this(trie, 1, 1);

    }
    public ConformanceChecker(Trie trie, int logCost, int modelCost)
    {
        this(trie, logCost, modelCost, 1000);

    }
    public ConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue)
    {
        this.trie = trie;
        this.logMoveCost = logCost;
        this.modelMoveCost = modelCost;
        nextChecks = new PriorityQueue<>();
        this.maxStatesInQueue = maxStatesInQueue;
    }

    public Alignment check(List<String> trace)
    {
        nextChecks.clear();

        TrieNode node;
        Alignment alg = new Alignment();
        List<String> traceSuffix;
        State state = new State(alg,trace,trie.getRoot(),0);
        nextChecks.add(state);
        State candidateState = null;
        String event;

        while(nextChecks.size() >0 )
        {
//            System.out.println("Queue size is "+nextChecks.size());

            state = nextChecks.poll();

//            System.out.println("Cost so far "+state.getCostSoFar());
//            System.out.println("Alignment cost so far "+state.getAlignment().getTotalCost());



            event = null;
            traceSuffix = state.getTracePostfix();

            // we have to check what is remaining
            if (traceSuffix.size() == 0 && state.getNode().isEndOfTrace())// We're done
            {
                //return state.getAlignment();

                if (candidateState == null)
                {
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
                }
                else if (state.getAlignment().getTotalCost() < candidateState.getCostSoFar())
                {
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
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
                node = null;
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
                state = new State(alg,traceSuffix,node, state.getCostSoFar());
                addStateToTheQueue(state, candidateState);


            }
            else // there is no match, we have to make the model move and the log move
            {
                // let make the log move if there are still more moves

                State logMoveState=null;
                if (event != null) {

                    Move logMove = new Move( event, ">>",1);// logMoveCost);
                    alg = state.getAlignment();
                    alg.appendMove(logMove);
                    logMoveState = new State(alg, traceSuffix, state.getNode(), computeCost(state.getNode().getMinPathLengthToEnd(),traceSuffix.size(), state.getAlignment().getTotalCost(),true));

                    //nextChecks.add(logMoveState);
                    addStateToTheQueue(logMoveState,candidateState);
                    // let's put the event back in the trace postfix to see how it check for model moves
                    traceSuffix.add(0,event);
                }

                // Let us make the model move

                List<TrieNode> nodes = state.getNode().getAllChildren();
//                if (nodes.size() > 1)
//                    System.out.println("We have multiple children in the trie "+nodes.size());
                Move modelMove;
                State minModelMoveState = null ;
                for (TrieNode nd : nodes)
                {


                    modelMove = new Move(">>", nd.getContent(),1);//modelMoveCost);
                    alg = state.getAlignment();
                    alg.appendMove(modelMove);

                    // find a child node that has length to the end less than the remaining postfix
                    State modelMoveState = new State(alg, traceSuffix,nd, computeCost(nd.getMinPathLengthToEnd(),traceSuffix.size(), state.getAlignment().getTotalCost(),false));
//                    nextChecks.add(modelMoveState);
                    addStateToTheQueue(modelMoveState,candidateState);
                }

            }
        }
        return candidateState.getAlignment();
        //return alg;
    }

    private void addStateToTheQueue(State state, State candidateState) {

        if (nextChecks.size() == maxStatesInQueue)
        {
//            System.out.println("Max queue size reached. New state is not added!");
            return;
        }
        if (candidateState != null) {
            if (state.getAlignment().getTotalCost() < candidateState.getAlignment().getTotalCost())
                nextChecks.add(state);
//            else {
//                System.out.println("State is not promising cost is greater than the best solution so far " + candidateState.getAlignment().getTotalCost());
//                System.out.println("Queue size "+nextChecks.size());
//            }
        }
        else if (state.getCostSoFar()< (nextChecks.size() == 0? Integer.MAX_VALUE: nextChecks.peek().getCostSoFar()))
            nextChecks.add(state);
    }

    private int computeCost(int minPathLengthToEnd, int traceSuffixLength, int cumulativeCost, boolean isLogMove)
    {
        int cost = isLogMove? logMoveCost: modelMoveCost;

        // If this is a log move, we have to add 1 to the trie length to end as we have not moved yet from the current node
        // in the trie.
        cost += /*cumulativeCost +*/ Math.abs( (/*(isLogMove? 1:0) +*/ minPathLengthToEnd) -  traceSuffixLength);
        return cost;
    }

}
