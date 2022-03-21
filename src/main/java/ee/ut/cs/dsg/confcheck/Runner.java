package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.AlignmentFactory;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.AlphabetService;
import ee.ut.cs.dsg.confcheck.util.Configuration;
import ee.ut.cs.dsg.confcheck.StatefulRandomConformanceChecker;
import ee.ut.cs.dsg.confcheck.util.Utils;
import gnu.trove.impl.sync.TSynchronizedShortByteMap;
import gnu.trove.impl.sync.TSynchronizedShortCharMap;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.apache.commons.math3.analysis.function.Add;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.logfiltering.algorithms.ProtoTypeSelectionAlgo;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SimilarityMeasure;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.SamplingReturnType;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.operationalsupport.xml.OSXMLConverter;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.elements.extensions.opennet.PnmlLabel;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import static ee.ut.cs.dsg.confcheck.util.Configuration.ConformanceCheckerType;
import static ee.ut.cs.dsg.confcheck.util.Configuration.LogSortType;
public class Runner {

    private static AlphabetService service;

    public static void main(String... args) throws UnknownHostException {
        //testBedPrefix();
        //testBed2();
//        System.exit(0);
//          testBed1();
//        testConformanceApproximation();
//
//        testJNI();

//        testBed3();
//        System.exit(0);

//            BPI2012
          String simulatedLog = "C:\\Users\\kristo88\\OneDrive - Tartu Ülikool\\Logs\\BPI2012\\simulatedLog.xml";
          String frequencyLog = "C:\\Users\\kristo88\\OneDrive - Tartu Ülikool\\Logs\\BPI2012\\frequencyLog.xml";
          String sampleLog = "C:\\Users\\kristo88\\OneDrive - Tartu Ülikool\\Logs\\BPI2012\\sampledLog.xml";
        //testBedPrefix("test");

        //BPI 2015
        /*
          String simulatedLog = "C:\\Users\\kristo88\\OneDrive - Tartu Ülikool\\PhD\\00_Project\\Data\\ICPM21\\BPI2015\\simulatedLog.xml";
        String frequencyLog = "C:\\Users\\kristo88\\OneDrive - Tartu Ülikool\\PhD\\00_Project\\Data\\ICPM21\\BPI2015\\frequencyLog.xml";
        String sampleLog = "C:\\Users\\kristo88\\OneDrive - Tartu Ülikool\\PhD\\00_Project\\Data\\ICPM21\\BPI2015\\sampledLog.xml";
*/
          testOnConformanceApproximationResults(simulatedLog, sampleLog, ConformanceCheckerType.TRIE_RANDOM, LogSortType.NONE);
          //testOnConformanceApproximationResults(simulatedLog, sampleLog, ConformanceCheckerType.TRIE_PREFIX, LogSortType.NONE);
        //String trietest_1 = "C:\\Users\\kristo88\\Documents\\PLG2\\trietest_1.1.xes";
        //String trietest_2 = "C:\\Users\\kristo88\\Documents\\PLG2\\trietest_2.xes";
        //testBedPrefix("test");
        //listenToEvents(trietest_2);


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


    private static void testBedPrefix2() {

        RandomConformanceChecker cnfChecker;


        Alignment alg;

        // Test by manual traces



        List<String> trace = new ArrayList<>();
        trace.add("a");
        trace.add("b");
        trace.add("c");
        trace.add("d");
        trace.add("e");

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");
        trace2.add("b");
        trace2.add("c");

        List<String> trace3 = new ArrayList<>();
        trace3.add("a");
        trace3.add("b");
        trace3.add("d");
        trace3.add("e");
        trace3.add("f");


        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);
        t.addTrace(trace3);

        cnfChecker = new RandomConformanceChecker(t,1,1,100000, 100000);
        List<String> prefixCase1 = new ArrayList<>();

        prefixCase1.add("a");
        alg = cnfChecker.check2(prefixCase1, true, "Case 1");

        System.out.println(alg.toString());

        prefixCase1.add("b");
        alg = cnfChecker.check2(prefixCase1, true, "Case 1");

        System.out.println(alg.toString());

        prefixCase1.add("e");
        alg = cnfChecker.check2(prefixCase1, true, "Case 1");

        System.out.println(alg.toString());

        prefixCase1.add("c");
        alg = cnfChecker.check2(prefixCase1, true, "Case 1");

        System.out.println(alg.toString());

    }

