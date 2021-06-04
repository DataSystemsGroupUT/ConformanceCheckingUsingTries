package ee.ut.cs.dsg.confcheck.alignment;

import java.util.ArrayList;
import java.util.List;

public class Alignment {
    private List<Move> moves;
    private int totalCost;

    public Alignment(Alignment other)
    {
        this.moves = other.getMoves();
        this.totalCost = other.getTotalCost();
    }
    public Alignment()
    {
        this(0);
    }
    public Alignment(int totalCost)
    {
        this.moves = new ArrayList<>();
        this.totalCost = totalCost;
    }

    public void appendMove(Move move)
    {
        appendMove(move, move.getCost());
    }
    public void appendMove(Move move, int oracleCost)
    {
        moves.add(move);
        totalCost+=oracleCost;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();
        StringBuilder logTrace = new StringBuilder();
        StringBuilder modelTrace = new StringBuilder();
        result.append(String.format("Total cost:%d\n", totalCost));
        for (Move m: moves)
        {
            result.append(m.toString()+"\n");
            if (!m.getLogMove().equals(">>"))
                logTrace.append(m.getLogMove());
            if(!m.getModelMove().equals(">>"))
                modelTrace.append(m.getModelMove());
        }
        result.append("Log: "+logTrace.toString()+"\n");
        result.append("Mod: "+modelTrace.toString());
        return result.toString();

    }

    public List<Move> getMoves()
    {
        List<Move> result= new ArrayList<>();
        result.addAll(moves);
        return result;
    }
    public int hashCode()
    {
        return this.toString().hashCode();
    }
}
