package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.util.AlphabetService;
import ee.ut.cs.dsg.confcheck.util.Configuration;
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
import org.processmining.operationalsupport.xml.OSXMLConverter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import static ee.ut.cs.dsg.confcheck.util.Configuration.ConformanceCheckerType;

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

        ConformanceCheckerType checkerType = ConformanceCheckerType.TRIE_STREAMING;

        String pathName = "output\\test.csv";

        String proxyLog = "input/BPI_2012_1k_sample.xes";
        String inputLog = "input/BPI_2012_Sim_2k_random_0.95.xes";
        System.out.println(inputLog);
        List<String> res = testStreamConformanceOnFile(proxyLog, inputLog, params);
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


    private static void init()
    {
        service = new AlphabetService();
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
//            System.out.print(alg.getTotalCost());
//            System.out.print("|");
//            System.out.println(alg.getTraceSize());
            result.add(Integer.toString(i) + "," + alg.getTotalCost() + "," + executionTime); //+ "," + alg.toString());

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

}
