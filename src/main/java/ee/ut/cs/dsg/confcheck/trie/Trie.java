package ee.ut.cs.dsg.confcheck.trie;


import at.unisalzburg.dbresearch.apted.node.Node;
import ee.ut.cs.dsg.confcheck.util.Utils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.functors.TruePredicate;

import java.util.*;

public class Trie {

    private final TrieNode root;
    private final List<TrieNode> leaves;
    private final int maxChildren;
    private int internalTraceIndex = 0;
    private int size = 0;
    private int numberOfEvents = 0;
    protected HashMap<Integer, String> traceIndexer;
    protected HashMap<Integer, Integer> levelBreadth;

    public Trie(int maxChildren) {
        this.maxChildren = Utils.nextPrime(maxChildren);
        root = new TrieNode("dummy", maxChildren, Integer.MAX_VALUE, Integer.MIN_VALUE, false, null);
        traceIndexer = new HashMap<>();
        leaves = new ArrayList<>();
        levelBreadth = new HashMap<>();
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public void addTrace(List<String> trace) {
        ++internalTraceIndex;
        addTrace(trace, internalTraceIndex);

    }

    public void addTrace(List<String> trace, int traceIndex) {
        TrieNode current = root;
        int minLengthToEnd = trace.size();
        if (minLengthToEnd > 0) {
            StringBuilder sb = new StringBuilder(trace.size());
            for (String event : trace) {
                current.addLinkedTraceIndex(traceIndex);
                TrieNode child = new TrieNode(event, maxChildren, minLengthToEnd - 1, minLengthToEnd - 1, minLengthToEnd - 1 == 0, current);
                TrieNode returned;
                returned = current.addChild(child);
                if (returned == child) // we added a new node to the trie
                {
                    size++;
                    updateTrieBreadth(returned);
                }
                current = returned;

                minLengthToEnd--;
                sb.append(event);
                if (returned.isEndOfTrace())
                    leaves.add(returned);
            }
            current.addLinkedTraceIndex(traceIndex);
            numberOfEvents += sb.length();
            traceIndexer.put(traceIndex, sb.toString());
        }


    }

    public TrieNode getRoot() {
        return root;
    }

    public String toString() {

        return root.toString();


    }

    public String getTrace(int index) {
        return traceIndexer.get(index);
    }

    public void printTraces() {
//        StringBuilder result = new StringBuilder();
//        TrieNode current;
//        for (TrieNode  leaf: leaves)
//        {
//            current = leaf;
//            result = new StringBuilder();
//            while (current != root)
//            {
//                result.append(current.getContent()+",");
//                current = current.getParent();
//            }
//
//            System.out.println(result.reverse().toString());
//        }
        for (String s : traceIndexer.values())
            System.out.println(s);
    }

    /**
     * This method finds the deepest node in the trie that provides the longest prefix match to the trace.
     * If there is no match at all, the method returns null.
     *
     * @param trace is a list of strings that define the trace to search a match for
     * @return a trie node
     */
    public TrieNode match(List<String> trace, TrieNode startFromThisNode) {
        TrieNode current = startFromThisNode;
        TrieNode result;
        int size = trace.size();
        int lengthDifference = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++)
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

    public TrieNode match(List<String> trace) {
        return match(trace, root);
    }

    public int getMaxTraceLength() {
        int maxLength = leaves.stream().map(node -> node.getLevel()).reduce(Integer.MIN_VALUE, (minSoFar, element) -> Math.max(minSoFar, element));
        return maxLength;
    }

    public int getMinTraceLength() {
        int minLength = leaves.stream().map(node -> node.getLevel()).reduce(Integer.MAX_VALUE, (minSoFar, element) -> Math.min(minSoFar, element));
        return minLength;
    }

    public int getAvgTraceLength() {
        int sumlength = leaves.stream().map(node -> node.getLevel()).reduce(0, (subtotal, element) -> subtotal + element);


        return sumlength / leaves.size();
    }

    public int getSize() {
        return size;
    }

    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    private void updateTrieBreadth(TrieNode nd)
    {
        if (levelBreadth.keySet().contains(nd.getLevel()))
        {
            levelBreadth.put(nd.getLevel(), levelBreadth.get(nd.getLevel())+1);
        }
        else
            levelBreadth.put(nd.getLevel(),1);
    }
    public void printTrieShape()
    {
        int keyWithMaxBreadth = 0;
        int largestBreadth = 1;

        for (Integer key : levelBreadth.keySet())
        {
            Integer val = levelBreadth.get(key);
//            System.out.println(String.format("Level %d has breadth %d", key,val));
            if (val > largestBreadth)
            {
                keyWithMaxBreadth = key;
                largestBreadth = val;
            }
        }

        int longestPathLength = root.getMaxPathLengthToEnd();

        double ratio = ((double) largestBreadth)/longestPathLength;
        System.out.println(String.format("Trie shape ratio is %f", ratio));
        System.out.println(String.format("Largest breadth is %d", largestBreadth));
        System.out.println(String.format("Level with largest breadth is %d", keyWithMaxBreadth));
        System.out.println(String.format("Trie depth is %d", longestPathLength));
        System.out.println(String.format("Shortest trace is of length %d", root.getMinPathLengthToEnd()));
    }
    public Node<TrieNode> getAPTEDTree() {

        Queue<TrieNode> nextParents = new LinkedList<>();
        Queue<Node<TrieNode>> nextTreeParents = new LinkedList<>();

        Node<TrieNode> aptedTreeRoot = new Node<>(this.getRoot());
        nextParents.offer(this.getRoot());
        nextTreeParents.offer(aptedTreeRoot);

        Node<TrieNode> currentTree;// = aptedTreeRoot;
        TrieNode current;
//
        while (nextParents.size() > 0)
        {
            current = nextParents.poll();
            currentTree = nextTreeParents.poll();
            for (TrieNode child: current.getAllChildren()) {
                nextParents.offer(child);
                Node<TrieNode> childNode =new Node<>(child);
                currentTree.addChild(childNode);
                nextTreeParents.offer(childNode);
            }

        }


        return aptedTreeRoot;
    }

    public List<TrieNode> getLeaves() {
        List<TrieNode> result = new ArrayList<>();
        result.addAll(leaves);
        return result;
    }
}
