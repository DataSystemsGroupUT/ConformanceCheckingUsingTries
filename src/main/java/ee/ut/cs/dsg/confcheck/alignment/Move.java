package ee.ut.cs.dsg.confcheck.alignment;

import ee.ut.cs.dsg.confcheck.util.AlphabetService;

public class Move {
    private final String logMove;
    private final String modelMove;
    private final int cost;
    private int oracle;

    public enum MoveType
    {
        SYNC_MOVE,
        LOG_MOVE,
        MODEL_MOVE
    }
    public Move(String logMove, String modelMove, int cost) {
        this.cost = cost;
        this.logMove = logMove;
        this.modelMove = modelMove;
    }

    public int getCost() {
        return cost;
    }

    public String toString() {
        return String.format("[logMove:%s, modelMove:%s, cost:%d]", logMove, modelMove, cost);
    }

    public String toString(AlphabetService service) {
        String l = service.deAlphabetize(logMove.charAt(0));
        String m = service.deAlphabetize(modelMove.charAt(0));
        return String.format("[logMove:%s, modelMove:%s, cost:%d]", l == null ? ">>" : l, m == null ? ">>" : m, cost);
    }

    public String getModelMove() {
        return modelMove;
    }

    public String getLogMove() {
        return logMove;
    }

    public MoveType getMoveType()
    {
        if (logMove.equals(">>"))
            return MoveType.MODEL_MOVE;
        if (modelMove.equals(">>"))
            return  MoveType.LOG_MOVE;
        return MoveType.SYNC_MOVE;
    }
}
