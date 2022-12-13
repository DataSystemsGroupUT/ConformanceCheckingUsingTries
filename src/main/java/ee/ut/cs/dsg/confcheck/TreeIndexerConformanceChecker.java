package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import it.unimi.dsi.fastutil.Hash;
import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class TreeIndexerConformanceChecker extends ApproximateConformanceChecker{
    private final HashMap<Integer, TrieNode> modelNodesIndexer ;
    private final HashMap<Character,int[]> trieLabelHistogram;
    private final HashMap<Character, List<TrieNode>> treeIndexer;
    private final int labelLookAheadWindow =5;
    private final int suffixLookAheadWindow =5;
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

    private List<TrieNode> getDeepestCoveringNodes(List<Mapping> mapping, HashMap<Integer, TrieNode> preOrderIndexer)
    {
        List<Integer> mappedIndexes = mapping.stream().map(m -> m.position).sorted(Integer::compare).collect(Collectors.toList());
        List<Integer> processed = new ArrayList<>();
        List<TrieNode> result = new ArrayList<>();
        for (int i = mappedIndexes.size()-1; i > 0; i--)
        {
            if (processed.contains(i))
                continue;
            TrieNode nd = preOrderIndexer.get(i);
            processed.add(nd.getPreOrderIndex());
            while (nd.getParent() != null)
            {
                nd = nd.getParent();
                if (mappedIndexes.contains(nd.getPreOrderIndex()))
                    processed.add(nd.getPreOrderIndex());
            }
            result.add(preOrderIndexer.get(i));
        }
        return result;
    }

    public List<Alignment> check (Trie logBehavior)
    {
        HashMap<Integer, TrieNode> logNodeIndexer = new HashMap<>();
        HashMap<Integer, TrieNode> postOrderLogNodeIndexer = new HashMap<>();
        logBehavior.getRoot().computeOrUpdatePreOrderIndex(logNodeIndexer);
        logBehavior.getRoot().computeOrUpdatePostOrderIndex(postOrderLogNodeIndexer);

        HashMap<TrieNode, TrieNode> mappings  = findMapping(logNodeIndexer);
        List<Alignment> alignments = new ArrayList<>();

        if (mappings.size() ==0)
        {
            Alignment alg = new Alignment();
            TrieNode nd = modelTrie.getRoot().getChildOnShortestPathToTheEnd();
            while (nd != null) {
                alg.appendMove(new Move(">>", nd.getContent(), modelMoveCost));
                if (nd.isEndOfTrace())
                    break;
                nd = nd.getChildOnShortestPathToTheEnd();
            }

            for (int i :logBehavior.getRoot().getLinkedTraces()) {

                Alignment alg2 = new Alignment(alg);
                for (char event : logBehavior.getTrace(i).toCharArray()) {
                    alg2.appendMove(new Move(String.valueOf(event), ">>", logMoveCost));
                }
                alignments.add(alg2);
            }
        }
        else
        {
            List<Integer> mappedIndexes = mappings.keySet().stream().map(m -> m.getPreOrderIndex()).collect(Collectors.toList());
            List<TrieNode> leaves = logBehavior.getLeaves();
            for (TrieNode nd: leaves)
            {
                Stack<Move> backward = new Stack<>();
                Alignment alg = new Alignment();
                TrieNode node = nd;
                while (!mappedIndexes.contains(node.getPreOrderIndex()))
                {
                    Move move = new Move(node.getContent(),">>", logMoveCost);
                    backward.push(move);
                    node = node.getParent();
                }
                // we have reached a node that has a match

                TrieNode modelNode = mappings.get(node).getChildOnShortestPathToTheEnd();
                // add all model moves
                List<Move> moves = new ArrayList<>();

                while (modelNode != null) {
                    moves.add(new Move(">>", modelNode.getContent(), modelMoveCost));
                    if (modelNode.isEndOfTrace())
                        break;
                    modelNode = modelNode.getChildOnShortestPathToTheEnd();
                }
                for (int i = moves.size()-1; i >=0; i--)
                    backward.push(moves.get(i));
                backward.push(new Move(node.getContent(), node.getContent(),0));
                //start traversing upward in both trees
                modelNode = mappings.get(node).getParent();
                node= node.getParent();
                while(!modelNode.getContent().equals("dummy") &&!node.getContent().equals("dummy") )
                {

                    // check if the log trie node has a mapping that is equal to
                    if (mappings.get(node) != null && mappings.get(node).equals(modelNode))
                    {
                        backward.push(new Move (node.getContent(), node.getContent(),0));
                        modelNode = modelNode.getParent();
                        node = node.getParent();
                    }
                    else if (mappings.get(node) == null) // this is not mapped and this is a log move
                    {
                        backward.push(new Move(node.getContent(),">>", logMoveCost));
                        node = node.getParent();
                    }
                    else if (mappings.get(node) != null)// this is mapped to another node
                    {
                        while(!mappings.get(node).equals(modelNode))
                        {
                            backward.push(new Move(">>", modelNode.getContent(), modelMoveCost));
                            modelNode = modelNode.getParent();
                        }
                    }
                }
                while (!modelNode.getContent().equals("dummy")) // we have to complete with model moves
                {
                    backward.push(new Move(">>", modelNode.getContent(), modelMoveCost));
                    modelNode = modelNode.getParent();
                }

                while (!node.getContent().equals("dummy")) // we have to complete with model moves
                {
                    backward.push(new Move(node.getContent(),">>", logMoveCost));
                    node = node.getParent();
                }

                while (!backward.isEmpty())
                    alg.appendMove(backward.pop());

               for (int i : nd.getLinkedTraces())
                   alignments.add(alg);
            }

        }

        return  alignments;
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

                // handle log moves before the first binding
                for (int i = mapping.get(0).position-1; i >= 0; i--)
                    backward.push(new Move( trace.get(i), ">>", logMoveCost));

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

    private HashMap<TrieNode, TrieNode> findMapping(HashMap<Integer, TrieNode> logNodeIndexer)
    {
        //Key is the log trie node , value is the model trie node
        HashMap<TrieNode,TrieNode> mapping = new HashMap<>();
        Set<TrieNode> mappedTreeNodes = new HashSet<>();
        HashMap<Pair<TrieNode,TrieNode>, Integer> visitedPairs = new HashMap<>();
        int max = logNodeIndexer.keySet().stream().max( Integer::compare).get();
        // let's directly add the roots mapping
        mapping.put(logNodeIndexer.get(1), modelTrie.getRoot());
        for (int i =2; i<= max ; i++)
        {
            List<TrieNode> indexContent = treeIndexer.get(logNodeIndexer.get(i).getContent().charAt(0));
            int positionFrequency=0;
            int[] hist = trieLabelHistogram.get(logNodeIndexer.get(i).getContent().charAt(0));
            if (hist != null)
                for (int x = logNodeIndexer.get(i).getLevel()-1 ; positionFrequency ==0 && x <= logNodeIndexer.get(i).getLevel()+labelLookAheadWindow && x < modelTrie.getRoot().getMaxPathLengthToEnd();x++ )
                    positionFrequency+= hist[x];
            if (positionFrequency > 0)
            {
                // look for a position match
                int minCost = Integer.MAX_VALUE;
                TrieNode chosen=null;
                ArrayList<TrieNode> filtered;
                if (mapping.size() >0) {
                    int finalCurrentPosition = logNodeIndexer.get(i).getLevel();
                    List<TrieNode> ancestors = logNodeIndexer.get(i).getAncestors();//.stream().map(a -> a.getPostOrderIndex()).collect(Collectors.toList());
                    List<TrieNode> mappedKeyNodes = mapping.keySet().stream().collect(Collectors.toList());
                    List<TrieNode> common = ancestors.stream().filter(mappedKeyNodes::contains).collect(Collectors.toList());
                    List<TrieNode> toKeep = new ArrayList<>();
                    for (TrieNode n1: common) {
                        boolean found = false;
                        for (TrieNode n2 : common) {
                            if (n2.getAncestors().contains(n1))
                            {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                        {
                            toKeep.add(n1);
                        }
                    }

                    List<Integer> mappedValues = new ArrayList<>();
                    toKeep.stream().forEach( x -> mappedValues.add(mapping.get(x).getPostOrderIndex()));
                    filtered= (ArrayList<TrieNode>) indexContent.stream()
                            .filter(nd -> !mappedTreeNodes.contains(nd)
                                    && hasAncestor(nd.getPostOrderIndex(), mappedValues , modelNodesIndexer)
                                    && Math.abs(finalCurrentPosition  - nd.getLevel()) <= labelLookAheadWindow
                            )
                            .collect(Collectors.toList());
                }
                else
                    filtered = (ArrayList<TrieNode>) indexContent;
                for (TrieNode nd: filtered)
                {

                    int levelAbs = Math.abs((logNodeIndexer.get(i).getLevel()) - nd.getLevel());// just used in case of skipping a match for an event that does not have enough support


                    int minPathAbs=0;


                    minPathAbs = nd.findInSubtree2(logNodeIndexer.get(i), suffixLookAheadWindow);//, visitedPairs);


                    if (levelAbs+minPathAbs <= minCost)// || (levelAbs+minPathAbs == minCost && chosen != null && nd.getLevel() < chosen.getLevel() ) )
                    {
                        chosen = nd;
                        minCost = levelAbs+minPathAbs;
//                        System.out.println("Found a better match");
                    }
//                    else if (levelAbs+minPathAbs==minCost && chosen != null
////                            // && Math.abs(((traceSize -(i+1)) - nd.getMinPathLengthToEnd())) < Math.abs(((traceSize -(i+1)) - chosen.getMinPathLengthToEnd()))
////                            &&  Math.min(Math.abs(nd.getMinPathLengthToEnd() - (traceSize -(i+1))), Math.abs(nd.getMaxPathLengthToEnd() - (traceSize -(i+1)))) <
////                            Math.min(Math.abs(chosen.getMinPathLengthToEnd() - (traceSize -(i+1))), Math.abs(chosen.getMaxPathLengthToEnd() - (traceSize -(i+1))))
//                    )
//                    {
//                        System.out.println("Another node with the same cost was found. Current:" +chosen+" Other:"+ nd);
//                        if (nd.getLevel() <  chosen.getLevel())
//                        {
//                            System.out.println("Replacing current with other");
//                            chosen = nd;
//                        }
//                    }
//

                }

                if (chosen != null)
                {
                    mapping.put(logNodeIndexer.get(i), chosen);
                }
            }
        }
        return mapping;
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

    private boolean hasAncestor(int childPostOrderIndex, List<Integer> ParentPostOrderIndex, HashMap<Integer, TrieNode> nodeIndexer)
    {
        for (int i: ParentPostOrderIndex) {
            if (isAncestor(childPostOrderIndex, i, nodeIndexer))
                return true;
        }
        return false;


    }

}
