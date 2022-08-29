package ee.ut.cs.dsg.confcheck.cost;

import ee.ut.cs.dsg.confcheck.ApproximateConformanceChecker;
import ee.ut.cs.dsg.confcheck.State;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Configuration.MoveType;

import java.util.List;

public class DualProgressiveCostFunction implements CostFunction {

    @Override
    public int computeCost(State state, List<String> suffix, String event, MoveType mt, ApproximateConformanceChecker conformanceChecker) {

        if (mt == MoveType.SYNCHRONOUS_MOVE)
            return 0;

        int cost = 0;
        cost += (state.getNode().isEndOfTrace() ? 0 : state.getNode().getMinPathLengthToEnd()) + suffix.size();

        List<TrieNode> ndz = state.getNode().getAllChildren();
        if (mt == MoveType.LOG_MOVE) {

            for (TrieNode nd : ndz) {
                if (nd.getChild(event) != null)// If we make a model move, we can reach a sync move. So, log move is not the best move
                {
                    //We add to the cost this unnecessary move.
//                    cost += ndz.size()-1;
                    cost += 1;
                    break;
                }
            }
        } else if (mt == MoveType.MODEL_MOVE) {
            if (suffix.size() > 0 && state.getNode().getChild(suffix.get(0)) != null) // we can find a next sync move this path
//                cost -= state.getNode().getLevel();
                cost += ndz.size()* (conformanceChecker.getMaxModelTraceSize() - state.getNode().getLevel());
        }

        return cost;
    }
}
