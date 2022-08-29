package ee.ut.cs.dsg.confcheck.trie;

import at.unisalzburg.dbresearch.apted.node.Node;
import ee.ut.cs.dsg.confcheck.State;
import ee.ut.cs.dsg.confcheck.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TrieNode {

    private final String content; // This is a numerical representation of the activity label. Should be part of a lookup table
    private final int maxChildren;
    private int minPathLengthToEnd;
    private int maxPathLengthToEnd;
    private TrieNode parent;
    private final TrieNode[] children;
    private boolean isEndOfTrace;
    private final List<Integer> linkedTraces;
    private int level = 0;
    private int numChildren = 0;
    private int postOrderIndex=-1;
    private boolean markedForDeletion=false;

    public void setMarkedForDeletion(boolean markedForDeletion) {
        this.markedForDeletion = markedForDeletion;
    }

    public boolean isMarkedForDeletion()
    {
        return markedForDeletion;
    }

    public State getAlignmentState() {
        return alignmentState;
    }

    public void setAlignmentState(State alignmentState) {
        this.alignmentState = alignmentState;
    }

    private State alignmentState;

    private void setEndOfTrace(boolean isEndOfTrace) {
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
    public TrieNode(String content, int maxChildren, int minPathLengthToEnd, int maxPathLengthToEnd, boolean isEndOfTrace, TrieNode parent) {
        this.content = content;
        this.maxChildren = Utils.isPrime(maxChildren) ? maxChildren : Utils.nextPrime(maxChildren);
        //TODO: Change children type to HashMap?
        this.children = new TrieNode[this.maxChildren];
        this.minPathLengthToEnd = minPathLengthToEnd;
        this.maxPathLengthToEnd = maxPathLengthToEnd;
        this.parent = parent;
        if (parent != null)
            this.level = parent.getLevel() + 1;

        this.isEndOfTrace = isEndOfTrace;
        this.linkedTraces = new ArrayList<>();
    }

    public int getLevel() {
        return level;
    }

    public void addLinkedTraceIndex(int i) {
        this.linkedTraces.add(i);
    }

    public List<Integer> getLinkedTraces() {
        return linkedTraces;
    }

    public String getContent() {
        return content;
    }

    public int getMinPathLengthToEnd() {
        return minPathLengthToEnd;
    }

    public int getMaxPathLengthToEnd() {
        return maxPathLengthToEnd;
    }

    public TrieNode getParent() {
        return parent;
    }

    public TrieNode getChild(String label) {
        TrieNode result = children[Math.abs(label.hashCode()) % maxChildren];
        if (result != null && !result.getContent().equals(label)) {
            //System.err.println(String.format("Different labels with the same hash code %s and %s", result.getContent(), label));
            result = null;
        }
        return result;
    }

    public boolean isEndOfTrace() {
        return isEndOfTrace;
    }

    public TrieNode getChildWithLeastPathLengthDifference(int pathLength) {
        int minPath = pathLength;
        TrieNode child = null;
        int minDiff = Integer.MAX_VALUE;
        for (TrieNode ch : children)
            if (ch != null) {
//                if (!ch.getContent().equals(label))
//                    continue;
                int diff = Math.abs(ch.getMinPathLengthToEnd() - minPath);
                if (diff < minDiff) {
                    child = ch;
                    minDiff = diff;
                }

            }
        return child;
    }

    public List<TrieNode> getAllChildren() {
        List<TrieNode> result = new ArrayList<>();
        for (TrieNode nd : children)
            if (nd != null)
                result.add(nd);

        return result;
    }

    public List<TrieNode> getSiblingsOfChild(String label) {

        if (this.getChild(label)==null)
            return null;
        List<TrieNode> result = new ArrayList<>();
        for (TrieNode nd : children)
            if (nd != null && !nd.getContent().equals(label))
                result.add(nd);

        return result;
    }

    public boolean hasChildren() {
        return numChildren != 0;
    }

    public TrieNode addChild(TrieNode child) {
//        System.out.println("Hash code "+child.getContent().hashCode());
        if (children[Math.abs(child.getContent().hashCode()) % maxChildren] == null) {
            children[Math.abs(child.getContent().hashCode()) % maxChildren] = child;
            child.parent = this;
            numChildren++;
        } else {
            if (!children[Math.abs(child.getContent().hashCode()) % maxChildren].getContent().equals(child.getContent())) {
                System.err.println("A collision occurred");
            } else // just double check the is end of trace
            {
                if (child.isEndOfTrace())
                    children[Math.abs(child.getContent().hashCode()) % maxChildren].setEndOfTrace(child.isEndOfTrace());
            }
        }
        this.minPathLengthToEnd = Math.min(this.minPathLengthToEnd, child.getMinPathLengthToEnd() + 1);
        this.maxPathLengthToEnd = Math.max(this.maxPathLengthToEnd, child.getMaxPathLengthToEnd() + 1);
        // return children[child.getContent().hashCode() % maxChildren];
        return children[Math.abs(child.getContent().hashCode()) % maxChildren];
    }

    public String preOrderTraversal()
    {
        StringBuilder result = new StringBuilder();
        if (numChildren > 0) {
            result.append(this.content + "(");
            for (TrieNode child : children)
                if (child != null)
                    result.append(child.preOrderTraversal());
            result.append(") ");
        }
        else
            result.append(this.content+" ");
        return result.toString();
    }

    public String preOrderTraversalAPTED()
    {
        return preOrderTraversalAPTED(false);
    }
    public String preOrderTraversalAPTED(boolean encodeNodeLevel)
    {
        StringBuilder result = new StringBuilder();
        if (numChildren > 0) {
            result.append("{"+this.getContentReplaceCurlyBraces()+ (encodeNodeLevel? ","+this.level:"") );
            for (TrieNode child : children)
                if (child != null)
                    result.append(child.preOrderTraversalAPTED(encodeNodeLevel));
            result.append("}");
        }
        else
            result.append("{"+this.getContentReplaceCurlyBraces()+ (encodeNodeLevel? ","+this.level:"")+"}");
        return result.toString();
    }
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(" Node(content:" + this.content + ", minPath:" + minPathLengthToEnd + ", maxPath:" + maxPathLengthToEnd + ", isEndOfATrace:" + isEndOfTrace + ", Level"+ this.level+")");
//        for (TrieNode child : children)
//            if (child != null)
//                result.append(child);
//        result.append(")");
        return result.toString();
    }

    public boolean equals(Object other) {
        if (other instanceof TrieNode) {
            TrieNode otherNode = (TrieNode) other;

            if (this.getParent() == null && otherNode.getParent() != null)
                return false;
            if (this.getParent() != null && otherNode.getParent() == null)
                return false;
            if (this.getParent() == null && otherNode.getParent() == null)
                return (this.content.equals(otherNode.getContent()) && this.level == otherNode.getLevel());

            return (this.content.equals(otherNode.getContent()) && this.level == otherNode.getLevel() && this.getParent().equals(otherNode.getParent()));
        }
        return false;
    }

    public int hashCode() {
        return this.content.hashCode() + this.level;
    }

    public TrieNode getChildOnShortestPathToTheEnd() {
        TrieNode child;
        child = this;
        for (TrieNode ch : children) {
            if (ch == null)
                continue;
            if (ch.getMinPathLengthToEnd() < child.getMinPathLengthToEnd())
                child = ch;
        }
        if (child == this)
            return null;
        else
            return child;
    }
    public void computeOrUpdatePostOrderIndex(HashMap<Integer, TrieNode> nodeIndexer)
    {
        nodeIndexer.clear();
        labelWithPostOrderIndex(0, nodeIndexer);
    }
    private int labelWithPostOrderIndex(int highestIndex, HashMap<Integer, TrieNode> nodeIndexer)
    {

        for (int i =0; i < children.length;i++)
        {
            if (children[i]== null)
                continue;
            highestIndex = Math.max(highestIndex, children[i].labelWithPostOrderIndex(highestIndex, nodeIndexer));
        }
        this.postOrderIndex=++highestIndex;
        nodeIndexer.put(highestIndex, this);
        return highestIndex;
    }
    public int getNodeCount() {
        int sum = 1;

        TrieNode child;
        List<TrieNode> itr = new ArrayList<>();
        for (TrieNode n: children)
            if (n!=null)
                itr.add(n);
        for(Iterator var2 = itr.iterator(); var2.hasNext(); sum += child.getNodeCount()) {
            child = (TrieNode) var2.next();
        }

        return sum;
    }
    public int getPostOrderIndex()
    {
        return postOrderIndex;
    }

    private String getContentReplaceCurlyBraces() {
//        return this.content.equals("{")? "$$$": (this.content.equals("}")? "###":this.content);
        return this.content.replace("{", "$$$").replace("}","###").replace("\\","___");
    }

    public int findInSubtree(List<String> suffix, int window)
    {

        if (suffix == null || suffix.size()==0 || window ==0)
            return 0;
        int cost=0;
//        String event = suffix.remove(0);

        if ( this.getChild(suffix.get(0)) != null) //we found it
        {
            // let's look for the rest of the suffix
            return this.getChild(suffix.get(0)).findInSubtree(suffix.subList(1,suffix.size()), window -1);
        }
        else if (window==1)
            return 1; //we could not find a direct child matching the label of the event.
        else
        {
            cost++;
            int childCost=window;// we assume that there is no children at all.
            for (TrieNode child: this.getAllChildren())
            {
                childCost=Math.min(childCost, child.findInSubtree(suffix, window -1));
            }


            return cost+childCost;

        }
    }



}
