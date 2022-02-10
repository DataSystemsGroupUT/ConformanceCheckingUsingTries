package ee.ut.cs.dsg.confcheck.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Utils {


    private static int[] primes = {2, 3,
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

    private static int currentPrimeIndex =29;
    public static int nextPrime(int m) {
        while(!isPrime(++m))
            // no need ++m; as I already added in the isPrime method's parameter.
            return m;
        return 0;
    }
    public static int getNextPrimeFromList()
    {
        if (currentPrimeIndex+1 > 44)
        {
            return primes[currentPrimeIndex];
        }
        else return primes[++currentPrimeIndex];
    }
    public static void resetPrimeIndex()
    {
        currentPrimeIndex = 29;
    }

    public static int getPreviousPrimeFromList()
    {
        if (currentPrimeIndex-1 < 9)
        {
            return primes[currentPrimeIndex];
        }
        else return primes[--currentPrimeIndex];
    }

    public static boolean isPrime(int m) {
        for(int i = 2; i <= m; i++)
            if(m % i == 0)
                return false;
        return true;
    }

    public static void getFileHeader(String logFileName)
    {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(logFileName));
            String line;

            line = reader.readLine();
            while (line != null && !line.toLowerCase().contains("trace"))
            {
                System.out.println(line);
                line = reader.readLine();
            }

            reader.close();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
