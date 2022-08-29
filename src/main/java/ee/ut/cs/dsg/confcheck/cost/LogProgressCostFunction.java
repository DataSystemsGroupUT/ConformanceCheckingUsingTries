package ee.ut.cs.dsg.confcheck.cost;

import ee.ut.cs.dsg.confcheck.ApproximateConformanceChecker;
import ee.ut.cs.dsg.confcheck.State;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Configuration;

import java.util.List;

public class LogProgressCostFunction implements CostFunction{
    @Override
    public int computeCost(State state, List<String> suffix, String event, Configuration.MoveType mt, ApproximateConformanceChecker conformanceChecker) {
        if (mt == Configuration.MoveType.SYNCHRONOUS_MOVE)
            return 0;

        int cost = suffix.size();

        if (mt == Configuration.MoveType.LOG_MOVE) {
            for (TrieNode nd : state.getNode().getAllChildren()) {
                if (nd.getChild(event) != null)// If we make a model move, we can reach a sync move. So, log move is not the best move
                {
                    cost += 1;
                    break;
                }
            }
        } else if (mt == Configuration.MoveType.MODEL_MOVE) {
            if (suffix.size() > 0 && state.getNode().getChild(suffix.get(0)) != null) // we can find a next sync move this path
                cost -= 1;
        }

        return cost;
    }
}
