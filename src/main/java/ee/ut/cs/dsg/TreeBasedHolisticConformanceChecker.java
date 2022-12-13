package ee.ut.cs.dsg;

import at.unisalzburg.dbresearch.apted.costmodel.PerEditOperationStringNodeDataCostModel;
import at.unisalzburg.dbresearch.apted.distance.APTED;
import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import at.unisalzburg.dbresearch.apted.parser.BracketStringInputParser;
import ee.ut.cs.dsg.confcheck.cost.ConformanceAPTEDCostModel;
import ee.ut.cs.dsg.confcheck.cost.ConformanceAPTEDStringCostModel;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Utils;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import java.util.*;

public class TreeBasedHolisticConformanceChecker {

    private final Trie model;
    private Trie log;
    private final HashMap<Integer, TrieNode> modelNodesIndexer ;
    private HashMap<Integer, TrieNode> logNodesIndexer ;
    private final Node<StringNodeData> modelTree;
    private List<Integer> deletedModelNodes = new ArrayList<>(10);
    private Set<TrieNode> subTreesCompletelyMarkedForDeletion = new HashSet<>();
    private final static BracketStringInputParser parser = new BracketStringInputParser();
    private final static APTED<ConformanceAPTEDStringCostModel, StringNodeData> apted = new APTED<>(new ConformanceAPTEDStringCostModel());
//    private final static APTED<PerEditOperationStringNodeDataCostModel, StringNodeData> apted = new APTED<>(new PerEditOperationStringNodeDataCostModel(1,1,1));
    private final static APTED<ConformanceAPTEDCostModel, TrieNode> aptedTrieNode = new APTED<>(new ConformanceAPTEDCostModel());
    private enum nodeIsMarkedFor
    {
        insertion,
        deletion,
        keeping
    }
    public TreeBasedHolisticConformanceChecker(String proxyLog, String toCheckLog)
    {

        this(Utils.constructTrie(proxyLog), Utils.constructTrie(toCheckLog));

    }
    public TreeBasedHolisticConformanceChecker(Trie model, Trie log)
    {
        //TODO: Fix this by defining a copy constructor for the Trie class
        this.model = model;
        this.log = log;
        modelNodesIndexer = new HashMap<>();
        logNodesIndexer = new HashMap<>();
        model.getRoot().computeOrUpdatePostOrderIndex(modelNodesIndexer);

        modelTree = parser.fromString(this.model.getRoot().preOrderTraversalAPTED(true));
//        modelTree = model.getAPTEDTree();
    }


    public Interval computeAlignmentCost()
    {
        log.getRoot().computeOrUpdatePostOrderIndex(logNodesIndexer);
        return computeAlignmentCost(log);
    }

    public Interval computeAlignmentCost(String anotherLogFile)
    {
        log = Utils.constructTrie(anotherLogFile);
        log.getRoot().computeOrUpdatePostOrderIndex(logNodesIndexer);

        return computeAlignmentCost(log);
    }

    private Interval computeAlignmentCost(Trie log)
    {
        //We need to reset any marking for deletion from previous alignment computation
        for (int i: deletedModelNodes)
            modelNodesIndexer.get(i).setMarkedForDeletion(false);

        System.out.println(String.format("Model tree size %d", modelTree.getNodeCount()));

        Node<StringNodeData> logTree = parser.fromString(log.getRoot().preOrderTraversalAPTED(true));

//        Node<TrieNode> logTree = log.getAPTEDTree();
        System.out.println(String.format("Log tree size %d", logTree.getNodeCount()));
        long start = System.currentTimeMillis();
//        float upperBound = aptedTrieNode.computeEditDistance(modelTree, logTree);
        float upperBound = apted.computeEditDistance(modelTree, logTree);
        System.out.println(String.format("Original TED %f", upperBound));
//        List<int[]> mapping = aptedTrieNode.computeEditMapping();
        List<int[]> mapping = apted.computeEditMapping();
        System.out.println(String.format("Time to compute TED is %d", System.currentTimeMillis() - start));
        deletedModelNodes = new ArrayList<>(mapping.size());
        HashMap<TrieNode, TrieNode> nodeMapping = new HashMap<>();
        upperBound = 0;
        for ( int[] m: mapping )
        {
//            System.out.printf("\nLeft node %d is mapped to right node %d", m[0],m[1]);

            if (m[0] == 0 || m[1]==0)
                upperBound++;
//            else {
//                int val = modelNodesIndexer.get(m[0]).getContent().equals(logNodesIndexer.get(m[1]).getContent()) ? 0 : 2;
////                System.out.printf(" %d", val);
//                upperBound += val;
//            }
            if (m[1]==0)
                deletedModelNodes.add(m[0]);
            else if (m[0] != 0)
                nodeMapping.put(modelNodesIndexer.get(m[0]), logNodesIndexer.get(m[1]));
        }
//        System.out.print("\n");
        for (int i : deletedModelNodes)
        {
            modelNodesIndexer.get(i).setMarkedForDeletion(true);
        }

        List<Integer> highestRoots = getHighestRootsWithCompletelyDeletedSubTrees(deletedModelNodes,modelNodesIndexer);
        List<Integer> nodesWithDeletedParent = filterForNodesWhoseParentIs(highestRoots,modelNodesIndexer, nodeIsMarkedFor.deletion);
        List<Integer> nodesWithKeptParent = filterForNodesWhoseParentIs(highestRoots,modelNodesIndexer, nodeIsMarkedFor.keeping);
//        System.out.println(highestRoots);
        int error = 0;
        for (int index: nodesWithDeletedParent)
        {
            error+=modelNodesIndexer.get(index).getNodeCount();
        }
        Set<TrieNode> parents = new HashSet<>(nodesWithKeptParent.size());
        for (int index: nodesWithKeptParent)
        {
            //System.out.println(modelNodesIndexer.get(index).getParent());
            //if (!parents.contains(modelNodesIndexer.get(index).getParent()))
                parents.add(modelNodesIndexer.get(index).getParent());
        }

        //We need to update the error for each kept parent node
        for (TrieNode parent: parents) {
            List<TrieNode> children = parent.getAllChildren();

            if (children.stream().allMatch(elem-> allSubTreeMarkedForDeletion(elem)))
            {
                // we need to find the child with the smallest sub-tree size
                TrieNode nodeWithSmallestSubtree =  children.stream().filter(elem -> allSubTreeMarkedForDeletion(elem))
                        .reduce(null, (accum, current) -> accum==null? current: current.getNodeCount() < accum.getNodeCount()? current:accum);

                error += children.stream().filter(elem -> !elem.equals(nodeWithSmallestSubtree) && allSubTreeMarkedForDeletion(elem))
                        .map(elem -> elem.getNodeCount()).reduce(0, (subtotal, elem) -> subtotal + elem);
                // We have to consider only the excess behavior
                error += nodeWithSmallestSubtree.getNodeCount() - (nodeWithSmallestSubtree.getMinPathLengthToEnd()+1);

            }
            else
            {
                error += children.stream().filter(elem -> allSubTreeMarkedForDeletion(elem) ).map(elem -> elem.getNodeCount()).reduce(0, (subtotal, elem)-> subtotal + elem);
            }
        }



        return new Interval((int) (upperBound - error), (int) upperBound);
    }

