# Efficient Approximate Conformance Checking Using Trie Data Structures

## What is conformance checking?
<div style="text-align: justify">
Business process conformance checking is a sub-field of process mining. The main question conformance checking 
answers is whether the actual execution of a process as recorded in so-called execution 
event logs abides to the intended behavior defined by a process model. This goes beyond the simple binary answer to quantify the amount of 
deviation, if any. Deviation can be attributed to one of two reasons:
- Extra unfitted behavior: This is in the form of behavior observed in the log that is not allowed by the model,
- Untapped model behavior: This is the case when branches of the model behavior are not observed at all in the recorded log.
</div>

## Approaches for conformance checking
<div style="text-align: justify">
In literature, there are basically three broad approaches for conformance checking [1]: 1) Rule checking, 2) Token replay and 3) Alignments.

Currently, alignment-based approaches are considered the de-facto standard for measuring conformance. However, such approaches are expensive to compute 
as they require generating the state space of the combined model and trace behavior, a.k.a synchronous product net. The generated state space, as a graph, is further enriched with cost estimates and is fed to the A*-algorithm to find the 
shortest path from the start state, where the trace and a first activity of the model start, to an end target state
where we have executed the whole trace and also a path in the model. In between, there could be situations where we observe only an
event from the trace without a counterpart activity from the model, log move, or vice versa, model move.

The shortest path found is called an optimal alignment as it will always have the minimum number of non-synchronous moves, 
either log moves or model moves, that makes the trace matches the model behavior. The deviation, also called cost of an alignment
is the number of non-synchronous moves. Of course, an alignment of cost 0 indicates a perfect match between the trace and the model.
</div>

## Approximate approaches for computing alignments
<div style="text-align: justify">
The approach above is guaranteed to find an optimal alignment as it will exhaust the search space. However, such space might be infinite. Or it might be finite but prohibitively large to exhaustively explore.
Thus, a number of approaches have been proposed to approximate the computation of optimal alignments.

A family of these approaches depend on the explicit representation of the model behavior as a set of traces [3]. Then, Levenshtein distance, based on insertion or deletions only
is used to find the minimum distance and thus the _cheapest_ alignment between a log trace and a trace in the model behavior [2]. This approach is considered approximate because the model behavior chosen is usually a subset of the 
_whole_ behavior of the model. So, the cheapest alignment found is not guaranteed to be optimal, because there might be another model trace that was not picked that better aligns with the log trace. 
</div>

### Using tries for efficient computation of approximate alignments

As we pick a finite subset of the model behavior and reduce alignment computation to a kind of string matching problem, we can encode the model behavior, represented by a set of (traces) strings in compact way that can speed up the match.

Currently, finding the best alignment would require one-to-one comparison between the log trace and each trace in the model behavior.

In string matching, tries, a.k.a as prefix trees, can speed up the search by logarithmically reducing the search space with each matching character between the input string, the log trace in our case, and the set of predefined strings, the model behavior. Moreover, the encoding of the model behavior requires a logarithmic space to store the model behavior as all common prefixes between model traces will be stored once.


### Example

Consider the Petri net below to be our process model.

![A Petri net to serve as the model](./src/main/resources/PetriNet.png "A Petri net as a process model")

Out of this model, we can obtain the following set of model traces. Note that the  model 

1. <a,b,c,e>
2. <a,b,c,d,b,e>
3. <a,b,c,d,b,d,b,e>
4. <a,c,b,e>
5. <a,b,e>

Note that, due to the loop in the model, the sequence <b,d> can repeat arbitrarily, causing traces of possibly infinite length.

Consider also the following log traces to compute alignment for:

1. <a,b,c,e>
2. <a,e>
3. <c,e>
4. <a,c,b,d,e>
5. <a,b,e>
6. <a, c, b, d, b, d, b, d, b, e>


<div style="text-align: justify">
To compute alignments using edit distance, for each trace in the log trace (the second set of traces), we need to compare it with each of the model traces (the upper set) computing the edit distance and at the end report the trace that gives the least edit distance and actually reporting the edit operations, insertions or deletions as the alignment. For instance, log trace 3 &lt;c, e&gt; would have an edit distance of 3 with model trace 5 &lt; a,b, e&gt;, whereas it has an edit distance of 2 compared to model trace 1 &lt;a, b, c, e&gt;. Note also that log trace number 6 is actually possible by the Petri net. So, in an exhaustive approach, it would have an alignment cost of 0. However, in our case, its cheapest alignment would be with model trace 3 &lt; a,b,c,d,b,d,b,e &gt; with edit distance of 4.
</div>


### References

[1] Josep Carmona, Boudewijn F. van Dongen, Andreas Solti, Matthias Weidlich: Conformance Checking - Relating Processes and Models. Springer 2018, ISBN 978-3-319-99413-0, pp. 1-263

[2] Mohammadreza Fani Sani, Sebastiaan J. van Zelst, Wil M. P. van der Aalst:
Conformance Checking Approximation Using Subset Selection and Edit Distance. CAiSE 2020: 234-251

[3] Mohammadreza Fani Sani, Juan J. Garza Gonzalez, Sebastiaan J. van Zelst, Wil M. P. van der Aalst:
Conformance Checking Approximation Using Simulation. ICPM 2020: 105-112