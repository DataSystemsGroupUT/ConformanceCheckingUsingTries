package ee.ut.cs.dsg.confcheck.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class Utils {


    public static int nextPrime(int m) {
        while(!isPrime(++m))
            // no need ++m; as I already added in the isPrime method's parameter.
            return m;
        return 0;
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
