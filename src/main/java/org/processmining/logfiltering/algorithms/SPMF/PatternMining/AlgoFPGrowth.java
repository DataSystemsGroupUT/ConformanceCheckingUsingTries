package org.processmining.logfiltering.algorithms.SPMF.PatternMining;


/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.logfiltering.algorithms.SPMF.MemoryLogger;
/** 
* This is an implementation of the FPGROWTH algorithm (Han et al., 2004).
* FPGrowth is described here:
* <br/><br/>
* 
* Han, J., Pei, J., & Yin, Y. (2000, May). Mining frequent patterns without candidate generation. In ACM SIGMOD Record (Vol. 29, No. 2, pp. 1-12). ACM
* <br/><br/>
* 
* This is an optimized version that saves the result to a file
* or keep it into memory if no output path is provided
* by the user to the runAlgorithm method().
*
* @see FPTree
* @see Itemset
* @see Itemsets
* @author Philippe Fournier-Viger
*/
public class AlgoFPGrowth {

	// for statistics
	private long startTimestamp; // start time of the latest execution
	private long endTime; // end time of the latest execution
	private int transactionCount = 0; // transaction count in the database
	private int itemsetCount; // number of freq. itemsets found
	
	// parameter
	public int minSupportRelative;// the relative minimum support
	
	BufferedWriter writer = null; // object to write the output file
	
	// The  patterns that are found 
	// (if the user want to keep them into memory)
	protected Itemsets patterns = null;
		
	// This variable is used to determine the size of buffers to store itemsets.
	// A value of 50 is enough because it allows up to 2^50 patterns!
	final int BUFFERS_SIZE = 100;
	
	// buffer for storing the current itemset that is mined when performing mining
	// the idea is to always reuse the same buffer to reduce memory usage.
	private int[] itemsetBuffer = null;
	// another buffer for storing fpnodes in a single path of the tree
	private FPNode[] fpNodeTempBuffer = null;
	
	// This buffer is used to store an itemset that will be written to file
	// so that the algorithm can sort the itemset before it is output to file
	// (when the user choose to output result to file).
	private int[] itemsetOutputBuffer = null;
	
	/** maximum pattern length */
	private int maxPatternLength = 2;

	/**
	 * Constructor
	 * @param minSupport 
	 * @param i 
	 */
	public AlgoFPGrowth(int i, int minSupport) {
		this.maxPatternLength=i;
		this.minSupportRelative=minSupport;
	}

	public AlgoFPGrowth(int oddDistance) {
		this.maxPatternLength=oddDistance;
;
	}

	/**
	 * Method to run the FPGRowth algorithm.
	 * @param mapTraceList 
	 * @param mapTraces 
	 * @param input the path to an input file containing a transaction database.
	 * @param output the output file path for saving the result (if null, the result 
	 *        will be returned by the method instead of being saved).
	 * @param minsupp the minimum support threshold.
	 * @return the result if no output file path is provided.
	 * @throws IOException exception if error reading or writing files
	 */
	
	
	public Itemsets runAlgorithm(FPTree tree, int logSize, HashMap<Integer, Integer> mapSupport, HashMap<Integer, List<Integer>> mapTraceList, List<Integer> mapTraces) throws FileNotFoundException, IOException {
		startTimestamp = System.currentTimeMillis();
		// number of itemsets found
		itemsetCount = 0;
		this.transactionCount=logSize;
		//initialize tool to record memory usage
		MemoryLogger.getInstance().reset();
		MemoryLogger.getInstance().checkMemory();
		
		// if the user want to keep the result into memory
		
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
			
			tree.createHeaderList(mapSupport);
		if(tree.headerList.size() > 0) {
			// initialize the buffer for storing the current itemset
			itemsetBuffer = new int[BUFFERS_SIZE];
			// and another buffer
			fpNodeTempBuffer = new FPNode[BUFFERS_SIZE];
			// recursively generate frequent itemsets using the fp-tree
			// Note: we assume that the initial FP-Tree has more than one path
			// which should generally be the case.
			fpgrowth(tree, itemsetBuffer, 0, transactionCount,mapTraces, mapSupport,mapTraceList);
		}
		
		
		
endTime= System.currentTimeMillis();
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// return the result (if saved to memory)
		return patterns;
		 
		
	}
	public Itemsets runAlgorithm(String input, String output, double minsupp) throws FileNotFoundException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		// number of itemsets found
		itemsetCount = 0;
		
