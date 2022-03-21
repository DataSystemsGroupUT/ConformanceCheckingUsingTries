package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class StatesBuffer {
    protected PriorityQueue<State> nextChecks;
    protected State matchingPrefixState;


    public StatesBuffer (int maxStatesInQueue, TrieNode node, List<String> trace){
        this.nextChecks = new PriorityQueue<>(maxStatesInQueue);
        this.matchingPrefixState = new State(new Alignment(), trace, node,0);
    }

    public void updateBuffer(State state, PriorityQueue<State> statesQueue){
        this.matchingPrefixState = state;
        this.nextChecks = statesQueue;
    }

    public PriorityQueue<State> getNextChecks() {
        return nextChecks;
    }

    public State getMatchingPrefixState() {
        return matchingPrefixState;
    }


}
