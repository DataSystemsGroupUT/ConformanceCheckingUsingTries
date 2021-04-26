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
        moves = new ArrayList<>();
        totalCost = 0;
    }

    public void appendMove(Move move)
    {
        moves.add(move);
        totalCost+=move.getCost();
    }

    public int getTotalCost() {
        return totalCost;
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        result.append(String.format("Total cost:%d\n", totalCost));
        for (Move m: moves)
        {
            result.append(m.toString()+"\n");
        }
        return result.toString();

    }

    public List<Move> getMoves()
    {
        List<Move> result= new ArrayList<>();
        result.addAll(moves);
        return result;
    }
}
