package ee.ut.cs.dsg.confcheck;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XesXmlGZIPParser;
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
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static ee.ut.cs.dsg.confcheck.util.Configuration.ConformanceCheckerType;
import static ee.ut.cs.dsg.confcheck.util.Configuration.LogSortType;


public class Runner {

    private static AlphabetService service;

    public static void main(String... args) throws UnknownHostException {


        String executionType = "cost_diff"; // "stress_test" or "cost_diff"


        // Cost difference

        if (executionType == "cost_diff") {
            long unixTime = Instant.now().getEpochSecond();
            Date date = new Date(unixTime*1000L);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String formattedDate = dateFormat.format(date);

            String pathPrefix = "output\\disc3\\";
            String fileType = ".csv";

            HashMap<String, HashMap<String, String>> logs = new HashMap<>();
            HashMap<String, String> subLog = new HashMap<>();
            subLog.put("log", "input\\BPI2012\\sampledLog.xml");
            subLog.put("simulated", "input\\BPI2012\\simulatedLog.xml");
            subLog.put("clustered", "input\\BPI2012\\sampledClusteredLog.xml");
            subLog.put("random", "input\\BPI2012\\randomLog.xml");
            subLog.put("frequency", "input\\BPI2012\\frequencyLog.xml");
            subLog.put("reduced", "input\\BPI2012\\reducedLogActivity.xml");
            logs.put("BPI2012", new HashMap<>(subLog));
            subLog.clear();
            subLog.put("log", "input\\BPI2015\\sampledLog.xml");
            subLog.put("simulated", "input\\BPI2015\\simulatedLog.xml");
            subLog.put("clustered", "input\\BPI2015\\sampledClusteredLog.xml");
            subLog.put("random", "input\\BPI2015\\randomLog.xml");
            subLog.put("frequency", "input\\BPI2015\\frequencyLog.xml");
            subLog.put("reduced", "input\\BPI2015\\reducedLogActivity.xml");
            logs.put("BPI2015", new HashMap<>(subLog));
            subLog.clear();
            subLog.put("log", "input\\BPI2017\\sampledLog.xml");
            subLog.put("simulated", "input\\BPI2017\\simulatedLog.xml");
            subLog.put("clustered", "input\\BPI2017\\sampledClusteredLog.xml");
            subLog.put("random", "input\\BPI2017\\randomLog.xml");
            subLog.put("frequency", "input\\BPI2017\\frequencyLog.xml");
            subLog.put("reduced", "input\\BPI2017\\reducedLogActivity.xml");
            logs.put("BPI2017", new HashMap<>(subLog));
            subLog.clear();
            subLog.put("log", "input\\BPI2019\\sampledLog.xml");
            subLog.put("simulated", "input\\BPI2019\\simulatedLog.xml");
            subLog.put("clustered", "input\\BPI2019\\sampledClusteredLog.xml");
            subLog.put("random", "input\\BPI2019\\randomLog.xml");
            subLog.put("frequency", "input\\BPI2019\\frequencyLog.xml");
            subLog.put("reduced", "input\\BPI2019\\reducedLogActivity.xml");
            logs.put("BPI2019", new HashMap<>(subLog));
            subLog.clear();
            subLog.put("log", "input\\Sepsis\\sampledLog.xml");
            subLog.put("simulated", "input\\Sepsis\\simulatedLog.xml");
            subLog.put("clustered", "input\\Sepsis\\sampledClusteredLog.xml");
            subLog.put("random", "input\\Sepsis\\randomLog.xml");
            subLog.put("frequency", "input\\Sepsis\\frequencyLog.xml");
            subLog.put("reduced", "input\\Sepsis\\reducedLogActivity.xml");
            logs.put("Sepsis", new HashMap<>(subLog));
            subLog.clear();

            ConformanceCheckerType checkerType = ConformanceCheckerType.TRIE_STREAMING;
            System.out.println(checkerType.toString());

            String runType = "general"; //"specific" for unique log/proxy combination, "logSpecific" for all proxies in one log, "general" for running all logs

            if (runType == "specific") {
                // run for specific log
                String sLog = "Sepsis";
                String sLogType = "frequency";
                String sLogPath = logs.get(sLog).get("log");
                String sProxyLogPath = logs.get(sLog).get(sLogType);
                String pathName = pathPrefix + formattedDate + "_" + sLog + "_" + sLogType + fileType;
                try {

                    List<String> res = testOnConformanceApproximationResults(sProxyLogPath, sLogPath, checkerType, LogSortType.NONE);
                    res.add(0, String.format("TraceId, Cost_%1$s, ExecutionTime_%1$s", checkerType));
                    FileWriter wr = new FileWriter(pathName);
                    for (String s : res) {
                        wr.write(s);
                        wr.write(System.lineSeparator());
                    }
                    wr.close();


                } catch (IOException e) {
                    System.out.println("Error occurred!");
                    e.printStackTrace();
                }
            } else if (runType == "logSpecific") {
                String sLog = "BPI2019";
                String sLogPath = logs.get(sLog).get("log");
                HashMap<String, String> logTypes = logs.get(sLog);

                for (Map.Entry<String, String> logTypesMap :
                        logTypes.entrySet()) {
                    if (logTypesMap.getKey().equals("log")) {
                        continue;
                    }
                    String pathName = pathPrefix + formattedDate + "_" + sLog + "_" + logTypesMap.getKey() + fileType;
                    String proxyLogPath = logTypesMap.getValue();


                    try {

                        List<String> res = testOnConformanceApproximationResults(proxyLogPath, sLogPath, checkerType, LogSortType.NONE);
                        res.add(0, String.format("TraceId, Cost_%1$s, ExecutionTime_%1$s", checkerType));

                        FileWriter wr = new FileWriter(pathName);
                        for (String s : res) {
                            wr.write(s);
                            wr.write(System.lineSeparator());
                        }
                        wr.close();


                    } catch (IOException e) {
                        System.out.println("Error occurred!");
                        e.printStackTrace();
                    }

                }


            } else if (runType == "general") {
                // run for all logs
                for (Map.Entry<String, HashMap<String, String>> logsMap :
                        logs.entrySet()) {

                    HashMap<String, String> logTypes = logsMap.getValue();
                    String logPath = logTypes.get("log");
                    String logName = logsMap.getKey();
                    System.out.println("-----##-----");
                    System.out.println(logName);


                    for (Map.Entry<String, String> logTypesMap :
                            logTypes.entrySet()) {
                        if (logTypesMap.getKey().equals("log")) {
                            continue;
                        }
                        String pathName = pathPrefix + formattedDate + "_" + logName + "_" + logTypesMap.getKey() + fileType;
                        String proxyLogPath = logTypesMap.getValue();

                        System.out.println("-----");
                        System.out.println(logTypesMap.getKey());


                        try {

                            List<String> res = testOnConformanceApproximationResults(proxyLogPath, logPath, checkerType, LogSortType.NONE);
                            res.add(0, String.format("TraceId, Cost_%1$s, ExecutionTime_%1$s", checkerType));

                            FileWriter wr = new FileWriter(pathName);
                            for (String s : res) {
                                wr.write(s);
                                wr.write(System.lineSeparator());
                            }
                            wr.close();


                        } catch (IOException e) {
                            System.out.println("Error occurred!");
                            e.printStackTrace();
                        }

                    }

                }
            } else {
                System.out.println("Run type not implemented");
            }
        } else if (executionType == "stress_test")
        {

            String proxyLog = null;
            String logSize = "small";
            if (logSize=="small")
                proxyLog = "input\\Stress_test\\Simulated_Log_Small.xes.gz";
            else if (logSize=="medium")
                proxyLog = "input\\Stress_test\\Simulated_Log_Medium.xes.gz";
            else if (logSize=="large")
                proxyLog = "input\\Stress_test\\Simulated_Log_Large.xes.gz";
            else
                System.out.println("log size undefined");
            listenToEvents(proxyLog);
            //printLogStatistics(proxyLog);

        } else {
            System.out.println("Unknown execution type");
        }

    }

