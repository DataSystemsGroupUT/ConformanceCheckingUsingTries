package ee.ut.cs.dsg.confcheck.trie;

import java.util.HashMap;
import java.util.List;

public class Trie {

    private final TrieNode root;
    private final int maxChildren;
    private int internalTraceIndex=0;

    protected HashMap<Integer,String> traceIndexer;

    public Trie(int maxChildren)
    {
        this.maxChildren = maxChildren;
        root = new TrieNode("dummy", maxChildren, Integer.MAX_VALUE, Integer.MIN_VALUE, false,null);
        traceIndexer = new HashMap<>();
    }
    public int getMaxChildren()
    {
        return maxChildren;
    }
    public void addTrace(List<String> trace)
    {
        ++internalTraceIndex;
        addTrace(trace, internalTraceIndex);

    }
    public void addTrace(List<String> trace, int traceIndex)
    {
        TrieNode current = root;
        int minLengthToEnd = trace.size();
        if (minLengthToEnd > 0)
        {
            StringBuilder sb = new StringBuilder(trace.size());
            for (String event : trace)
            {
                current.addLinkedTraceIndex(traceIndex);
                TrieNode child = new TrieNode(event,maxChildren,minLengthToEnd-1, minLengthToEnd-1, minLengthToEnd-1==0? true: false,current);
                child = current.addChild(child);
                current = child;
                minLengthToEnd--;
                sb.append(event);
            }
            current.addLinkedTraceIndex(traceIndex);
            traceIndexer.put(traceIndex, sb.toString());
        }

    }
    public TrieNode getRoot()
    {
        return root;
    }
    public String toString()
    {

            return root.toString();


    }
    public String getTrace(int index)
    {
        return traceIndexer.get(index);
    }

    /**
     * This method finds the deepest node in the trie that provides the longest prefix match to the trace.
     * If there is no match at all, the method returns null.
     * @param trace is a list of strings that define the trace to search a match for
     * @return a trie node
     */
    public TrieNode match(List<String> trace, TrieNode startFromThisNode)
    {
        TrieNode current = startFromThisNode;
        TrieNode result;
        int size = trace.size();
        int lengthDifference = Integer.MAX_VALUE;
        for(int i = 0; i < size; i++)
//        for (String event : trace)
        {
            result = current.getChild(trace.get(i));
            // result = current.getChildWithLeastPathLengthDifference(trace.get(i), size - i);

            if (result == null && current == startFromThisNode)
                return null;
            else if (result == null)
                return current;
            else {
                TrieNode result2 = result;
                //result2 = result.getChildWithLeastPathLengthDifference(size-(i+1));

//                if (Math.abs(result2.getMinPathLengthToEnd() - (size - (i+1))) <= lengthDifference)
                //               {
                // we still have a promising direction
                current = result;
//                    lengthDifference = Math.abs(result.getMinPathLengthToEnd() - (size - (i+1)));
//                }
//                else
//                    return current.getParent();


            }
        }
        return current;
    }
    public TrieNode match(List<String> trace)
    {
        return match(trace, root);
    }

}
