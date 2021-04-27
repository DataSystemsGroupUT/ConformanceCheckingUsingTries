package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.trie.Trie;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Runner {

    public static void main(String... args)
    {
        //testBed2();
        //testBed1();
//        System.out.println("abc".hashCode()%29);
//        System.out.println("cab".hashCode()%29);
//        System.out.println("bca".hashCode()%29);
//        System.out.println(Math.abs("etoile".hashCode())%29);
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
        trace4.add("b");
        trace4.add("c");
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

        ConformanceChecker cnfChecker = new ConformanceChecker(t);

        Alignment alg;
        alg = cnfChecker.check(trace4);
        System.out.println(alg.toString());

        alg = cnfChecker.check(trace6);
        System.out.println(alg.toString());

        alg = cnfChecker.check(trace7);
        System.out.println(alg.toString());

        alg = cnfChecker.check(trace8);
        System.out.println(alg.toString());




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
        ConformanceChecker cnfChecker = new ConformanceChecker(t);

        Alignment alg = cnfChecker.check(trace6);

        System.out.println(alg.toString());
    }
}
