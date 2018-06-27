/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.dfa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Quan-speedLab
 */
public class PPLiveInferror {

    private String delimiter = ";";
    private String dataPath = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\PPLive\\Test Traces\\PPLive_sequence.txt";
    private String testPath = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\PPLive\\Test Traces\\PPLive_Test1.txt";
    private List<List<String>> traces = new ArrayList<>();
    private Map< Integer, List<String>> test = new HashMap<>();

    public void doLoading() throws IOException {
        File file = new File(dataPath);
        List<String> lines = Files.readAllLines(file.toPath());
        int count = 0;
        for (String line : lines) {
            String[] elm = line.split(delimiter);
            List<String> lst = new ArrayList<>();
            for (int i = 0; i < elm.length; i = i + 1) {
                String sb = elm[i];
                lst.add(sb);

            }
            count += lst.size();
            traces.add(lst);
        }
        System.out.println("Number of messages: " + count);
        File testfile = new File(testPath);
        List<String> tlines = Files.readAllLines(testfile.toPath());
        int positive = 0;
        int negative = 0;
        for (String line : tlines) {
            String[] data = line.split("\\|");
            if (data.length < 2) {
                break;
            }
            String dataline = data[0];
            int accepted = Integer.parseInt(data[1]);
            String[] elm = dataline.split(delimiter);
            List<String> lst = new ArrayList<>();
            for (int i = 0; i < elm.length; i = i + 1) {
                String sb = elm[i];
                lst.add(sb);
            }
            if (accepted == 1) {
                test.put(positive++, lst);
            } else if (accepted == 0) {
                test.put(negative--, lst);
            }

        }
    }

    public void doResconstruct() {
        DFAState initialDFA = new DFAState(true);
        for (List<String> s : traces) {
            DFAState currentState = initialDFA;
            for (String iol : s) {
                String input = iol;
                String output = iol;

                currentState.visit();
                Map<String, DFAState> nextStates = currentState.getNextStates();
                if (nextStates.containsKey(input)) {
                    currentState = nextStates.get(input);
                } else {
                    DFAState state = new DFAState();
                    currentState.addTransition(input, output, state);
                    currentState = state;
                }
            }
        }
        System.out.println("No of states: ");
        System.out.println(initialDFA.getAllState().size());
        //doCutOff(root, 0.1);
        DFAState root = doMerge(initialDFA);
        //doEvaluate_v1(root);
        System.out.println("_______________________________");
        doEvaluate(root);

    }

    public DFAState doMerge(DFAState state) {
        Set<DFAState> allStates = state.getAllState();
        Set<DFATransition> allTransitions = state.getAllTransition();
        System.out.println("Initial nodes: " + allStates.size());
        System.out.println("Initial transitions: " + allTransitions.size());
        Map<DFAState, List<DFATransition>> previous = new HashMap<>();
        int counter = 0;
        for (DFAState st : allStates) {
            counter++;
            st.setHashNo(counter);
            //map.put(counter, st);
        }
        int countEndStates = 0;
        for (DFAState s : allStates) {
            if (s.getNextStates().isEmpty()) {
                countEndStates++;
            }
        }
        System.out.println(countEndStates);
        List<Set<DFAState>> markeds = new ArrayList<>();
        Map<String, List<DFATransition>> map = new HashMap<>();
        for (DFATransition tr1 : allTransitions) {

            if (!map.containsKey(tr1.getInput())) {
                map.put(tr1.getInput(), new ArrayList<>());
            }
            List<DFATransition> lst = map.get(tr1.getInput());
            lst.add(tr1);
        }
        for (String lbl : map.keySet()) {
            List<DFATransition> lst = map.get(lbl);
            Set<DFAState> fromSet = new HashSet<>();
            Set<DFAState> toSet = new HashSet<>();
            for (DFATransition trans : lst) {
                fromSet.add(trans.getCurrentState());
                toSet.add(trans.getNextState());
            }
            markeds.add(toSet);
            markeds.add(fromSet);

        }

        boolean isStop = true;
        while (isStop) {
            isStop = false;
            List<Set<DFAState>> removed = new ArrayList<>();
            for (int i = 0; i < markeds.size(); i++) {
                Set<DFAState> set1 = markeds.get(i);
                for (int j = 0; j < markeds.size(); j++) {
                    if (i != j) {
                        Set<DFAState> set2 = markeds.get(j);
                        Set<DFAState> mutual = new HashSet<>(set1);
                        mutual.retainAll(set2);
                        if (!mutual.isEmpty()) {
                            set1.addAll(set2);
                            removed.add(set2);
                        }
                    }

                }
                if (!removed.isEmpty()) {
                    break;
                }
            }
            if (!removed.isEmpty()) {
                isStop = true;
                for (Set<DFAState> set : removed) {
                    markeds.remove(set);
                }
            }
        }

        Map<Set<DFAState>, DFAState> lstofStates = new HashMap<>();
        for (Set<DFAState> set : markeds) {
            DFAState newState = new DFAState();
            lstofStates.put(set, newState);
        }

        for (String lbl : map.keySet()) {
            List<DFATransition> lst = map.get(lbl);
            DFATransition trans = lst.get(0);
            DFAState from = trans.getCurrentState();
            DFAState to = trans.getNextState();
            if (to != null) {
                DFAState correspondingFrom = null;
                DFAState correspondingTo = null;
                for (Set<DFAState> set : lstofStates.keySet()) {
                    if (set.contains(from)) {
                        correspondingFrom = lstofStates.get(set);
                    }
                    if (set.contains(to)) {
                        correspondingTo = lstofStates.get(set);
                    }
                }
                if (correspondingFrom != null && correspondingTo != null) {
                    //correspondingFrom.addTransition(lbl, correspondingTo);
                    correspondingFrom.addTransition(lbl, trans.getOutput(), correspondingTo);
                    correspondingTo.getPreviousStates().put(lbl, correspondingFrom);
                }
            }
        }
        DFAState root = new DFAState();
        for (DFAState st : lstofStates.values()) {
            if (st.getPreviousStates().isEmpty()) {
                root = st;
            }
            if (st.getNextStates().isEmpty()) {
                st.setEndState(true);
            }
        }
        //Set<DFAState> finalStates = root.getAllState();
        System.out.println(markeds.size());
        return root;

    }

    public void doEvaluate(DFAState root) {
        int count = 0;
        for (List<String> sbm : test.values()) {
            DFAState current = root;
            boolean isAccepted = true;
            for (int i = 0; i < sbm.size(); i++) {
                String input = sbm.get(i);
                current = current.doTransitWithoutSefl(input);
                if (current == null) {
                    isAccepted = false;
                    break;
                }
            }
            if (isAccepted) {
                count++;
            }
        }
        System.err.println("Accepted sequence: " + count);
    }

}
