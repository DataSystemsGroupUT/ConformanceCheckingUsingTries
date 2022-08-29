package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

import java.util.*;
import java.util.stream.Collectors;

public class TreeIndexerConformanceChecker extends ApproximateConformanceChecker{
    private final HashMap<Integer, TrieNode> modelNodesIndexer ;
    private final HashMap<Character,int[]> trieLabelHistogram;
    private final HashMap<Character, List<TrieNode>> treeIndexer;
    private final int labelLookAheadWindow = 4;
    private final int suffixLookAheadWindow =100;
    private final double support = 0.0001;
    private boolean computeCostOnly=false;
    public TreeIndexerConformanceChecker(Trie modelTrie, int logCost, int modelCost) {
        super(modelTrie, logCost, modelCost);
        modelNodesIndexer = new HashMap<>();
        modelTrie.getRoot().computeOrUpdatePostOrderIndex(modelNodesIndexer);
        trieLabelHistogram = new HashMap<>();
        treeIndexer = new HashMap<>();
        for (int key: modelNodesIndexer.keySet())
        {
            TrieNode node = modelNodesIndexer.get(key);
            if (node.getContent().equalsIgnoreCase("dummy"))
                continue;
            // build the label histogram
            int[] hist = trieLabelHistogram.get(node.getContent().charAt(0));
            if (hist == null) // this is the first time to add a histogram for this label
            {
                hist = new int[modelTrie.getRoot().getMaxPathLengthToEnd()];
                hist[node.getLevel()-1]=1;
            }
            else
            {
                hist[node.getLevel()-1]++;

            }
            trieLabelHistogram.put(node.getContent().charAt(0), hist);
            // build the index
            List<TrieNode> indexContent = treeIndexer.get(node.getContent().charAt(0));
            if (indexContent == null) // this is the first time to add a histogram for this label
            {
                indexContent = new ArrayList<>();

            }
            indexContent.add(node);
            treeIndexer.put(node.getContent().charAt(0), indexContent);
        }
        //loop over the tree indexer to order the values by level.
//        for (Character k: treeIndexer.keySet())
//        {
//            List<TrieNode> values = treeIndexer.get(k);
//            values.sort(new Comparator<TrieNode>() {
//                    @Override
//                    public int compare(TrieNode o1, TrieNode o2) {
//                        if (o1.getLevel() == o2.getLevel())
//                            return 0;
//                        if (o1.getLevel() < o2.getLevel())
//                            return -1;
//                        return 1;
//                    }
//                });
//            treeIndexer.put(k,values);
//        }
    }

    public void setComputeCostOnly(boolean value)
    {
        computeCostOnly = value;
    }
    @Override
    public Alignment check(List<String> trace) {
        traceSize = trace.size();
        List<Mapping> mapping = findMapping(trace);
        Alignment alg = new Alignment();

        if (!computeCostOnly) {
            if (mapping.size() == 0) // we couldn't find any node mapping
            {
                TrieNode nd = modelTrie.getRoot().getChildOnShortestPathToTheEnd();
                while (nd != null) {
                    alg.appendMove(new Move(">>", nd.getContent(), modelMoveCost));
                    if (nd.isEndOfTrace())
                        break;
                    nd = nd.getChildOnShortestPathToTheEnd();
                }
                for (String event : trace) {
                    alg.appendMove(new Move(event, ">>", logMoveCost));
                }

            } else {

                // check for model moves first
                TrieNode parent = mapping.get(0).node.getParent();
                Stack<Move> backward = new Stack<>();
                while (!parent.getContent().equalsIgnoreCase("dummy")) {

                    backward.push(new Move(">>", parent.getContent(), modelMoveCost));
                    parent = parent.getParent();
                }

                while (!backward.isEmpty())
                    alg.appendMove(backward.pop());

//            Mapping currentMapping = mapping.get(mapping.size()-1);

//            int tracePosition = currentMapping.position;
                for (int i = mapping.size() - 1; i > 0; i--) {

                    backward.push(new Move(mapping.get(i).node.getContent(), mapping.get(i).node.getContent(), 0));
                    parent = mapping.get(i).node.getParent();
                    //Handle model moves
                    for (int j = mapping.get(i).node.getLevel() - 1; j > mapping.get(i - 1).node.getLevel(); j--) {

                        backward.push(new Move(">>", parent.getContent(), modelMoveCost));
                    }
                    //Handle log moves
                    for (int j = mapping.get(i).position - 1; j > mapping.get(i - 1).position; j--) {

                        backward.push(new Move(trace.get(j), ">>", logMoveCost));
                    }
                }
                backward.push(new Move(mapping.get(0).node.getContent(), mapping.get(0).node.getContent(), 0));
                while (!backward.isEmpty())
                    alg.appendMove(backward.pop());


                //handle remaining log moves
                for (int j = mapping.get(mapping.size() - 1).position + 1; j < traceSize; j++) {
                    alg.appendMove(new Move(trace.get(j), ">>", logMoveCost));

                }
                //handle remaining model moves
                if (!mapping.get(mapping.size() - 1).node.isEndOfTrace()) {
                    TrieNode next = mapping.get(mapping.size() - 1).node.getChildOnShortestPathToTheEnd();
                    while (next != null) {
                        alg.appendMove(new Move(">>", next.getContent(), modelMoveCost));
                        next = next.getChildOnShortestPathToTheEnd();
                    }
                }


            }
        }
        else
        {

        }
        return alg;
    }

