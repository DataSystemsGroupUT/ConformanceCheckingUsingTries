package ee.ut.cs.dsg.confcheck.trie;

import java.util.List;

public class Trie {

    private TrieNode root;
    private int maxChildren;

    public Trie(int maxChildren)
    {
        this.maxChildren = maxChildren;
        root = new TrieNode("dummy", maxChildren, Integer.MAX_VALUE, false,null);
    }
    public void addTrace(List<String> trace)
    {
        TrieNode current = root;
        int minLengthToEnd = trace.size();
        if (minLengthToEnd > 0)
        {

            for (String event : trace)
            {
                TrieNode child = new TrieNode(event,maxChildren,minLengthToEnd-1, minLengthToEnd-1==0? true: false,current);
                child = current.addChild(child);
                current = child;
                minLengthToEnd--;
            }
        }

    }
    public TrieNode getRoot()
    {
        return root;
    }
    public String toString()
    {
        if (root != null)
        {
            return root.toString();

        }
        return null;
    }
}