    public static void listenToEvents(String inputLog) throws UnknownHostException {

        int port = 1234;
        InetAddress address = InetAddress.getByName("127.0.0.1");
        long eventsReceived = 0;
        boolean execute = true;
        OSXMLConverter converter = new OSXMLConverter();
        init();
        long start = System.currentTimeMillis();
        Trie t = constructTrie(inputLog);
        //System.out.println(String.format("Time taken for trie construction: %d", System.currentTimeMillis()-start));
        //System.out.println("Trie size: "+t.getSize());

        Socket s = null;
        Boolean streamStarted = false;
        System.out.println("Waiting for stream to start...");
        start = System.currentTimeMillis();

        while(!streamStarted){
            try {
                s = new Socket(address, port);
                streamStarted = true;
            } catch (IOException e) {
                if (System.currentTimeMillis()-start >= 60000){
                    System.out.println("Unable to establish connection");
                    break;
                }
                try {Thread.sleep(1);} catch (InterruptedException ex) {ex.printStackTrace();}
            }

        }
        if(streamStarted){
            try {

                System.out.println("Stream started");

                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String str = "";
                String caseId;
                String newEventName;
                String eventLabel;
                XTrace trace;
                start = System.currentTimeMillis();
                long prevStart = start;
                long runTimeMillis = 300000;
                long eventReceivedTime = System.currentTimeMillis();
                long eventPreparedTime = System.currentTimeMillis();
                long eventHandledTime = System.currentTimeMillis();
                long eventReceivedToPrepared = 0;
                long eventPreparedToHandled = 0;
                long eventReceivedToHandled = 0;
                long totalIdleTime = 0;
                long idleTime = 0;
        /*
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

        */

                StreamingConformanceChecker cnfChecker = new StreamingConformanceChecker(t,1,1,100000, 100000);

                Alignment alg;
                List<String> events = new ArrayList<>();
                List<String> cases = new ArrayList<>();

                eventHandledTime = System.currentTimeMillis();
                while (execute && (str = br.readLine()) != null) {
                    //System.out.println((eventsReceived++) + " events observed");
                    eventsReceived++;
                    eventReceivedTime = System.currentTimeMillis();
                    idleTime = eventReceivedTime-eventHandledTime;
                    /*if (eventsReceived % 1000 == 0)
                    {
                        System.out.println(String.format("Events observed: %d",eventsReceived));
                        System.out.println(String.format("Time taken in milliseconds for last 1000 events: %d",System.currentTimeMillis()- prevStart));
                        prevStart = System.currentTimeMillis();
                    }*/

                    // extract the observed components
                    trace = (XTrace) converter.fromXML(str);
                    caseId = XConceptExtension.instance().extractName(trace);
                    if (!cases.contains(caseId))
                        cases.add(caseId);
                    newEventName = XConceptExtension.instance().extractName(trace.get(0));

                    // alphabetize newEventName
                    eventLabel = Character.toString(service.alphabetize(newEventName));

                    events.clear();
                    events.add(eventLabel);

                    eventPreparedTime = System.currentTimeMillis();

                    cnfChecker.check(events, caseId);
                    eventHandledTime = System.currentTimeMillis();
                    eventReceivedToPrepared += eventPreparedTime - eventReceivedTime;
                    eventReceivedToHandled += eventHandledTime - eventReceivedTime;
                    eventPreparedToHandled += eventHandledTime - eventPreparedTime;
                    totalIdleTime += idleTime;
                    //System.out.println(String.format("%d\t%d\t%d\t%d", eventPrepared-eventReceived, eventHandled-eventReceived, eventHandled-eventPrepared, idleTime));
                    if(System.currentTimeMillis()-start>=runTimeMillis){
                        System.out.println(String.format("Run time exhausted. Run time: %d", runTimeMillis));
                        System.out.println("Received to prepared, Received to handled, Prepared to handled, Idle time");
                        System.out.println(String.format("%d, %d, %d, %d", eventReceivedToPrepared, eventReceivedToHandled, eventPreparedToHandled, totalIdleTime));
                        break;
                    }
                    //alg = cnfChecker.getCurrentOptimalState(caseId, true).getAlignment();



                    /*
                    // this is for writing every alignment to a file
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
                    }*/


                }
                br.close();
                s.close();
                long endTime = System.currentTimeMillis();
                System.out.println(String.format("Time taken in milliseconds: %d",endTime- start));
                System.out.println(String.format("Events observed: %d",eventsReceived));
                System.out.println(String.format("Cases observed: %d",cases.size()));
                // get prefix alignments
                /*System.out.println("Prefix alignments:");
                long algStart = System.currentTimeMillis();
                for(String c:cases){
                    alg = cnfChecker.getCurrentOptimalState(c, false).getAlignment();
                    System.out.println(c + ","+ alg.getTotalCost());
                }
                long algEnd = System.currentTimeMillis();
                System.out.println(String.format("Time taken prefix-alignments: %d", algEnd-algStart));

                // get complete alignments
                System.out.println("Complete alignments:");
                algStart = System.currentTimeMillis();
                for(String c:cases){
                    alg = cnfChecker.getCurrentOptimalState(c, true).getAlignment();
                    System.out.println(c + ","+ alg.getTotalCost());
                }
                algEnd = System.currentTimeMillis();
                System.out.println(String.format("Time taken complete-alignments: %d", algEnd-algStart));
*/
            } catch (IOException e) {
                System.out.println("Network error");
            }
        }

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


    private static ArrayList<String> testOnConformanceApproximationResults(String inputProxyLogFile, String inputSampleLogFile, ConformanceCheckerType confCheckerType, LogSortType sortType)
    {
        init();
        Trie t = constructTrie(inputProxyLogFile);

        ArrayList<String> result = new ArrayList<>();

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

            if (confCheckerType == ConformanceCheckerType.TRIE_STREAMING) {
                checker = new StreamingConformanceChecker(t, 1, 1, 100000, 100000);
                //System.out.print("Average trie size: ");
                //System.out.println(checker.modelTrie.getAvgTraceLength());
            } else {

                if (confCheckerType == ConformanceCheckerType.TRIE_PREFIX)
                    checker = new PrefixConformanceChecker(t,1,1, false);
                else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM)
                    checker = new RandomConformanceChecker(t,1,1, 100000, 100000);//Integer.MAX_VALUE);
                else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM_STATEFUL)
                    checker = new StatefulRandomConformanceChecker(t,1,1, 50000, 420000);//Integer.MAX_VALUE);
                else
                {
                    testVanellaConformanceApproximation(inputProxyLogFile,inputSampleLogFile, result);
                    return result;
                }
            }


