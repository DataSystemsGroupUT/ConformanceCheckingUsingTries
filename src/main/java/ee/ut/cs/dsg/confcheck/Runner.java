package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.util.AlphabetService;
import it.unimi.dsi.fastutil.Hash;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
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
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.operationalsupport.xml.OSXMLConverter;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
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
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static ee.ut.cs.dsg.confcheck.util.Configuration.ConformanceCheckerType;
import static ee.ut.cs.dsg.confcheck.util.Configuration.LogSortType;


public class Runner {

    private static AlphabetService service;

    public static void main(String... args) throws Exception {


        // settings
        HashMap<String, Object> params = new HashMap<>();
        params.put("logCost", 1); // not implemented
        params.put("modelCost", 1); // not implemented
        params.put("stateLimit", 10000); // not implemented
        params.put("caseLimit", 10000);
        params.put("minDecayTime", 3); // 3
        params.put("decayTimeMultiplier", 0.3F); //0.3F
        params.put("discountedDecayTime", true);

        // log files
        String inputLog = "input/BPI_2020_1k_sample.xes";
        String proxyLog = "input/Stress_Test/Simulated_Log_Small.xes.gz"; //"input/BPI_2020_Sim_2k_random_0.95.xes";
/*
        ArrayList<String> result;

        // file based
        result = testStreamConformanceOnFile(proxyLog, inputLog, params);

        for (String r:result){
            System.out.println(r);
        }
*/
        /*for (Character s : service.getAlphabet()){
            System.out.println(s+"\t"+service.deAlphabetize(s));
        }*/

        // stream based
        testStreamConformanceOnStream(proxyLog,params);


        String executionType = "other"; // "stress_test" or "cost_diff"

        // Cost difference

        if (executionType == "cost_diff") {
            long unixTime = Instant.now().getEpochSecond();
            Date date = new Date(unixTime*1000L);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String formattedDate = dateFormat.format(date);

            String pathPrefix = "output\\disc3\\";
            String fileType = ".csv";

            ConformanceCheckerType checkerType = ConformanceCheckerType.TRIE_STREAMING;

            String sProxyLogPath = "";
            String sLogPath = "";
            String pathName = "";
            List<String> res = null;
            String path_folder = "";
            String im_setting = "";


            // M-models
            path_folder = "M-models";
            List<String> logs = new ArrayList<>();
            logs.addAll(Arrays.asList("M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "M10"));

            for (String log_name:logs){
                sProxyLogPath = "input\\"+path_folder+"\\"+log_name+"_sim.xes";
                sLogPath = "input\\"+path_folder+"\\"+log_name+".xes";
                pathName = pathPrefix + formattedDate + "_" + log_name + fileType;

                res = testStreamConformanceOnFile(sProxyLogPath, sLogPath, params);
                res.add(0, String.format("TraceId, Cost_%1$s, ExecutionTime_%1$s", checkerType));
                try {
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


            // BPI-models
            logs = new ArrayList<>();
            logs.addAll(Arrays.asList("BPI_2012_0.2", "BPI_2012_0.5", "BPI_2012_0.8", "BPI_2012_0.95",
                    "BPI_2017_0.2", "BPI_2017_0.5", "BPI_2017_0.8", "BPI_2017_0.95",
                    "BPI_2020_0.2", "BPI_2020_0.5", "BPI_2020_0.8", "BPI_2020_0.95"));

            for (String log_name:logs){
                sProxyLogPath = "input\\"+log_name.substring(0,8)+"_Sim_2k_random"+log_name.substring(8)+".xes";
                sLogPath = "input\\"+log_name.substring(0,8)+"_1k_sample.xes";
                pathName = pathPrefix + formattedDate + "_" + log_name + fileType;

                res = testStreamConformanceOnFile(sProxyLogPath, sLogPath, params);
                res.add(0, String.format("TraceId, Cost_%1$s, ExecutionTime_%1$s", checkerType));
                try {
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



        } else {
            System.out.println("Unknown execution type");
        }



    }

    public static void testStreamConformanceOnStream(String proxyLog, HashMap<String, Object> params) throws UnknownHostException {


        int logCost = (int) params.get("logCost"); // not implemented
        int modelCost = (int) params.get("modelCost"); // not implemented
        int stateLimit = (int) params.get("stateLimit"); // not implemented
        int caseLimit = (int) params.get("caseLimit");
        int minDecayTime = (int) params.get("minDecayTime");
        float decayTimeMultiplier = (float) params.get("decayTimeMultiplier");
        boolean discountedDecayTime = (boolean) params.get("discountedDecayTime");

        int port = 1234;
        InetAddress address = InetAddress.getByName("127.0.0.1");
        long eventsReceived = 0;
        boolean execute = true;
        OSXMLConverter converter = new OSXMLConverter();
        init();
        long start = System.currentTimeMillis();
        Trie t = constructTrie(proxyLog);
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
                long runTimeMillis = 60000*60;
                long eventsLimit = 5000000;
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

                StreamingConformanceChecker checker = new StreamingConformanceChecker(t, logCost, modelCost,stateLimit,caseLimit,minDecayTime,decayTimeMultiplier,discountedDecayTime);

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

                    checker.check(events, caseId);
                    eventHandledTime = System.currentTimeMillis();
                    eventReceivedToPrepared += eventPreparedTime - eventReceivedTime;
                    eventReceivedToHandled += eventHandledTime - eventReceivedTime;
                    eventPreparedToHandled += eventHandledTime - eventPreparedTime;
                    totalIdleTime += idleTime;
                    //System.out.println(String.format("%d\t%d\t%d\t%d", eventPrepared-eventReceived, eventHandled-eventReceived, eventHandled-eventPrepared, idleTime));
                    if(System.currentTimeMillis()-start>runTimeMillis){
                        System.out.println(String.format("Time limit reached. Run time: %d", runTimeMillis));
                        System.out.print("events: ");
                        System.out.print(eventsReceived);
                        System.out.print(" | ");
                        checker.printCurrentCaseAndStateCounts();
                        System.out.print("\n");
                        System.out.println("Received to prepared, Received to handled, Prepared to handled, Idle time");
                        System.out.println(String.format("%d, %d, %d, %d", eventReceivedToPrepared, eventReceivedToHandled, eventPreparedToHandled, totalIdleTime));
                        break;
                    }
                    if(eventsReceived%500==0){
                        System.out.print("events: ");
                        System.out.print(eventsReceived);
                        System.out.print(" | ");
                        checker.printCurrentCaseAndStateCounts();
                        System.out.print("\n");
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

    public static ArrayList<String> testStreamConformanceOnFile(String proxyLog, String inputLog, HashMap<String, Object> params){

        int logCost = (int) params.get("logCost"); // not implemented
        int modelCost = (int) params.get("modelCost"); // not implemented
        int stateLimit = (int) params.get("stateLimit"); // not implemented
        int caseLimit = (int) params.get("caseLimit");
        int minDecayTime = (int) params.get("minDecayTime");
        float decayTimeMultiplier = (float) params.get("decayTimeMultiplier");
        boolean discountedDecayTime = (boolean) params.get("discountedDecayTime");

        ArrayList<String> result = new ArrayList<>();

        init();
        Trie t = constructTrie(proxyLog);

        XLog inputSamplelog;
        XesXmlParser parser = new XesXmlParser();
        long totalTime=0;

        try {
            InputStream is = new FileInputStream(inputLog);
            inputSamplelog = parser.parse(is).get(0);

            List<String> templist = new ArrayList<>();
            List<String> tracesToSort = new ArrayList<>();
            XTrace trace;

            for (int i = 0; i < inputSamplelog.size(); i++)
            {
                trace = inputSamplelog.get(i);
                templist = new ArrayList<>();

                for (XEvent e: trace)
                {
                    String label = "";
                    try{
                        label = e.getAttributes().get(inputSamplelog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                    } catch (Exception ex) {
                        label = e.getAttributes().get("concept:name").toString();
                    }
                    templist.add(Character.toString(service.alphabetize(label)));
                }

                StringBuilder sb = new StringBuilder(templist.size());
                sb.append(i).append((char)63); // we prefix the trace with its ID

                Arrays.stream(templist.toArray()).forEach( e-> sb.append(e));


                tracesToSort.add(sb.toString());
            }

            ConformanceChecker checker = new StreamingConformanceChecker(t, logCost, modelCost,stateLimit,caseLimit,minDecayTime,decayTimeMultiplier,discountedDecayTime);

            for (int i = 0; i < tracesToSort.size(); i++) {
                totalTime = computeAlignmentStream(tracesToSort, checker, totalTime, i, result);

            }

        } catch(Exception e)
        {
            e.printStackTrace();
        }

        return result;
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

      //t.printTraces();
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
                checker = new StreamingConformanceChecker(t, 1, 1, 100000, 100000, 3, 0.3F, true);
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
            int takeTo = 99999;
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
                    String label = "";
                    try{
                        label = e.getAttributes().get(inputSamplelog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                    } catch (Exception ex) {
                        label = e.getAttributes().get("concept:name").toString();
                    }
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
                        totalTime = computeAlignmentStream(tracesToSort, checker, totalTime, i, result);
                    } else {
                        totalTime = computeAlignment(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i, result);
                    }
                }
            }
//
            else {
                for (int i = 0; i < tracesToSort.size(); i++) {
                    if (confCheckerType == ConformanceCheckerType.TRIE_STREAMING) {
                        totalTime = computeAlignmentStream(tracesToSort, checker, totalTime, i, result);
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


    private static long computeAlignmentStream(List<String> tracesToSort, ConformanceChecker checkerC, long totalTime, int i, ArrayList<String> result) {
        long start;
        long executionTime;
        Alignment alg;
        List<String> trace = new ArrayList<String>();
        List<String> tempList;
        StreamingConformanceChecker checker = (StreamingConformanceChecker) checkerC;

        int pos = tracesToSort.get(i).indexOf((char) 63);

        String actualTrace = tracesToSort.get(i).substring(pos + 1);
        for (char c : actualTrace.toCharArray()) {
            trace.add(new StringBuilder().append(c).toString());
        }

        start = System.currentTimeMillis();

        for (String e : trace) {
            tempList = new ArrayList<String>();
            tempList.add(e);
            checker.check(tempList, Integer.toString(i));
        }

        alg = checker.getCurrentOptimalState(Integer.toString(i), false).getAlignment();


        executionTime = System.currentTimeMillis() - start;
        totalTime += executionTime;
        if (alg != null) {
            //System.out.println(Integer.toString(i) + "," + actualTrace);

            result.add(Integer.toString(i) + "," + alg.getTotalCost() + "," + executionTime); //+ "," + alg.toString());

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
            count=0;
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
                count++;
                /*
                if (count==25000){

                    break;
                }
                */


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
