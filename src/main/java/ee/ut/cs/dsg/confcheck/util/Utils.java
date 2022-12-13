package ee.ut.cs.dsg.confcheck.util;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.*;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    public static AlphabetService service;

    public static void init()
    {
        service = new AlphabetService();
    }

    private static final int[] primes = {2, 3,
            5,
            7,
            11,
            13,
            17,
            19,
            23,
            29,
            31,
            37,
            41,
            43,
            47,
            53,
            59,
            61,
            67,
            71,
            73,
            79,
            83,
            89,
            97,
            101,
            103,
            107,
            109,
            113,
            127,
            131,
            137,
            139,
            149,
            151,
            157,
            163,
            167,
            173,
            179,
            181,
            191,
            193,
            197,
            199};

    private static int currentPrimeIndex = 29;

    public static int nextPrime(int m) {
        while (!isPrime(++m))
            // no need ++m; as I already added in the isPrime method's parameter.
            return m;
        return 0;
    }

    public static int getNextPrimeFromList() {
        if (currentPrimeIndex + 1 > 44) {
            return primes[currentPrimeIndex];
        } else return primes[++currentPrimeIndex];
    }

    public static void resetPrimeIndex() {
        currentPrimeIndex = 29;
    }

    public static int getPreviousPrimeFromList() {
        if (currentPrimeIndex - 1 < 9) {
            return primes[currentPrimeIndex];
        } else return primes[--currentPrimeIndex];
    }

    public static boolean isPrime(int m) {
        for (int i = 2; i <= m; i++)
            if (m % i == 0)
                return false;
        return true;
    }

    public static void getFileHeader(String logFileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFileName));
            String line;

            line = reader.readLine();
            while (line != null && !line.toLowerCase().contains("trace")) {
                System.out.println(line);
                line = reader.readLine();
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Trie constructTrie(String inputProxyLogFile)
    {
        if (inputProxyLogFile.endsWith(".xml") || inputProxyLogFile.endsWith(".xes") )
            return handleXESLogs(inputProxyLogFile);
        else
            return handleTXTLogs(inputProxyLogFile);
    }

    public static Trie constructTrie(List<String> traces)
    {
        Trie t = new Trie(1024);
        for (String s : traces)
        {

            List<String> templist = new ArrayList<>(s.length());
            for (int i =0; i < s.length();i++)
                templist.add(String.valueOf(s.charAt(i)));
                t.addTrace(templist);
        }

        return t;
    }

    private static Trie handleTXTLogs(String inputProxyLogFile) {
        Path filePath = FileSystems.getDefault().getPath(inputProxyLogFile);
        Trie t = new Trie(1024);
        try
        {

            Files.lines(filePath).forEach(l -> {
                List<String> templist = new ArrayList<>(l.length());
                for (int i =0; i < l.length();i++)
                    templist.add(String.valueOf(l.charAt(i)));
                t.addTrace(templist);
            } );
            return t;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static Trie handleXESLogs(String inputProxyLogFile) {
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
            Trie t = new Trie(1024);//new Trie(count);
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
//                    String sss = templist.toString().replace("[","").replace("]","").replace(",","").replace(" ","");
//                    if(sss.startsWith("AEBnFIQJD"))
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

    public static List<String> readLogFileAsListOfStrings(String inputProxyLogFile)
    {
        XLog inputProxyLog = loadLog(inputProxyLogFile);
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        List<String> traces = new ArrayList<>(inputProxyLog.size());
        try {

            XLogInfo logInfo;
            logInfo = XLogInfoFactory.createLogInfo(inputProxyLog, inputProxyLog.getClassifiers().get(0));

//            System.out.println("Number of unique activities " + count);

            //Let's construct the trie from the proxy log


            StringBuilder traceLabels;

//            System.out.println("Proxy log size "+inputProxyLog.size());
            for (XTrace trace : inputProxyLog) {
                traceLabels = new StringBuilder(trace.size());
                for (XEvent e : trace) {
                    String label = e.getAttributes().get(inputProxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();

                    traceLabels.append(service.alphabetize(label));
                }
                traces.add(traceLabels.toString());
//                count++;
                //System.out.println(templist.toString());

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
            return traces;

    }
    public static void shortenTraces(String inputProxyLogFile, String outputFile, int low, int high)
    {

        try{
            List<String> traces = readLogFileAsListOfStrings(inputProxyLogFile);
            //write to the file the shortened traces
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (String trace: traces)
                writer.write(Utils.removeLoopsInRange(trace, low, high )+"\n");
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static XLog loadLog(String inputProxyLogFile)
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

    public static String removeLoops(String trace, int loopLength)
    {
        if (loopLength < 0)
            throw  new IllegalArgumentException("Loop length cannot be negative!");
        if (loopLength > trace.length() || loopLength==0)
            return trace;

        StringBuilder result = new StringBuilder(trace.length());
        String lastBodyLoop="";
        int lastAppendedIndex=-1;
        for (int i = 0; i+2*loopLength < trace.length();)
        {

            if (trace.substring(i,i+loopLength).equals(trace.substring(i+loopLength, i+2*loopLength)))// || trace.substring(i,i+loopLength).equals(lastBodyLoop))

            {
                if (!trace.substring(i,i+loopLength).equals(lastBodyLoop))
                {
                    lastBodyLoop = trace.substring(i,i+loopLength);
                    result.append(trace.substring(i,i+loopLength));
                }



                i+=2*loopLength;
            }
            else if (trace.substring(i,i+loopLength).equals(lastBodyLoop)) {// this is the last occurence of the loop
//                result.append(trace.charAt(i));
                lastBodyLoop="";
                i+=loopLength;
            }
            else
            {
                result.append(trace.charAt(i));
                lastBodyLoop="";
                i++;
            }
            lastAppendedIndex=i-1;
        }


//        //let's put the first loopLength letters to the output
//        for (int i =0; i < loopLength; i++)
//            result.append(trace.charAt(i));
//        int lastAppendedIndex = loopLength-1;
//        for (int i = loopLength; i+loopLength < trace.length();)
//        {
//            if (!trace.substring((i-loopLength)+1,i+1).equals(trace.substring(i+1,i+loopLength+1))) {
//
//                result.append(trace.charAt(i));
//                lastAppendedIndex=i;
//                i++;
//            }
//            else
//            {
//                // we have to move loopLength forward
//                result.append(trace.charAt(i));
//                i+= loopLength;
//                lastAppendedIndex=i;
//            }
//        }
        for (int i = lastAppendedIndex+1; i < trace.length();i++)
            result.append(trace.charAt(i));
        return result.toString();
    }
    public static String removeLoopsInRange(String trace, int low, int high)
    {
        String result = trace.toString();
        for (int i = high; i >= low; i--)
            result = removeLoops(result, i);
        return  result;
    }
    public static String parseReachabilityGraphFileInPetrifyFormatToNeo4JCommands(String pathToFile)
    {
        Path filePath = FileSystems.getDefault().getPath(pathToFile);
        Set<String> statesCypher = new HashSet<>();
        Set<String> sources = new HashSet<>();
        Set<String> states = new HashSet<>();
        List<String> transitions =  new ArrayList<>();
        List<String> lines = new ArrayList<>();
        String startState="";
        String endState;
        try
        {

            Files.lines(filePath).forEach(l -> lines.add(l));

            for (String l: lines)
            {
                if (l.startsWith(".dummy") || l.startsWith(".model")|| l.startsWith(".end")|| l.startsWith(".state"))
                    continue;
                if (l.startsWith(".marking"))
                {

                    startState = l.replace(".marking {","").replace(" }","").trim();
                }
                else
                {
                    String[] elements = l.split(" ");
                    statesCypher.add(String.format("(%s:State{name:'%s'}),\n", elements[0].trim(),elements[0].trim()));
                    statesCypher.add(String.format("(%s:State{name:'%s'}),\n", elements[2].trim(),elements[2].trim()));
                    transitions.add(String.format("(%s)-[:CF{activity:'%s'}]->(%s),\n",elements[0].trim(),elements[1].trim(),elements[2].trim() ));
                    sources.add(elements[0].trim());
                    states.add(elements[0].trim());
                    states.add(elements[2].trim());
                }
            }

            endState = states.stream().filter(x-> !sources.contains(x)).collect(Collectors.toList()).get(0);
            StringBuilder result = new StringBuilder();
            result.append("CREATE\n");
            for (String s: statesCypher)
                result.append(s);
            for (String t:transitions)
                result.append(t);
            result.append("\n"+startState+"\n"+endState);
            return result.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Alignment> getAlignmentsFromCSVFile(String filePath)
    {
        Path path = FileSystems.getDefault().getPath(filePath);


        List<Alignment> algs =  new ArrayList<>();
        List<String> lines = new ArrayList<>();
        String startState="";
        String endState;
        try
        {

            Files.lines(path).forEach(l -> lines.add(l));

            for (String l: lines)
            {
                if (l.startsWith("Case"))
                    continue;
               String[] elems = l.split(",");
               Alignment alg = new Alignment(elems[0]);
               String[] alg2 = elems[1].split("\\|");
               for (String s: alg2)
               {
                   String[] alg3 = s.split("]");
                   Move mv;
                   if (alg3[0].replace("[","").trim().equals("L/M"))
                   {
                        mv = new Move(alg3[1].trim(), alg3[1].trim(),0);
                   }
                   else if (alg3[0].replace("[","").trim().equals("M"))
                   {
                       if (alg3[1].trim().startsWith("t"))
                           continue;
                       mv = new Move(">>", alg3[1].trim(),1);
                   }
                   else
                       mv = new Move(alg3[1].trim(),">>",1);
                   alg.appendMove(mv);
               }
               algs.add(alg);

            }

           return algs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public static void unfoldFileOfResults(String filePath, String outputFilePath)
    {
        /*
        *****
        * This is a method used to unfold content of proprietary CSV file where the
        * the first column combines together several rows, the elements of the first column are comma separated
        * Columns are semi colon separated.
         */
        Path path = FileSystems.getDefault().getPath(filePath);



        List<String> lines = new ArrayList<>();
        List<String> linesOut = new ArrayList<>();
        try
        {

            Files.lines(path).forEach(l -> lines.add(l));

            for (String l: lines)
            {

                String[] elems = l.split(";");

                String[] alg2 = elems[0].split(",");
                for (String s: alg2)
                {
                    StringBuilder sbd = new StringBuilder();
                    sbd.append(s.replace("\\|","")).append(";");
                    for (int i = 1; i < elems.length;i++)
                        sbd.append(elems[i].replace(",",".")).append(";");
                    linesOut.add(sbd.toString());
                }


            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            PrintWriter printWriter = new PrintWriter(writer);

            for (String s: linesOut)
            {
                printWriter.println(s);
            }
            printWriter.close();
            writer.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
