package ee.ut.cs.dsg.confcheck.cost;

import ee.ut.cs.dsg.confcheck.ConformanceChecker;
import ee.ut.cs.dsg.confcheck.RandomConformanceChecker;
import ee.ut.cs.dsg.confcheck.State;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Configuration;

import java.util.List;

public class PessimisticCostFunction implements CostFunction{


    @Override
    public int computeCost(State state, List<String> suffix, String event, Configuration.MoveType mt, ConformanceChecker conformanceChecker) {
        int cost = state.getAlignment().getTotalCost();
        cost += conformanceChecker.getMaxModelTraceSize()+ conformanceChecker.getTraceSize();
        cost -= state.getNode().getLevel() ;
        cost -= conformanceChecker.getTraceSize()-suffix.size();
        if (mt == Configuration.MoveType.LOG_MOVE)
        {
            for (TrieNode nd: state.getNode().getAllChildren()) {
                if (nd.getChild(event) != null)// If we make a model move, we can reach a sync move. So, log move is not the best move
                {
                    cost += 1;
                    break;
                }
            }
        }
        else if (mt == Configuration.MoveType.MODEL_MOVE)
        {
            if (suffix.size() > 0 && state.getNode().getChild(suffix.get(0))!= null) // we can find a next sync move this path
                cost-=1;
        }

        return cost;
    }
}
