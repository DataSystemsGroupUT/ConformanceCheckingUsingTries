# I Will Survive: An Online Conformance Checking Algorithm Using Decay Time

## Info
This code is part of a paper submitted for the BPI 2022 workshop (part of BPM conference).

## How to run?
<div style="text-align: justify">
The Runner.java file main method has a few string values which can be modified, e.g. 
whether to run the stress test or cost deviation test.

If running the stress test, load a model from "input" folder into PLG2 and be ready to 
initiate the stream. Make sure that the correct model size is defined in Runner.java, and then
build and run the algorithm, and start the stream in PLG2.
The algorithm is currently set to stop after 5 minutes of receiving a stream, and it outputs some 
high level metrics into StdOut.

For running the cost deviations, the input folders contain the proxy logs and have already been defined 
in Runner.java. It is possible to run the deviation-checker for all logs ("general") or one specific
log ("specific"). Note: due to Github storage limitations, only Sepsis and BPI2015 logs are provided
in this repo. Please contact us in case you need the other datasets.

In case you want to alter between fixed/discounted settings for the algorithm, this can be done in 
StreamingConformanceChecker.java:
* **discountedDecayTime** (false = Fixed, true = Discounted)
* **minDecayTime** is either the value for Fixed or the minimum value for Discounted setting
* **decayTimeMultiplier** is the Discounting Factor - only used if discountedDecayTime = true
</div>


