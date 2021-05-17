package org.processmining.logfiltering.Juan.algo.Juan;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

import org.processmining.logfiltering.Juan.trie.Trie;
public class TrieDemo {
	public static void main(String[] args) {

		// This is a case sensitive trie
		Trie trie = new Trie(true, StandardCharsets.UTF_8);

		// Add words.
		trie.add("AA");
		Map<String, Integer> x = trie.getSimilarityMap("AI", 7);
		
		
		System.out.println("Number Of Words: " + trie.getNumberOfWords());
		
		trie.show();
		
		// Return true if the word is in the trie.
		System.out.println(trie.search("Jane"));
		// Return true if there is any word in the trie that starts with the given prefix.
		System.out.println(trie.startsWith("Ja"));
		// Remove a word
		trie.remove("Johnny");

		// Count words starts with a partial name to search the application for
		System.out.println("Number Of Words Starts with 'John': " + trie.countWordStartsWith("John"));
		System.out.println("Number Of Words Starts with 'Ja': " + trie.countWordStartsWith("Ja"));
		// Get words starts with a partial name to search the application for
		Stream<String> words = trie.getWordStartsWith("Jo");
		words.forEach(System.out::println);
		
		// String Similarity using Levenshtein distance		
		// Get words that are less than the given maximum distance from the target word
		for (Map.Entry<String, Integer> entry : trie.getSimilarityMap("acbd", 2).entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
		// Get word that is less than the given maximum distance from the target word
		System.out.println(trie.similarity("acbd", 1));
	}
}
