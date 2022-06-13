package org.processmining.logfiltering.algorithms;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import nl.tue.astar.util.ilp.LPMatrixException;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.algorithms.ICC.AlignmentReplayResult;
import org.processmining.logfiltering.algorithms.ICC.AlignmentReplayer;
import org.processmining.logfiltering.algorithms.ICC.ApproxAlignmentReplayer;
import org.processmining.logfiltering.algorithms.ICC.ApproxFitnessReplayer;
import org.processmining.logfiltering.algorithms.ICC.FitnessReplayer;
import org.processmining.logfiltering.algorithms.ICC.IccParameters;
import org.processmining.logfiltering.algorithms.ICC.IccResult;
import org.processmining.logfiltering.algorithms.ICC.IncrementalConformanceChecker;
import org.processmining.logfiltering.algorithms.ICC.IncrementalReplayer;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SimilarityMeasure;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinetsimulator.parameters.SimulationSettings;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import cern.jet.random.Exponential;
import cern.jet.random.engine.DRand;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
public class ProtoTypeSelectionAlgo {

    static int nThreads = 4;
    static int costUpperBound = Integer.MAX_VALUE;
    // timeout per trace in milliseconds
    static int timeoutMilliseconds = 10 * 1000;
    // preprocessing time to be added to the statistics if necessary
    static long preProcessTimeNanoseconds = 0;

    static Marking initialMarking;
    static Marking finalMarking;
    static long time;
    static XLog InputLog;
    static XEventClass dummyEvClass;
    static XEventClassifier eventClassifier;
    static TransEvClassMapping mapping;
    static XEventClassifier EventCol;
    static XLogInfo summary;
    static XEventClasses classes;


    static XFactory factory = XFactoryRegistry.instance().currentDefault();
    static XLogInfo logInfo;
    static HashMap<String,String >ActivityCoder =new HashMap<String, String>();
    static HashMap<String,String >ActivityDeCoder =new HashMap<String, String>();
    static HashMap<String, Integer> AsyncrousMoves= new HashMap<String, Integer>();
    static HashMap<String, Double> AsyncrousDistribution= new HashMap<String, Double>();
    static HashMap<String, Integer> SyncrousMoves= new HashMap<String, Integer>();
    static HashMap<String, Integer> ModelMoves= new HashMap<String, Integer>();
    static HashMap<String, Integer> LogMoves= new HashMap<String, Integer>();


    static int LogSize = 0;
    static PriorityQueue<Integer> pickedVariant=new PriorityQueue<>();
    static int charcounter=65;
    static Set<String> ActivitySet = new HashSet<String>();
    static HashMap<String,Integer >ActivityCounter =new HashMap<String, Integer>();


    static HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
    static HashMap<String,String >TraceHash =new HashMap<String, String>();
    static HashMap<String,String >TraceHashReverse =new HashMap<String, String>();
    static HashMap<Integer,String >tempMapper =new HashMap<Integer,String>();
    static HashMap<String,Integer >HashTraceCounter =new HashMap<String, Integer>();
    static HashMap<String,Integer >FilterHashMaper =new HashMap<String, Integer>();
    static HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
    static HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
    static HashMap<String, Integer> DFrelationMapper= new HashMap<String, Integer>();
    static HashMap<Integer, String> DFrelationReverseMapper =new HashMap<Integer, String>();
    static HashMap<Integer, XTrace> VariantMapper =new HashMap<Integer, XTrace>();
    static int chCount=0;
    static int dfCount=0;
    static HashMap<Integer,List<Integer> >Clusters =new HashMap<Integer,List<Integer>>();
    static HashMap<Integer,Integer>SelectedList =new HashMap<Integer,Integer>();
    static HashMap<Integer,Integer>SelectedListTemp =new HashMap<Integer,Integer>();
    static HashMap<String,Integer >ModelBehaviorSim =new HashMap<String, Integer>();
    static HashMap<Integer,String >ModelBehaviorSimReverse =new HashMap< Integer,String>();
    static HashMap<String,String >ReducedVariants =new HashMap<String,String>();
    static HashMap<Integer,String >charecterTraces =new HashMap<Integer,String>();
    static int maxTraceLength= -1;

    static double[] AlignmentCosts;
    static double[] FitnessValues;
    static long MBTime;

    static Future<TraceReplayTask>[] futures;

    static ReplayerParameters RepParameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
    static Replayer replayer;
    static ExecutorService service = Executors.newFixedThreadPool(nThreads);

    static float [] VariantFreq;
    static int chCount2=0;

    static double AlignFit=0;
    static double AlignCost=0;

    static String[] VariantChar;
    public static String apply(XLog inputLog,Petrinet net, MatrixFilterParameter parameters,TransEvClassMapping mapping2) {
        time = System.currentTimeMillis();
        InputLog= (XLog) inputLog.clone();

        dummyEvClass = new XEventClass("DUMMY", 99999);
        eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        mapping = constructMapping(net, InputLog, dummyEvClass, eventClassifier);

        EventCol = parameters.getEventClassifier();

        logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);



        ///////////////////////////Compute the Replay Values///////////////////
    //    Multiset<String> asynchronousMoveBag=TreeMultiset.create();
        initialMarking = getInitialMarking(net);
        finalMarking = getFinalMarking(net);



        summary = XLogInfoFactory.createLogInfo(InputLog, eventClassifier);
        classes = summary.getEventClasses();






//        SortedSet<String> eventAttributeSet = new TreeSet<String>();
       XAttributeMap eventAttributeMap;
  //      int KLength =parameters.getSubsequenceLength();

        ActivityCoder.put("ArtStart", Character.toString((char)charcounter));
        ActivityDeCoder.put(Character.toString((char)charcounter), "ArtStart");


        for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
            charcounter++;
            ActivitySet.add(clazz.toString());
            ActivityCoder.put(clazz.toString(), Character.toString((char)charcounter));
            ActivityDeCoder.put( Character.toString((char)charcounter),clazz.toString());
            ActivityCounter.put( Character.toString((char)charcounter),0);

            SyncrousMoves.put ( clazz.toString(), 0);
            AsyncrousMoves.put ( clazz.toString(), 0);
            ModelMoves.put ( clazz.toString(), 0);
            LogMoves.put ( clazz.toString(), 0);
        }
        charcounter++;
        ActivityCoder.put("ArtEnd", Character.toString((char)charcounter));
        ActivityDeCoder.put( Character.toString((char)charcounter),"ArtEnd");
        int ActivitiesSize = ActivitySet.size();
        //Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
        String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
        List<String> ActivityList = java.util.Arrays.asList(Activities);
