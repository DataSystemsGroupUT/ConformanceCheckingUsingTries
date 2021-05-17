package org.processmining.logfiltering.Juan.trie;

import it.unimi.dsi.fastutil.chars.Char2ObjectAVLTreeMap;

public class Node{

    private char c;
    // to count how many words starting with prefix
    private int count;
    private boolean isVisited; 
    private boolean isLeaf;
	private boolean isRoot;
    private Node parent;
    //Map<Character, Node> children = new HashMap<Character, Node>();
    Char2ObjectAVLTreeMap<Node> children = new Char2ObjectAVLTreeMap<Node>();
  
    public Node() {
        setCount(0);
        setVisited(false);
    }
     
    public Node(char c){
    	this();
        setC(c);
    }

	public char getC() {
		return c;
	}

	public void setC(char c) {
		this.c = c;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isVisited() {
		return isVisited;
	}

	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

}