    private boolean isInMySubTree(int postOrderIndex1, int postOrderIndex2, HashMap<Integer, TrieNode> nodeIndexer)
    {
        // a member in sub-tree must have a smaller index number as it will be visited first
        if (postOrderIndex1< postOrderIndex2)
            return false;
        // A node on the same level or less, i.e. closer to the root, cannot be in the subtree of the first index
        TrieNode node1 = nodeIndexer.get(postOrderIndex1);
        TrieNode node2 = nodeIndexer.get(postOrderIndex2);
        if (node2.getLevel() <= node1.getLevel())
            return false;
        //now the expensive test to traverse the path to the root from the node with index 2
        TrieNode node2Parent = node2.getParent();
        boolean found = false;
        while (node2Parent != null)
        {
            if (node2Parent.equals(node1)) {
                found = true;
                break;
            }
            node2Parent = node2Parent.getParent();
        }
        return found;
    }
    private boolean allSubTreeMarkedForDeletion(TrieNode nd)
    {
        if (subTreesCompletelyMarkedForDeletion.contains(nd))
            return true;
        if (nd.getAllChildren().size()==0)
            return nd.isMarkedForDeletion();
        boolean partialResult = true;
        for (TrieNode child: nd.getAllChildren())
        {
            partialResult = partialResult && allSubTreeMarkedForDeletion(child);
        }
        return nd.isMarkedForDeletion() && partialResult;
    }
    private List<Integer> getHighestRootsWithCompletelyDeletedSubTrees(List<Integer> deletedNodes, HashMap<Integer, TrieNode> nodeIndexer)
    {
        deletedNodes.sort(Collections.reverseOrder());
        List<Integer> result = new ArrayList<>(deletedNodes.size());
        int nextI=0;
        for (int i = 0; i < deletedNodes.size();i = nextI+1) {
            if (allSubTreeMarkedForDeletion(nodeIndexer.get(deletedNodes.get(i)))) {
                subTreesCompletelyMarkedForDeletion.add(nodeIndexer.get(deletedNodes.get(i)));
                result.add(deletedNodes.get(i));
                boolean cycled = false;
                for (int j = i + 1; j < deletedNodes.size(); j++) {
                    cycled = true;
                    if (!isInMySubTree(deletedNodes.get(i), deletedNodes.get(j), nodeIndexer)) {

                        nextI = j - 1; // so that next time outer loop increments i
                        break;
                    } else
                        nextI = j;
                }
                if (!cycled)
                    break;
            }
            else
            {
                nextI=i;
            }
        }
        return result;
    }

    public List<Integer> filterForNodesWhoseParentIs(List<Integer> nodes, HashMap<Integer, TrieNode> nodeIndexer, nodeIsMarkedFor action )
    {
        List<Integer> result = new ArrayList<>(nodes.size());

        for (Integer i: nodes) {
            TrieNode node = nodeIndexer.get(i);
            if (node.getParent()!= null && node.getParent().isMarkedForDeletion() && action == nodeIsMarkedFor.deletion)
                result.add(i);
            else if (node.getParent()!= null && !node.getParent().isMarkedForDeletion() && action == nodeIsMarkedFor.keeping)
                result.add(i);
        }
        return result;
    }

}
