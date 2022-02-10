package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class State implements Comparable<State>{
    private Alignment alignment;
    private List<String> tracePostfix;
    private TrieNode node;
    private int costSoFar;
    private State parentState;

    public State(Alignment alignment, List<String> tracePostfix, TrieNode node, int costSoFar)
    {
        this.alignment = alignment;
        this.tracePostfix = new LinkedList<>();
        this.tracePostfix.addAll(tracePostfix);
        this.node = node;
        this.costSoFar = costSoFar;
        this.parentState = null;
    }

    // This new constructor was added to link back to previous states and track the cost of partial alignments
    public State(Alignment alignment, List<String> tracePostfix, TrieNode node, int costSoFar, final State parentState)
    {
        this.alignment = alignment;
        this.tracePostfix = new LinkedList<>();
        this.tracePostfix.addAll(tracePostfix);
        this.node = node;
        this.costSoFar = costSoFar;
        this.parentState = parentState;
    }
    @Override
    public int compareTo(State other)
    {
        if (this.costSoFar > other.getCostSoFar())
            return 1;
        else if (this.costSoFar < other.getCostSoFar())
            return -1;
        else
            return 0;

    }

    public Alignment getAlignment() {
        Alignment copy = new Alignment(alignment);
        return copy;
    }

    public List<String> getTracePostfix() {
        return tracePostfix;
    }

    public TrieNode getNode() {
        return node;
    }

    public int getCostSoFar() {
        return costSoFar;
    }

    public int hashCode()
    {
        return node.hashCode();
    }

    public State getParentState()
    {
        return parentState;
    }
}
