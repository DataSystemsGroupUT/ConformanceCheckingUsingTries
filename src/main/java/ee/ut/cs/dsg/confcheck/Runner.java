package ee.ut.cs.dsg.confcheck;

import com.raffaeleconforti.noisefiltering.event.infrequentbehaviour.automaton.AutomatonInfrequentBehaviourDetector;
import ee.ut.cs.dsg.TreeBasedHolisticConformanceChecker;
import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.AlignmentFactory;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Utils;
import gnu.trove.impl.sync.TSynchronizedShortByteMap;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logfiltering.algorithms.ProtoTypeSelectionAlgo;
import org.processmining.logfiltering.algorithms.SPMF.RuleGrowth.AlgoCMDeogun;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SimilarityMeasure;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.SamplingReturnType;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static ee.ut.cs.dsg.confcheck.util.Configuration.ConformanceCheckerType;
import static ee.ut.cs.dsg.confcheck.util.Configuration.LogSortType;
public class Runner {

    private static double logCoverage=0.0;

    public static void main(String... args)
    {
//        testBed2();
//        System.exit(0);
//        testBed1();
//        testConformanceApproximation();
//
//        testJNI();

//        testBed3();
//        testBed5();
////        testBed4();
//        System.exit(0);
//        System.out.println(Utils.parseReachabilityGraphFileInPetrifyFormatToNeo4JCommands("C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\M1-RG.sg"));
//        System.exit(0);

//        String ILPM1Alignnment = "C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\BPI2017-Alignment.csv";
//        List<Alignment> algs = Utils.getAlignmentsFromCSVFile(ILPM1Alignnment);
//
//        for (Alignment alg: algs)
//        {
//            System.out.println(String.format("%s,%f,%d", alg.getTraceID(), alg.weightedLogCoverage(),alg.getTotalCost() ));
//        }
//        System.exit(0);

//        Utils.unfoldFileOfResults("C:\\Work\\DSG\\RL-align-master\\data\\alignments\\ILPSDP\\BPIC2017.csv", "C:\\Work\\DSG\\RL-align-master\\data\\alignments\\ILPSDP\\BPIC2017-unfolded.csv");
//        System.exit(0);
        String randomProxyLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\randomLog.xml";

        String clusteredLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\sampledClusteredLog.xml";
        String clusteredShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\sampledClusteredLog-1-5-loop.txt";

        String simulatedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\simulatedLog.xml";
        String simulatedLogUnix = "/home/ubuntu/confCheck/Data/Logs/BPI2015/simulatedLog.xml";
        String reducedActivityLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\reducedLogActivity.xml";
//        String reducedActivityLog = "/home/ubuntu/confCheck/Data/Logs/BPI2015/reducedLogActivity.xml";
        String frequencyActivityLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\frequencyLog.xml";
        String frequencyActivityShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\frequencyLog-1-5-loop.txt";

        String sampleLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\sampledLog.xml";
        String sampleShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\sampledLog-1-5-loop.txt";
        String sampleLogUnix = "/home/ubuntu/confCheck/Data/Logs/BPI2015/sampledLog.xml";

        String singular = "C:\\Work\\DSG\\Data\\Logs\\BPI2015\\Singular.xes";

        String randomSepsisProxyLog = "C:\\Work\\DSG\\Data\\Logs\\Sepsis\\randomLog.xml";
        String clusteredSepsisLog = "C:\\Work\\DSG\\Data\\Logs\\Sepsis\\sampledClusteredLog.xml";
        String simulatedSepsisLog = "C:\\Work\\DSG\\Data\\Logs\\Sepsis\\simulatedLog.xml";
        String frequencySepsisLog = "C:\\Work\\DSG\\Data\\Logs\\Sepsis\\frequencyLog.xml";
        String reducedSepsisActivityLog = "C:\\Work\\DSG\\Data\\Logs\\Sepsis\\reducedLogActivity.xml";
        String sampleSepsisLog = "C:\\Work\\DSG\\Data\\Logs\\Sepsis\\sampledLog.xml";

        // BPI 2019
        String originalLog2019 = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\BPI_Challenge_2019.xml";
        String random2019ProxyLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\randomLog.xml";
        String random2019ProxyShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\randomLog-1-5-loop.txt";
        String clustered2019Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\sampledClusteredLog.xml";
        String clustered2019ShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\sampledClusteredLog-1-5-loop.txt";
        String simulated2019Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\simulatedLog.xml";
        String reduced2019ActivityLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\reducedLogActivity.xml";
        String sample2019Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\sampledLog.xml";
        String sample2019ShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\sampledLog-1-5-loop.txt";
        String frequency2019Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2019\\frequencyLog.xml";

        // BPI 2012
        String originalLog2012 = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\BPIC2012.xes";
        String random2012ProxyLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\randomLog.xml";
        String clustered2012Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\sampledClusteredLog.xml";
        String clustered2012ShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\sampledClusteredLog-1-5-loop.txt";
        String simulated2012Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\simulatedLog.xml";
        String reduced2012ActivityLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\reducedLogActivity.xml";
        String sample2012Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\sampledLog.xml";
        String sample2012ShortenedLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\sampledLog-1-5-loop.txt";
        String frequency2012Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2012\\frequencyLog.xml";

        // BPI 2017
        String originalLog2017 = "C:\\Work\\DSG\\Data\\Logs\\BPI2017\\BPIC2017.xes.xes";
        String random2017ProxyLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2017\\rand_randomLog.xml";
        String clustered2017Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2017\\sampledClusteredLog.xml";
        String simulated2017Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2017\\simulatedLog.xml";
//        String simulated2017Log = "/home/ubuntu/confCheck/Data/Logs/BPI2017/simulatedLog.xml";
        String reduced2017ActivityLog = "C:\\Work\\DSG\\Data\\Logs\\BPI2017\\reducedLogActivity.xml";
        String sample2017Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2017\\sampledLog.xml";
        String sample2017LogUnix = "/home/ubuntu/confCheck/Data/Logs/BPI2017/sampledLog.xml";
        String frequency2017Log = "C:\\Work\\DSG\\Data\\Logs\\BPI2017\\freq_frequencyLog.xml";

        String proxyLogM1 = "C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\M1-Simulated-ProM.xes";
        String logM1 ="C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\M1.xes";

        String proxyLogM2 = "C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\M2-Simulated-ProM.xes";
        String logM2 ="C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\M2.xes";

        String proxyLogM3 = "C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\M3-Simulated-ProM.xes";
        String logM3 ="C:\\Work\\DSG\\RL-align-master\\data\\originals\\M-models\\M3.xes";

        String proxyLogBPI2020 = "C:\\Users\\Ahmed Awad\\Downloads\\input Data\\BPI_2020_Sim_2k_random_0.2.xes";
        String logBPI2020 ="C:\\Users\\Ahmed Awad\\Downloads\\input Data\\BPI_2020_1k_sample.xes";

        String proxyLogBPI2017 = "C:\\Users\\Ahmed Awad\\Downloads\\input Data\\BPI_2017_Sim_2k_random_0.2.xes";
        String logBPI2017 ="C:\\Users\\Ahmed Awad\\Downloads\\input Data\\BPI_2017_1k_sample.xes";

//        testTED(proxyLogM1,logM1);
//        int low=1;
//        int high=5;
//        Utils.shortenTraces(frequencyActivityLog, frequencyActivityLog.replace(".xml", "-"+low+"-"+high+"-loop.txt"), low, high);

//        System.out.println(Utils.removeLoops("IIABCDFEGHIPIJMJMJMJMJMJMJMJMJMJMKKKKKKKKKLIKIPJMKLIIIIIPABCDLLFETUJMJMJMJMJMJMJMJMJMIPKKKLJMJMJMJMJMJMJMJMJMJMJMIPKKKKKKKKKKKKKJMJMJMJMJMJMJMJMJMJMJMJMJMOLIPKLQKLKKKKKKKKKKKLKJMJMJMJMJMJMJMJMLLKKKJMABCDKFETUIKKLKKQKKLKJMJMKJMLJMJMJMJMJMJMJMJMJMJMJMJMKKKIKQLKKKKKKKLKKKJMJMJMJMJMJMJMJMJMMJJMLLKIIKQKSKKKKKKKKKLLABCDFETULJMJMJMJMJMJMJMJMJMJMJMJMJMKKKKKKKQKLQKLQKLQKLQKLKKKIKKOKQKLQKLJMJMJMJMJMJMJMJMJMJMKKJMJMJMJMLIKJMJMJMJMJMJMJMJMJMJMJMKKKKKKKKKKKKKKKKKKKKKKKENIP",loopLength));
//        System.out.println(Utils.removeLoops("IABCDFEGHIPIJMJMJMJMJMJMJMJMJMJMKLIKIPJMKLIPABCDLFETUJMJMJMJMJMJMJMJMJMIPKLJMJMJMJMJMJMJMJMJMJMJMIPKJMJMJMJMJMJMJMJMJMJMJMJMJMOLIPKLQKLKLKJMJMJMJMJMJMJMJMLKJMABCDKFETUIKLKQKLKJMJMKJMLJMJMJMJMJMJMJMJMJMJMJMJMKIKQLKLKJMJMJMJMJMJMJMJMJMJMLKIKQKSKLABCDFETULJMJMJMJMJMJMJMJMJMJMJMJMJMKQKLQKLQKLQKLQKLKIKOKQKLQKLJMJMJMJMJMJMJMJMJMJMKJMJMJMJMLIKJMJMJMJMJMJMJMJMJMJMJMKENIP", 2));
//        System.exit(0);


//        Utils.init();
//        Trie tModel, tLog;
//        tModel = Utils.constructTrie(simulatedLog);
//        System.out.println("Model - 2015");
//        System.out.println(tModel.getRoot().preOrderTraversalAPTED());
//        System.exit(0);
//        Utils.shortenTraces(simulatedLog, simulatedLog.replace(".xml", ".csv"),0,0);
//        Utils.shortenTraces(clusteredLog, clusteredLog.replace(".xml",".csv"),0,0);
////        tLog = Utils.constructTrie(clusteredLog);
////        System.out.println("Log - 2015");
////        System.out.println(tLog.getRoot().preOrderTraversalAPTED());
//        Utils.init();
//        tModel = Utils.constructTrie(simulated2019Log);
//        System.out.println("Model - 2019");
//        System.out.println(tModel.getRoot().preOrderTraversalAPTED());
//        Utils.shortenTraces(simulated2019Log, simulated2019Log.replace(".xml",".txt"),0,0);
//        Utils.shortenTraces(reduced2019ActivityLog, reduced2019ActivityLog.replace(".xml",".txt"),0,0);
//        tLog = Utils.constructTrie(reduced2019ActivityLog);
//        System.out.println("Log - 2019");
//        System.out.println(tLog.getRoot().preOrderTraversalAPTED());
////        Utils.shortenTraces(reduced2019ActivityLog, reduced2019ActivityLog.replace(".xml",".csv"),0,0);
//        System.exit(0);

        testOnConformanceApproximationResults(proxyLogBPI2017, logBPI2017, ConformanceCheckerType.TRIE_TREE_INDEXER, LogSortType.NONE);


//        // BPI 2015
//        printLogStatistics(simulatedLog);
////        printLogStatistics(sampleLog);
//        printLogStatistics(clusteredLog);
//        printLogStatistics(randomProxyLog);
//        printLogStatistics(frequencyActivityLog);
//        printLogStatistics(reducedActivityLog);

//        // BPI 2012
//        printLogStatistics(simulated2012Log);
////        printLogStatistics(sample2012Log);
//        printLogStatistics(clustered2012Log);
//        printLogStatistics(random2012ProxyLog);
//        printLogStatistics(frequency2012Log);
//        printLogStatistics(reduced2012ActivityLog);

//        // BPI 2017
//        printLogStatistics(simulated2017Log);
////        printLogStatistics(sample2017Log);
//        printLogStatistics(clustered2017Log);
//        printLogStatistics(random2017ProxyLog);
//        printLogStatistics(frequency2017Log);
//        printLogStatistics(reduced2017ActivityLog);

//        // BPI 2019
//        printLogStatistics(simulated2019Log);
////        printLogStatistics(sample2019Log);
//        printLogStatistics(clustered2019Log);
//        printLogStatistics(random2019ProxyLog);
//        printLogStatistics(frequency2019Log);
//        printLogStatistics(reduced2019ActivityLog);

        //SEPSIS
//        printLogStatistics(simulatedSepsisLog);
////        printLogStatistics(sampleSepsisLog);
//        printLogStatistics(clusteredSepsisLog);
//        printLogStatistics(randomSepsisProxyLog);
//        printLogStatistics(frequencySepsisLog);
//        printLogStatistics(reducedActivityLog);
    }

