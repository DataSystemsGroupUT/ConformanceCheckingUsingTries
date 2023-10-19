# I Will Survive: An Event-driven Conformance Checking Approach Over Process Streams

## Info
Kristo Raun, Riccardo Tommasini, Ahmed Awad  
June 2023, ACM (Association for Computing Machinery)  
DOI: 10.1145/3583678.3596887

**Best paper award of DEBS 2023  
17TH ACM International Conference on Distributed and Event-Based Systems  
https://2023.debs.org/**

## What is it about?
In business processes, it is important to find discrepancies between expected and actual behavior. This paper introduces an algorithm that is able to find discrepancies in a fast and explainable manner in streaming data.

## Notions
We use a **proxy log** to describe the allowed behavior - i.e., the process model. This proxy log can be generated manually, simulated using a simulation method and a process model in a different dialect (Petri Net, BPMN), or sampled from an actual event log.

An **event log** describes the actual behavior observed. For this algorithm, it is possible to load in an event log from a .xes file, or stream an event log via TCP using an external tool (e.g., PLG2).  

## How to run?
The Runner.java file main method is the place that should be run to get results. Based on the current example, it will output at `test.csv` in the `output` folder, indicating a .

For the version used in the DEBS'23 paper, the commit to be used is `https://github.com/DataSystemsGroupUT/ConformanceCheckingUsingTries/tree/e49877f7bcb95e4cef3422a879665e1c84422e7c`

## Citing
Raun, K., Tommasini, R., & Awad, A. (2023, June).  
I Will Survive: An Event-driven Conformance Checking Approach Over Process Streams.  
In Proceedings of the 17th ACM International Conference on Distributed and Event-based Systems (pp. 49-60).  

