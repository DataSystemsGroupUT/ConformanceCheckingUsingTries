package ee.ut.cs.dsg.confcheck.trie;

import java.util.ArrayList;
import java.util.List;

public class TrieNode {

    private String content; // This is a numerical representation of the activity label. Should be part of a lookup table
    private int maxChildren;
    private int minPathLengthToEnd;
    private TrieNode parent;
    private TrieNode[] children;
    private boolean isEndOfTrace;

    public TrieNode(String content, int maxChildren, int minPathLengthToEnd, boolean isEndOfTrace, TrieNode parent)
    {
        this.content = content;
        this.children = new TrieNode[maxChildren];
        this.minPathLengthToEnd = minPathLengthToEnd;
        this.parent = parent;
        this.maxChildren = maxChildren;
        this.isEndOfTrace = isEndOfTrace;
    }
    public String getContent()
    {
        return  content;
    }

    public int getMinPathLengthToEnd() {
        return minPathLengthToEnd;
    }

    public TrieNode getParent()
    {
        return parent;
    }
    public TrieNode getChild(String label)
    {

        return children[label.hashCode()%maxChildren];
    }

    public boolean isEndOfTrace() {
        return isEndOfTrace;
    }

    public TrieNode getChildWithLeastPath(int pathLength)
    {
        int minPath = pathLength;
        TrieNode child = null;
        for (TrieNode ch : children)
            if(ch != null)
            {
                if (ch.getMinPathLengthToEnd() < minPath)
                    child = ch;
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
    public TrieNode addChild(TrieNode child)
    {
//        System.out.println("Hash code "+child.getContent().hashCode());
        if (children[child.getContent().hashCode()%maxChildren] == null) {
            children[child.getContent().hashCode() % maxChildren] = child;
            child.parent = this;
        }
        this.minPathLengthToEnd = Math.min(this.minPathLengthToEnd, child.getMinPathLengthToEnd()+1);
        return children[child.getContent().hashCode() % maxChildren];
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(" Node(content:"+this.content+", minPath:"+minPathLengthToEnd+") Children(");
        for (TrieNode child : children)
            if (child != null)
                result.append(child.toString());
        result.append(")");
        return result.toString();
    }

}
