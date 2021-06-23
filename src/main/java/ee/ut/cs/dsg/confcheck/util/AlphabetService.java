package ee.ut.cs.dsg.confcheck.util;

import java.util.HashMap;
import java.util.Set;

public class AlphabetService {

    HashMap<String, Character> activityToAlphabet = new HashMap<>();
    HashMap<Character, String> alphabetToActivity = new HashMap<>();
    int charCounter = 64;

    public char alphabetize(String label)
    {
        if (!activityToAlphabet.containsKey(label))
        {
            charCounter++;
            activityToAlphabet.put(label, (char) charCounter);
            alphabetToActivity.put((char) charCounter, label);
        }
        return activityToAlphabet.get(label);
    }
    public String deAlphabetize(char character)
    {
        return alphabetToActivity.get(character);
    }

    public Set<Character> getAlphabet()
    {
        return alphabetToActivity.keySet();
    }

    public void clear()
    {
        activityToAlphabet.clear();
        alphabetToActivity.clear();
        charCounter=64;
    }
}
