package ee.ut.cs.dsg.confcheck.alignment;

import ee.ut.cs.dsg.confcheck.util.AlphabetService;

import java.util.ArrayList;
import java.util.List;

public class Alignment {
    private final List<Move> moves;
    private int totalCost;
    private String traceID;

    public Alignment(Alignment other) {
        this.moves = other.getMoves();
        this.totalCost = other.getTotalCost();
    }

    public Alignment() {
        this(0);
    }

    public Alignment(int totalCost) {
        this.moves = new ArrayList<>();
        this.totalCost = totalCost;
    }
    public Alignment(String id)
    {
        this.moves = new ArrayList<>();
        this.totalCost = 0;
        this.traceID = id;
    }

    public void appendMove(Move move) {
        appendMove(move, move.getCost());
    }

    public void appendMove(Move move, int oracleCost) {
        moves.add(move);
        totalCost += oracleCost;
    }

    public String getTraceID(){
        return traceID;
    }
    public int getTotalCost() {
        return totalCost;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        StringBuilder logTrace = new StringBuilder();
        StringBuilder modelTrace = new StringBuilder();
        result.append(String.format("Total cost:%d\n", totalCost));
        for (Move m : moves) {
            result.append(m.toString() + "\n");
            if (!m.getLogMove().equals(">>"))
                logTrace.append(m.getLogMove());
            if (!m.getModelMove().equals(">>"))
                modelTrace.append(m.getModelMove());
        }
        result.append("Log: " + logTrace + "\n");
        result.append("Mod: " + modelTrace);
        return result.toString();

    }

    public String toString(AlphabetService service) {
        StringBuilder result = new StringBuilder();
        StringBuilder logTrace = new StringBuilder();
        StringBuilder modelTrace = new StringBuilder();
        result.append(String.format("Total cost:%d\n", totalCost));
        for (Move m : moves) {
            result.append(m.toString(service) + "\n");
            if (!m.getLogMove().equals(">>"))
                logTrace.append(service.deAlphabetize(m.getLogMove().charAt(0)));
            if (!m.getModelMove().equals(">>"))
                modelTrace.append(service.deAlphabetize(m.getModelMove().charAt(0)));
        }
        result.append("Log: " + logTrace + "\n");
        result.append("Mod: " + modelTrace);
        return result.toString();

    }

    public List<Move> getMoves() {
        List<Move> result = new ArrayList<>();
        result.addAll(moves);
        return result;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public String logProjection() {
        StringBuilder sb = new StringBuilder();
        this.getMoves().stream().filter(x -> !x.getLogMove().equals(">>")).forEach(e -> sb.append(e.getLogMove().trim()));
        return sb.toString();
    }

    public String modelProjection() {
        StringBuilder sb = new StringBuilder();
        this.getMoves().stream().filter(x -> !x.getModelMove().equals(">>")).forEach(e -> sb.append(e.getModelMove().trim()));
        return sb.toString();
    }
    public double logCoverage()
    {
        int syncMoves=0, logLength=0;
        for (int i = 0; i < moves.size();i++)
        {
            Move move = moves.get(i);
            if (move.getMoveType()== Move.MoveType.SYNC_MOVE)
            {
                syncMoves++;
                logLength++;
            }
            else if (move.getMoveType()== Move.MoveType.LOG_MOVE)
            {
                logLength++;
            }
        }
        if (logLength==0)
            return 0.0;
        return ((double) syncMoves)/logLength;
    }
    public double weightedLogCoverage()
    {
        String log = logProjection();
        int startFrom = 0;
        double coverage = 0.0d;
        double perfectCoverage = 0.0d;
        for (int i = 0; i < log.length();i++) {
            perfectCoverage += Math.pow(i+1,-1);
            for (int j = startFrom; j < moves.size(); j++) {
                Move move = moves.get(j);
                if (move.getMoveType() == Move.MoveType.SYNC_MOVE && move.getLogMove().charAt(0) == log.charAt(i)) {
                    coverage += Math.pow(i+1,-1);
                    startFrom = j + 1;
                    break;
                }
            }
        }
        return coverage== 0.0d? 0.0d: coverage/perfectCoverage;

    }
    public double weightedLogCoverageOld()
    {
        int syncMovesWeight=0, logLength=this.logProjection().length();
        for (int i = 0; i < moves.size();i++)
        {
            Move move = moves.get(i);
            if (move.getMoveType()== Move.MoveType.SYNC_MOVE)
            {
                syncMovesWeight += ((logLength +1)-i);

            }
//            else if (move.getMoveType()== Move.MoveType.LOG_MOVE)
//            {
//                logLength++;
//            }
        }
        if (logLength==0)
            return 0.0;
        int max = 0;
        for (int i =0; i < logLength;i++)
            max+= ((logLength+1)-i);
        return ((double) syncMovesWeight)/max;
    }
}
