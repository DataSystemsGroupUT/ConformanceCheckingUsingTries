package org.processmining.logfiltering.algorithms.SPMF.PatternMining;


/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/


import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a FPTree node as used by the FPGrowth algorithm.
 *
 * @see FPTree
 * @see Itemset
 * @see AlgoFPGrowth
 * @author Philippe Fournier-Viger
 */
public class FPNode {
	public int itemID = -1;  // item id
	public int counter = 1;  // frequency counter  (a.k.a. support)
	public List<Integer> traces= new ArrayList<Integer>();
	// the parent node of that node or null if it is the root
	public FPNode parent = null; 
	// the child nodes of that node
	public List<FPNode> childs = new ArrayList<FPNode>();
	
	public FPNode nodeLink = null; // link to next node with the same item id (for the header table).
	
	/**
	 * constructor
	 */
	FPNode(){
		
	}

	public FPNode(int i) {
		// TODO Auto-generated constructor stub
		this.traces.add(i);
	}

	/**
	 * Return the immediate child of this node having a given ID.
	 * If there is no such child, return null;
	 */
	FPNode getChildWithID(int id) {
		// for each child node
		for(FPNode child : childs){
			// if the id is the one that we are looking for
			if(child.itemID == id){
				// return that node
				return child;
			}
		}
		// if not found, return null
		return null;
	}

	/**
	 * Method for getting a string representation of this tree 
	 * (to be used for debugging purposes).
	 * @param an indentation
	 * @return a string
	 */
	public String toString(String indent) {
		StringBuilder output = new StringBuilder();
		output.append(""+ itemID);
		output.append(" (count="+ counter);
		output.append(")\n");
		String newIndent = indent + "   ";
		for (FPNode child : childs) {
			output.append(newIndent+ child.toString(newIndent));
		}
		return output.toString();
	}
	
	public String toString() {
		return ""+itemID;
	}
}
