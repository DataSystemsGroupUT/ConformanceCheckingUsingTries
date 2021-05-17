package org.processmining.logfiltering.algorithms.SPMF.RuleGrowth;

/**
 * This class represents a sequential rule found by the CMDeo algorithm.
 * 
 * @see AlgoCMDeogun
 * @see Itemset
 * @author Philippe Fournier-Viger
 */
public class Rule {
	/** antecedent */
	private Itemset itemset1; 
	 /** consequent */
	private Itemset itemset2;
	/** absolute support */
	private int transactioncount; 
	
	/**
	 * Constructor
	 * @param itemset1  the left itemset
	 * @param itemset2  the right itemset
	 */
	public Rule(Itemset itemset1, Itemset itemset2){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
	}

	/**
	 * Get the antecedent of the rule (left itemset)
	 * @return an Itemset
	 */
	public Itemset getItemset1() {
		return itemset1;
	}

	/**
	 * Get the consequent of the rule (right itemset)
	 * @return an Itemset
	 */
	public Itemset getItemset2() {
		return itemset2;
	}
	
	/**
	 * Get the  support of this rule as a percentage.
	 * @param sequencecount the number of sequence in the sequence database
	 * @return the support as a double
	 */
	public double getAbsoluteSupport(int sequencecount) {
		return ((double)transactioncount) / ((double) sequencecount);
	}
	
	// Could these terms possibly be reversed?  Is this the absolute and above the relative?
	public int getRelativeSupport(){
		return transactioncount;
	}

	/**
	 * Get the confidence of this rule.
	 * @return a double value.
	 */
	public double getConfidence() {
		return ((double)transactioncount) / ((double) itemset1.getAbsoluteSupport());
	}

	/**
	 * Get the lift of this rule.
	 * @return a double value.
	 */
	public double getLift(int sequencecount) {

		//Verbose for review purposes
		double firstTerm = ((double) transactioncount) / ((double) sequencecount);
		double secondTerm = ((double) itemset2.getAbsoluteSupport()) / ((double) sequencecount);
		double thirdTerm = ((double) itemset1.getAbsoluteSupport()) / ((double) sequencecount);
		double lift = firstTerm / (secondTerm * thirdTerm);
		return lift;
	}

	/**
	 * Get the lift of this rule. - TEMP!!
	 * @return a String value.
	 */
	public String getLiftAsString(int sequencecount) {
//		if(itemset1.contains(2) && itemset2.contains(6)){
//			System.out.println();
//		}

		//Verbose for review purposes
//		double itemset2_rel_support = ((double) transactioncount) / ((double) sequencecount);
//		return (String)" itemset2  abs sup:" + itemset2.getAbsoluteSupport()  + "/  sequencecount:" + sequencecount +
//				"|itemset2_rel_support:" + itemset2_rel_support + "| lift:" + (getConfidence()) / (itemset2_rel_support);
		double firstTerm = ((double) transactioncount) / ((double) sequencecount);
		double secondTerm = ((double) itemset2.getAbsoluteSupport()) / ((double) sequencecount);
		double thirdTerm = ((double) itemset1.getAbsoluteSupport()) / ((double) sequencecount);
		double lift = firstTerm / (secondTerm * thirdTerm);
		return Double.toString(lift);
	}
	
	/**
	 * Print this rule to System.out
	 */
	public void print(){
		System.out.println(toString());
	}
	
	/**
	 * Get a string representation of this rule.
	 */
	public String toString(){
		return itemset1.toString() +  " ==> " + itemset2.toString();
	}

	/**
	 * Increase the support of this rule.
	 */
	void incrementTransactionCount() {
		this.transactioncount++;
	}

	/**
	 * Set the relative support of this rule.
	 * @param transactioncount the support as an integer.
	 */
	void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}
}
