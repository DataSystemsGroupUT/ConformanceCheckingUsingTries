package ee.ut.cs.dsg.confcheck.trie;

import ee.ut.cs.dsg.confcheck.State;
import ee.ut.cs.dsg.confcheck.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class TrieNode {

    private String content; // This is a numerical representation of the activity label. Should be part of a lookup table
    private int maxChildren;
    private int minPathLengthToEnd;
    private int maxPathLengthToEnd;
    private TrieNode parent;
    private TrieNode[] children;
    private boolean isEndOfTrace;
    private List<Integer> linkedTraces;
    private int level=0;
    private int numChildren=0;

    public State getAlignmentState() {
        return alignmentState;
    }

    public void setAlignmentState(State alignmentState) {
        this.alignmentState = alignmentState;
    }

    private State alignmentState;
    private void setEndOfTrace(boolean isEndOfTrace)
    {
        this.isEndOfTrace = isEndOfTrace;
    }
//    public TrieNode(String content, int maxChildren, int minPathLengthToEnd, boolean isEndOfTrace, TrieNode parent)
//    {
//        this.content = content;
//        this.maxChildren = Utils.isPrime(maxChildren)? maxChildren:  Utils.nextPrime(maxChildren);
//        //TODO: Change children type to HashMap?
//        this.children = new TrieNode[this.maxChildren];
//        this.minPathLengthToEnd = minPathLengthToEnd;
//        this.parent = parent;
//
//        this.isEndOfTrace = isEndOfTrace;
//        this.linkedTraces = new ArrayList<>();
//    }
    public TrieNode(String content, int maxChildren, int minPathLengthToEnd, int maxPathLengthToEnd, boolean isEndOfTrace, TrieNode parent)
    {
        this.content = content;
        this.maxChildren = Utils.isPrime(maxChildren)? maxChildren:  Utils.nextPrime(maxChildren);
        //TODO: Change children type to HashMap?
        this.children = new TrieNode[this.maxChildren];
        this.minPathLengthToEnd = minPathLengthToEnd;
        this.maxPathLengthToEnd = maxPathLengthToEnd;
        this.parent = parent;
        if(parent != null)
            this.level = parent.getLevel()+1;

        this.isEndOfTrace = isEndOfTrace;
        this.linkedTraces = new ArrayList<>();
    }

    public int getLevel() {
        return level;
    }

    public void addLinkedTraceIndex(int i )
    {
        this.linkedTraces.add(i);
    }
    public List<Integer> getLinkedTraces()
    {
        return linkedTraces;
    }
    public String getContent()
    {
        return  content;
    }

    public int getMinPathLengthToEnd() {
        return minPathLengthToEnd;
    }

    public int getMaxPathLengthToEnd()
    {
        return maxPathLengthToEnd;
    }

    public TrieNode getParent()
    {
        return parent;
    }
    public TrieNode getChild(String label)
    {
        TrieNode result =children[Math.abs(label.hashCode())%maxChildren];
        if (result != null && !result.getContent().equals(label))
        {
            //System.err.println(String.format("Different labels with the same hash code %s and %s", result.getContent(), label));
            result = null;
        }
        return result ;
    }

    public boolean isEndOfTrace() {
        return isEndOfTrace;
    }

    public TrieNode getChildWithLeastPathLengthDifference( int pathLength)
    {
        int minPath = pathLength;
        TrieNode child = null;
        int minDiff = Integer.MAX_VALUE;
        for (TrieNode ch : children)
            if(ch != null)
            {
//                if (!ch.getContent().equals(label))
//                    continue;
                int diff = Math.abs(ch.getMinPathLengthToEnd() - minPath);
                if ( diff < minDiff)
                {
                    child = ch;
                    minDiff = diff;
                }

            }
        return child;
    }
    public List<TrieNode> getAllChildren()
    {
        List<TrieNode> result = new ArrayList<>();
        for (TrieNode nd : children)
            if (nd !=null)
                result.add(nd);

        return result;
    }
    public boolean hasChildren()
    {
        return numChildren != 0;
    }
    public TrieNode addChild(TrieNode child)
    {
//        System.out.println("Hash code "+child.getContent().hashCode());
        if (children[Math.abs(child.getContent().hashCode())%maxChildren] == null) {
            children[Math.abs(child.getContent().hashCode()) % maxChildren] = child;
            child.parent = this;
            numChildren++;
        }
        else
        {
            if (!children[Math.abs(child.getContent().hashCode()) % maxChildren].getContent().equals(child.getContent()))
            {
                System.err.println("A collision occurred");
            }
            else // just double check the is end of trace
            {
                if (child.isEndOfTrace())
                    children[Math.abs(child.getContent().hashCode()) % maxChildren].setEndOfTrace(child.isEndOfTrace());
            }
        }
        this.minPathLengthToEnd = Math.min(this.minPathLengthToEnd, child.getMinPathLengthToEnd()+1);
        this.maxPathLengthToEnd = Math.max(this.maxPathLengthToEnd, child.getMaxPathLengthToEnd()+1);
       // return children[child.getContent().hashCode() % maxChildren];
        return children[Math.abs(child.getContent().hashCode()) % maxChildren];
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(" Node(content:"+this.content+", minPath:"+minPathLengthToEnd+", maxPath:"+maxPathLengthToEnd+", isEndOfATrace:"+isEndOfTrace+") Children(");
        for (TrieNode child : children)
            if (child != null)
                result.append(child.toString());
        result.append(")");
        return result.toString();
    }

    public boolean equals(Object other)
    {
        if (other instanceof  TrieNode)
        {
            TrieNode otherNode = (TrieNode) other;

            return (this.content.equals(otherNode.getContent()) && this.level==otherNode.getLevel());
        }
        return false;
    }

    public int hashCode()
    {
        return this.content.hashCode()+this.level;
    }

    public TrieNode getChildOnShortestPathToTheEnd()
    {
        TrieNode child;
        child = this;
        for (TrieNode ch: children)
        {
            if (ch==null)
                continue;
            if (ch.isEndOfTrace()){
                child = ch;
                break;
            }
            if (ch.getMinPathLengthToEnd() < child.getMinPathLengthToEnd())
                child = ch;
        }
        if (child == this)
            return null;
        else
            return  child;
    }

}