    private static void testBed2()
    {
        List<String> trace = new ArrayList<>();
        trace.add("a");
        trace.add("c");
        trace.add("b");
        trace.add("e");

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");
        trace2.add("b");
        trace2.add("c");
        trace2.add("d");
        trace2.add("b");
        trace2.add("e");

        List<String> trace3 = new ArrayList<>();
        trace3.add("a");
        trace3.add("b");
        trace3.add("c");
        trace3.add("d");
        trace3.add("b");
        trace3.add("d");
        trace3.add("b");
        trace3.add("e");

        List<String> trace4 = new ArrayList<>();
        trace4.add("a");
        trace4.add("c");
        trace4.add("b");
        trace4.add("e");

        List<String> trace5 = new ArrayList<>();
        trace5.add("a");
        trace5.add("b");
        trace5.add("e");

        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);
        t.addTrace(trace3);
        t.addTrace(trace4);
        t.addTrace(trace5);

        System.out.println(t.getRoot().preOrderTraversal());
//        System.out.println(t.toString());
        // Now log traces

        // we can reuse trace 4
        List<String> trace6 = new ArrayList<>();
        trace6.add("a");
        trace6.add("e");

        List<String> trace7 = new ArrayList<>();
        trace7.add("a");
        trace7.add("c");
        trace7.add("b");
        trace7.add("d");
        trace7.add("e");



