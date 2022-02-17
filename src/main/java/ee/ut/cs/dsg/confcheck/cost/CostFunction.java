package ee.ut.cs.dsg.confcheck.cost;

import ee.ut.cs.dsg.confcheck.ConformanceChecker;
import ee.ut.cs.dsg.confcheck.State;

import java.util.List;

import static ee.ut.cs.dsg.confcheck.util.Configuration.MoveType;

@FunctionalInterface
public interface CostFunction {

    int computeCost(State state, List<String> suffix, String event, MoveType mt, ConformanceChecker conformanceChecker);
}
