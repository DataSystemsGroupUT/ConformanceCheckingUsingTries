package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.cost.CostFunction;
import ee.ut.cs.dsg.confcheck.cost.DualProgressiveCostFunction;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import ee.ut.cs.dsg.confcheck.util.Configuration;
import org.cpntools.accesscpn.model.graphics.Align;

import java.sql.Array;
import java.util.*;

public class StreamingConformanceChecker extends ConformanceChecker{



    protected boolean verbose = false;
    protected final CostFunction costFunction;

    // Streaming variables

    protected boolean replayWithLogMoves = true; // if set to false the performance is faster but result is less precise
    protected int minDecayTime = 3;
    protected float decayTimeMultiplier = 0.25F; // not yet implemented
    protected boolean discountedDecayTime = false; // if set to false then uses fixed minDecayTime value
    protected int averageTrieLength = 0;



    public StreamingConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue, int maxTrials, CostFunction costFunction)
    {
        super(trie, logCost, modelCost, maxStatesInQueue);
        rnd = new Random(19);
        this.maxTrials = maxTrials;
        inspectedLogTraces = new Trie(trie.getMaxChildren());
        this.costFunction = costFunction;
        if (discountedDecayTime){
            this.averageTrieLength = trie.getAvgTraceLength();
        }
    }
    public StreamingConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue) {
        this(trie,logCost,modelCost,maxStatesInQueue, 10000);

    }

    public StreamingConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue, int maxTrials) {
        this(trie, logCost, modelCost, maxStatesInQueue, maxTrials, new DualProgressiveCostFunction());
    }

    public State checkForSyncMoves(String event, State currentState){

        TrieNode prev = currentState.getNode();
        TrieNode node;
        Alignment alg = new Alignment(currentState.getAlignment());
        Move syncMove;

        node = prev.getChild(event);
        if(node==null){
            return null;
        } else {
            syncMove = new Move(event, event, 0);
            alg.appendMove(syncMove);
            prev = node;
            int decayTime;
            if(discountedDecayTime){
                decayTime = Math.max(Math.round((averageTrieLength-alg.getNumberOfEventsSeen())*decayTimeMultiplier),minDecayTime);
            } else {
                decayTime = minDecayTime;
            }

            return new State(alg, new ArrayList<>(), prev, currentState.getCostSoFar(), currentState, decayTime);
        }

    }

    public State getCurrentOptimalState(String caseId, boolean finalState){ //
        State state;
        StatesBuffer caseStatesInBuffer;
        HashMap<String, State> currentStates;
        List<State> statesList = new ArrayList<>();

        List<State> optimalStates = new ArrayList<>();
        int postfixSize;
        int minLengthToEnd;
        int minAdditionalCost = 99999;
        int decayTime;
        if (statesInBuffer.containsKey(caseId)){
            caseStatesInBuffer = statesInBuffer.get(caseId);
            currentStates = caseStatesInBuffer.getCurrentStates();
            statesList.addAll(currentStates.values());
            for(State s:statesList){
                if (finalState){
                    postfixSize = s.getTracePostfix().size();
                    minLengthToEnd = s.getNode().getMinPathLengthToEnd();
                    if((postfixSize+minLengthToEnd)==0){
                        return s;
                    } else if ((postfixSize+minLengthToEnd)<=minAdditionalCost){
                        minAdditionalCost = postfixSize+minLengthToEnd;
                        optimalStates.add(s);
                    }
                } else {
                    if(discountedDecayTime){
                        decayTime = Math.max(Math.round((averageTrieLength-s.getAlignment().getNumberOfEventsSeen())*decayTimeMultiplier),minDecayTime);
                    } else {
                        decayTime = minDecayTime;
                    }
                    if(s.getDecayTime() == decayTime & s.getTracePostfix().size()==0){
                        return s;
                    }
                }

            }

            for (State optS:optimalStates){
                postfixSize = optS.getTracePostfix().size();
                minLengthToEnd = optS.getNode().getMinPathLengthToEnd();
                if ((postfixSize+minLengthToEnd)==minAdditionalCost){
                    Alignment alg = optS.getAlignment();
                    int currentCost = optS.getCostSoFar();
                    TrieNode currentNode = optS.getNode();
                    List<String> postfix = new ArrayList<>(optS.getTracePostfix());
                    // add log moves
                    while(postfix.size()>0){
                        Move m = new Move(postfix.get(0),">>",1);
                        alg.appendMove(m, 1);
                        currentCost++;
                        postfix.remove(0);
                    }
                    // add model moves
                    while(currentNode.getMinPathLengthToEnd()>0){
                        currentNode = currentNode.getChildOnShortestPathToTheEnd();
                        Move m = new Move(">>", currentNode.getContent(), 1);
                        alg.appendMove(m, 1);
                        currentCost++;

                    }

                    // return state
                    return new State(alg, new ArrayList<>(),currentNode, currentCost);
                }
            }
        }

        // did not find a matching case ID
        // OR there is no state with most recent decay time and no trace postfix (note: this part should not happen)
        return null;

    }



    public Alignment check(List<String> trace){
        System.out.println("Only implemented for compatibility with interface");
        return new Alignment();
    }

    public HashMap<String, State> check(List<String> trace, String caseId)
    {

        traceSize = trace.size();
        State state;
        State previousState;
        StatesBuffer caseStatesInBuffer = null;
        Alignment alg;
        TrieNode node;
        TrieNode prev;
        List<String> traceSuffix;
        int suffixLookAheadLimit;
        HashMap<String, State> currentStates = new HashMap<>();
        ArrayList<State> syncMoveStates = new ArrayList<>();

        // iterate over the trace - choose event by event
        // modify everything into accepting event instead of list of events

        if (statesInBuffer.containsKey(caseId))
        {
            // case exists, fetch last state
            caseStatesInBuffer = statesInBuffer.get(caseId);
            currentStates = caseStatesInBuffer.getCurrentStates();

        }
        else
        {
            // if sync move(s) --> add sync move(s) to currentStates. If one of the moves will not be sync move, then start checking from that move.
            int decayTime;
            if(discountedDecayTime){
                decayTime = Math.max(Math.round(averageTrieLength*decayTimeMultiplier),minDecayTime);
            } else {
                decayTime = minDecayTime;
            }
            currentStates.put(new Alignment().toString(), new State(new Alignment(), new ArrayList<String>(), modelTrie.getRoot(), 0, decayTime+1)); // larger decay time because this is decremented in this iteration
        }

        for(String event:trace){
            // sync moves
            // we iterate over all states
            for (Iterator<Map.Entry<String, State>> states = currentStates.entrySet().iterator(); states.hasNext(); ) {

                Map.Entry<String, State> entry = states.next();
                previousState = entry.getValue();
                if (previousState.getTracePostfix().size()!=0){
                    continue; // we are not interested in previous states which already have a suffix (i.e. they already are non-synchronous)
                }

                state = checkForSyncMoves(event, previousState);
                if (state!=null){
                    // we are only interested in new states which are synced (i.e. they do not have a suffix)
                    syncMoveStates.add(state);
                }
            }


            // check if sync moves --> if yes, add sync states, update old states, remove too old states
            if (syncMoveStates.size() > 0){
                for (Iterator<Map.Entry<String, State>> states = currentStates.entrySet().iterator(); states.hasNext(); ) {
                    Map.Entry<String, State> entry = states.next();
                    previousState = entry.getValue();
                    int previousDecayTime = previousState.getDecayTime();
                    // remove states with decayTime less than 2
                    if (previousDecayTime < 2) {
                        states.remove();
                    } else {
                        List<String> postfix = new ArrayList<>();
                        postfix.add(event);
                        previousState.addTracePostfix(postfix);
                        previousState.setDecayTime(previousDecayTime - 1);
                    }
                }

                for(State s:syncMoveStates){
                    alg = s.getAlignment();
                    currentStates.put(alg.toString(), s);
                }
                //caseStatesInBuffer.setCurrentStates(currentStates);
                //statesInBuffer.put(caseId, caseStatesInBuffer);
                //return currentStates;
                syncMoveStates.clear();
                continue;
            }


            // no sync moves. We iterate over the states, trying to make model and log moves
            HashMap<String, State> statesToIterate = new HashMap<>(currentStates);
            List<State> interimCurrentStates = new ArrayList<>();
            List<String> traceEvent = new ArrayList<>();
            traceEvent.add(event);
            int currentMinCost = 99999;
            for (Iterator<Map.Entry<String, State>> states = statesToIterate.entrySet().iterator(); states.hasNext(); ) {
                Map.Entry<String, State> entry = states.next();
                previousState = entry.getValue();


                State logMoveState = handleLogMove(traceEvent, previousState, "");

                traceSuffix = previousState.getTracePostfix();
                traceSuffix.addAll(traceEvent);
                List<State> modelMoveStates = handleModelMoves(traceSuffix, previousState, null);


                // add log move
                if(logMoveState.getCostSoFar() < currentMinCost){
                    interimCurrentStates.clear();
                    interimCurrentStates.add(logMoveState);
                    currentMinCost = logMoveState.getCostSoFar();
                } else if(logMoveState.getCostSoFar() == currentMinCost){
                    interimCurrentStates.add(logMoveState);
                }

                // add model moves
                for(State s:modelMoveStates){
                    if(s.getCostSoFar()< currentMinCost){
                        interimCurrentStates.clear();
                        interimCurrentStates.add(s);
                        currentMinCost = s.getCostSoFar();
                    } else if(s.getCostSoFar() == currentMinCost) {
                        interimCurrentStates.add(s);
                    }
                }


                int previousStateDecayTime = previousState.getDecayTime();
                if(previousStateDecayTime<2){
                    currentStates.remove(previousState.getAlignment().toString());
                } else {
                    previousState.setDecayTime(previousStateDecayTime-1);
                }

            }

            // add new states with the lowest cost
            for (State s:interimCurrentStates) {
                if (s.getCostSoFar() == currentMinCost) {
                    currentStates.put(s.getAlignment().toString(), s);
                }
            }


        }





        if(caseStatesInBuffer==null){
            caseStatesInBuffer = new StatesBuffer(currentStates);
        } else {
            caseStatesInBuffer.setCurrentStates(currentStates);
        }

        statesInBuffer.put(caseId, caseStatesInBuffer);
        return currentStates;

    }





    protected List<State> handleModelMoves(List<String> traceSuffix, State state, State dummyState){
        TrieNode matchNode;
        Alignment alg;
        List<String> suffixToCheck = new ArrayList<>(); //make a new list and add to it
        suffixToCheck.addAll(traceSuffix);
        int lookAheadLimit = traceSuffix.size();
        List<TrieNode> currentNodes = new ArrayList<>();
        List<TrieNode> childNodes = new ArrayList<>();
        List<TrieNode> matchingNodes = new ArrayList<>();
        List<State> matchingStates = new ArrayList<>();
        currentNodes.add(state.getNode());

        while (lookAheadLimit>0) {
            // from current level, fetch all child nodes
            for(TrieNode n:currentNodes){
                childNodes.addAll(n.getAllChildren());
            }
            // for all child nodes, try to get a substring match
            for(TrieNode n:childNodes){
                matchNode = modelTrie.matchCompletely(suffixToCheck, n);
                // !! matching node != substring
                if (matchNode!=null){
                    matchingNodes.add(matchNode);
                }
            }

            // something has matched, we will not look further
            if(matchingNodes.size()>0){
                break;
            }

            // no match, so child nodes become current nodes, and we reduce look ahead
            currentNodes.clear();
            currentNodes.addAll(childNodes);
            childNodes.clear();
            lookAheadLimit--;

            //if lookAhead is exhausted, but we can split suffix
            if (lookAheadLimit==0 & suffixToCheck.size()>1 & replayWithLogMoves){
                suffixToCheck.remove(0);
                lookAheadLimit = suffixToCheck.size();
                currentNodes.clear();
                currentNodes.add(state.getNode());
            }

        }

        if(matchingNodes.size()==0){
            //we didn't find any match, return empty array
        } else {
            // iterate back from matchingNode until parent = state.getNode
            // because we need correct alignment and cost
            for(TrieNode n:matchingNodes){
                alg = state.getAlignment();
                int cost = state.getCostSoFar();
                TrieNode currentNode = n;
                TrieNode parentNode = n.getParent();
                TrieNode lastMatchingNode = state.getNode();
                List<Move> moves = new ArrayList<>();
                boolean makeLogMoves = false;

                // first find all sync moves, then add model moves (parent does not match event), then add log moves (events still remaining in traceSuffix)
                for(int i = traceSuffix.size(); --i >= 0;){
                    String event = traceSuffix.get(i);
                    if(event.equals(currentNode.getContent())){
                        Move syncMove = new Move(event, event, 0);
                        moves.add(0, syncMove);
                        currentNode = parentNode;
                        parentNode = currentNode.getParent();
                        if(i>0){
                            continue; // there could still be more sync moves
                        }
                    } else {
                        makeLogMoves = true;
                    }

                    // we either have a non-sync move or we have exhausted the suffix.
                    // so we need to add model moves (and log moves if applicable)

                    // we first iterate until we get to the lastMatchingNode
                    while(!currentNode.equals(lastMatchingNode)){
                        Move modelMove = new Move(">>",currentNode.getContent(),1);
                        cost++;
                        moves.add(0,modelMove);
                        currentNode = parentNode;
                        if(currentNode.getLevel()==0){ //we have reached the root node
                            break;
                        }
                        parentNode = currentNode.getParent();
                    }

                    // we also add all log moves now
                    while(makeLogMoves & i>=0) {
                        event = traceSuffix.get(i);
                        Move logMove = new Move(event, ">>", 1);
                        cost++;
                        moves.add(0, logMove);
                        i--;
                    }
                }

                // matching states
                for(Move m:moves){
                    alg.appendMove(m);
                }
                int decayTime;

                if(discountedDecayTime){
                    decayTime = Math.max(Math.round((averageTrieLength-alg.getNumberOfEventsSeen())*decayTimeMultiplier),minDecayTime);
                } else {
                    decayTime = minDecayTime;
                }
                matchingStates.add(new State(alg, new ArrayList<>(), n, cost, decayTime));

            }

        }

        return matchingStates;


    }



    @Override
    protected State handleLogMove(List<String> traceSuffix, State state, String event) {
        Alignment alg = new Alignment(state.getAlignment());
        State logMoveState;
        List<String> suffix = new ArrayList<>(state.getTracePostfix());
        suffix.addAll(traceSuffix);
        for (String e:suffix){
            Move logMove = new Move(e, ">>", 1);
            alg.appendMove(logMove);
        }
        int decayTime;

        if(discountedDecayTime){
            decayTime = Math.max(Math.round((averageTrieLength-alg.getNumberOfEventsSeen())*decayTimeMultiplier),minDecayTime);
        } else {
            decayTime = minDecayTime;
        }
        logMoveState = new State(alg, new ArrayList<String>(), state.getNode(), state.getCostSoFar()+suffix.size(), decayTime);
        return logMoveState;
    }
}