		//initialize tool to record memory usage
		MemoryLogger.getInstance().reset();
		MemoryLogger.getInstance().checkMemory();
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
			itemsetOutputBuffer = new int[BUFFERS_SIZE];
		}
		
		// (1) PREPROCESSING: Initial database scan to determine the frequency of each item
		// The frequency is stored in a map:
		//    key: item   value: support
		final Map<Integer, Integer> mapSupport = scanDatabaseToDetermineFrequencyOfSingleItems(input); 

		// convert the minimum support as percentage to a
		// relative minimum support
		this.minSupportRelative = (int) Math.ceil(minsupp * transactionCount);
		
		// (2) Scan the database again to build the initial FP-Tree
		// Before inserting a transaction in the FPTree, we sort the items
		// by descending order of support.  We ignore items that
		// do not have the minimum support.
		FPTree tree = new FPTree();
		
		// read the file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		int index=0;
		// for each line (transaction) until the end of the file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||	line.charAt(0) == '#' || line.charAt(0) == '%'
				|| line.charAt(0) == '@') {
				continue;
			}
			index++;
			String[] lineSplited = line.split(" ");
//			Set<Integer> alreadySeen = new HashSet<Integer>();
			List<Integer> transaction = new ArrayList<Integer>();
			
			// for each item in the transaction
			for(String itemString : lineSplited){  
				Integer item = Integer.parseInt(itemString);
				// only add items that have the minimum support
				if(mapSupport.get(item) >= minSupportRelative){
					transaction.add(item);	
				}
			}
			// sort item in the transaction by descending order of support
			Collections.sort(transaction, new Comparator<Integer>(){
				public int compare(Integer item1, Integer item2){
					// compare the frequency
					int compare = mapSupport.get(item2) - mapSupport.get(item1);
					// if the same frequency, we check the lexical ordering!
					if(compare == 0){ 
						return (item1 - item2);
					}
					// otherwise, just use the frequency
					return compare;
				}
			});
			// add the sorted transaction to the fptree.
			tree.addTransaction(transaction, index);
		}
		// close the input file
		reader.close();
		
		// We create the header table for the tree using the calculated support of single items
		tree.createHeaderList(mapSupport);
		
		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, the prefix alpha is empty.
		// if at least an item is frequent
		if(tree.headerList.size() > 0) {
			// initialize the buffer for storing the current itemset
			itemsetBuffer = new int[BUFFERS_SIZE];
			// and another buffer
			fpNodeTempBuffer = new FPNode[BUFFERS_SIZE];
			// recursively generate frequent itemsets using the fp-tree
			// Note: we assume that the initial FP-Tree has more than one path
			// which should generally be the case.
			fpgrowth(tree, itemsetBuffer, 0, transactionCount, null, mapSupport, null);
		}
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		// record the execution end time
		endTime= System.currentTimeMillis();
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// return the result (if saved to memory)
		return patterns;
	}


	
	/**
	 * Mine an FP-Tree having more than one path.
	 * @param tree  the FP-tree
	 * @param prefix  the current prefix, named "alpha"
	 * @param mapTraces 
	 * @param mapSupport the frequency of items in the FP-Tree
	 * @param mapTraceList 
	 * @throws IOException  exception if error writing the output file
	 */
	private void fpgrowth(FPTree tree, int [] prefix, int prefixLength, int prefixSupport, List<Integer> mapTraces, Map<Integer, Integer> mapSupport, Map<Integer, List<Integer>> mapTraceList) throws IOException {
		
		if(prefixLength == maxPatternLength){
			return;
		}
		
		
		////		======= DEBUG ========
//		System.out.print("###### Prefix: ");
//		for(int k=0; k< prefixLength; k++) {
//			System.out.print(prefix[k] + "  ");
//		}
//		System.out.println("\n");
////				========== END DEBUG =======
//		System.out.println(tree);
		
		// We will check if the FPtree contains a single path
		boolean singlePath = true;
		// This variable is used to count the number of items in the single path
		// if there is one
		int position = 0;
		// if the root has more than one child, than it is not a single path
		if(tree.root.childs.size() > 1) {
			singlePath = false;
		}else {
			
			// Otherwise,
			// if the root has exactly one child, we need to recursively check childs
			// of the child to see if they also have one child
			FPNode currentNode = tree.root.childs.get(0);
			while(true){
				// if the current child has more than one child, it is not a single path!
				if(currentNode.childs.size() > 1) {
					singlePath = false;
					break;
				}
				// otherwise, we copy the current item in the buffer and move to the child
				// the buffer will be used to store all items in the path
				fpNodeTempBuffer[position] = currentNode;
				
				position++;
				// if this node has no child, that means that this is the end of this path
				// and it is a single path, so we break
				if(currentNode.childs.size() == 0) {
					break;
				}
				currentNode = currentNode.childs.get(0);
			}
		}
		
		// Case 1: the FPtree contains a single path
		if(singlePath){	
			// We save the path, because it is a maximal itemset
			saveAllCombinationsOfPrefixPath(fpNodeTempBuffer, position, prefix, prefixLength);
		}else {
			// For each frequent item in the header table list of the tree in reverse order.
			for(int i = tree.headerList.size()-1; i>=0; i--){
				// get the item
				Integer item = tree.headerList.get(i);
				
				// get the item support
				int support = mapSupport.get(item);
				List<Integer> tracesList =new ArrayList<Integer>(mapTraceList.get(item));
				
	
				// Create Beta by concatening prefix Alpha by adding the current item to alpha
				prefix[prefixLength] = item;
				
				// calculate the support of the new prefix beta
				int betaSupport = (prefixSupport < support) ? prefixSupport: support;
				List<Integer> bettaTraceList= (mapTraces.size() < tracesList.size()) ? mapTraces : tracesList;
				// save beta to the output file
				saveItemset(prefix, prefixLength+1, betaSupport,bettaTraceList);
				
				if(prefixLength+1 < maxPatternLength){
				
					// === (A) Construct beta's conditional pattern base ===
					// It is a subdatabase which consists of the set of prefix paths
					// in the FP-tree co-occuring with the prefix pattern.
					List<List<FPNode>> prefixPaths = new ArrayList<List<FPNode>>();
					FPNode path = tree.mapItemNodes.get(item);
					
					// Map to count the support of items in the conditional prefix tree
					// Key: item   Value: support
					HashMap<Integer, Integer> mapSupportBeta = new HashMap<Integer, Integer>();
					Map<Integer, List<Integer>> mapTracesBeta = new HashMap<Integer, List<Integer>>();
					while(path != null){
						// if the path is not just the root node
						if(path.parent.itemID != -1){
							// create the prefixpath
							List<FPNode> prefixPath = new ArrayList<FPNode>();
							// add this node.
							prefixPath.add(path);   // NOTE: we add it just to keep its support,
							// actually it should not be part of the prefixPath
							
							// ####
							int pathCount = path.counter;
							List<Integer> traceList = new ArrayList<Integer>(path.traces);
							//Recursively add all the parents of this node.
							FPNode parent = path.parent;
							List<Integer> seenItem=new ArrayList<Integer>();
							while(parent.itemID != -1  ){
								prefixPath.add(parent);
								/*if(!seenItem.contains(parent.itemID)) {
									seenItem.add(parent.itemID);*/
									
									// FOR EACH PATTERN WE ALSO UPDATE THE ITEM SUPPORT AT THE SAME TIME
									// if the first time we see that node id
									
									if(mapSupportBeta.get(parent.itemID) == null){
										// just add the path count
										mapSupportBeta.put(parent.itemID, pathCount);
										mapTracesBeta.put(parent.itemID, traceList);
									}else{
										// otherwise, make the sum with the value already stored
										mapSupportBeta.put(parent.itemID, mapSupportBeta.get(parent.itemID) + pathCount);
										List<Integer> currentList =new ArrayList<Integer>(mapTracesBeta.get(parent.itemID));
										currentList.addAll(traceList);
										mapTracesBeta.put(parent.itemID, currentList);
									}
							
								parent = parent.parent;
							}
				
								prefixPaths.add(prefixPath);
								}
						
						// We will look for the next prefixpath
						path = path.nodeLink;
					}
	
					// (B) Construct beta's conditional FP-Tree
					// Create the tree.
					FPTree treeBeta = new FPTree();
					// Add each prefixpath in the FP-tree.
					for(List<FPNode> prefixPath : prefixPaths){
						treeBeta.addPrefixPath(prefixPath, mapSupportBeta, minSupportRelative); 
					}  
					
					// Mine recursively the Beta tree if the root has child(s)
					if(treeBeta.root.childs.size() > 0){
	
						// Create the header list.
						treeBeta.createHeaderList(mapSupportBeta); 
						// recursive call
						fpgrowth(treeBeta, prefix, prefixLength+1, betaSupport, bettaTraceList, mapSupportBeta,mapTracesBeta);
					}
				}
			}
		}
		
	}


	/**
	 * This method saves all combinations of a prefix path if it has enough support
	 * @param prefix the current prefix
	 * @param prefixLength the current prefix length
	 * @param prefixPath the prefix path
	 * @throws IOException if exception while writting to output file
	 */
	private void saveAllCombinationsOfPrefixPath(FPNode[] fpNodeTempBuffer, int position, 
			int[] prefix, int prefixLength) throws IOException {

		int support = 0;
		List<Integer> traceslist = new ArrayList<Integer>();
		// Generate all subsets of the prefixPath except the empty set
		// and output them
		// We use bits to generate all subsets.
		
loop1:	for (long i = 1, max = 1 << position; i < max; i++) {
			
			// we create a new subset
			int newPrefixLength = prefixLength;
			
			// for each bit
			for (int j = 0; j < position; j++) {
				// check if the j bit is set to 1
				int isSet = (int) i & (1 << j);
				// if yes, add the bit position as an item to the new subset
				if (isSet > 0) {
					if(newPrefixLength == maxPatternLength){
						continue loop1;
					}
					
					prefix[newPrefixLength++] = fpNodeTempBuffer[j].itemID;
					// 2018-03-18: REMOVED THE FOLLOWING "IF" to fix
					// support counting error.
//					if(support == 0) {
						support = fpNodeTempBuffer[j].counter;
						traceslist=fpNodeTempBuffer[j].traces;
//					}
				}
			}
			// save the itemset
			saveItemset(prefix, newPrefixLength, support, traceslist);
		}
	}
	

	/**
	 * This method scans the input database to calculate the support of single items
	 * @param input the path of the input file
	 * @throws IOException  exception if error while writing the file
	 * @return a map for storing the support of each item (key: item, value: support)
	 */
	private  Map<Integer, Integer> scanDatabaseToDetermineFrequencyOfSingleItems(String input)
			throws FileNotFoundException, IOException {
		// a map for storing the support of each item (key: item, value: support)
		 Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
		//Create object for reading the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||  line.charAt(0) == '#' || line.charAt(0) == '%' 	|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line into items
			String[] lineSplited = line.split(" ");
			// for each item
			for(String itemString : lineSplited){  
				// increase the support count of the item
				Integer item = Integer.parseInt(itemString);
				// increase the support count of the item
				Integer count = mapSupport.get(item);
				if(count == null){
					mapSupport.put(item, 1);
				}else{
					mapSupport.put(item, ++count);
				}
			}
			// increase the transaction count
			transactionCount++;
		}
		// close the input file
		reader.close();
		
		return mapSupport;
	}


	/**
	 * Write a frequent itemset that is found to the output file or
	 * keep into memory if the user prefer that the result be saved into memory.
	 * @param traceslist 
	 */
	private void saveItemset(int [] itemset, int itemsetLength, int support, List<Integer> traceslist) throws IOException {
		
		// increase the number of itemsets found for statistics purpose
		itemsetCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			// copy the itemset in the output buffer and sort items
			System.arraycopy(itemset, 0, itemsetOutputBuffer, 0, itemsetLength);
			Arrays.sort(itemsetOutputBuffer, 0, itemsetLength);
			
			// Create a string buffer
			StringBuilder buffer = new StringBuilder();
			// write the items of the itemset
			for(int i=0; i< itemsetLength; i++){
				buffer.append(itemsetOutputBuffer[i]);
				if(i != itemsetLength-1){
					buffer.append(' ');
				}
			}
			// Then, write the support
			buffer.append(" #SUP: ");
			buffer.append(support);
			// write to file and create a new line
			writer.write(buffer.toString());
			writer.newLine();
			
		}// otherwise the result is kept into memory
		else{
			// create an object Itemset and add it to the set of patterns 
			// found.
			int[] itemsetArray = new int[itemsetLength];
			System.arraycopy(itemset, 0, itemsetArray, 0, itemsetLength);
			
			// sort the itemset so that it is sorted according to lexical ordering before we show it to the user
			Arrays.sort(itemsetArray);
			
			Itemset itemsetObj = new Itemset(itemsetArray);
			itemsetObj.setAbsoluteSupport(support);
			itemsetObj.setTraceList(traceslist);
			patterns.addItemset(itemsetObj, itemsetLength);
		}
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  FP-GROWTH 0.96r19 - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + transactionCount);
		System.out.print(" Max memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " mb \n");
		System.out.println(" Frequent itemsets count : " + itemsetCount); 
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Get the number of transactions in the last transaction database read.
	 * @return the number of transactions.
	 */
	public int getDatabaseSize() {
		return transactionCount;
	}

	/** 
	 * Set the maximum pattern length
	 * @param length the maximum length
	 */
	public void setMaximumPatternLength(int length) {
		maxPatternLength = length;
	}

	

}
