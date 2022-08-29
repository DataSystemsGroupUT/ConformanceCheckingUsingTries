package ee.ut.cs.dsg.confcheck.cost;

import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;

public class ConformanceAPTEDStringCostModel implements CostModel<StringNodeData> {
    @Override
    public float del(Node<StringNodeData> node) {
        return 1;
    }

    @Override
    public float ins(Node<StringNodeData> node) {
        return 1;
    }

    @Override
    public float ren(Node<StringNodeData> node, Node<StringNodeData> node1) {
        String[] s1 = node.getNodeData().getLabel().split(",");
        String[] s2 = node1.getNodeData().getLabel().split(",");


        if (s1[0].trim().equals(s2[0].trim())) {
            if (s1.length == 1 && s2.length == 1)
                return 0;
            else if (s1.length == 2 && s2.length == 2)
                return Math.abs(Integer.valueOf(s1[1].trim()) - Integer.valueOf(s2[1].trim()));
        }
//        else if (s1.length==2 && s2.length ==2 && Integer.valueOf(s1[1].trim()) == Integer.valueOf(s2[1].trim())) // the two nodes are on the same level but have different labels
//            return 2;
        return Integer.MAX_VALUE;
    }
}