# I Will Survive: An Event-driven Conformance Checking Approach Over Process Streams

## Info
This code is part of a conference paper currently under revision.

## How to run?
<div style="text-align: justify">
The Runner.java file main method has a few string values which can be modified, e.g. 
whether to run the stress test or cost deviation test.

If running the stress test, load a model from "input" folder into PLG2 and be ready to 
initiate the stream. Make sure that the correct model size is defined in Runner.java, and then
build and run the algorithm, and start the stream in PLG2.
The algorithm is currently set to stop after 60 minutes of receiving a stream, and it outputs some 
high level metrics into StdOut.

For running the cost deviations, the input folders contain the proxy logs and have already been defined 
in Runner.java. 

In case you want to alter between fixed/discounted settings for the algorithm, this can be done by modifying:
* **discountedDecayTime** (false = Fixed, true = Discounted)
* **minDecayTime** is either the value for Fixed or the minimum value for Discounted setting
* **decayTimeMultiplier** is the Discounting Factor - only used if discountedDecayTime = true
</div>


