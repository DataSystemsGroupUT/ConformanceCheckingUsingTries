package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

import java.util.List;
import java.util.PriorityQueue;

public class ConformanceChecker {
    private Trie trie;
    private PriorityQueue<State> nextChecks;

    public ConformanceChecker(Trie trie)
    {
        this.trie = trie;
        nextChecks = new PriorityQueue<>();

    }

    public Alignment check(List<String> trace)
    {
        TrieNode node;
        Alignment alg = new Alignment();
        List<String> tracePostfix;
        State state = new State(alg,trace,trie.getRoot(),0);
        nextChecks.add(state);
        String event = null;
        while(nextChecks.size() !=0)
        {
            state = nextChecks.poll();

            event = null;
            tracePostfix = state.getTracePostfix();

            // we have to check what is remaining
            if (tracePostfix.size() == 0 && state.getNode().isEndOfTrace())// We're done
            {
                return state.getAlignment();
            }
            else if (tracePostfix.size() ==0)
            {
                // we still have model moves to do
                node = null;
            }
            else {
                event = tracePostfix.remove(0);
                node = state.getNode().getChild(event);
            }
            if (node != null) // we found a match => synchronous move
            {

                Move syncMove = new Move(event,event,0);
                alg = state.getAlignment();
                alg.appendMove(syncMove);
                if (node.isEndOfTrace() &&  tracePostfix.size()==0) // we should stop
                    return alg;
                state = new State(alg,tracePostfix,node, state.getCostSoFar());
                nextChecks.add(state);

            }
            else // there is no match, we have to make the model move and the log move
            {
                // let make the log move if there are still more moves
                if (event != null) {
                    Move logMove = new Move(event, ">>", 1);
                    alg = state.getAlignment();
                    alg.appendMove(logMove);
                    State logMoveState = new State(alg, tracePostfix, state.getNode(), alg.getTotalCost());
                    nextChecks.add(logMoveState);
                    // let's put the event back in the trace postfix to see how it check for model moves
                    tracePostfix.add(0,event);
                }

                // Let us make the model move
                // We need to try all possibilities, we can later try the optimization of the remaining length

//                node = state.getNode().getChildWithLeastPath(tracePostfix.size());
                List<TrieNode> nodes = state.getNode().getAllChildren();
                Move modelMove;
                for (TrieNode nd : nodes)
                {
                    modelMove = null;
                    modelMove = new Move(">>", nd.getContent(),1);
                    alg = state.getAlignment();
                    alg.appendMove(modelMove);

                    // find a child node that has length to the end less than the remaining postfix
                    State modelMoveState = new State(alg, tracePostfix,nd,alg.getTotalCost() );
                    nextChecks.add(modelMoveState);
                }
            }
        }
        return alg;
    }

}