            Alignment alg;
            HashMap<String, Integer> sampleTracesMap = new HashMap<>();
            long start;
            long totalTime=0;
            int skipTo =0;
            int current = -1;
            int takeTo = 100;
            DeviationChecker devChecker = new DeviationChecker(service);
            int cnt = 0;
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

            //System.out.println("Trace#, Alignment cost");
            //result.add("TraceId,Cost,ExecutionTime,ConfCheckerType");

            if (sortType == LogSortType.LEXICOGRAPHIC_DESC || sortType == LogSortType.TRACE_LENGTH_DESC)
            {
                for (int i = tracesToSort.size() -1; i>=0; i--)
                {
                    if (confCheckerType == ConformanceCheckerType.TRIE_STREAMING) {
                        totalTime = computeAlignment2(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i, result);
                    } else {
                        totalTime = computeAlignment(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i, result);
                    }
                }
            }
//
            else {
                for (int i = 0; i < tracesToSort.size(); i++) {
                    if (confCheckerType == ConformanceCheckerType.TRIE_STREAMING) {
                        totalTime = computeAlignment2(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i, result);
                    } else {
                        totalTime = computeAlignment(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i, result);
                    }
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
        return result;


    }


    private static long computeAlignment2(List<String> tracesToSort, ConformanceChecker checkerC, HashMap<String, Integer> sampleTracesMap, long totalTime, DeviationChecker devChecker, int i, ArrayList<String> result) {
        long start;
        long executionTime;
        Alignment alg;
        List<String> trace = new ArrayList<String>();
        StreamingConformanceChecker checker = (StreamingConformanceChecker) checkerC;

        int pos = tracesToSort.get(i).indexOf((char) 63);

        String actualTrace = tracesToSort.get(i).substring(pos + 1);
        for (char c : actualTrace.toCharArray()) {
            trace.add(new StringBuilder().append(c).toString());
        }

        //System.out.println("Case id: "+Integer.toString(i));
        //System.out.println(trace);

        start = System.currentTimeMillis();

        for (String e : trace) {
            List<String> tempList = new ArrayList<String>();
            tempList.add(e);
            checker.check(tempList, Integer.toString(i));
        }

        alg = checker.getCurrentOptimalState(Integer.toString(i), true).getAlignment();


        executionTime = System.currentTimeMillis() - start;
        totalTime += executionTime;
        if (alg != null) {

            result.add(Integer.toString(i) + "," + alg.getTotalCost() + "," + executionTime);

        } else {
            System.out.println("Couldn't find an alignment under the given constraints");
            result.add(Integer.toString(i) + ",9999999," + executionTime);
        }

        return totalTime;
    }

    private static long computeAlignment(List<String> tracesToSort, ConformanceChecker checker, HashMap<String, Integer> sampleTracesMap, long totalTime, DeviationChecker devChecker, int i, ArrayList<String> result) {
        long start;
        long executionTime;
        Alignment alg;
        List<String> trace = new ArrayList<String>();

        int pos = tracesToSort.get(i).indexOf((char)63);
        int traceNum = Integer.parseInt(tracesToSort.get(i).substring(0,pos));

        String actualTrace = tracesToSort.get(i).substring(pos+1);
//        System.out.println(actualTrace);
        for (char c : actualTrace.toCharArray()) {
            trace.add(new StringBuilder().append(c).toString());
        }

        //System.out.println("Case id: "+Integer.toString(i));
        //System.out.println(trace);

        //Integer traceSize = trace.size();
        start = System.currentTimeMillis();
        //alg = checker.prefix_check(trace, Integer.toString(i));
        //alg = checker.check2(trace, true, Integer.toString(i));
        alg = checker.check(trace);

        //alg = null;

        /*
        for (String e : trace) {
            List<String> tempList = new ArrayList<String>();
            tempList.add(e);
            alg = checker.check2(tempList, true, Integer.toString((i)));
            //System.out.println(", " + alg.getTotalCost());
            //System.out.println(alg.toString());
        }*/
        executionTime = System.currentTimeMillis() - start;
        totalTime += executionTime;
        if (alg != null) {
            //System.out.print(sampleTracesMap.get(tracesToSort.get(i)));
            //System.out.println(", " + alg.getTotalCost());

            result.add(Integer.toString(i) + "," + alg.getTotalCost() + "," + executionTime);

        } else {
            System.out.println("Couldn't find an alignment under the given constraints");
            result.add(Integer.toString(i) + ",9999999," + executionTime);
        }

        return totalTime;
    }

    private static XLog loadLog(String inputProxyLogFile)
    {
        XLog inputProxyLog;//, inputSamplelog;
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        XesXmlParser parser = null;
        if (inputProxyLogFile.substring(inputProxyLogFile.length()-6).equals("xes.gz"))
            parser = new XesXmlGZIPParser();
        else
            parser = new XesXmlParser();

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

            //
            XEventClassifier attClassifier = null;
            if (inputProxyLog.getClassifiers().size()>0)
                attClassifier = inputProxyLog.getClassifiers().get(0);
            else
                attClassifier = new XEventAttributeClassifier("concept:name",new String[]{"concept:name"});
            XLogInfo logInfo = XLogInfoFactory.createLogInfo(inputProxyLog,attClassifier);
            int count = 999;
            if (logInfo.getNameClasses().getClasses().size()>0) {
                count = 0;
                for (XEventClass clazz : logInfo.getNameClasses().getClasses()) {
                    count++;
                    //        System.out.println(clazz.toString());
                }
            }

//            System.out.println("Number of unique activities " + count);

            //Let's construct the trie from the proxy log
            Trie t = new Trie(count);
            List<String> templist;
//            count=1;
            //count=0;
//            System.out.println("Proxy log size "+inputProxyLog.size());
            for (XTrace trace : inputProxyLog) {
                templist = new ArrayList<String>();
                for (XEvent e : trace) {
                    String label = e.getAttributes().get(attClassifier.getDefiningAttributeKeys()[0]).toString();

                    templist.add(Character.toString(service.alphabetize(label)));
                }
//                count++;
                //System.out.println(templist.toString());
                if (templist.size() > 0 ) {

                    //System.out.println(templist.toString());
//                    if (count == 37)
//                    StringBuilder sb = new StringBuilder();
//                    templist.stream().forEach(e -> sb.append(e));
//                    System.out.println(sb.toString());
                    t.addTrace(templist);
//                    if (count ==5)
//                    break;
                }
                /*count++;
                if (count%25000==0) {
                    break;
                    //System.out.println(count);
                    //System.out.println(String.format("Trie size: %d",t.getSize()));
                    //System.out.println(String.format("Trie avg length: %d",t.getAvgTraceLength()));
                }*/
            }
            return t;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    private static void testVanellaConformanceApproximation(String inputProxyLogFile, String inputSampleLogFile, ArrayList<String> result)
    {
        XLog proxyLog, sampleLog;
        StringBuilder sb;
        List<String> proxyTraces = new ArrayList<>();
        List<String> sampleTraces = new ArrayList<>();
        proxyLog = loadLog(inputProxyLogFile);
        sampleLog = loadLog(inputSampleLogFile);
        //HashMap<String, Integer> sampleTracesMap = new HashMap<>();
        init();

        for (XTrace trace : proxyLog) {
            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = e.getAttributes().get(proxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                sb.append(service.alphabetize(label));
            }
            proxyTraces.add(sb.toString());

        }
        int cnt=0;
        for (XTrace trace : sampleLog) {
            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = e.getAttributes().get(sampleLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                sb.append(service.alphabetize(label));
            }
            sampleTraces.add(sb.toString());
            //sampleTracesMap.put(sb.toString(),cnt);
            cnt++;
        }

        DeviationChecker deviationChecker = new DeviationChecker(service);
        // Now compute the alignments
        long start=System.currentTimeMillis(),timeTaken=0 ;
        long executionTime;
        int skipTo =0;
        int current = -1;
        int takeTo = 100;
        try {
            //System.out.println("Trace#, Alignment cost");
            //result.add("TraceId,Cost,ExecutionTime,ConfCheckerType");

            //for (String logTrace : sampleTraces) {
            for (int i = 0; i < sampleTraces.size(); i++) {
                String logTrace = sampleTraces.get(i);
                current++;
                if (current < skipTo)
                    continue;
                if (current > takeTo)
                    break;
                int minCost = Integer.MAX_VALUE;
                String bestTrace = "";
                String bestAlignment = "";
                start = System.currentTimeMillis();

                if(i==55)
                    System.out.println("debug");
                for (String proxyTrace : proxyTraces) {

                    if (proxyTrace.length()==0){
                        continue;
                    }


                    ProtoTypeSelectionAlgo.AlignObj obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(logTrace, proxyTrace);
                    if (obj.cost < minCost) {
                        minCost = (int) obj.cost;
                        if (logTrace.length()==0)
                            minCost++; // small fix if log trace is empty, then levenshteinDistancewithAlignment wrongly discounts the cost by 1
                        bestAlignment = obj.Alignment;
                        bestTrace = proxyTrace;
                        if (obj.cost == 0)
                            break;
                    }
                }

                executionTime = System.currentTimeMillis() - start;
                timeTaken += executionTime;


//            System.out.println("Total proxy traces "+proxyTraces.size());
//            System.out.println("Total candidate traces to inspect "+proxyTraces.size());
                //print trace number

                result.add(i+","+minCost+","+executionTime);
//                System.out.print(sampleTracesMap.get(logTrace));
                // print cost
//                System.out.println(", " + minCost);
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

}
