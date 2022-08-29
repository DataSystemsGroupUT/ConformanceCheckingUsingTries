package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.trie.TrieNode;

public class Mapping {
    public TrieNode node;
    public int position;

    public Mapping(TrieNode n, int p)
    {
        node = n;
        position = p;
    }
}
