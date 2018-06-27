/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.dfa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import nbl.tgr.mtc.Cluster;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public class EFSMInferor {

    private String delimiter = ";";
    private String dataPath = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\ftp_sequence_fixed.txt";
    private String subDataPath = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\ftp_field.txt";
    private String testPath = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\Test001.txt";
    private List<List<String>> traces = new ArrayList<>();
    private Map<Integer,List<String>> test = new HashMap<>();

    private Map<String, List<String>> expecteds = new HashMap<>();

    private boolean tryParse(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String randomPickup(Set<String> set) {
        int index = new Random().nextInt(set.size());
        Iterator<String> iter = set.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }

    public void doFieldInfer() throws IOException {
        File file = new File(subDataPath);
        List<String> lines = Files.readAllLines(file.toPath());
        Map<String, List<String>> dataset = new HashMap<>();
        for (String line : lines) {
            String[] elm = line.split("\\|");
            if (elm.length > 1) {
                String lbl = elm[0];
                List<String> dt = Arrays.asList(elm[1].split(delimiter));
                dataset.put(lbl, dt);
                System.out.println(lbl + ":" + dt.size());
            }
        }

    }

    public void doFixing() throws IOException {
        File file = new File(dataPath);
        List<String> lines = Files.readAllLines(file.toPath());
        List<String> moved = new ArrayList<>();
        for (String line : lines) {
            String[] elm = line.split(delimiter);
            Stack stack = new Stack();
            for (int i = 0; i < elm.length; i++) {
                if (!tryParse(elm[i])) {
                    stack.push(0);
                } else {
                    if (stack.isEmpty()) {
                        moved.add(line);
                        break;
                    }
                    stack.pop();
                }
            }
        }
        lines.removeAll(moved);
        Map<String, Set<String>> responses = new HashMap<>();
        for (String line : lines) {
            String[] elm = line.split(delimiter);
            List<String> lst = new ArrayList<>();
            for (int i = 2; i < elm.length; i = i + 2) {
                String sb = elm[i];
                if (i + 1 < elm.length) {
                    if (!responses.containsKey(sb)) {
                        responses.put(sb, new HashSet<String>());
                    }
                    Set<String> rsps = responses.get(sb);
                    rsps.add(elm[i + 1]);
                }
                lst.add(sb);
            }
            traces.add(lst);
        }

        List<String> newlines = new ArrayList<>();
        for (String line : lines) {
            String[] elm = line.split(delimiter);
            if (elm.length % 2 == 1) {
                String last = elm[elm.length - 1];
                Set<String> candidates = responses.get(last);
                if (candidates != null && !candidates.isEmpty()) {
                    line = line + delimiter + randomPickup(candidates);
                } else {
                    line = line + delimiter + "200";
                }
            }
            newlines.add(line);
        }
        String dataFixedPath = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\ftp_sequence_fixed.txt";
        FileOutputStream fos = new FileOutputStream(dataFixedPath);

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
            for (String line : newlines) {
                bw.write(line);
                bw.newLine();
            }
        }
        fos.close();

    }

    public void doLoading() throws IOException {
        File file = new File(dataPath);
        List<String> lines = Files.readAllLines(file.toPath());
        int count = 0;
        for (String line : lines) {
            String[] elm = line.split(delimiter);
            List<String> lst = new ArrayList<>();
            for (int i = 2; i < elm.length; i = i + 2) {
                String sb = elm[i];
                if (i + 1 < elm.length) {
                    sb = sb + "|" + elm[i + 1];
                }
                lst.add(sb);

            }
            lst.add("QUIT|221");
            count++;
            traces.add(lst);
        }

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
            for (int i = 2; i < elm.length; i = i + 2) {
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

    public void doEvaluate_v2(DFAState root) {
        Map<List<String>, Queue<String>> map = new HashMap<>();
        for (List<String> lst : test.values()) {
            List<String> sbm = new ArrayList<>();
            Queue<String> exp = new LinkedList<>();
            for (String str : lst) {
                String[] splitted = str.split("\\|");
                if (splitted.length > 1) {
                    sbm.add(splitted[0]);
                    exp.add(splitted[1]);
                }
            }
            map.put(sbm, exp);
        }
        int count = 0;
        for (Map.Entry<List<String>, Queue<String>> entry : map.entrySet()) {
            List<String> sbm = entry.getKey();
            Queue<String> exp = entry.getValue();

            DFAState current = root;
            for (String input : sbm) {
                String output = current.doOutput(input);

                current = current.doTransitWithoutSefl(input);
                if (current == null) {
                    break;
                }
                String expected = exp.poll();
                if (expected == null) {
                    break;
                }
//                else {
//                    if (!output.equals(expected)) {
//                        break;
//                    }
//                }
            }
            if (exp.isEmpty()) {
                count++;
            } else {
                System.out.println("");
            }

        }
        double precise = (double) count / test.size();
        System.out.println(count);
        System.out.println(test.size());
        System.out.println(precise);

    }

    public void doEvaluate_v3(DFAState root) {
        Map<List<String>, List<String>> map = new HashMap<>();
        for (List<String> lst : test.values()) {
            List<String> sbm = new ArrayList<>();
            List<String> exp = new ArrayList<>();
            for (String str : lst) {
                String[] splitted = str.split("\\|");
                if (splitted.length > 1) {
                    sbm.add(splitted[0]);
                    exp.add(splitted[1]);
                }
            }
            map.put(sbm, exp);
        }
        int count = 0;
        int total = 0;
        for (Map.Entry<List<String>, List<String>> entry : map.entrySet()) {
            List<String> sbm = entry.getKey();
            List<String> exp = entry.getValue();
            total = total + sbm.size();
            DFAState current = root;
            for (int i = 0; i < sbm.size(); i++) {
                String input = sbm.get(i);
                String expected = exp.get(i);
                String output = current.doOutput(input);
                current = current.doTransitWithoutSefl(input);
                if (current == null) {
                    break;
                }
                if (output != null && expected != null && output.equals(expected)) {
                    count++;

                } else {
                    System.out.println(input + "|" + output + "| expected: " + expected);
                }
            }
        }
        double precise = (double) count / total;
        System.out.println(count);
        System.out.println(total);
        System.out.println(precise);
    }

    public void doEvaluate_v1(DFAState root) {
        Map<List<String>, List<String>> map = new HashMap<>();
        for (List<String> lst : test.values()) {
            List<String> sbm = new ArrayList<>();
            List<String> exp = new ArrayList<>();
            for (String str : lst) {
                String[] splitted = str.split("\\|");
                if (splitted.length > 1) {
                    sbm.add(splitted[0]);
                    exp.add(splitted[1]);
                }
            }
            map.put(sbm, exp);
        }

        int count = 0;
        int total = 0;
        for (Map.Entry<List<String>, List<String>> entry : map.entrySet()) {
            List<String> sbm = entry.getKey();
            List<String> exp = entry.getValue();
            total = total + sbm.size();
            DFAState current = root;
            for (int i = 0; i < sbm.size(); i++) {
                String input = sbm.get(i);
                String expected = exp.get(i);
                String output = current.doOutput(input);

                current = current.doTransitWithoutSefl(input);
                if (current == null) {
                    break;
                }
                if (output != null && expected != null && output.equals(expected)) {
                    count++;
                    System.out.println(input);
                }
            }

        }
        double precise = (double) count / total;
        System.out.println(count);
        System.out.println(total);
        System.out.println(precise);

    }
    
    public void doResconstruct() {
        DFAState initialDFA = new DFAState(true);
        for (List<String> s : traces) {
            DFAState currentState = initialDFA;
            for (String iol : s) {
                String[] splitted = iol.split("\\|");
                if (splitted.length > 1) {
                    String input = splitted[0];
                    String output = splitted[1];
                    // For future use
                    if (!expecteds.containsKey(input)) {
                        expecteds.put(input, new ArrayList<>());
                    }
                    expecteds.get(input).add(output);

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
        }
        System.out.println("No of states: ");
        System.out.println(initialDFA.getAllState().size());
        //doCutOff(root, 0.1);
        DFAState root = doMerge(initialDFA);
        //doEvaluate_v1(root);
        System.out.println("_______________________________");
        doEvaluate_v3(root);

    }

    public DFAState doMerge(DFAState state) {
        Set<DFAState> allStates = state.getAllState();
        Set<DFATransition> allTransitions = state.getAllTransition();
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

        System.out.println(markeds.size());
        return root;

    }

    private final int M = 10000;

    private int doHash(DFAState src, DFAState end) {
        int q = src.getHashNo() < end.getHashNo() ? src.getHashNo() : end.getHashNo();
        int p = src.getHashNo() < end.getHashNo() ? end.getHashNo() : src.getHashNo();
        return q * M + p;
    }

    public void doCutOff(DFAState state, double threshHold) {
        int total = 0;
        for (DFAState stt : state.getNextStates().values()) {
            total += stt.getNo_of_seqs();
        }
        List<String> removeKeys = new ArrayList<>();
        for (String key : state.getNextStates().keySet()) {
            DFAState stt = state.getNextStates().get(key);
            double rate = (double) stt.getNo_of_seqs() / total;
            if (rate < threshHold) {
                removeKeys.add(key);
            }
        }
        for (String key : removeKeys) {
            state.removeState(key);
        }
        for (DFAState stt : state.getNextStates().values()) {
            doCutOff(stt, threshHold);
        }
    }

    public String doSequence(Session s) {
        String unknownLbl = "unknown";
        String result = "" + s.getMessages().size();
        String srcIP = s.getSrcIP();

        result = result.concat("->" + srcIP);
        result = result.concat("->" + s.getDestIP());
//        String c="";
//        if(s.getMessages().size()>1){
//            c = s.getMessages().get(1).getCluterLabel();
//        }
//        
//        if (c != null && c.equals("USER anonymous") && !serverIPs.contains(s.getDestIP())) {
//            serverIPs.add(s.getDestIP());
//        }
        for (RawMessage rm : s.getMessages()) {
            if (rm.getSrcIP() == null ? srcIP == null : rm.getSrcIP().equals(srcIP)) {
                result = result.concat("->");
                String lbl = rm.getCluterLabel();
                if (lbl == null) {
                    lbl = unknownLbl;
                }
                result = result.concat(lbl);

            }
        }
        return result;
    }
}