        List<String> trace8 = new ArrayList<>();
        trace8.add("c");
        trace8.add("e");

        List<String> trace9 = new ArrayList<>();
        trace9.add("xxx");
        trace9.add("z");
        trace9.add("e");

        ApproximateConformanceChecker cnfChecker;// = new ConformanceChecker(t);
        cnfChecker = new StatefulRandomApproximateConformanceChecker(t,1,1,1000, 100000);

        Alignment alg;
        System.out.println(trace4.toString());
        alg = cnfChecker.check(trace4);
        System.out.println(alg.toString());

        System.out.println(trace6.toString());
        alg = cnfChecker.check(trace6);
        System.out.println(alg.toString());

        System.out.println(trace7.toString());
        alg = cnfChecker.check(trace7);
        System.out.println(alg.toString());

        alg = cnfChecker.check(trace7);
        System.out.println(alg.toString());

//
        System.out.println(trace8.toString());
        alg = cnfChecker.check(trace8);
        System.out.println(alg.toString());
//
//        long start = System.currentTimeMillis();
//        System.out.println(trace9.toString());
//        alg = cnfChecker.check(trace9);
//        System.out.println(alg.toString());
//        System.out.println(String.format("Time taken: %d ms", System.currentTimeMillis()-start));


    }

    private static void testBed4()
    {
        List<String> modelTraces = new ArrayList<>();
        List<String> logTraces = new ArrayList<>();

        List<String> trace = new ArrayList<>();
        trace.add("a");
        trace.add("b");
        trace.add("c");
        trace.add("e");

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");
        trace2.add("b");
        trace2.add("c");
        trace2.add("d");
        trace2.add("b");
        trace2.add("e");

        List<String> trace3 = new ArrayList<>();
        trace3.add("a");
        trace3.add("b");
        trace3.add("c");
        trace3.add("d");
        trace3.add("b");
        trace3.add("d");
        trace3.add("b");
        trace3.add("e");

        List<String> trace4 = new ArrayList<>();
        trace4.add("a");
        trace4.add("c");
        trace4.add("b");
        trace4.add("e");

        List<String> trace5 = new ArrayList<>();
        trace5.add("a");
        trace5.add("b");
        trace5.add("e");

        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);
        t.addTrace(trace3);
        t.addTrace(trace4);
        t.addTrace(trace5);
        modelTraces.add(stringify(trace));
        modelTraces.add(stringify(trace2));
        modelTraces.add(stringify(trace3));
        modelTraces.add(stringify(trace4));
        modelTraces.add(stringify(trace5));
        System.out.println(t.getRoot().preOrderTraversalAPTED());
        HashMap<Integer, TrieNode> nodeIndexer = new HashMap<>();
        t.getRoot().computeOrUpdatePreOrderIndex(nodeIndexer);

        List<String> trace6 = new ArrayList<>();
        trace6.add("a");
        trace6.add("b");
        trace6.add("c");
        trace6.add("e");

        List<String> trace7 = new ArrayList<>();
        trace7.add("a");
//        trace7.add("b");
//        trace7.add("x");
        trace7.add("e");

        List<String> trace8 = new ArrayList<>();
        trace8.add("e");
        trace8.add("v");
        trace8.add("d");
        trace8.add("e");


        List<String> trace9 = new ArrayList<>();
//        trace9.add("a");
        trace9.add("c");
        trace9.add("e");

        List<String> trace10 = new ArrayList<>();
        trace10.add("a");
        trace10.add("c");
        trace10.add("b");
        trace10.add("d");
        trace10.add("e");

        List<String> trace11 = new ArrayList<>();
        trace11.add("a");
        trace11.add("b");
        trace11.add("e");

        List<String> trace12 = new ArrayList<>();
        trace12.add("a");
        trace12.add("c");
        trace12.add("z");
//        trace12.add("b");

        Trie t2 = new Trie(28);
        t2.addTrace(trace6);
        t2.addTrace(trace7);
        t2.addTrace(trace9);
        t2.addTrace(trace10);
        t2.addTrace(trace11);
//        t2.addTrace(trace12);
        logTraces.add(stringify(trace6));
        logTraces.add(stringify(trace7));
        logTraces.add(stringify(trace9));
        logTraces.add(stringify(trace10));
        logTraces.add(stringify(trace11));

        long start, timeTaken=0;
        start = System.currentTimeMillis();
        TreeBasedHolisticConformanceChecker tbcf= new TreeBasedHolisticConformanceChecker(t, t2);
        Interval intv = tbcf.computeAlignmentCost();
        timeTaken = System.currentTimeMillis() - start;
        System.out.printf("Alignment cost ranges from %f to %f\n",intv.getInf(), intv.getSup());
        System.out.printf("Tree-based alignment took %d milliseconds\n",timeTaken);
        //now string edit distance

        System.out.println("Trace#, Alignment cost");
        start = System.currentTimeMillis();
        double totalCost=0;
        for (String logTrace : logTraces) {

            double minCost = Double.MAX_VALUE;
            String bestTrace = "";
            String bestAlignment = "";
            start = System.currentTimeMillis();
            for (String proxyTrace : modelTraces) {

//                ProtoTypeSelectionAlgo.AlignObj obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(proxyTrace, logTrace);
                int currentCost = LevenshteinDistance.getDefaultInstance().apply(proxyTrace,logTrace);
                if (currentCost < minCost) {
                    minCost = currentCost;
                    bestTrace = proxyTrace;
                    if (currentCost == 0)
                        break;
                }
//
            }
            timeTaken += System.currentTimeMillis() - start;
//            System.out.println("Total proxy traces "+proxyTraces.size());
//            System.out.println("Total candidate traces to inspect "+proxyTraces.size());
            //print trace number

            // print cost
            System.out.println(logTrace +"->"+bestTrace +" , " + minCost);
            totalCost+=minCost;
//            System.out.println(bestAlignment);
//                Alignment alg = AlignmentFactory.createAlignmentFromString(bestAlignment);
//              System.out.println(alg.toString());
//                deviationChecker.processAlignment(alg);
//            System.out.println("Log trace "+logTrace);
//            System.out.println("Aligned trace "+bestTrace);
//            System.out.println("Trace number "+sampleTracesMap.get(bestTrace));
        }
        System.out.println(String.format("Total cost %f", totalCost));
        System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", timeTaken ));
    }

    private static void testBed5()
    {
        List<String> modelTraces = new ArrayList<>();
        List<String> logTraces = new ArrayList<>();

        List<String> trace = new ArrayList<>();
        trace.add("a");
        trace.add("b");
        trace.add("c");
        trace.add("e");

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");
        trace2.add("b");
        trace2.add("c");
        trace2.add("d");
        trace2.add("b");
        trace2.add("e");

        List<String> trace3 = new ArrayList<>();
        trace3.add("a");
        trace3.add("b");
        trace3.add("c");
        trace3.add("d");
        trace3.add("b");
        trace3.add("d");
        trace3.add("b");
        trace3.add("e");

        List<String> trace4 = new ArrayList<>();
        trace4.add("a");
        trace4.add("c");
        trace4.add("b");
        trace4.add("e");

        List<String> trace5 = new ArrayList<>();
        trace5.add("a");
        trace5.add("b");
        trace5.add("e");

        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);
        t.addTrace(trace3);
        t.addTrace(trace4);
        t.addTrace(trace5);

