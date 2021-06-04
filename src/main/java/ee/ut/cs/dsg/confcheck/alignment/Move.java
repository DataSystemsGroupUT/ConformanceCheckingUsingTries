package ee.ut.cs.dsg.confcheck.alignment;

public class Move {
    private String logMove;
    private String modelMove;
    private int cost;
    private int oracle;

    public Move(String logMove, String modelMove, int cost)
    {
        this.cost = cost;
        this.logMove = logMove;
        this.modelMove = modelMove;
    }

    public int getCost() {
        return  cost;
    }

    public String toString()
    {
        return String.format("[logMove:%s, modelMove:%s, cost:%d]", logMove,modelMove,cost);
    }

    public String getModelMove(){return modelMove;}
    public String getLogMove(){return  logMove;}
}