//        int[] ActivityCount = new int[ActivitiesSize];
//        FilterLevel FilteringMethod = parameters.getFilterLevel();
//        FilterSelection FilteringSelection =parameters.getFilteringSelection();

        for (XTrace trace : InputLog) { // for each trace
            LogSize++;
            /// Put trace to array
            String[] Trace = new String[trace.size()];
            List<String> templist = new ArrayList<String>();
            for (XEvent event : trace) {
                eventAttributeMap = event.getAttributes();
                templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
            }
            Trace = templist.toArray(new String[trace.size()]);
            String tr= Trace[0];
            String TraceinChar=ActivityCoder.get(tr);
            ActivityCounter.put(ActivityCoder.get(Trace[0]), ActivityCounter.get(ActivityCoder.get(Trace[0]))+1);
            for (int i =1; i < Trace.length; i++){
                tr= tr.concat("=>"+Trace[i]);
                TraceinChar= TraceinChar.concat(ActivityCoder.get(Trace[i]));
                ActivityCounter.put(ActivityCoder.get(Trace[i]), ActivityCounter.get(ActivityCoder.get(Trace[i]))+1);
            }
            //TraceinChar= TraceinChar.concat(ActivityCoder.get("ArtEnd"));
            //= tr.concat("=>"+"ArtEnd");
            if (ActionMaper.get(tr)==null ){
                ActionMaper.put(tr,1);
                ReverseMapper.put(chCount, tr);
                HashMaper.put(tr, chCount);
                HashTraceCounter.put(TraceinChar, 1);
                TraceHash.put(tr, TraceinChar);
                TraceHashReverse.put(TraceinChar, tr);
                VariantMapper.put(chCount, trace);
                charecterTraces.put(chCount, TraceinChar);
                chCount++;
                maxTraceLength=maxTraceLength+ TraceinChar.length();

            }else{
                ActionMaper.put(tr, ActionMaper.get(tr)+1);
                HashTraceCounter.put(TraceinChar, HashTraceCounter.get(TraceinChar)+1);
            }
        }



        VariantFreq= new float[HashMaper.size()];
        int[] VariantInd= new int[HashMaper.size()];
        for (int i = 0; i < VariantFreq.length; i++) {
            VariantFreq[i]= ActionMaper.get(ReverseMapper.get(i));
            VariantInd[i]=i;
            String TraceinChar= TraceHash.get(ReverseMapper.get(i));
            char[] DFRelations=TraceinChar.toCharArray();
            String DF= "";
            for (int j = 0; j < DFRelations.length-1; j++) {
                DF= Character.toString(DFRelations[j])+Character.toString(DFRelations[j+1]);
                if(DFrelationMapper.get(DF)==null) {
                    DFrelationMapper.put(DF,dfCount);
                    DFrelationReverseMapper.put(dfCount,DF);
                    dfCount++;
                }
            }
        }
        int[][] VariantProfile = new int [HashMaper.size()][DFrelationMapper.size()];
        VariantChar = new String[HashMaper.size()];
        for (int i = 0; i < VariantFreq.length; i++) {
            String TraceinChar= TraceHash.get(ReverseMapper.get(i));
            VariantChar[i]=TraceinChar;
            char[] DFRelations=TraceinChar.toCharArray();
            String DF= "";
            for (int j = 0; j < DFRelations.length-1; j++) {
                DF= Character.toString(DFRelations[j])+Character.toString(DFRelations[j+1]);
                VariantProfile[i][DFrelationMapper.get(DF)]= DFrelationMapper.get(DF)+1;
            }
        }


        double Threshold =1- parameters.getSubsequenceLength()*1.0/100;


        maxTraceLength= maxTraceLength/ReverseMapper.size();


        double INTRA= 0;
        Variant[] VariantSimilarity = new Variant[HashMaper.size()];
        double[] [] distanceMatrix =new double[HashMaper.size()][HashMaper.size()];
        if(parameters.getPrototypeType()==PrototypeType.KMeansClusteringApprox) {
            switch (parameters.getSimilarityMeasure()) {
                case Levenstein :

                    for (int i = 0; i < VariantProfile.length; i++) {
                        double temp=0;
                        double minTemp= 1000;
                        for (int j = 0; j < VariantProfile.length; j++) {
                            double x = levenshteinDistance (VariantChar[i],VariantChar[j]) ;
                            temp= temp+ x;
                            distanceMatrix[i][j]= x;
                            if (minTemp> x && x>0)
                                minTemp=x;
                        }
                        VariantSimilarity[i]= new Variant(i, temp );
                        INTRA= INTRA+minTemp;
                        //INTRA= INTRA+(temp*1.0/(VariantProfile.length-1));
                    }
                    INTRA=INTRA/(VariantProfile.length-1);
                    break;
                case Incremental :
                    for (int i = 0; i < VariantProfile.length; i++) {
                        double temp=0;
                        for (int j = 0; j < VariantProfile.length; j++) {
                            temp= temp+ levenshteinDistance (VariantChar[i],VariantChar[j]) ;
                            distanceMatrix[i][j]= levenshteinDistance (VariantChar[i],VariantChar[j]);
                        }
                        VariantSimilarity[i]= new Variant(i, temp );
                    }
                    break;
                case Jacard:
                    for (int i = 0; i < VariantProfile.length; i++) {
                        double temp=0;
                        for (int j = 0; j < VariantProfile.length; j++) {
                            temp= temp+ jaccardSimilarity (VariantProfile[i],VariantProfile[j])  ;

                        }
                        VariantSimilarity[i]= new Variant(i, temp  );
                    }
                    break;
                case Difference:
                    for (int i = 0; i < VariantProfile.length; i++) {
                        double temp=0;
                        for (int j = 0; j < VariantProfile.length; j++) {
                            temp= temp+  DiffSimilarity (VariantProfile[i],VariantProfile[j]) ;

                        }
                        VariantSimilarity[i]= new Variant(i, temp  );
                    }
                    break;
            }
        }



        List<Integer> SampledList=  new ArrayList<>();

        int[] MostSimilarVariant= new int [HashMaper.size()];

        int counter= 0;

        // reducedHashTraceCounter = HashTraceCounter.clone();
        long PreprocessTime = System.currentTimeMillis() - time ;

        ///////////////////////////Selection Phase/////////////////////////////////
        PrototypeType  SelectionType=parameters.getPrototypeType();
        switch (SelectionType) {
            case First:

                handleFirstSelection(VariantInd, Threshold, SampledList);

                break;

            case Frequency :
                handleFrequencySelection(VariantFreq, VariantInd, Threshold, SampledList);
                break;

            case Random:
                handleRandomSelection(VariantInd, Threshold, SampledList);
                break;
            case KMeansClusteringApprox:
                handleClusteringSelection(VariantFreq, VariantInd, Threshold, distanceMatrix, SampledList);
                break;
            case Simulation:
                handleSimulationSelection(net, VariantInd, Threshold, SampledList);

                break;

            case Activity:

                String output2 = handleActivitySelection((Petrinet) net, VariantFreq, VariantChar, Threshold);

                return output2;
        }
        //////////////////////////////////////////////////
        MBTime = System.currentTimeMillis();

        XLog TraceLog = factory.createLog();
        TraceLog.setAttributes(InputLog.getAttributes());
        XAttributeMapImpl case_map = new XAttributeMapImpl();
        String case_id = String.valueOf(charcounter);
        case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
        XTraceImpl trace = new XTraceImpl(case_map);
        TraceLog.add(trace);
        //double ShortestPath=replayTraceOnNet( TraceLog,  net,  mapping);
        AlignmentCosts= new double [SelectedList.size()+1];
        FitnessValues= new double [SelectedList.size()];





        for (int i = 0; i < SelectedList.size(); i++) {
            TraceLog.add(VariantMapper.get(SelectedList.get(i)));
            VariantMapper.get(SelectedList.get(i)).toArray();
            // AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
        }//

        XesXmlSerializer ser = new XesXmlSerializer();
        OutputStream os;
        try {
            os = new FileOutputStream("C:\\Work\\DSG\\Data\\sampledLog.xml");
            ser.serialize(TraceLog, os);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        ///// Compute the actual alignment

        computeActualAlignment(net, TraceLog);

        ///////////////////////Compute the approximation Cost
        MBTime = System.currentTimeMillis();
        double Costs = 0;
        double SmapleFitness=0;
        double approximateCosts = 0;
        double approximateCosts2 = 0;
        double upperBoundCost=0;
        double[] SampledFitness = new double[SampledList.size()];
        chCount2=0;
        double SampledFitnessApproximationValue=0;
        double SampledFitnessApproximationValue2=0;
        double[] distancesApprox= new double [MostSimilarVariant.length];
        int sampledFreq= 0;
        boolean increament=false;
        if (parameters.getPrototypeType()!=PrototypeType.Simulation) {

            for (int i = 0; i < SampledList.size(); i++) {
                sampledFreq= (int) (sampledFreq+VariantFreq[SampledList.get(i)]);
                double similarVariantcost =10000;
                int tempIndex=0;
                double tempDist=0;
                String alignment="";
                SimilarLoop:	 for (int j = 0; j < ModelBehaviorSimReverse.size(); j++) {
                    tempDist=levenshteinDistanceCost(VariantChar[SampledList.get(i)],  ModelBehaviorSimReverse.get(j) );
                    if( tempDist  < similarVariantcost) {
                        similarVariantcost=tempDist  ;
                        tempIndex=j;
                        if (similarVariantcost==0)
                            break SimilarLoop;
                    }
                }
                AlignObj temp=levenshteinDistancewithAlignment(VariantChar[SampledList.get(i)],  ModelBehaviorSimReverse.get(tempIndex) );
                alignment=temp.Alignment;
                String[] Moves = alignment.split(">>");
                for (int j = 0; j < Moves.length; j++) {
                    if(Moves[j].contains("Sync"))
                        SyncrousMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (SyncrousMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
                    else {
                        AsyncrousMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (AsyncrousMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
                        if(Moves[j].contains("Deletion"))
                            ModelMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (ModelMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
                        else
                            LogMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (LogMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
                    }
                }

                distancesApprox[SampledList.get(i)]= similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length() );
                double similarVariantAlignCost=similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());

                SampledFitness[i]=1-similarVariantAlignCost;
                if (similarVariantAlignCost< 0.10) {
                    approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                    approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                }else if(parameters.getSimilarityMeasure()!= SimilarityMeasure.Incremental) {

                    SampledFitness[i] =1 -( similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length())   );
                    approximateCosts= approximateCosts+( ((similarVariantAlignCost+ AlignCost)*1.0/2)  * VariantFreq[SampledList.get(i)]);
                    approximateCosts2= approximateCosts2+( AlignCost  * VariantFreq[SampledList.get(i)]);
                }else {
                    increament=true;
                    chCount2++;
                    futures = new Future[1];
                    TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, VariantMapper.get(SampledList.get(i)), 0, timeoutMilliseconds,
                            RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
                    // submit for execution
                    futures[0] = service.submit(task);
                    TraceReplayTask result;
                    try {
                        result = futures[0].get();
                    } catch (Exception e) {
                        // execution os the service has terminated.
                        assert false;
                        throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
                    }
                    SyncReplayResult replayResult = result.getSuccesfulResult();
                    double x = replayResult.getInfo().get("Raw Fitness Cost");

                    List<Object> ModelBehavior = replayResult.getNodeInstance();
                    List<StepTypes> TypeBehavior = replayResult.getStepTypes();
                    String modelTrace="";
                    for (int j=0;j<replayResult.getNodeInstance().size();j++) {
                        if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
                            if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
                                modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
                            }
                            else {
                                charcounter++;
                                ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
                                modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
                            }
                        }
                    }

                    if (ModelBehaviorSim.get(modelTrace)==null) {
                        ModelBehaviorSim.put(modelTrace, chCount);
                        ModelBehaviorSimReverse.put(chCount, modelTrace);
                        chCount++;
                    }
                    similarVariantAlignCost= x*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());
                    SampledFitness[i] =1 - similarVariantAlignCost  ;
                    approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                    approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                }


                SmapleFitness= SmapleFitness+(   SampledFitness[i]      * VariantFreq[SampledList.get(i)]   );

                if(!increament) {
                    Costs= Costs + (similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                    if(VariantChar[SampledList.get(i)].length() < AlignmentCosts[0]) {
                        upperBoundCost= upperBoundCost+ ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) ) *VariantFreq[SampledList.get(i)] );
                        if (similarVariantAlignCost< ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) )  )) {
                            approximateCosts2= approximateCosts2- ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                            approximateCosts2= ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) )  );
                            approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                        }
                    }
                }else {
                    increament=false;
                    upperBoundCost= upperBoundCost+(similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                }



            }
        }else {   //////simulation or XXX
            chCount= ModelBehaviorSimReverse.size();
            String trace2= "";
            for (int i = 0; i < SampledList.size(); i++) {
                double similarVariantcost =1000000;
                //int x2=  levenshteinDistanceCost("a","bazz"); for test
                for (int j = 0; j < ModelBehaviorSimReverse.size(); j++) {
                    int temp=levenshteinDistanceCost(VariantChar[SampledList.get(i)], ModelBehaviorSimReverse.get(j) );
                    if( temp   < similarVariantcost) {
                        similarVariantcost=temp  ;
                        trace2= ModelBehaviorSimReverse.get(j) ;

                    }

                }
                distancesApprox[SampledList.get(i)]= similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length() );
                double similarVariantAlignCost=similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());

                SampledFitness[i] =1 - similarVariantAlignCost  ;



                if (parameters.getSimilarityMeasure()== SimilarityMeasure.Incremental && similarVariantAlignCost >0.15) {
                    increament=true;
                    chCount2++;
                    futures = new Future[1];
                    TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, VariantMapper.get(SampledList.get(i)), 0, timeoutMilliseconds,
                            RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
                    // submit for execution
                    futures[0] = service.submit(task);
                    TraceReplayTask result;
                    try {
                        result = futures[0].get();
                    } catch (Exception e) {
                        // execution os the service has terminated.
                        assert false;
                        throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
                    }
                    SyncReplayResult replayResult = result.getSuccesfulResult();
                    double x = replayResult.getInfo().get("Raw Fitness Cost");

                    List<Object> ModelBehavior = replayResult.getNodeInstance();
                    List<StepTypes> TypeBehavior = replayResult.getStepTypes();
                    String modelTrace="";
                    for (int j=0;j<replayResult.getNodeInstance().size();j++) {
                        if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
                            if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
                                modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
                            }
                            else {
                                charcounter++;
                                ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
                                modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
                            }
                        }
                    }

                    if (ModelBehaviorSim.get(modelTrace)==null) {
                        ModelBehaviorSim.put(modelTrace, chCount);
                        ModelBehaviorSimReverse.put(chCount, modelTrace);
                        chCount++;
                    }
                    similarVariantAlignCost= x*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());
                    SampledFitness[i] =1 - similarVariantAlignCost  ;
                    approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                    approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                }

                distancesApprox[SampledList.get(i)]= similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length() );
                SampledFitness[i]=1-similarVariantAlignCost;
                approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                SmapleFitness= SmapleFitness+(   SampledFitness[i]      * VariantFreq[SampledList.get(i)]   );
                sampledFreq= (int) (sampledFreq+VariantFreq[SampledList.get(i)]);
                if(!increament) {
                    Costs= Costs + (similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                    if(VariantChar[SampledList.get(i)].length() < AlignmentCosts[0]) {
                        upperBoundCost= upperBoundCost+ ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) ) *VariantFreq[SampledList.get(i)] );
                    }
                }else {
                    increament=false;
                    upperBoundCost= upperBoundCost+(similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
                }
            }

        } ////simulation



        SampledFitnessApproximationValue= 1-(approximateCosts*1.0/sampledFreq);
        SampledFitnessApproximationValue2= 1-(approximateCosts2*1.0/sampledFreq);
        double SampledFitnessValue= SmapleFitness*1.0/sampledFreq;

        double computedFitness=0; int computedFreq=0;
        double computedAlignCost=0;
        for (int i = 0; i < SelectedList.size(); i++) {
            computedFitness= computedFitness+(FitnessValues[i]*VariantFreq[SelectedList.get(i)]);
            computedAlignCost= computedAlignCost+((1-FitnessValues[i])*VariantFreq[SelectedList.get(i)]);
            computedFreq=(int) (computedFreq+VariantFreq[SelectedList.get(i)]);
        }

        double computedFitnessValue=0;

        if (parameters.getPrototypeType()!=PrototypeType.Simulation) {
            computedFitnessValue=computedFitness/computedFreq;
        }


        double LowerBoundCosts = Costs*1.0 /LogSize;
        double LowerBoundFitness = ( computedFitness+ SmapleFitness)*1.0 / (sampledFreq+computedFreq);
        //double ApproximatedFitness= LowerBoundFitness+ (LowerBound*computedFitnessValue);
        double ApproximatedFitness= ( computedFitness + (SampledFitnessApproximationValue*sampledFreq) )*1.0 / (sampledFreq+computedFreq);

        double ApproximatedFitness2= ( (computedFitnessValue *computedFreq) + (SampledFitnessApproximationValue2*sampledFreq) )*1.0 / (sampledFreq+computedFreq);

        long Totaltime = System.currentTimeMillis() - time ;
        time= System.currentTimeMillis();
        double UpperBound =1- (( computedAlignCost + upperBoundCost) *1.0/LogSize);

        //			if (parameters.getPrototypeType()==PrototypeType.Simulation){
        //					ApproximatedFitness=  (LowerBoundFitness + UpperBound )/2;
        //
        //					}
        Set<String> T = SyncrousMoves.keySet();
        for (Iterator t = T.iterator(); t.hasNext();) {
            String string = (String) t.next();
            AsyncrousDistribution.put(string, (double) (AsyncrousMoves.get(string)*1.0/(SyncrousMoves.get(string)+AsyncrousMoves.get(string))));
        }


        if (ApproximatedFitness2> UpperBound)
            ApproximatedFitness2=(UpperBound+LowerBoundFitness)/2;
        String outp=( LowerBoundFitness +"==>>"+ UpperBound+"==>>"+ ApproximatedFitness+"==>>"+ LowerBoundCosts +"==>>" + Totaltime+"==>>" + MBTime+"==>>" +PreprocessTime +"==>>" +ApproximatedFitness2 +"==>>"+ModelBehaviorSim.size());// +"==>>"+ (double)(chCount2+SelectedList.size())/VariantMapper.size()+"==>>"+ AsyncrousDistribution);
        String htmlDeviation="<tr>\n" +"<td style=\"width: 200px; font-size: 1.2em;\">Activities</td>\n" + "<td style=\"width: 200px; font-size: 1.2em;color: green;\">Synchronous Moves</td>\n" + "<td style=\"width: 200px; font-size: 1.2em; color: red;\">Asynchronous Moves</td>\n" +"<tr>\n"
                +"<tr>\n" +"<tr>\n";
        for (Iterator t = T.iterator(); t.hasNext();) {
            String string = (String) t.next();
            AsyncrousDistribution.put(string, (double) (AsyncrousMoves.get(string)*1.0/(SyncrousMoves.get(string)+AsyncrousMoves.get(string))));
            htmlDeviation=htmlDeviation +"<tr>\n" +
                    "<td style=\"font-size: 1.2em;\">"+string+"</td>\n"  +
                    "<td style=\"font-size: 1.2em;color: green;\">"+SyncrousMoves.get(string)+"</td>\n"+
                    "<td style=\"font-size: 1.2em;color: red;\">"+AsyncrousMoves.get(string)+"</td>\n"
                    +"<tr>\n" ;
        }
        System.out.println(String.format("Time taken for approximate conformance checking %d msc", System.currentTimeMillis()- MBTime));


        String outp2 = "<html><table>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td style=\"width: 100px; font-size: 1.2em;\">Upper Bound:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+UpperBound+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 100px; font-size: 1.2em;\">Approximation1:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+ApproximatedFitness+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 100px; font-size: 1.2em;\">Approximation2:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+ApproximatedFitness2+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 100px; font-size: 1.2em;\">Lower Bound:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+LowerBoundFitness+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 100px; font-size: 1.2em;height: 300px\">&nbsp;</td>\n" +
                "</tr>\n" +
                htmlDeviation+
                "</tbody>\n" +
                "</table></html>";
        //////////////////////////////////Actual Fitness/////////////////////////////////////////////
		/*		double[] AlignmentCosts2= new double [InputLog.size()];
			 futures = new Future[InputLog.size()];
			 service = Executors.newFixedThreadPool(RepParameters.nThreads);
			 for (int i = 0; i < InputLog.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, InputLog.get(i), i, timeoutMilliseconds,
			 				RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
			 		// submit for execution
			 		futures[i] = service.submit(task);
			 	}
			 for (int i = 0; i < InputLog.size(); i++) {
			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		AlignmentCosts2[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 	}
			 double ActualFitness=0;
			 for (int i = 0; i < AlignmentCosts2.length; i++) {
				 ActualFitness= ActualFitness + (1-(AlignmentCosts2[i]*1.0/ (AlignmentCosts[0]+InputLog.get(i).size() )  ));
			}
			 double ActualFitnessValue=ActualFitness/InputLog.size();

			 long timer3= System.currentTimeMillis() - time ;
			 double PerformanceImprovement= timer2 *1.0 / timer2;
			 double AccuracyClosness= Math.abs(ApproximatedFitness -  ActualFitnessValue );
		///////////////////////////////////////////////////////////////////////////////////////////


			 AlignFit=0;
			double  LogSize2=0;
			 XLog TraceLog2 = factory.createLog();
			 TraceLog2.setAttributes(InputLog.getAttributes());
			 TraceLog2.setAttributes(InputLog.getAttributes());

			  case_id = String.valueOf(charcounter+2000000);
			 case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
			 XTraceImpl trace2 = new XTraceImpl(case_map);
			 TraceLog2.add(trace2);
 for (int i = 0; i < VariantFreq.length; i++) {

				 TraceLog2.add(VariantMapper.get(i));

				// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
			}//
 			AlignmentCosts=	 new double [TraceLog2.size()];
 			FitnessValues= new double [TraceLog2.size()-1];
 				futures = new Future[TraceLog2.size()];
			 for (int i = 0; i < TraceLog2.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog2.get(i), i, timeoutMilliseconds,
			 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);

			 		futures[i] = service.submit(task);
			 	}
			 for (int i = 0; i < TraceLog2.size(); i++) {

			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		 AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 		for (int j=0;j<replayResult.getStepTypes().size();j++) {
			 			if(replayResult.getStepTypes().get(j).toString().equals("Log move") || replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 				//System.out.println(replayResult.getNodeInstance().get(j).toString());
			 				if(replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 					if(mapping.containsKey(replayResult.getNodeInstance().get(j))) {
			 						asynchronousMoveBag.add(mapping.get(replayResult.getNodeInstance().get(j)).toString());
			 					}
			 					else {
			 						asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 					}
			 				}
			 				if(replayResult.getStepTypes().get(j).toString().equals("Log move")) {
			 					asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 				}
			 			}
			 		}
			 	}
			 for (int i = 0; i < FitnessValues.length; i++) {
				 FitnessValues[i]= 1- ( AlignmentCosts[i+1]*1.0/ (AlignmentCosts[0]+VariantChar[i].length() ));
				 AlignFit= AlignFit+ (FitnessValues[i]* VariantFreq[i]);

				 LogSize2=LogSize2+VariantFreq[i];
			}

			 double AlignFit2=AlignFit/LogSize2;


			 boolean flag= false;
			 for (int i = 0; i < FitnessValues.length; i++) {
				if (FitnessValues[i] < SampledFitness[i]) {
					flag=true;
				}

			} */

        return outp2;






    }

    private static void handleFirstSelection(int[] VariantInd, double Threshold, List<Integer> SampledList) {
        for (int i = 0; i < VariantInd.length; i++) {
            if (i< (1- Threshold)* VariantInd.length) {
                SelectedList.put(i, VariantInd[i]);
            }else {
                SampledList.add(VariantInd[i]);
            }
        }

        // Ahmed Awad
        // Now add those sampled to a new log
        XLog firstLog =  factory.createLog();
        firstLog.setAttributes(InputLog.getAttributes());
        for (Integer i : SelectedList.keySet())
            firstLog.add(VariantMapper.get(i));
        XesXmlSerializer ser = new XesXmlSerializer();
        OutputStream os;
        try {
            os = new FileOutputStream("C:\\\\Work\\DSG\\Data\\firstLog.xml");
            ser.serialize(firstLog, os);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private static void handleFrequencySelection(float[] VariantFreq, int[] VariantInd, double Threshold, List<Integer> SampledList) {
        int counter;
        OutputStream os;
        XesXmlSerializer ser;
        quicksort (VariantFreq.clone(), VariantInd);
        counter=0;
        for (int i = 0; i < VariantInd.length; i++) {
            if (i< Threshold * VariantInd.length) {
                SampledList.add(VariantInd[i]);
            }else {
                SelectedList.put(counter, VariantInd[i]);
                counter++;
            }

        }
        // Ahmed Awad
        // Now add those sampled to a new log
        XLog frequencyLog =  factory.createLog();
        frequencyLog.setAttributes(InputLog.getAttributes());
        for (Integer i : SelectedList.keySet())
            frequencyLog.add(VariantMapper.get(i));
        ser = new XesXmlSerializer();

        try {
            os = new FileOutputStream("C:\\Work\\DSG\\Data\\frequencyLog.xml");
            ser.serialize(frequencyLog, os);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private static void handleSimulationSelection(Petrinet net, int[] VariantInd, double Threshold, List<Integer> SampledList) {
        XAttributeMap eventAttributeMap;
        OutputStream os;
        XesXmlSerializer ser;
        long initialDate=0;

        int simulationSize=0;
        simulationSize=(int) ((1- Threshold)*10000);
        simulationSize=10000;
        SimulationSettings SimSet=  new SimulationSettings(0,simulationSize, maxTraceLength+2,initialDate,  new Exponential(1.0/3600000.0, new DRand(new Date(System.currentTimeMillis()))), new Exponential(1.0/3600000.0, new DRand(new Date(System.currentTimeMillis()))));
        SimulatorCombo lgSimulator= new SimulatorCombo(net,initialMarking,SimSet, factory );
        XLog VariantModelog = lgSimulator.simulate();
        //Ahmed Awad: just write the log resulting from simulation to the disk
        ser = new XesXmlSerializer();

        try {
            os = new FileOutputStream("C:\\Work\\DSG\\Data\\simulatedLog.xml");
            ser.serialize(VariantModelog, os);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        chCount=0;
        for (XTrace trace : VariantModelog) { // for each trace
            if(trace.size()>0) {
                String[] Trace = new String[trace.size()];
                List<String> templist = new ArrayList<String>();
                for (XEvent event : trace) {
                    eventAttributeMap = event.getAttributes();
                    templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
                }
                Trace = templist.toArray(new String[trace.size()]);
                String tr= Trace[0];
                String TraceinChar=ActivityCoder.get(tr);
                for (int i =1; i < Trace.length; i++){
                    TraceinChar= TraceinChar.concat(ActivityCoder.get(Trace[i]));
                }

                if (ModelBehaviorSim.get(TraceinChar)==null ){
                    ModelBehaviorSim.put(TraceinChar, chCount);
                    ModelBehaviorSimReverse.put(chCount, TraceinChar);
                    chCount++;
                }
            }
        }
        for (int i = 0; i < VariantInd.length; i++) {

            SampledList.add(i);

        }
    }

    private static void handleClusteringSelection(float[] VariantFreq, int[] VariantInd, double Threshold, double[][] distanceMatrix, List<Integer> SampledList) {
        OutputStream os;
        XesXmlSerializer ser;
        int K= (int) ((1- Threshold)* VariantInd.length);

        int iterations=2;
        for (int i = 0; i < K ; i++) {
            List<Integer> tempList = new ArrayList<>();
            Clusters.put(i, tempList);
            SelectedList.put(i,i);
        }////KMEDOIDS
        for (int i = 0; i < iterations; i++) {
            Clusters=  FindCluster(distanceMatrix,SelectedList);
            SelectedList= UpdateKMedoids(SelectedList, distanceMatrix, VariantFreq,Clusters);
        }
        for (int i = 0; i < SelectedList.size(); i++) {
            pickedVariant.add(SelectedList.get(i));
        }
        for (int i = 0; i < VariantInd.length; i++) {
            if (!pickedVariant.contains(i)) {
                SampledList.add(i);
            }
        }
        // Ahmed Awad
        // Now add those sampled to a new log
        XLog sampledClusteredLog =  factory.createLog();
        sampledClusteredLog.setAttributes(InputLog.getAttributes());
        for (Integer i : SampledList)
            sampledClusteredLog.add(VariantMapper.get(i));
        ser = new XesXmlSerializer();

        try {
            os = new FileOutputStream("C:\\Work\\DSG\\Data\\sampledClusteredLog.xml");
            ser.serialize(sampledClusteredLog, os);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private static void handleRandomSelection(int[] VariantInd, double Threshold, List<Integer> SampledList) {
        int counter;
        OutputStream os;
        XesXmlSerializer ser;
        Random generator = new Random(System.currentTimeMillis());
        chCount=0;
        ArrayList<Integer> TraceNumber = new ArrayList<Integer>();
        XLog outputLog2 = factory.createLog();
        outputLog2.setAttributes(InputLog.getAttributes());
        for (int i = 0; i < InputLog.size(); i++) {
            TraceNumber.add(i);
        }
        while (chCount < (1- Threshold) * VariantInd.length )  {
            int index =ThreadLocalRandom.current().nextInt(0, TraceNumber.size());
            outputLog2.add(InputLog.get(TraceNumber.get(index)));
            TraceNumber.remove(TraceNumber.get(index));// Do not select this trace again
            int randomTrace=generator.nextInt(VariantInd.length-chCount);
            pickedVariant.add(randomTrace);
            chCount++;
        }//while
        counter=0;
        for (int i = 0; i < VariantInd.length; i++) {
            if (pickedVariant.contains(i)) {
                SelectedList.put(counter, i);
                counter++;
            }else {
                SampledList.add(i);
            }

        }
        // Ahmed Awad
        // Now add those sampled to a new log
        XLog randomLog =  factory.createLog();
        randomLog.setAttributes(InputLog.getAttributes());
        for (Integer i : SampledList)
            randomLog.add(VariantMapper.get(i));
        ser = new XesXmlSerializer();

        try {
            os = new FileOutputStream("C:\\\\Work\\DSG\\Data\\randomLog.xml");
            ser.serialize(randomLog, os);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private static String handleActivitySelection(Petrinet net, float[] VariantFreq, String[] VariantChar, double Threshold) {
        OutputStream os;
        XesXmlSerializer ser;
        float [] activityFreq= new float[ActivityCounter.size()];
        int [] activityInd= new int[ActivityCounter.size()];
        HashMap<Integer,String> activityDecodeIndex =new HashMap< Integer,String>();
        List<Integer> infreqActivitiesInd=  new ArrayList<>();
        List<Integer> freqActivitiesInd=  new ArrayList<>();
        List<String> infreqActivities=  new ArrayList<>();
        List<String> freqActivities=  new ArrayList<>();
        charcounter=65;
        for (int i = 0; i < ActivityCounter.size(); i++) {
            charcounter++;
            activityFreq[i]= ActivityCounter.get(Character.toString((char)charcounter));
            activityInd[i]=i;
            activityDecodeIndex.put(i, Character.toString((char)charcounter));
        }
        quicksort (activityFreq.clone(), activityInd);

        for (int i = 0; i < activityInd.length - Math.round(ActivityCounter.size()* Threshold); i++) {
            infreqActivitiesInd.add(activityInd[i]);
            infreqActivities.add(activityDecodeIndex.get(activityInd[i]));
        }
        for (int i = (int) (activityInd.length - Math.round(ActivityCounter.size()* Threshold)); i < activityInd.length; i++) {
            freqActivitiesInd.add(activityInd[i]);
            freqActivities.add(activityDecodeIndex.get(activityInd[i]));
        }
        double[] VariantDifferences= new double [VariantMapper.size()+1];
        double[] AlignmentupperboundCost= new double [VariantMapper.size()+1];
        XLog reducedTraceLog = factory.createLog();
        reducedTraceLog.setAttributes(InputLog.getAttributes());
        XAttributeMapImpl case_map2 = new XAttributeMapImpl();
        String case_id2 = String.valueOf(charcounter);
        case_map2.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id2));
        XTraceImpl emptyTrace = new XTraceImpl(case_map2);
        reducedTraceLog.add(emptyTrace);

        boolean flagDiff = false;
        int counter3=1;
        tempMapper = (HashMap<Integer,String>) charecterTraces.clone();
        Iterator variants = tempMapper.entrySet().iterator();
        while (variants.hasNext()) {
            Map.Entry pair = (Map.Entry)variants.next();
            String tempVariant= (String) pair.getValue();
            XTrace tempTrace = (XTrace) VariantMapper.get(pair.getKey());
            String tr="";
            int count=-1;int differenceCounts=0;
            for (char activity: tempVariant.toCharArray()) {
                count++;
                if(!infreqActivities.contains(String.valueOf(activity)) ) {
                    tr=tr+activity;
                }else {
                    flagDiff=true;
                }
                if(flagDiff) {
                    tempTrace.remove(count);
                    count--;
                    differenceCounts++;
                }
                flagDiff=false;
            }

            reducedTraceLog.add(tempTrace);
            ReducedVariants.put(tempVariant, tr);
            VariantDifferences[counter3]=differenceCounts;
            variants.remove(); // avoids a ConcurrentModificationException
            counter3++;
        }
        //Ahmed Awad: just want to save the log to disk
        ser = new XesXmlSerializer();

        try {
            os = new FileOutputStream("C:\\Work\\DSG\\Data\\\\reducedLogActivity.xml");
            ser.serialize(reducedTraceLog, os);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        long preprocessing =  System.currentTimeMillis();
        ReplayerParameters RepParameters2 = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
        Replayer replayer2 = new Replayer(RepParameters2, net, initialMarking, finalMarking, classes, mapping, false);
        Future<TraceReplayTask>[] futures2 = new Future[reducedTraceLog.size()];
        ExecutorService service2 = Executors.newFixedThreadPool(RepParameters2.nThreads);

        for (int i = 0; i < reducedTraceLog.size(); i++) {
            // Setup the trace replay task
            TraceReplayTask task = new TraceReplayTask(replayer2, RepParameters2, reducedTraceLog.get(i), i, timeoutMilliseconds,
                    RepParameters2.maximumNumberOfStates, preProcessTimeNanoseconds);
            // submit for execution
            futures2[i] = service2.submit(task);
        }
        double AlignFit2=0;
        double AlignCost2=0;
        // obtain the results one by one.
        chCount=0;

        double[] AlignmentCosts2= new double [reducedTraceLog.size()];
        double[] AlignmentCostsReduced= new double [reducedTraceLog.size()];
        double[] AlignmentCostsm= new double [reducedTraceLog.size()];
        double[] FitnessValues2= new double [reducedTraceLog.size()-1];
        double[] FitnessValuesLowerbound= new double [reducedTraceLog.size()-1];
        double[] FitnessValuesUpperbound= new double [reducedTraceLog.size()-1];
        for (int i = 0; i < reducedTraceLog.size(); i++) {

            TraceReplayTask result2;
            try {
                result2 = futures2[i].get();
            } catch (Exception e) {
                // execution os the service has terminated.
                assert false;
                throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
            }
            SyncReplayResult replayResult2 = result2.getSuccesfulResult();
            AlignmentCosts2[i]= replayResult2.getInfo().get("Raw Fitness Cost");
            AlignmentupperboundCost[i]=AlignmentCosts2[i]+VariantDifferences[i];
            List<Object> ModelBehavior2 = replayResult2.getNodeInstance();
            List<StepTypes> TypeBehavior2 = replayResult2.getStepTypes();
            String modelTrace="";
            for (int j=0;j<replayResult2.getNodeInstance().size();j++) {
                /*if(i>0) {
                             if(TypeBehavior2.get(j).equals(StepTypes.L)||TypeBehavior2.get(j).equals(StepTypes.MREAL)) {
                                 AsyncrousMoves.put(ModelBehavior2.get(j).toString(), (int) (AsyncrousMoves.get(ModelBehavior2.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                                 if(TypeBehavior2.get(j).equals(StepTypes.L))
                                     LogMoves.put(ModelBehavior2.get(j).toString(), (int) (LogMoves.get(ModelBehavior2.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                                 else
                                     ModelMoves.put(ModelBehavior2.get(j).toString(), (int) (ModelMoves.get(ModelBehavior2.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                             }else if(TypeBehavior2.get(j).equals(StepTypes.LMGOOD))
                                 SyncrousMoves.put(ModelBehavior2.get(j).toString(), (int) (SyncrousMoves.get(ModelBehavior2.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                         }*/

                if(!ModelBehavior2.get(j).toString().contains("tau") && !TypeBehavior2.get(j).equals(StepTypes.MINVI)&& !TypeBehavior2.get(j).equals(StepTypes.L)) {

                    if(ActivityCoder.containsKey(ModelBehavior2.get(j).toString()) )  {
                        modelTrace=modelTrace + ActivityCoder.get(ModelBehavior2.get(j).toString());
                        if(infreqActivities.contains(ActivityCoder.get(ModelBehavior2.get(j).toString())))
                            AlignmentCostsReduced[i]++;
                    }
                    else {
                        charcounter++;
                        ActivityCoder.put(ModelBehavior2.get(j).toString(), Character.toString((char)charcounter));
                        modelTrace=modelTrace + ActivityCoder.get(ModelBehavior2.get(j).toString());
                    }
                }

            }

            if (ModelBehaviorSim.get(modelTrace)==null) {
                ModelBehaviorSim.put(modelTrace, chCount);
                ModelBehaviorSimReverse.put(chCount, modelTrace);
                chCount++;
            }
            if(i>0) {
                AlignmentCostsm[i]=levenshteinDistanceCost(modelTrace, VariantChar[i-1]);
            }

            /*					 		for (int j=0;j<replayResult2.getStepTypes().size();j++) {
                         if(replayResult2.getStepTypes().get(j).toString().equals("Log move") || replayResult2.getStepTypes().get(j).toString().equals("Model move")) {
                             //System.out.println(replayResult.getNodeInstance().get(j).toString());
                             if(replayResult2.getStepTypes().get(j).toString().equals("Model move")) {
                                 if(mapping.containsKey(replayResult2.getNodeInstance().get(j))) {
                                     asynchronousMoveBag.add(mapping.get(replayResult2.getNodeInstance().get(j)).toString());
                                 }
                                 else {
                                     asynchronousMoveBag.add((replayResult2.getNodeInstance().get(j)).toString());
                                 }
                             }
                             if(replayResult2.getStepTypes().get(j).toString().equals("Log move")) {
                                 asynchronousMoveBag.add((replayResult2.getNodeInstance().get(j)).toString());
                             }
                         }
                     }*/
        }
        double lowerboundCost= 0;
        double lowerboundFitness=0;
        double computedFitness=0;
        double upperboundFitness=0;
        int chCount3=0;
        for (int i = 0; i < FitnessValues2.length; i++) {
            chCount3= chCount3+ (int) VariantFreq[i];
            FitnessValues2[i]= 1- ( (AlignmentCostsm[i+1]+AlignmentCostsReduced[i+1])*1.0/ (AlignmentCosts2[0]+ VariantChar[i].length() ));
            if(AlignmentCosts2[i+1] <VariantDifferences[i+1])
                lowerboundCost= 0;
            else
                lowerboundCost= AlignmentCosts2[i+1]- VariantDifferences[i+1];
            FitnessValuesLowerbound[i]= 1- ( (AlignmentCostsm[i+1]) *1.0/ (AlignmentCosts2[0]+ VariantChar[i].length() ));
            FitnessValuesUpperbound[i]= 1- ( (lowerboundCost) *1.0/ (AlignmentCosts2[0]+ VariantChar[i].length() ));
            AlignFit2= AlignFit2+FitnessValues2[i];
            AlignCost2= AlignCost2+ (1- FitnessValues2[i]);
            lowerboundFitness= lowerboundFitness+ (FitnessValuesLowerbound[i]* VariantFreq[i]);
            computedFitness = computedFitness+ (FitnessValues2[i]* VariantFreq[i]);
            upperboundFitness= upperboundFitness+ (FitnessValuesUpperbound[i]* VariantFreq[i]);
        }
        lowerboundFitness= lowerboundFitness/InputLog.size();
        upperboundFitness= upperboundFitness/InputLog.size();
        computedFitness=computedFitness/InputLog.size();
        AlignFit2=AlignFit2/FitnessValues2.length;
        AlignCost2=AlignCost2/FitnessValues2.length;
        double apxfitness =(upperboundFitness+lowerboundFitness )/2;
        long totalTime =  System.currentTimeMillis()-time;
        long ComputationTime =  System.currentTimeMillis()-preprocessing;
        String outp2 = "<html><table>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td style=\"width: 150px; font-size: 1.2em;\">Upper Bound fitness:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+upperboundFitness+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 150px; font-size: 1.2em;\">Approximated Fitness:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+apxfitness+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 150px; font-size: 1.2em;\">Lower bound Fitness:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+lowerboundFitness+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 150px; font-size: 1.2em;\">computation Time:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+ComputationTime+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 150px; font-size: 1.2em;\">Total Time:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+totalTime+"</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td style=\"width: 150px; font-size: 1.2em;\">Removed Activities:</td>\n" +
                "<td style=\"font-size: 1.2em;\">"+infreqActivities.size()+"</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table></html>";
        return outp2;
    }

    private static void computeActualAlignment(Petrinet net, XLog TraceLog) {

//        RepParameters = new ReplayerParameters.AStar();//.Default(nThreads, costUpperBound, Debug.NONE);

        MBTime = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        replayer = new Replayer(RepParameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);
        futures = new Future[TraceLog.size()];


        for (int i = 0; i < TraceLog.size(); i++) {
            // Setup the trace replay task
            TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog.get(i), i, timeoutMilliseconds,
                    RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
            try {
                task.call();
            } catch (LPMatrixException e) {
                e.printStackTrace();
            }

            // submit for execution
            futures[i] = service.submit(task);
        }

        // obtain the results one by one.
        chCount=0;
        for (int i = 0; i < TraceLog.size(); i++) {

            TraceReplayTask result;
            try {
                result = futures[i].get();
            } catch (Exception e) {
                // execution os the service has terminated.
                assert false;
                //e.printStackTrace();
                Throwable th = e.getCause();
                System.out.println(th.toString());
                throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
            }
            SyncReplayResult replayResult = result.getSuccesfulResult();
            AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
            List<Object> ModelBehavior = replayResult.getNodeInstance();
            List<StepTypes> TypeBehavior = replayResult.getStepTypes();
            String modelTrace="";
            for (int j=0;j<replayResult.getNodeInstance().size();j++) {
                if(i>0) {
                    if(TypeBehavior.get(j).equals(StepTypes.L)||TypeBehavior.get(j).equals(StepTypes.MREAL)) {
                        AsyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (AsyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                        if(TypeBehavior.get(j).equals(StepTypes.L))
                            LogMoves.put(ModelBehavior.get(j).toString(), (int) (LogMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                        else
                            ModelMoves.put(ModelBehavior.get(j).toString(), (int) (ModelMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                    }else if(TypeBehavior.get(j).equals(StepTypes.LMGOOD))
                        SyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (SyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
                }

                if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {

                    if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
                        modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
                    }
                    else {
                        charcounter++;
                        ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
                        modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
                    }
                }

            }

            if (ModelBehaviorSim.get(modelTrace)==null) {
                ModelBehaviorSim.put(modelTrace, chCount);
                ModelBehaviorSimReverse.put(chCount, modelTrace);
                chCount++;
            }



        }

        //MBTime = System.currentTimeMillis() - MBTime ;

        chCount2=0;
        for (int i = 0; i < FitnessValues.length; i++) {
            chCount2= chCount2+ (int)VariantFreq[SelectedList.get(i)];
            FitnessValues[i]= 1- ( AlignmentCosts[i+1]*1.0/ (AlignmentCosts[0]+VariantChar[SelectedList.get(i)].length() ));
            AlignFit= AlignFit+FitnessValues[i];
            AlignCost= AlignCost+ (1- FitnessValues[i]);
        }
        AlignFit=AlignFit/FitnessValues.length;
        AlignCost=AlignCost/FitnessValues.length;
        MBTime = System.currentTimeMillis() - MBTime ;
        System.out.println(String.format("Time taken for running exact conformance checking %d msec", MBTime));
        System.out.println(String.format("Time taken for running exact conformance checking %d msec", System.currentTimeMillis() - start));

    }

    public static String apply2(PluginContext context,XLog log, Petrinet net, MatrixFilterParameter parameters,
                                TransEvClassMapping mapping) {
        // TODO Auto-generated method stub
        //Martin Bauer
        long time = System.currentTimeMillis();
        System.out.println(	System.currentTimeMillis() - time);
        XLog log2= (XLog) log.clone();

        double delta=parameters.getSubsequenceLength()*0.001;//0.01
        double alpha=0.99;
        double epsilon=parameters.getSubsequenceLength()*0.001;//0.01
        double k=0.5;
        int initialSize=20;
        String goal="fitness";
        boolean approximate=false;
        IccParameters iccParameters=new IccParameters(delta, alpha, epsilon, k, initialSize, goal, approximate);

        IncrementalReplayer replayer = null;
        if (goal.equals("fitness")&& !iccParameters.isApproximate()) {
            replayer=new FitnessReplayer(iccParameters);
        }
        if (goal.equals("fitness")&&iccParameters.isApproximate()) {
            replayer=new ApproxFitnessReplayer(iccParameters);
        }
        if (iccParameters.getGoal().equals("alignment")&& !iccParameters.isApproximate())
            replayer=new AlignmentReplayer(iccParameters);
        if (iccParameters.getGoal().equals("alignment") && iccParameters.isApproximate()) {
            replayer= new ApproxAlignmentReplayer(iccParameters);
            //replayer.init(context, net, log);
        }

        //make own parameter function for alignment/fitness
        AlignmentReplayResult result= calculateAlignmentWithICC(context, replayer, net, log2, iccParameters, mapping);

        result.setTime(System.currentTimeMillis()-time);
        System.out.println("Fitness         : "+result.getFitness());
        System.out.println("Time(ms)        : "+result.getTime());
        System.out.println("Log Size        : "+result.getLogSize());
        System.out.println("No AsynchMoves  : "+result.getTotalNoAsynchMoves());
        System.out.println("AsynchMoves abs : "+result.getAsynchMovesAbs().toString());
        System.out.println("AsynchMoves rel : "+result.getAsynchMovesRel().toString());
        long timer2 = System.currentTimeMillis() - time ;
        String outp=( 0 +"==>>"+ 1+"==>>"+ result.getFitness()+"==>>"+ result.getLogSize() +"==>>" + timer2+"==>>" +0+"==>>"+ result.getFitness())+"==>>"+ result.getLogSize() ;

        return outp;

    }

    static private double jaccardSimilarity(int[] a, int[] b) {

        Set<Integer> s1 = new HashSet<Integer>();
        for (int i = 0; i < a.length; i++) {
            s1.add(a[i]);
        }
        Set<Integer> s2 = new HashSet<Integer>();
        for (int i = 0; i < b.length; i++) {
            s2.add(b[i]);
        }

        final int sa = s1.size();
        final int sb = s2.size();
        s1.retainAll(s2);
        final int intersection = s1.size();
        return 1d / (sa + sb - intersection) * intersection;
    }


    static private double DiffSimilarity(int[] a, int[] b) {
        int Tdf=0;
        int Sdf=0;
        for (int i = 0; i < b.length; i++) {
            if (a[i]>0 || b[i]>0) {
                if (a[i]>0 && b[i]>0) {
                    Sdf++;
                }
                Tdf++;
            }

        }

        return  (double)(Sdf*1.0)/Tdf;
    }



    static class Variant
    {
        private int VariantIndex;
        private int IntScore;
        private double DoubleScore;

        public Variant()
        {

            this.VariantIndex = 0;
            this.IntScore = 0;
            this.DoubleScore=0.0;
        }
        public Variant(int ind, int intScore)
        {

            this.VariantIndex = ind;
            this.IntScore = intScore;
            this.DoubleScore=0.0;
        }
        public Variant(int ind, double doubleScore)
        {

            this.VariantIndex = ind;
            this.IntScore = 0;
            this.DoubleScore=doubleScore;
        }
        public Variant(int ind, int intScore,double doubleScore)
        {

            this.VariantIndex = ind;
            this.IntScore = intScore;
            this.DoubleScore=doubleScore;
        }
        public int getIntScore() {
            // TODO Auto-generated method stub
            return IntScore;
        }
        public double getDoubleScore() {
            // TODO Auto-generated method stub
            return DoubleScore;
        }
        public int getIndex() {
            // TODO Auto-generated method stub
            return VariantIndex;
        }


    }


    public static HashMap<Integer, List<Integer>> FindCluster (double[][] distanceMatrix,HashMap<Integer,Integer> SelectedList) {
        int[] belongCluster = new int[distanceMatrix.length];
        HashMap<Integer,List<Integer> >Clusters =new HashMap<Integer,List<Integer>>();
        int K = SelectedList.size();
        for (int i = 0; i < K ; i++) {
            List<Integer> tempList = new ArrayList<>();
            Clusters.put(i, tempList);
        }

        for (int i = 0; i < belongCluster.length; i++) {

            double cost=1000;
            int counter =0; int index=0;
            for (int j = 0; j < K; j++) {

                if (distanceMatrix[i][SelectedList.get(j)]< cost) {
                    cost =distanceMatrix[i][SelectedList.get(j)];
                    counter= SelectedList.get(j);
                    index=j;
                }


            }
            belongCluster[i]= index;
            List<Integer> List = Clusters.get(index);
            List.add(i);
            Clusters.put(index,List);

        }
        return Clusters;
    }


    public static HashMap<Integer, Integer> UpdateKMedoids(HashMap<Integer,Integer> SelectedList,double[][] distanceMatrix, float[] variantFreq, HashMap<Integer,List<Integer> > Clusters) {
        int K = SelectedList.size();
        for (int i = 0; i < K; i++) {

            List<Integer> List= Clusters.get(i);
            double distance = 980000000;

            for (int j = 0; j < List.size(); j++) {
                double cost = 0;
                int counter=0;

                for (int j2 = 0; j2 < List.size(); j2++) {
                    cost=cost+ (distanceMatrix[List.get(j)][List.get(j2)] * variantFreq[List.get(j2)]);
                }
                if( cost <distance) {
                    distance = cost;
                    SelectedList.put(i,List.get(j));
                }
            }




        }

        return SelectedList;

    }



    public static int levenshteinDistanceCost (CharSequence lhs, CharSequence rhs) {

        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;
        int maxLen= 0;
        if (len0>len1) {
            maxLen=len0;
        }
        else {
            maxLen= len1;
        }
        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1] ;
    }

    public static double levenshteinDistance (CharSequence lhs, CharSequence rhs) {

        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;
        int maxLen= 0;
        if (len0>len1) {
            maxLen=len0;
        }
        else {
            maxLen= len1;
        }
        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j-1;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return (cost[len0 - 1]*1.0 )/ (len1+len0) ;
    }


    public static AlignObj levenshteinDistancewithAlignment (CharSequence lhs, CharSequence rhs) {



        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;
        int maxLen= 0;
        if (len0>len1) {
            maxLen=len0;
        }
        else {
            maxLen= len1;
        }
        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];
        String[] costString = new String[len0];
        String[] newcostString = new String[len0];
        String[][] alignment= new String [len1][len0];
        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = i;
        alignment[0][0] ="";newcostString[0] ="";
        for (int i = 1; i < len0; i++)
            alignment[0][i] =alignment[0][i-1]+ ">> Deletion " + lhs.charAt(i-1);
        for (int i = 1; i < len1; i++)
            alignment[i][0] =alignment[i-1][0]+ ">> Insertion " + rhs.charAt(i-1);
        // dynamically computing the array of distances
        boolean deleted= true;
        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j-1;
            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
                if (match==0)
                    alignment[j][i]= alignment[j-1][i-1]+">> Sync "+ rhs.charAt(j - 1);
                else { if (Math.min(cost_insert, cost_delete) < cost_replace )
                {
                    if(cost_insert>cost_delete)
                        alignment[j][i]= alignment[j][i-1]+">> Deletion " + lhs.charAt(i-1);
                    else
                        alignment[j][i]= alignment[j-1][i]+ ">> Insertion " + rhs.charAt(j-1);
                }
                else
                    alignment[j][i]= alignment[j-1][i-1]+ ">> Insertion " + rhs.charAt(j-1) +">> Deletion " + lhs.charAt(i-1);

                }
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;


        }
        String align = alignment[len1-1][len0-1].substring(2);
        if (lhs.charAt(0)!=rhs.charAt(0))
            cost[len0-1]++; // otherwise the method discounts error if string first letters do not match

//        AlignObj alignObj = new AlignObj(align, (cost[len0 - 1]*1.0 ) / (len1+len0)) ;
        AlignObj alignObj = new AlignObj(align, cost[len0 - 1]) ;
        // the distance is the cost for transforming all letters in both strings
        return alignObj;//alignment[len0][len0-1] ;
    }



    public static void quicksort(float[] main, int[] index) {
        quicksort(main, index, 0, index.length - 1);
    }

    // quicksort a[left] to a[right]
    public static void quicksort(float[] a, int[] index, int left, int right) {
        if (right <= left) return;
        int i = partition(a, index, left, right);
        quicksort(a, index, left, i-1);
        quicksort(a, index, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(float[] a, int[] index,
                                 int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(a[++i], a[right]))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (less(a[right], a[--j]))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, index, i, j);               // swap two elements into place
        }
        exch(a, index, i, right);               // swap with partition element
        return i;
    }

    // is x < y ?
    private static boolean less(float x, float y) {
        return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(float[] a, int[] index, int i, int j) {
        float swap = a[i];
        a[i] = a[j];
        a[j] = swap;
        int b = index[i];
        index[i] = index[j];
        index[j] = b;
    }




    private static Marking getInitialMarking(PetrinetGraph net) {
        Marking initMarking = new Marking();

        for (Place p : net.getPlaces()) {
            if (net.getInEdges(p).isEmpty())
                initMarking.add(p);
        }

        return initMarking;
    }


    private static Marking getFinalMarking(PetrinetGraph net) {
        Marking finalMarking = new Marking();

        for (Place p : net.getPlaces()) {
            if (net.getOutEdges(p).isEmpty())
                finalMarking.add(p);
        }

        return finalMarking;
    }



    private static TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClass dummyEvClass,
                                                        XEventClassifier eventClassifier) {
        TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

        XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

        for (Transition t : net.getTransitions()) {
            boolean mapped = false;

            for (XEventClass evClass : summary.getEventClasses().getClasses()) {
                String id = evClass.getId();
                String label = t.getLabel();

                if (label.equals(id)) {
                    mapping.put(t, evClass);
                    mapped = true;
                    break;
                }
            }
        }

        return mapping;
    }





    public static AlignmentReplayResult calculateAlignmentWithICC(final PluginContext context, IncrementalReplayer replayer, PetrinetGraph net, XLog log, IccParameters parameters, TransEvClassMapping mapping)
    {

        IncrementalConformanceChecker icc =new IncrementalConformanceChecker(context, replayer, parameters, log, net);
        IccResult iccresult = icc.apply(context, log, net, mapping);
        Map<String, Integer> asynchMoveAbs=new TreeMap<String, Integer>();
        Map<String, Double> asynchMoveRel=new TreeMap<String, Double>();

        if(parameters.getGoal().equals("alignment")) {
            int asynchMovesSize=iccresult.getAlignmentContainer().getAsynchMoves().size();

            for (String key:iccresult.getAlignmentContainer().getAsynchMoves().elementSet()) {
                int absValue=iccresult.getAlignmentContainer().getAsynchMoves().count(key);
                double relValue=(double)absValue/(double)asynchMovesSize;
                asynchMoveAbs.put(key, absValue);
                asynchMoveRel.put(key, relValue);
            }
            AlignmentReplayResult result = new AlignmentReplayResult(iccresult.getFitness(), iccresult.getTime(), iccresult.getTraces(), asynchMovesSize, asynchMoveAbs, asynchMoveRel);
            return result;
        }
        else {
            AlignmentReplayResult result = new AlignmentReplayResult(iccresult.getFitness(), iccresult.getTime(), iccresult.getTraces(), -1, asynchMoveAbs, asynchMoveRel);
            return result;
        }

    }



    public static class AlignObj {
        public final String Alignment;
        public final double cost;

        public AlignObj(String Alignment, double d) {
            this.Alignment = Alignment;
            this.cost = d;
        }
    }





}


