    private List<Mapping> findMapping(List<String> trace) {

        //We need to compute a histogram for the trace as well.
//        HashMap<Character,Integer> traceHistogram = new HashMap<>(trace.size());
//        for (int i =0; i < traceSize;i++)
//        {
//            Integer hist = traceHistogram.get(trace.get(i).charAt(0));
//            if (hist == null) // this is the first time to add a histogram for this label
//            {
//                hist = new Integer(1);
//
//            }
//            else
//            {
//                hist++;
//
//            }
//            traceHistogram.put(trace.get(i).charAt(0), hist);
//        }
        List<String> suffix =  new ArrayList<>();
        for (String elem: trace.subList(0, suffixLookAheadWindow > traceSize? traceSize: suffixLookAheadWindow))
            suffix.add(elem);
        List<Mapping> mapping = new ArrayList<>();
        Set<TrieNode> mappedTreeNodes = new HashSet<>();
        int currentPosition = -1;
        boolean computeSuffixCost = true;
        for (int i=0; i< traceSize; i++)
        {
            if (currentPosition == -1)
            {
                currentPosition = i;
            }
            List<TrieNode> indexContent = treeIndexer.get(trace.get(i).charAt(0));

//            int frequency = indexContent==null? 0: indexContent.size();
            int positionFrequency=0;
            int[] hist = trieLabelHistogram.get(trace.get(i).charAt(0));
            if (hist != null)
                for (int x = currentPosition; positionFrequency ==0 && x <= currentPosition+labelLookAheadWindow && x < modelTrie.getRoot().getMaxPathLengthToEnd();x++ )
                    positionFrequency+= hist[x];
//            Integer labelFrequency = traceHistogram.get(trace.get(i).charAt(0));




//            if (((double) positionFrequency)/frequency >= support)
//            if (positionFrequency >= labelFrequency.intValue())
            if (positionFrequency > 0)
            {
                // look for a position match
                int minCost = Integer.MAX_VALUE;
                TrieNode chosen=null;
                ArrayList<TrieNode> filtered;
                if (mapping.size() >0) {
                    int finalCurrentPosition = currentPosition;
                    filtered= (ArrayList<TrieNode>) indexContent.stream()
                        .filter(nd -> !mappedTreeNodes.contains(nd)
                                && isAncestor(nd.getPostOrderIndex(), mapping.get(mapping.size()-1).node.getPostOrderIndex(), modelNodesIndexer)
                                && Math.abs((finalCurrentPosition +1) - nd.getLevel()) <= labelLookAheadWindow
                        )
                        .collect(Collectors.toList());
                }
                else
                    filtered = (ArrayList<TrieNode>) indexContent;
//                Collections.sort(filtered,new Comparator<TrieNode>() {
//                    @Override
//                    public int compare(TrieNode o1, TrieNode o2) {
//                        if (o1.getLevel() == o2.getLevel())
//                            return 0;
//                        if (o1.getLevel() < o2.getLevel())
//                            return -1;
//                        return 1;
//                    }
//                });
                if (computeSuffixCost && suffix.size()> 0)
                    suffix.remove(0);
                if (computeSuffixCost && (i+ suffixLookAheadWindow)-1< traceSize && suffixLookAheadWindow> 0)
                    suffix.add(trace.get((suffixLookAheadWindow +i)-1));
                // try to add a dummy node to skip this match
                for (TrieNode nd: filtered)
                {
//                    if (mapping.stream().map(x -> x.node).collect(Collectors.toList()).contains(nd))// search among non-chosen nodes from before
//                    if(mappedTreeNodes.contains(nd))
//                        continue;
//                    if (mapping.size() > 0 && !isAncestor(nd.getPostOrderIndex(), mapping.get(mapping.size()-1).node.getPostOrderIndex(), modelNodesIndexer))
//                        continue;

                    int levelAbs = Math.abs((currentPosition+1) - nd.getLevel());// just used in case of skipping a match for an event that does not have enough support


                    int minPathAbs=0;
                    if (computeSuffixCost) {
                        minPathAbs = nd.findInSubtree(suffix, suffixLookAheadWindow);  //Math.abs(((traceSize -(i+1)) - nd.getMinPathLengthToEnd()));

                    }
                    if (levelAbs+minPathAbs < minCost)// || (levelAbs+minPathAbs == minCost && chosen != null && nd.getLevel() < chosen.getLevel() ) )
                    {
                        chosen = nd;
                        minCost = levelAbs+minPathAbs;
                    }
                    else if (levelAbs+minPathAbs==minCost && chosen != null
                           // && Math.abs(((traceSize -(i+1)) - nd.getMinPathLengthToEnd())) < Math.abs(((traceSize -(i+1)) - chosen.getMinPathLengthToEnd()))
                            &&  Math.min(Math.abs(nd.getMinPathLengthToEnd() - (traceSize -(i+1))), Math.abs(nd.getMaxPathLengthToEnd() - (traceSize -(i+1)))) <
                                Math.min(Math.abs(chosen.getMinPathLengthToEnd() - (traceSize -(i+1))), Math.abs(chosen.getMaxPathLengthToEnd() - (traceSize -(i+1))))
                    )

                    {
//                        System.out.println("Another node with the same cost");
                        chosen = nd;
                    }
                }
                //Check whether we need to compute suffix cost again
                if (suffixLookAheadWindow > (traceSize-i))// we no longer need to compute suffix cost.
                    computeSuffixCost=false;
                //sorting overhead takes more than linear search in the list, probably because the list is very small.
//                chosen = findBestMapping(filtered,currentPosition+1,(traceSize -(i+1)));
                if (chosen == null)// the decision is to skip the match for this event
                {
                    currentPosition=i;
                }
                else
                {
                    currentPosition++;
                    mapping.add(new Mapping(chosen, i));
                    mappedTreeNodes.add(chosen);
                }
            }


        }
        return mapping;
    }
    // we apply approximate binary search to find either a node at the same level or the nearest greater level
    // we assume that the input is sorted in ascending order
    private TrieNode findBestMapping(ArrayList<TrieNode> candidates, int level, int traceSuffixSize)
    {
        TrieNode chosen=null;
        if (candidates.size() > 0 && candidates.get(0).getLevel() == level)
            return candidates.get(0);
        int low =0; int high=candidates.size()-1;
        while (low <= high)
        {
            int mid = low +(high-low+1)/2;
            TrieNode candidate =candidates.get(mid);
            if (candidate.getLevel() < level)
            {
                low = mid+1;
            }
            else if (candidate.getLevel() > level)
            {
                chosen = candidate;
                high = mid-1;
            }
            else if (candidate.getLevel() == level)
            {
                chosen = candidate;
                break;
            }
        }
        return chosen;
    }
    @Override
    protected List<State> handleModelMoves(List<String> traceSuffix, State state, State candidateState) {
        return null;
    }

    @Override
    protected State handleLogMove(List<String> traceSuffix, State state, String event) {
        return null;
    }

    private boolean isAncestor(int childPostOrderIndex, int ParentPostOrderIndex, HashMap<Integer, TrieNode> nodeIndexer)
    {
        // a member in sub-tree must have a smaller index number as it will be visited first
        if (childPostOrderIndex > ParentPostOrderIndex)
            return false;
        // A node on the same level or less, i.e. closer to the root, cannot be in the subtree of the first index
        TrieNode node1 = nodeIndexer.get(childPostOrderIndex);
        TrieNode node2 = nodeIndexer.get(ParentPostOrderIndex);
        if (node2.getLevel() >= node1.getLevel())
            return false;
        //now the expensive test to traverse the path to the root from the node with index 2
        TrieNode node2Parent = node1.getParent();
        boolean found = false;
        while (node2Parent != null)
        {
            if (node2Parent.equals(node2)) {
                found = true;
                break;
            }
            node2Parent = node2Parent.getParent();
        }
        return found;
    }
}