//        for (TrieNode n: t.getRoot().getDescendantsAtMaxLevel(2))
//            System.out.println(n);
//        System.exit(0);
//        modelTraces.add(stringify(trace));
//        modelTraces.add(stringify(trace2));
//        modelTraces.add(stringify(trace3));
//        modelTraces.add(stringify(trace4));
//        modelTraces.add(stringify(trace5));
//        System.out.println(t.getRoot().preOrderTraversalAPTED());

        List<String> trace6 = new ArrayList<>();
        trace6.add("a");
        trace6.add("b");
        trace6.add("c");
        trace6.add("e");

        List<String> trace7 = new ArrayList<>();
        trace7.add("a");
//        trace7.add("b");
//        trace7.add("x");
        trace7.add("e");

        List<String> trace8 = new ArrayList<>();
        trace8.add("e");
        trace8.add("v");
        trace8.add("d");
        trace8.add("e");


        List<String> trace9 = new ArrayList<>();
        trace9.add("x");
        trace9.add("c");
        trace9.add("e");

        List<String> trace10 = new ArrayList<>();
        trace10.add("a");
        trace10.add("c");
        trace10.add("b");
        trace10.add("d");
        trace10.add("e");

        List<String> trace11 = new ArrayList<>();
        trace11.add("a");
        trace11.add("b");
        trace11.add("e");

        List<String> trace12 = new ArrayList<>();
        trace12.add("a");
        trace12.add("c");
        trace12.add("z");
//        trace12.add("b");

        Trie t2 = new Trie(28);
        t2.addTrace(trace6);
        t2.addTrace(trace7);
        t2.addTrace(trace9);
        t2.addTrace(trace10);
        t2.addTrace(trace11);
        t2.addTrace(trace12);
        logTraces.add(stringify(trace6));
        logTraces.add(stringify(trace7));
        logTraces.add(stringify(trace9));
        logTraces.add(stringify(trace10));
        logTraces.add(stringify(trace12));

        long start, timeTaken=0;
        start = System.currentTimeMillis();
        TreeIndexerConformanceChecker cnf = new TreeIndexerConformanceChecker(t,1,1);

//        System.out.println(cnf.check(trace6));
//        System.out.println(cnf.check(trace7));
//        System.out.println(cnf.check(trace9));
//        System.out.println(cnf.check(trace10));
//        System.out.println(cnf.check(trace12));

        Alignment alg = cnf.check(trace9);

        System.out.println(alg);
        System.exit(0);

        timeTaken = System.currentTimeMillis() - start;
//        System.out.printf("Alignment cost ranges from %f to %f\n",intv.getInf(), intv.getSup());
        System.out.printf("Tree-based alignment took %d milliseconds\n",timeTaken);
        //now string edit distance

        System.out.println("Trace#, Alignment cost");
        start = System.currentTimeMillis();
        double totalCost=0;
        for (String logTrace : logTraces) {

            double minCost = Double.MAX_VALUE;
            String bestTrace = "";
            String bestAlignment = "";
            start = System.currentTimeMillis();
            for (String proxyTrace : modelTraces) {

//                ProtoTypeSelectionAlgo.AlignObj obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(proxyTrace, logTrace);
                int currentCost = LevenshteinDistance.getDefaultInstance().apply(proxyTrace,logTrace);
                if (currentCost < minCost) {
                    minCost = currentCost;
                    bestTrace = proxyTrace;
                    if (currentCost == 0)
                        break;
                }
//
            }
            timeTaken += System.currentTimeMillis() - start;
//            System.out.println("Total proxy traces "+proxyTraces.size());
//            System.out.println("Total candidate traces to inspect "+proxyTraces.size());
            //print trace number

            // print cost
            System.out.println(logTrace +"->"+bestTrace +" , " + minCost);
            totalCost+=minCost;
//            System.out.println(bestAlignment);
//                Alignment alg = AlignmentFactory.createAlignmentFromString(bestAlignment);
//              System.out.println(alg.toString());
//                deviationChecker.processAlignment(alg);
//            System.out.println("Log trace "+logTrace);
//            System.out.println("Aligned trace "+bestTrace);
//            System.out.println("Trace number "+sampleTracesMap.get(bestTrace));
        }
        System.out.println(String.format("Total cost %f", totalCost));
        System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", timeTaken ));
    }

    private static String stringify(List<String> trace) {
        return trace.toString().replace("[", "").replace("]", "").replace(",", "").replace(" ", "");
    }

    //    private static boolean isInMySubTree(int postOrderIndex1, int postOrderIndex2, HashMap<Integer, TrieNode> nodeIndexer)