    private static void testBedPrefix(String inputLog)
    {

        RandomConformanceChecker cnfChecker;


        Alignment alg;

        // Test by manual traces


        List<String> trace = new ArrayList<>();
        trace.add("a");
        trace.add("b");
        trace.add("c");
        trace.add("d");
        trace.add("e");

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");
        trace2.add("b");
        trace2.add("c");

        List<String> trace3 = new ArrayList<>();
        trace3.add("a");
        trace3.add("b");
        trace3.add("d");
        trace3.add("e");
        trace3.add("f");

        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);
        t.addTrace(trace3);

        cnfChecker = new RandomConformanceChecker(t,1,1,100000, 100000);


        List<String> prefixCase1 = new ArrayList<>();
        String prefixCase1Id = "Case 1";
        List<String> prefixCase2 = new ArrayList<>();
        String prefixCase2Id = "Case 2";
        List<String> prefixCase3 = new ArrayList<>();
        String prefixCase3Id = "Case 3";
        List<String> prefixCase4 = new ArrayList<>();
        String prefixCase4Id = "Case 4";
        List<String> prefixCase5 = new ArrayList<>();
        String prefixCase5Id = "Case 5";
        List<String> prefixCase6 = new ArrayList<>();
        String prefixCase6Id = "Case 6";


        prefixCase1.add("a");
        alg = cnfChecker.check2(prefixCase1, true, prefixCase1Id);

        prefixCase1.add("b");
        alg = cnfChecker.check2(prefixCase1, true, prefixCase1Id);

        prefixCase1.add("c");
        alg = cnfChecker.check2(prefixCase1, true, prefixCase1Id);

        System.out.println("Case 1:");
        System.out.println(alg.toString());



        prefixCase2.add("a");
        alg = cnfChecker.check2(prefixCase2, true, prefixCase2Id);

        prefixCase2.add("b");
        alg = cnfChecker.check2(prefixCase2, true, prefixCase2Id);

        prefixCase2.add("e");
        alg = cnfChecker.check2(prefixCase2, true, prefixCase2Id);

        prefixCase2.add("f");
        alg = cnfChecker.check2(prefixCase2, true, prefixCase2Id);

        System.out.println("Case 2:");
        System.out.println(alg.toString());



        prefixCase3.add("a");
        alg = cnfChecker.check2(prefixCase3, true, prefixCase3Id);

        prefixCase3.add("b");
        alg = cnfChecker.check2(prefixCase3, true, prefixCase3Id);

        prefixCase3.add("c");
        alg = cnfChecker.check2(prefixCase3, true, prefixCase3Id);

        prefixCase3.add("c");
        alg = cnfChecker.check2(prefixCase3, true, prefixCase3Id);

        prefixCase3.add("d");
        alg = cnfChecker.check2(prefixCase3, true, prefixCase3Id);

        prefixCase3.add("e");
        alg = cnfChecker.check2(prefixCase3, true, prefixCase3Id);

        System.out.println("Case 3:");
        System.out.println(alg.toString());



        prefixCase4.add("b");
        alg = cnfChecker.check2(prefixCase4, true, prefixCase4Id);

        prefixCase4.add("a");
        alg = cnfChecker.check2(prefixCase4, true, prefixCase4Id);

        prefixCase4.add("c");
        alg = cnfChecker.check2(prefixCase4, true, prefixCase4Id);

        prefixCase4.add("d");
        alg = cnfChecker.check2(prefixCase4, true, prefixCase4Id);

        prefixCase4.add("e");
        alg = cnfChecker.check2(prefixCase4, true, prefixCase4Id);

        System.out.println("Case 4:");
        System.out.println(alg.toString());



        prefixCase5.add("x");
        alg = cnfChecker.check2(prefixCase5, true, prefixCase5Id);

        prefixCase5.add("a");
        alg = cnfChecker.check2(prefixCase5, true, prefixCase5Id);

        prefixCase5.add("b");
        alg = cnfChecker.check2(prefixCase5, true, prefixCase5Id);

