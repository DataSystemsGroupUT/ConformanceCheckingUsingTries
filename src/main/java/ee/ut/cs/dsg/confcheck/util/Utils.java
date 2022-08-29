package ee.ut.cs.dsg.confcheck.util;

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

}
