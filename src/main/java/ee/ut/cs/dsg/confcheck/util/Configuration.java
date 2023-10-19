package ee.ut.cs.dsg.confcheck.util;

public class Configuration {


    public enum ConformanceCheckerType
    {
        DISTANCE,
        TRIE_PREFIX,
        TRIE_RANDOM,
        TRIE_RANDOM_STATEFUL,
        TRIE_STREAMING
    }

    public enum LogSortType
    {
        NONE,
        TRACE_LENGTH_ASC,
        TRACE_LENGTH_DESC,
        LEXICOGRAPHIC_ASC,
        LEXICOGRAPHIC_DESC

    }
    public enum MoveType
    {
        SYNCHRONOUS_MOVE,
        LOG_MOVE,
        MODEL_MOVE
    }
}