        prefixCase5.add("c");
        alg = cnfChecker.check2(prefixCase5, true, prefixCase5Id);

        System.out.println("Case 5:");
        System.out.println(alg.toString());



        prefixCase6.add("a");
        alg = cnfChecker.check2(prefixCase6, true, prefixCase6Id);

        prefixCase6.add("b");
        alg = cnfChecker.check2(prefixCase6, true, prefixCase6Id);

        prefixCase6.add("c");
        alg = cnfChecker.check2(prefixCase6, true, prefixCase6Id);

        prefixCase6.add("x");
        alg = cnfChecker.check2(prefixCase6, true, prefixCase6Id);

        System.out.println("Case 6:");
        System.out.println(alg.toString());



    }


    public static void listenToEvents(String inputLog) throws UnknownHostException {

        int port = 1234;
        InetAddress address = InetAddress.getByName("127.0.0.1");
        long eventsReceived = 0;
        boolean execute = true;
        OSXMLConverter converter = new OSXMLConverter();
        init();
        Trie t = constructTrie(inputLog);
        System.out.println("Trie size: "+t.getSize());
        try {
            Socket s = new Socket(address, port);

            System.out.println("Stream started");

            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String str = "";
            String caseId;
            String newEventName;
            String eventLabel;
            XTrace trace;
            long start = System.currentTimeMillis();
            long prevStart = start;

            try {
                FileWriter writer = new FileWriter(String.format("Executions\\%d.txt",start), true);
                writer.write("Log path: "+inputLog);
                writer.write("\r\n");
                writer.write("Random Conf Checker"); // store the settings dynamically here. Conformance checker type and checker settings, cost function
                writer.write("\r\n");
                writer.write("\r\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ConformanceChecker cnfChecker;// = new ConformanceChecker(t);
            cnfChecker = new RandomConformanceChecker(t,1,1,100000, 100000);

            Alignment alg;
            List<String> events = new ArrayList<>();

            while (execute && (str = br.readLine()) != null) {
                //System.out.println((eventsReceived++) + " events observed");
                eventsReceived++;
                if (eventsReceived % 1000 == 0)
                {
                    System.out.println(String.format("Events observed: %d",eventsReceived));
                    System.out.println(String.format("Time taken in milliseconds for last 1000 events: %d",System.currentTimeMillis()- prevStart));
                    prevStart = System.currentTimeMillis();
                }

                // extract the observed components
                trace = (XTrace) converter.fromXML(str);
                caseId = XConceptExtension.instance().extractName(trace);
                newEventName = XConceptExtension.instance().extractName(trace.get(0));

                // alphabetize newEventName
                eventLabel = Character.toString(service.alphabetize(newEventName));

                events.add(eventLabel);

                alg = cnfChecker.prefix_check(events, caseId);

                try {
                    FileWriter writer = new FileWriter(String.format("Executions\\%d.txt",start), true);
                    writer.write("CaseId: "+caseId);
                    writer.write("\r\n");
                    writer.write("Alignment: ");
                    writer.write(alg.toString());
                    writer.write("\r\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            br.close();
            s.close();
            System.out.println(String.format("Time taken in milliseconds: %d",System.currentTimeMillis()- start));
            System.out.println(String.format("Events observed: %d",eventsReceived));

        } catch (IOException e) {
            System.out.println("Network Exception");
        }

    }

    private static void testBed2()
    {
        List<String> trace = new ArrayList<>();
        trace.add("a");
        trace.add("b");
        trace.add("c");
        trace.add("d");
        trace.add("e");

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");
        trace2.add("b");
        trace2.add("c");

        List<String> trace3 = new ArrayList<>();
        trace3.add("a");
        trace3.add("b");
        trace3.add("d");
        trace3.add("e");
        trace3.add("f");
/*
        List<String> trace4 = new ArrayList<>();
        trace4.add("a");
        trace4.add("c");
        trace4.add("b");
        trace4.add("e");

        List<String> trace5 = new ArrayList<>();
        trace5.add("a");
        trace5.add("b");
        trace5.add("e");
*/
        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);
        t.addTrace(trace3);
        //t.addTrace(trace4);
        //t.addTrace(trace5);

//        System.out.println(t.toString());
        // Now log traces

        // we can reuse trace 4
        List<String> trace6 = new ArrayList<>();
        trace6.add("a");
        trace6.add("b");

        List<String> trace7 = new ArrayList<>();
        trace7.add("x");
        trace7.add("a");
        trace7.add("b");
        trace7.add("d");
        trace7.add("c");
        trace7.add("y");


/*
        List<String> trace8 = new ArrayList<>();
        trace8.add("c");
        trace8.add("e");

        List<String> trace9 = new ArrayList<>();
        trace9.add("xxx");
        trace9.add("z");
        trace9.add("e");
*/
        ConformanceChecker cnfChecker;// = new ConformanceChecker(t);
        cnfChecker = new RandomConformanceChecker(t,1,1,100000, 100000);

        Alignment alg;

        System.out.println(trace6.toString());
        alg = cnfChecker.check(trace6);
        System.out.println(alg.toString());

        System.out.println(trace7.toString());
        alg = cnfChecker.check(trace7);
        System.out.println(alg.toString());
/*
        alg = cnfChecker.check(trace7);
        System.out.println(alg.toString());
*/
//
//
//        long start = System.currentTimeMillis();
//        System.out.println(trace9.toString());
//        alg = cnfChecker.check(trace9);
//        System.out.println(alg.toString());
//        System.out.println(String.format("Time taken: %d ms", System.currentTimeMillis()-start));


    }

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

    private static void init()
    {
        service = new AlphabetService();
    }


    private static void printLogStatistics(String inputLog)
    {
        init();
        long startTs = System.currentTimeMillis();
        Trie t = constructTrie(inputLog);
        long endTs = System.currentTimeMillis();

        System.out.println(String.format("Stats for trace from %s", inputLog));
        System.out.println(String.format("Max length of a trace %d", t.getMaxTraceLength()));
        System.out.println(String.format("Min length of a trace %d", t.getMinTraceLength()));
        System.out.println(String.format("Avg length of a trace %d", t.getAvgTraceLength()));
        System.out.println(String.format("Number of nodes in the trie %d", t.getSize()));
        System.out.println(String.format("Total number of events %d", t.getNumberOfEvents()));
        System.out.println(String.format("Trie construction time %d ms", (endTs-startTs)));
    }


    private static void testOnConformanceApproximationResults(String inputProxyLogFile, String inputSampleLogFile, ConformanceCheckerType confCheckerType, LogSortType sortType)
    {
        init();
        Trie t = constructTrie(inputProxyLogFile);

        //Configuration variables

        boolean sortTraces=true;

//      t.printTraces();
//        System.out.println(t);
        XLog inputSamplelog;
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        XesXmlParser parser = new XesXmlParser();

        try{
            InputStream is = new FileInputStream(inputSampleLogFile);
            inputSamplelog = parser.parse(is).get(0);


            List<String> templist = new ArrayList<>();
            List<String> tracesToSort = new ArrayList<>();
           // AlphabetService service = new AlphabetService();

            ConformanceChecker checker;
            if (confCheckerType == ConformanceCheckerType.TRIE_PREFIX)
                          checker = new PrefixConformanceChecker(t,1,1, false);
            else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM)
                checker = new RandomConformanceChecker(t,1,1, 1000, 1000);//Integer.MAX_VALUE);
            else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM_STATEFUL)
                checker = new StatefulRandomConformanceChecker(t,1,1, 50000, 420000);//Integer.MAX_VALUE);
            else
            {
                testVanellaConformanceApproximation(inputProxyLogFile,inputSampleLogFile);
                return;
            }

            Alignment alg;
            HashMap<String, Integer> sampleTracesMap = new HashMap<>();
            long start;
            long totalTime=0;
            int skipTo =0;
            int current = -1;
            int takeTo = 100;
            DeviationChecker devChecker = new DeviationChecker(service);
            int cnt = 1;
            for (XTrace trace: inputSamplelog)
            {
                current++;
                if (current < skipTo)
                    continue;
                if (current> takeTo)
                    break;
                templist = new ArrayList<String>();

                for (XEvent e: trace)
                {
                    String label = e.getAttributes().get(inputSamplelog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                    templist.add(Character.toString(service.alphabetize(label)));
                }
//                System.out.println(templist.toString());

                StringBuilder sb = new StringBuilder(templist.size());
                sb.append(cnt).append((char)63); // we prefix the trace with its ID

                Arrays.stream(templist.toArray()).forEach( e-> sb.append(e));

                sampleTracesMap.put(sb.toString(),cnt);
                cnt++;

                tracesToSort.add(sb.toString());
            }
            if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM_STATEFUL) {

                if (sortType == LogSortType.TRACE_LENGTH_ASC || sortType == LogSortType.TRACE_LENGTH_DESC)
                    tracesToSort.sort(Comparator.comparingInt(String::length));
                else if (sortType == LogSortType.LEXICOGRAPHIC_ASC || sortType == LogSortType.LEXICOGRAPHIC_DESC)
                    Collections.sort(tracesToSort);
            }

            System.out.println("Trace#, Alignment cost");

            if (sortType == LogSortType.LEXICOGRAPHIC_DESC || sortType == LogSortType.TRACE_LENGTH_DESC)
            {
                for (int i = tracesToSort.size() -1; i>=0; i--)
                {
                    totalTime = computeAlignment(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i);
                }
            }
//
            else {
                for (int i = 0; i < tracesToSort.size(); i++) {
                    totalTime = computeAlignment(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i);
                }
            }


            System.out.println(String.format("Time taken for trie-based conformance checking %d milliseconds",totalTime));

//            for (String label: devChecker.getAllActivities())
//            {
//                System.out.println(String.format("%s, %f",label, devChecker.getDeviationPercentage(label)));
//            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static long computeAlignment(List<String> tracesToSort, ConformanceChecker checker, HashMap<String, Integer> sampleTracesMap, long totalTime, DeviationChecker devChecker, int i) {
        long start;
        Alignment alg;
        List<String> trace = new ArrayList<String>();

        int pos = tracesToSort.get(i).indexOf((char)63);
        int traceNum = Integer.parseInt(tracesToSort.get(i).substring(0,pos));

        String actualTrace = tracesToSort.get(i).substring(pos+1);
//        System.out.println(actualTrace);
        for (char c : actualTrace.toCharArray()) {
            trace.add(new StringBuilder().append(c).toString());
        }

        //Integer traceSize = trace.size();
        start = System.currentTimeMillis();
        //alg = checker.prefix_check(trace, Integer.toString(i));
        alg = checker.check2(trace, true, Integer.toString(i));
        //alg = checker.check(trace);
        totalTime += System.currentTimeMillis() - start;
        if (alg != null) {
            System.out.print(sampleTracesMap.get(tracesToSort.get(i)));
            //System.out.print(", alignment cost minus trace size: "+ (alg.getTotalCost()-traceSize));
            System.out.println(", " + alg.getTotalCost());
//                        System.out.println(alg.toString(service));
//            devChecker.processAlignment(alg);
//                    System.out.println(alg.toString());
//                        t.printTraces();
        } else //if (usePrefixChecker == false)
            System.out.println("Couldn't find an alignment under the given constraints");

        return totalTime;
    }

    private static XLog loadLog(String inputProxyLogFile)
    {
        XLog inputProxyLog;//, inputSamplelog;
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        XesXmlParser parser = new XesXmlParser();

        try {
            InputStream is = new FileInputStream(inputProxyLogFile);
            inputProxyLog = parser.parse(is).get(0);
//            XLogInfo logInfo = inputProxyLog.getInfo(eventClassifier);
//            logInfo = XLogInfoFactory.createLogInfo(inputProxyLog, inputProxyLog.getClassifiers().get(0));
            return inputProxyLog;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    private static Trie constructTrie(String inputProxyLogFile)
    {
        XLog inputProxyLog = loadLog(inputProxyLogFile);
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;


        try {

            XLogInfo logInfo ;
            logInfo = XLogInfoFactory.createLogInfo(inputProxyLog, inputProxyLog.getClassifiers().get(0));
            int count = 0;
            for (XEventClass clazz : logInfo.getNameClasses().getClasses()) {
                count++;
                //        System.out.println(clazz.toString());
            }
//            System.out.println("Number of unique activities " + count);

            //Let's construct the trie from the proxy log
            Trie t = new Trie(count);
            List<String> templist;
//            count=1;
            count=1;
//            System.out.println("Proxy log size "+inputProxyLog.size());
            for (XTrace trace : inputProxyLog) {
                templist = new ArrayList<String>();
                for (XEvent e : trace) {
                    String label = e.getAttributes().get(inputProxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                    templist.add(Character.toString(service.alphabetize(label)));
                }
//                count++;
                //System.out.println(templist.toString());
                if (templist.size() > 0 ) {

//                    System.out.println(templist.toString());
//                    if (count == 37)
//                    StringBuilder sb = new StringBuilder();
//                    templist.stream().forEach(e -> sb.append(e));
//                    System.out.println(sb.toString());
                    t.addTrace(templist);
//                    if (count ==5)
//                    break;
                }
                count++;
            }
            return t;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    private static void testVanellaConformanceApproximation(String inputProxyLogFile, String inputSampleLogFile)
    {
        XLog proxyLog, sampleLog;
        StringBuilder sb;
        List<String> proxyTraces = new ArrayList<>();
        List<String> sampleTraces = new ArrayList<>();
        proxyLog = loadLog(inputProxyLogFile);
        sampleLog = loadLog(inputSampleLogFile);
        HashMap<String, Integer> sampleTracesMap = new HashMap<>();
        init();

        for (XTrace trace : proxyLog) {
            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = e.getAttributes().get(proxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                sb.append(service.alphabetize(label));
            }
            proxyTraces.add(sb.toString());

        }
        int cnt=1;
        for (XTrace trace : sampleLog) {
            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = e.getAttributes().get(sampleLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                sb.append(service.alphabetize(label));
            }
            sampleTraces.add(sb.toString());
            sampleTracesMap.put(sb.toString(),cnt);
            cnt++;
        }

        DeviationChecker deviationChecker = new DeviationChecker(service);
        // Now compute the alignments
        long start=System.currentTimeMillis(),timeTaken=0 ;
        int skipTo =0;
        int current = -1;
        int takeTo = 100;
        try {
            System.out.println("Trace#, Alignment cost");

            for (String logTrace : sampleTraces) {
                current++;
                if (current < skipTo)
                    continue;
                if (current > takeTo)
                    break;
                double minCost = Double.MAX_VALUE;
                String bestTrace = "";
                String bestAlignment = "";
                start = System.currentTimeMillis();
                for (String proxyTrace : proxyTraces) {

                    ProtoTypeSelectionAlgo.AlignObj obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(logTrace, proxyTrace);
                    if (obj.cost < minCost) {
                        minCost = obj.cost;
                        bestAlignment = obj.Alignment;
                        bestTrace = proxyTrace;
                        if (obj.cost == 0)
                            break;
                    }
                }
                timeTaken += System.currentTimeMillis() - start;
//            System.out.println("Total proxy traces "+proxyTraces.size());
//            System.out.println("Total candidate traces to inspect "+proxyTraces.size());
                //print trace number
                System.out.print(sampleTracesMap.get(logTrace));
                // print cost
                System.out.println(", " + minCost);
//            System.out.println(bestAlignment);
//                Alignment alg = AlignmentFactory.createAlignmentFromString(bestAlignment);
//              System.out.println(alg.toString());
//                deviationChecker.processAlignment(alg);
//            System.out.println("Log trace "+logTrace);
//            System.out.println("Aligned trace "+bestTrace);
//            System.out.println("Trace number "+sampleTracesMap.get(bestTrace));
            }
            System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", timeTaken ));

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

        List<String> trace2 = new ArrayList<>();
        trace2.add("a");


        Trie t = new Trie(28);
        t.addTrace(trace);
        t.addTrace(trace2);

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
        ConformanceChecker cnfChecker = new RandomConformanceChecker(t,1,1,1000);

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
        ConformanceChecker cnfChecker = new RandomConformanceChecker(t,1,1,100000);
        Alignment alg = cnfChecker.check(traceTrace);
        long total = System.currentTimeMillis() - start;
        System.out.println(alg.toString());
        System.out.println(String.format("Total time %d", total));


    }
}
