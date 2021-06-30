# Efficient Approximate Conformance Checking Using Trie Data Structures

## What is conformance checking?

Business process conformance checking is a sub-field of process mining. The main question conformance checking 
answers is whether the actual execution of a process as recorded in so-called execution 
event logs abides to the intended behavior defined by a process model. This goes beyond the simple binary answer to quantify the amount of 
deviation, if any. Deviation can be attributed to one of two reasons:
- Extra unfitted behavior: This is in the form of behavior observed in the log that is not allowed by the model,
- Untapped model behavior: This is the case when branches of the model behavior are not observed at all in the recorded log.

## Approaches for conformance checking

In literature, there are basically three broad approaches for conformance checking: 1) Rule checking, 2) Token replay and 3) Alignments.

Currently, 