//    {
//        // a member in sub-tree must have a smaller index number as it will be visited first
//        if (postOrderIndex1< postOrderIndex2)
//            return false;
//        // A node on the same level or less, i.e. closer to the root, cannot be in the subtree of the first index
//        TrieNode node1 = nodeIndexer.get(postOrderIndex1);
//        TrieNode node2 = nodeIndexer.get(postOrderIndex2);
//        if (node2.getLevel() <= node1.getLevel())
//            return false;
//        //now the expensive test to traverse the path to the root from the node with index 2
//        TrieNode node2Parent = node2.getParent();
//        boolean found = false;
//        while (node2Parent != null)
//        {
//            if (node2Parent.equals(node1)) {
//                found = true;
//                break;
//            }
//            node2Parent = node2Parent.getParent();
//        }
//        return found;
//    }
//    private static List<Integer> getHighestRoots(List<Integer> deletedNodes, HashMap<Integer, TrieNode> nodeIndexer)
//    {
//        Collections.sort(deletedNodes,Collections.reverseOrder());
//        List<Integer> result = new ArrayList<>(deletedNodes.size());
//        int nextI=0;
//        for (int i = 0; i < deletedNodes.size();i = nextI+1) {
//            result.add(deletedNodes.get(i));
//            boolean cycled=false;
//            for (int j = i + 1; j < deletedNodes.size(); j++) {
//                cycled=true;
//                if (!isInMySubTree(deletedNodes.get(i), deletedNodes.get(j), nodeIndexer)) {
//
//                    nextI = j - 1; // so that next time outer loop increments i
//                    break;
//                }
//                else
//                    nextI=j;
//            }
//            if (!cycled)
//                break;
//        }
//        return result;
//    }
//    private static boolean allSubTreeMarkedForDeletion(TrieNode nd)
//    {
//        if (nd.getAllChildren().size()==0)
//            return nd.isMarkedForDeletion();
//        boolean partialResult = true;
//        for (TrieNode child: nd.getAllChildren())
//        {
//            partialResult = partialResult && allSubTreeMarkedForDeletion(child);
//        }
//        return nd.isMarkedForDeletion() && partialResult;
//    }
    private static Pnml importPnmlFromStream(InputStream input) throws
            XmlPullParserException, IOException {
        FullPnmlElementFactory pnmlFactory = new FullPnmlElementFactory();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(input, null);
        int eventType = xpp.getEventType();
        Pnml pnml = new Pnml();
        synchronized (pnmlFactory) {
            pnml.setFactory(pnmlFactory);
            /*
             * Skip whatever we find until we've found a start tag.
             */
            while (eventType != XmlPullParser.START_TAG) {
                eventType = xpp.next();
            }
            /*
             * Check whether start tag corresponds to PNML start tag.
             */
            if (xpp.getName().equals(Pnml.TAG)) {
                /*
                 * Yes it does. Import the PNML element.
                 */
                pnml.importElement(xpp, pnml);
            } else {
                /*
                 * No it does not. Return null to signal failure.
                 */
                pnml.log(Pnml.TAG, xpp.getLineNumber(), "Expected pnml");
            }
            if (pnml.hasErrors()) {
                return null;
            }
            return pnml;
        }
    }




    private static void printLogStatistics(String inputLog)
    {
        Utils.init();
        long startTs = System.currentTimeMillis();
        Trie t = Utils.constructTrie(inputLog);
        long endTs = System.currentTimeMillis();

        System.out.println(String.format("Stats for trace from %s", inputLog));
        System.out.println(String.format("Max length of a trace %d", t.getMaxTraceLength()));
        System.out.println(String.format("Min length of a trace %d", t.getMinTraceLength()));
        System.out.println(String.format("Avg length of a trace %d", t.getAvgTraceLength()));
        System.out.println(String.format("Number of nodes in the trie %d", t.getSize()));
        System.out.println(String.format("Total number of events %d", t.getNumberOfEvents()));
        System.out.println(String.format("Trie construction time %d ms", (endTs-startTs)));
        t.printTrieShape();
    }

    /***
     *
     * @param inputProxyLogFile: The log file that contains the model behavior
     * @param inputSampleLogFile: The log file contains the traces to compute alignment for
     * The method creates a special trie, a deep tree for each separate log trace and computes TED
     *  against the model trie
     *
     */
    private static void testTED2(String inputProxyLogFile, String inputSampleLogFile)
    {
        Utils.init();
        Long start = System.currentTimeMillis();

        Trie tProxy = Utils.constructTrie(inputProxyLogFile);
        System.out.println(String.format("Model tree construction time is %d ms", System.currentTimeMillis() - start));
        System.out.println(String.format("Model tree depth is %d", tProxy.getRoot().getMaxPathLengthToEnd()));
        System.out.println(String.format("Model tree size is %d",tProxy.getRoot().getNodeCount()));

        List<String> sampleTraces;
        if (inputSampleLogFile.endsWith(".xml") || inputProxyLogFile.endsWith(".xes"))
            sampleTraces = readXESLog(inputSampleLogFile, 0, 1000);
        else
            sampleTraces = readTXTLog(inputSampleLogFile, 0, 1000);
        long totalTime=0;
        for (int i =0; i < sampleTraces.size();i++)
        {
            start = System.currentTimeMillis();
            Trie tLog = Utils.constructTrie(sampleTraces.subList(i,i+1));
            totalTime+= (System.currentTimeMillis()-start);
            start = System.currentTimeMillis();
            try
            {
                TreeBasedHolisticConformanceChecker tbcf= new TreeBasedHolisticConformanceChecker(tProxy, tLog);
                Interval intv = tbcf.computeAlignmentCost();
                System.out.printf("Alignment cost ranges from %f to %f\n",intv.getInf(), intv.getSup());
                System.out.printf("Tree-based alignment took %d milliseconds\n",System.currentTimeMillis() - start);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }
//        Trie tLog = Utils.constructTrie(inputSampleLogFile);
//        System.out.println(String.format("Log tree construction time is %d ms", totalTime));
//        System.out.println(String.format("Log tree depth is %d", tLog.getRoot().getMaxPathLengthToEnd()));
//        System.out.println(String.format("Log tree size is %d",tLog.getRoot().getNodeCount()));




//        System.out.println("Model tree "+ tProxy.getRoot().preOrderTraversalAPTED());
//        System.out.println("Log tree "+ tLog.getRoot().preOrderTraversalAPTED());


    }
    private static void testTED(String inputProxyLogFile, String inputSampleLogFile)
    {
        Utils.init();
        Long start = System.currentTimeMillis();
        Trie tLog = Utils.constructTrie(inputSampleLogFile);
        System.out.println(String.format("Log tree construction time is %d ms", System.currentTimeMillis() - start));
        System.out.println(String.format("Log tree depth is %d", tLog.getRoot().getMaxPathLengthToEnd()));
        System.out.println(String.format("Log tree size is %d",tLog.getRoot().getNodeCount()));

        start = System.currentTimeMillis();
        Trie tProxy = Utils.constructTrie(inputProxyLogFile);
        System.out.println(String.format("Model tree construction time is %d ms", System.currentTimeMillis() - start));
        System.out.println(String.format("Model tree depth is %d", tProxy.getRoot().getMaxPathLengthToEnd()));
        System.out.println(String.format("Model tree size is %d",tProxy.getRoot().getNodeCount()));

        System.out.println("Model tree "+ tProxy.getRoot().preOrderTraversalAPTED());
        System.out.println("Log tree "+ tLog.getRoot().preOrderTraversalAPTED());

        try
        {
            TreeBasedHolisticConformanceChecker tbcf= new TreeBasedHolisticConformanceChecker(tProxy, tLog);
            Interval intv = tbcf.computeAlignmentCost();
            System.out.printf("Alignment cost ranges from %f to %f\n",intv.getInf(), intv.getSup());
            System.out.printf("Tree-based alignment took %d milliseconds\n",System.currentTimeMillis() - start);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }



        testVanillaConformanceApproximationCostOnly(inputProxyLogFile, inputSampleLogFile);

    }
    private static void testOnConformanceApproximationResults(String inputProxyLogFile, String inputSampleLogFile, ConformanceCheckerType confCheckerType, LogSortType sortType)
    {
        Utils.init();

//        t.printTrieShape();
        //Configuration variables



        ApproximateConformanceChecker checker;
        if (confCheckerType == ConformanceCheckerType.DISTANCE)
        {
            testVanillaConformanceApproximation(inputProxyLogFile, inputSampleLogFile);
//                testVanillaConformanceApproximationCostOnly(inputProxyLogFile, inputSampleLogFile);
//            return;
        }
        else
        {
            Trie t = Utils.constructTrie(inputProxyLogFile);
            if (confCheckerType == ConformanceCheckerType.TRIE_PREFIX)
                checker = new PrefixApproximateConformanceChecker(t,1,1, false);
            else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM)
                checker = new RandomApproximateConformanceChecker(t, 1, 1, 10000, 10000);//Integer.MAX_VALUE);
            else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM_STATEFUL)
                checker = new StatefulRandomApproximateConformanceChecker(t, 1, 1, 5000000, 1200000);//Integer.MAX_VALUE);
            else// if (confCheckerType == ConformanceCheckerType.TRIE_TREE_INDEXER)
                checker = new TreeIndexerConformanceChecker(t,1,1);

            List<String> tracesToSort;
            if (inputSampleLogFile.endsWith(".xml")|| inputSampleLogFile.endsWith(".xes") )
                tracesToSort= readXESLog(inputSampleLogFile, 0, 1000);
            else
                tracesToSort = readTXTLog(inputSampleLogFile, 0, 1000);

            DeviationChecker devChecker = new DeviationChecker(Utils.service);
            long totalTime = 0;
            if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM_STATEFUL) {

                if (sortType == LogSortType.TRACE_LENGTH_ASC || sortType == LogSortType.TRACE_LENGTH_DESC)
                    tracesToSort.sort(Comparator.comparingInt(String::length));
                else if (sortType == LogSortType.LEXICOGRAPHIC_ASC || sortType == LogSortType.LEXICOGRAPHIC_DESC)
                    Collections.sort(tracesToSort);
            }

            System.out.println("Trace#, matching model trace, coverage, Alignment cost");

            if (confCheckerType == ConformanceCheckerType.TRIE_TREE_INDEXER_HOLISTIC)
            {
                Trie logTrie = Utils.constructTrie(tracesToSort);
                long start = System.currentTimeMillis();
                List<Alignment> alignments = ((TreeIndexerConformanceChecker) checker).check(logTrie);

                totalTime = System.currentTimeMillis() - start;

                for (Alignment alg: alignments)
                {
                    double coverage =alg.logCoverage();
                    logCoverage += coverage;
                    System.out.println(alg.logProjection()+", "+alg.modelProjection()+", "+coverage+", "+ alg.getTotalCost());
                }
            }
            else {

                if (sortType == LogSortType.LEXICOGRAPHIC_DESC || sortType == LogSortType.TRACE_LENGTH_DESC) {
                    for (int i = tracesToSort.size() - 1; i >= 0; i--) {
                        totalTime = computeAlignment(tracesToSort, checker, totalTime, devChecker, i);
                    }
                }
//
                else {
                    for (int i = 0; i < tracesToSort.size(); i++) {
                        totalTime = computeAlignment(tracesToSort, checker, totalTime, devChecker, i);
                    }
                }

            }
            System.out.println(String.format("Time taken for trie-based conformance checking %d milliseconds",totalTime));
            System.out.println(String.format("Average log coverage %f",logCoverage/tracesToSort.size()));
        }







