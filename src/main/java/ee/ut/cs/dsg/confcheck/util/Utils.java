package ee.ut.cs.dsg.confcheck.util;

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

}
