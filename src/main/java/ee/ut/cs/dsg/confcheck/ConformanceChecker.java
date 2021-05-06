package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

import java.util.List;
import java.util.PriorityQueue;

public class ConformanceChecker {
    private final Trie trie;
    private final int logMoveCost ;
    private final int modelMoveCost ;
    private PriorityQueue<State> nextChecks;

    public ConformanceChecker(Trie trie)
    {
       this(trie, 1, 1);

    }
    public ConformanceChecker(Trie trie, int logCost, int modelCost)
    {
        this.trie = trie;
        this.logMoveCost = logCost;
        this.modelMoveCost = modelCost;
        nextChecks = new PriorityQueue<>();
    }

    public Alignment check(List<String> trace)
    {
        TrieNode node;
        Alignment alg = new Alignment();
        List<String> traceSuffix;
        State state = new State(alg,trace,trie.getRoot(),0);
        nextChecks.add(state);
        String event;
        while(nextChecks.size() !=0 )
        {
            state = nextChecks.poll();

            event = null;
            traceSuffix = state.getTracePostfix();

            // we have to check what is remaining
            if (traceSuffix.size() == 0 && state.getNode().isEndOfTrace())// We're done
            {
                return state.getAlignment();
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
                if (node.isEndOfTrace() &&  traceSuffix.size()==0) // we should stop
                    return alg;
                state = new State(alg,traceSuffix,node, state.getCostSoFar());
                nextChecks.add(state);

            }
            else // there is no match, we have to make the model move and the log move
            {
                // let make the log move if there are still more moves
                State logMoveState=null;
                if (event != null) {

                    Move logMove = new Move( event, ">>", logMoveCost);
                    alg = state.getAlignment();
                    alg.appendMove(logMove);
                    logMoveState = new State(alg, traceSuffix, state.getNode(), computeCost(state.getNode().getMinPathLengthToEnd(),traceSuffix.size(), state.getCostSoFar(),true));
//                    nextChecks.add(logMoveState);
                    // let's put the event back in the trace postfix to see how it check for model moves
                    traceSuffix.add(0,event);
                }

                // Let us make the model move

                List<TrieNode> nodes = state.getNode().getAllChildren();
                Move modelMove;
                State minModelMoveState = null ;
                for (TrieNode nd : nodes)
                {
//                    modelMove = null;

                    modelMove = new Move(">>", nd.getContent(),modelMoveCost);
                    alg = state.getAlignment();
                    alg.appendMove(modelMove);

                    // find a child node that has length to the end less than the remaining postfix
                    State modelMoveState = new State(alg, traceSuffix,nd, computeCost(nd.getMinPathLengthToEnd(),traceSuffix.size(), state.getCostSoFar(),false));
                    if (minModelMoveState == null)
                        minModelMoveState = modelMoveState;
                    else if (minModelMoveState.getCostSoFar() > modelMoveState.getCostSoFar())
                        minModelMoveState = modelMoveState;

                  //  nextChecks.add(modelMoveState);
                }
                if (minModelMoveState.getCostSoFar() < logMoveState.getCostSoFar())
                    nextChecks.add(minModelMoveState);
                else
                    nextChecks.add(logMoveState);

            }
        }
        return alg;
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
