package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.util.AlphabetService;

import java.util.HashMap;
import java.util.Set;

public class DeviationChecker {

    private AlphabetService alphabetService;
    private HashMap<String, Integer> nonSynchrnousMovesCount;

    private int totalNonSynchronousMoves;
    public DeviationChecker()
    {
        alphabetService = new AlphabetService();
        nonSynchrnousMovesCount = new HashMap<>();
        totalNonSynchronousMoves = 0;
    }
    public DeviationChecker(AlphabetService service)
    {
        this.alphabetService = service;
        nonSynchrnousMovesCount = new HashMap<>(service.getAlphabet().size());
        for (Character c: service.getAlphabet())
        {
            updateNonSynchronousMovesCount(c.toString());
        }

    }

    public void processAlignment(Alignment alg)
    {
        for (Move v: alg.getMoves())
        {
            if (v.getCost() != 0)
            {
                updateNonSynchronousMovesCount(v.getModelMove());
                updateNonSynchronousMovesCount(v.getLogMove());
                totalNonSynchronousMoves++;

            }
        }
    }

    public Set<String> getAllActivities()
    {
        return nonSynchrnousMovesCount.keySet();
    }

    public double getDeviationPercentage(String label)
    {
        Integer result = nonSynchrnousMovesCount.get(label);
        if (result != null)
        {
            return ((double) result.intValue())/totalNonSynchronousMoves;
        }
        return 0;
    }
    private void updateNonSynchronousMovesCount(String label)
    {
        Integer result =nonSynchrnousMovesCount.get(label);
        if ( result == null)
        {
            nonSynchrnousMovesCount.put(label, 1);
        }
        else
        {
            nonSynchrnousMovesCount.put(label, result.intValue() +1);
        }

    }
}