//            for (String label: devChecker.getAllActivities())
//            {
//                System.out.println(String.format("%s, %f",label, devChecker.getDeviationPercentage(label)));
//            }
    }


    private static List<String> readTXTLog(String inputSampleLogFile, int skipTo, int takeTo)
    {
        Path filePath = FileSystems.getDefault().getPath(inputSampleLogFile);
        List<String> tracesToSort = new ArrayList<>();



        try
        {

            Files.lines(filePath).forEach(l -> tracesToSort.add(l) );

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return takeTo < tracesToSort.size()? tracesToSort.subList(skipTo, takeTo+1): tracesToSort.subList(skipTo, tracesToSort.size());
    }
    private static List<String> readXESLog(String inputSampleLogFile, int skipTo, int takeTo)
    {
        XLog inputSamplelog;
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;

        XesXmlParser parser = new XesXmlParser();
        List<String> tracesToSort = new ArrayList<>();
        try {
            InputStream is = new FileInputStream(inputSampleLogFile);
            inputSamplelog = parser.parse(is).get(0);


            List<String> templist = new ArrayList<>();

            // AlphabetService service = new AlphabetService();



//            HashMap<String, Integer> sampleTracesMap = new HashMap<>();



            int current = -1;


            int cnt = 1;

            for (XTrace trace : inputSamplelog) {
                String caseID="NONE";
                for (String ext: trace.getAttributes().keySet()) {
                    if (ext.equalsIgnoreCase("concept:name")) {
                        caseID = trace.getAttributes().get(ext).toString();
                        break;
                    }
                }
                current++;
                if (current < skipTo)
                    continue;
                if (current > takeTo)
                    break;
                templist = new ArrayList<String>();

                for (XEvent e : trace) {
                    String label = e.getAttributes().get(inputSamplelog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                    templist.add(Character.toString(Utils.service.alphabetize(label)));
                }
//                System.out.println(templist.toString());

                StringBuilder sb = new StringBuilder(templist.size());
                sb.append(caseID).append((char) 63); // we prefix the trace with its ID

                Arrays.stream(templist.toArray()).forEach(e -> sb.append(e));

//                sampleTracesMap.put(sb.toString(), cnt);
                cnt++;

                tracesToSort.add(sb.toString());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return tracesToSort;
    }
    private static long computeAlignment(List<String> tracesToSort,
                                         ApproximateConformanceChecker checker,
                                         long totalTime, DeviationChecker devChecker, int i) {
        long start;
        Alignment alg;
        List<String> trace = new ArrayList<String>();

        int pos = tracesToSort.get(i).indexOf((char)63);
        String traceID = tracesToSort.get(i).substring(0,pos);

//        String actualTrace = tracesToSort.get(i);//.substring(pos+1);
        String actualTrace = tracesToSort.get(i).substring(pos+1);
//        System.out.println(actualTrace);
        for (char c : actualTrace.toCharArray()) {
            trace.add(new StringBuilder().append(c).toString());
        }
        start = System.currentTimeMillis();
        alg = checker.check(trace);
        totalTime += System.currentTimeMillis() - start;
        if (alg != null) {
//            System.out.print(sampleTracesMap.get(tracesToSort.get(i)));
            System.out.print(traceID);
            System.out.println(", "+alg.modelProjection()+", "+alg.weightedLogCoverage()+", " + alg.getTotalCost());
//                        System.out.println(alg.toString(service));
//            devChecker.processAlignment(alg);
//                    System.out.println(alg.toString());
//                        t.printTraces();
            logCoverage+= alg.logCoverage();
        } else //if (usePrefixChecker == false)
            System.out.println("Couldn't find an alignment under the given constraints");
        return totalTime;
    }



    private static void testVanillaConformanceApproximation(String inputProxyLogFile, String inputSampleLogFile)
    {
        List<String> proxyTraces;
        if (inputProxyLogFile.endsWith(".xml") || inputProxyLogFile.endsWith(".xes"))
            proxyTraces = readXESLog(inputProxyLogFile, 0, 100000);
        else
            proxyTraces = readTXTLog(inputProxyLogFile, 0, 100000);

//        XLog proxyLog, sampleLog;
//        StringBuilder sb;

        List<String> sampleTraces;
        if (inputSampleLogFile.endsWith(".xml") || inputProxyLogFile.endsWith(".xes"))
            sampleTraces = readXESLog(inputSampleLogFile, 0, 1000);
        else
            sampleTraces = readTXTLog(inputSampleLogFile, 0, 1000);

//        proxyLog = Utils.loadLog(inputProxyLogFile);
//        sampleLog = Utils.loadLog(inputSampleLogFile);
//        HashMap<String, Integer> sampleTracesMap = new HashMap<>();
//        Utils.init();

//        for (XTrace trace : proxyLog) {
//            sb = new StringBuilder();
//            for (XEvent e : trace) {
//                String label = e.getAttributes().get(proxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
//
//                sb.append(Utils.service.alphabetize(label));
//            }
//            proxyTraces.add(sb.toString());
//
//        }
//        int cnt=1;
        long start=System.currentTimeMillis(),timeTaken=0 ;
//        int skipTo =0;
//        int current = -1;
//        int takeTo = 100;
//        for (XTrace trace : sampleLog) {
//            current++;
//            if (current < skipTo)
//                continue;
//            if (current > takeTo)
//                break;
//            sb = new StringBuilder();
//            for (XEvent e : trace) {
//                String label = e.getAttributes().get(sampleLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
//
//                sb.append(Utils.service.alphabetize(label));
//            }
//
//        }

//        DeviationChecker deviationChecker = new DeviationChecker(service);
        // Now compute the alignments

        double totalCost = 0;
        try {
            System.out.println("Trace#, optimal, log coverage,  Alignment cost");

            for (String logTrace : sampleTraces) {

                int pos = logTrace.indexOf((char)63);
                String traceID = logTrace.substring(0,pos);

                double minCost = Double.MAX_VALUE;
                String bestTrace = "";
                String bestAlignment = "";
                start = System.currentTimeMillis();
                Alignment alg;
                for (String proxyTrace : proxyTraces) {

                    ProtoTypeSelectionAlgo.AlignObj obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(proxyTrace.substring(proxyTrace.indexOf((char)63)+1), logTrace.substring(pos+1));
                    if (obj.cost < minCost) {
                        minCost = obj.cost;
                        bestAlignment = obj.Alignment;
                        bestTrace = proxyTrace.substring(proxyTrace.indexOf((char)63)+1);
                        if (obj.cost == 0)
                            break;
                    }
//                    if (obj.cost == 47.0)
//                    {

//                        System.out.println(alg.toString());
//                    }
                }
                timeTaken += System.currentTimeMillis() - start;
//            System.out.println("Total proxy traces "+proxyTraces.size());
//            System.out.println("Total candidate traces to inspect "+proxyTraces.size());
                //print trace number
//                System.out.print(logTrace);
                // print cost
                alg = AlignmentFactory.createAlignmentFromString(bestAlignment);
//                System.out.print(logTrace+", "+bestTrace+" ,"+alg.weightedLogCoverage());
                System.out.print(traceID+", "+bestTrace+", "+alg.weightedLogCoverage());
                System.out.println(", " + minCost);

                logCoverage+=alg.logCoverage();
                totalCost+= minCost;
//            System.out.println(bestAlignment);
//                Alignment alg = AlignmentFactory.createAlignmentFromString(bestAlignment);
//              System.out.println(alg.toString());
//                deviationChecker.processAlignment(alg);
//            System.out.println("Log trace "+logTrace);
//            System.out.println("Aligned trace "+bestTrace);
//            System.out.println("Trace number "+sampleTracesMap.get(bestTrace));
            }
            System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", timeTaken ));
            System.out.println(String.format("total alignment cost for Distance-based approximate conformance checking %f ", totalCost ));
            System.out.println(String.format("Average log coverage %f",logCoverage/sampleTraces.size()));

//            for (String label: deviationChecker.getAllActivities())
//            {
//                System.out.println(String.format("%s, %f",label, deviationChecker.getDeviationPercentage(label)));
//            }

        }
        catch (Exception e)
        {
            System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", System.currentTimeMillis() - start));
            e.printStackTrace();

        }

    }

    private static void testVanillaConformanceApproximationCostOnly(String inputProxyLogFile, String inputSampleLogFile)
    {
        XLog proxyLog, sampleLog;
        StringBuilder sb;
        Set<String> proxyTraces = new HashSet<>();
        Set<String> sampleTraces = new HashSet<>();
        proxyLog = Utils.loadLog(inputProxyLogFile);
        sampleLog = Utils.loadLog(inputSampleLogFile);
        HashMap<String, Integer> sampleTracesMap = new HashMap<>();
//        Utils.init();

        for (XTrace trace : proxyLog) {
            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = e.getAttributes().get(proxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                sb.append(Utils.service.alphabetize(label));
            }
//            System.out.println(sb.toString());

            proxyTraces.add(sb.toString());

        }
//        System.exit(0);
        int cnt=1;
        int skipTo =0;
        int current = -1;
        int takeTo = 100;
        for (XTrace trace : sampleLog) {

            current++;
            if (current < skipTo)
                continue;
            if (current > takeTo)
                break;

            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = e.getAttributes().get(sampleLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                sb.append(Utils.service.alphabetize(label));
            }
            if (sampleTraces.add(sb.toString()))
            {
                sampleTracesMap.put(sb.toString(),cnt++);
            }
        }

//        DeviationChecker deviationChecker = new DeviationChecker(service);
        // Now compute the alignments
        long start=System.currentTimeMillis(),timeTaken=0 ;

        double totalCost = 0;
        try {
            System.out.println(String.format("Total log variants is %d", sampleTraces.size()));
            System.out.println(String.format("Total model variants is %d", proxyTraces.size()));
            System.out.println("Trace#, Alignment cost");

            for (String logTrace : sampleTraces) {

                double minCost = Double.MAX_VALUE;
                start = System.currentTimeMillis();
                String bestTrace="";
                for (String proxyTrace : proxyTraces) {

                    int currentCost = LevenshteinDistance.getDefaultInstance().apply(proxyTrace, logTrace);
                    if (currentCost < minCost) {
                        minCost = currentCost;
                        bestTrace = String.valueOf(proxyTrace);
//                        if (obj.cost == 0)
//                            break;
                    }
                }
                timeTaken += System.currentTimeMillis() - start;

                //print trace number
                System.out.print(logTrace+", "+bestTrace);
                // print cost
                System.out.println(", " + minCost);

                totalCost+= minCost;

            }
            System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", timeTaken ));
            System.out.println(String.format("total alignment cost for Distance-based approximate conformance checking %f ", totalCost ));


        }
        catch (Exception e)
        {
            System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", System.currentTimeMillis() - start));
            e.printStackTrace();

        }

    }
    private static void testConformanceApproximation()
    {
        //This method is used to test the approach by Fani Sani
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        XesXmlParser parser = new XesXmlParser();
        XLog inputLog;

        try {
            InputStream is = new FileInputStream("C:\\Work\\DSG\\Data\\BPI2015Reduced2014.xml");
            inputLog = parser.parse(is).get(0);
            Pnml pnml = importPnmlFromStream(new FileInputStream("C:\\Work\\DSG\\Data\\IM_Petrinet.pnml"));
            Petrinet pn = PetrinetFactory.newPetrinet(pnml.getLabel());
            Marking imk=new Marking();
            Collection<Marking> fmks = new HashSet<>();
            GraphLayoutConnection glc = new GraphLayoutConnection(pn);
            pnml.convertToNet(pn,imk, fmks,glc);
            MatrixFilterParameter parameter = new MatrixFilterParameter(10, inputLog.getClassifiers().get(0), SimilarityMeasure.Levenstein, SamplingReturnType.Traces, PrototypeType.KMeansClusteringApprox);
            //now the target
            String result = ProtoTypeSelectionAlgo.apply(inputLog,pn,parameter,null);

            System.out.println(result);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private static void testJNI()
    {
        try {
            // Create a problem with 4 variables and 0 constraints
            LpSolve solver = LpSolve.makeLp(0, 4);

            // add constraints
            solver.strAddConstraint("3 2 2 1", LpSolve.LE, 4);
            solver.strAddConstraint("0 4 3 1", LpSolve.GE, 3);

            // set objective function
            solver.strSetObjFn("2 3 -2 3");

            // solve the problem
            solver.solve();

            // print solution
            System.out.println("Value of objective function: " + solver.getObjective());
            double[] var = solver.getPtrVariables();
            for (int i = 0; i < var.length; i++) {
                System.out.println("Value of var[" + i + "] = " + var[i]);
            }

            // delete the problem and free memory
            solver.deleteLp();
        }
        catch (LpSolveException e) {
            e.printStackTrace();
        }
    }
    private static void testBed1() {
        List<String> trace = new ArrayList<>();
        trace.add("a");
        trace.add("b");
        trace.add("c");
        trace.add("d");

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");
        trace2.add("b");
        trace2.add("x");
        trace2.add("e");

        List<String> trace3 = new ArrayList<>();
        trace3.add("e");
        trace3.add("v");
        trace3.add("d");
        trace3.add("e");


        List<String> trace4 = new ArrayList<>();
        trace4.add("a");
        trace4.add("b");
        trace4.add("c");

        List<String> trace5 = new ArrayList<>();
        trace5.add("a");
        trace5.add("e");

        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);
        t.addTrace(trace3);
        t.addTrace(trace4);
        t.addTrace(trace5);

//        List<String> trace6 = new ArrayList<>();
//        trace6.add("a");
//        trace6.add("b");
//        trace6.add("c");
//        trace6.add("d");
//        trace6.add("e");

        List<String> trace6 = new ArrayList<>();
        trace6.add("e");
//        trace6.add("b");
//        trace6.add("c");
//        trace6.add("d");
        trace6.add("x");

//        System.out.println(t.toString());
        ApproximateConformanceChecker cnfChecker = new RandomApproximateConformanceChecker(t,1,1,1000);

        Alignment alg = cnfChecker.check(trace6);

        System.out.println(alg.toString());
    }

    private static void testBed3()
    {
        String model = "AEFwBCDCJIKLOMlmGonpqtMrZuvN\u0081OPQRSTUV\\[`WabHXgYcdfeheji";
        String trace = "FGHBCDEAJICKLMlmOonptqusrvNOPQRSTUVWabMhg[i^Y\\_]c`eXZjdfe";

        List<String> modelTrace = new ArrayList<>(model.length());


        for (char c : model.toCharArray())
        {
            modelTrace.add(String.valueOf(c));
        }

        List<String> traceTrace = new ArrayList<>(model.length());


        for (char c : trace.toCharArray())
        {
            traceTrace.add(String.valueOf(c));
        }

        Trie t = new Trie(100);
        t.addTrace(modelTrace);
        long start = System.currentTimeMillis();
        ApproximateConformanceChecker cnfChecker = new RandomApproximateConformanceChecker(t,1,1,100000);
        Alignment alg = cnfChecker.check(traceTrace);
        long total = System.currentTimeMillis() - start;
        System.out.println(alg.toString());
        System.out.println(String.format("Total time %d", total));


    }
}
