package ee.ut.cs.dsg.confcheck.cost;

import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

public class ConformanceAPTEDCostModel implements CostModel<TrieNode> {
    @Override
    public float del(Node<TrieNode> node) {
        return 1;
    }

    @Override
    public float ins(Node<TrieNode> node) {
        return 1;
    }

    @Override
    public float ren(Node<TrieNode> node, Node<TrieNode> node1) {
        if (node.getNodeData().getContent().equals(node1.getNodeData().getContent()))
//            return 0;
            return Math.abs(node.getNodeData().getLevel()-node1.getNodeData().getLevel());
        return 1000;
    }
}
