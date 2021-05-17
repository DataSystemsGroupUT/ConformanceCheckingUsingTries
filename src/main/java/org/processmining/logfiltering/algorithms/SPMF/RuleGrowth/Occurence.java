package org.processmining.logfiltering.algorithms.SPMF.RuleGrowth;

/**
 * This class represent the first and last occurences of an itemset in a sequence, as defined 
 * in the RuleGrowth algorithm.
 * 
 * @see AlgoRULEGROWTH
 * @see Sequence
 * @see SequenceDatabase
 * @author Philippe Fournier-Viger
 */
public class Occurence {
	/** the first occurence <br/>
	//  e.g.   1 means that the occurence starts at the second itemset of the sequence <br/>
	//         2 means that the occurence starts at the third itemset of the sequence */
	public short firstItemset;
	/** the last occurence */
	public short lastItemset;
	
	/**
	 * Constructor
	 * @param firstItemset  the first occurence 
	 * @param lastItemset   the last occurence 
	 */
	public Occurence(short firstItemset, short lastItemset){
		this.firstItemset = firstItemset;
		this.lastItemset = lastItemset;
	}
}
