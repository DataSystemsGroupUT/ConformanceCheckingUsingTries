package ee.ut.cs.dsg.confcheck.alignment;

public class AlignmentFactory {

    public static Alignment createAlignmentFromString(String input)
    {
        Alignment alg = new Alignment();
        String[] moves = input.split(">>");
        Move mv;
        for (String move : moves)
        {
             if (move.trim().startsWith("Sync"))
             {
                 String label = move.replace("Sync","").trim();
                 mv = new Move(label, label,0);
                 alg.appendMove(mv);
             }
             else if (move.trim().startsWith("Insertion"))
             {
                 String label = move.replace("Insertion","").trim();
                 mv = new Move(label, ">>",1);
                 alg.appendMove(mv);
             }
             else if (move.trim().startsWith("Deletion"))
             {
                 String label = move.replace("Deletion","").trim();
                 mv = new Move(">>",label,1);
                 alg.appendMove(mv);
             }

        }
        return alg;
    }
}
