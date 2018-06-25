
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.dfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nbl.tgr.mtc.Cluster;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public class EFSMResconstructor {

    private List<Session> traces;
    private List<Cluster> clusters;

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public List<Session> getTraces() {
        return traces;
    }

    public void setTraces(List<Session> traces) {
        this.traces = traces;
    }

    public EFSMResconstructor(List<Session> traces) {
        this.traces = traces;
    }

    public EFSMResconstructor(List<Session> traces, List<Cluster> clusters) {
        this.traces = traces;
        this.clusters = clusters;
    }

    private List<String> serverIPs;

    public List<String> getServerIPs() {
        return serverIPs;
    }

    public void setServerIPs(List<String> serverIPs) {
        this.serverIPs = serverIPs;
    }

    public void doResconstruct() {
        doLabeling();
        serverIPs = new ArrayList<>();
        int count = 0;
        DFAState root = new DFAState(true);
        for (Session s : traces) {
            boolean isCheck = false;
            if (s.getMessages() != null) {
                int size = s.getMessages().size();
                if (size > 2) {
                    RawMessage first = s.getMessages().get(1);
                    RawMessage second = s.getMessages().get(size - 2);
                    if (first.getCluterLabel() != null && second.getCluterLabel() != null && first.getCluterLabel().equals("USER")) {
                        isCheck = true;
                    }
                }

            }

            if (isCheck && count < 3) {
                count++;
                String srcIP = s.getSrcIP();
                DFAState currentState = root;
                for (RawMessage rm : s.getMessages()) {
                    if (rm.getSrcIP() == null ? srcIP == null : rm.getSrcIP().equals(srcIP)) {
                        currentState.visit();
                        Map<String, DFAState> nextStates = currentState.getNextStates();
                        if (nextStates.containsKey(rm.getCluterLabel())) {
                            currentState = nextStates.get(rm.getCluterLabel());
                        } else {
                            DFAState state = new DFAState();
                            currentState.getNextStates().put(rm.getCluterLabel(), state);
                            state.getPreviousStates().put(rm.getCluterLabel(), currentState);
                            currentState = state;
                            //state.getPreviousStates().put(srcIP, state)
                        }
                    }
                }
            }

            System.out.println(doSequence(s));

        }
        // cut off
        doCutOff(root, 0.12);

        Set<DFAState> allStates = root.getAllState();
        //Set<String> allTransitions = root.getAllTransition();
        System.out.println("Quan");
        doMerge(root);

    }

    
    
    
    public void doMerge(DFAState state) {
        Set<DFAState> allStates = state.getAllState();
        Set<String> allTransitions = state.getAllIOSymbols();
        int counter = 0;
        for (DFAState st : allStates) {
            counter++;
            st.setHashNo(counter);
        }
        int MOD = 10000;

        Set<Integer> markeds = new HashSet<>();
        Set<DFAState> finalStates = new HashSet<>();
        for (DFAState st1 : allStates) {
            if (st1.getNextStates().isEmpty()) {
                finalStates.add(st1);
            }
        }

        for (DFAState sS : allStates) {
            for (DFAState fS : finalStates) {
                if (sS != fS) {
                    int hashValue = doHash(sS, fS);
                    markeds.add(hashValue);
                }

            }
        }

        List<DFAState> lst = new ArrayList<DFAState>(allStates);
        Map<Integer, DFAState> map = new HashMap<>();
        boolean isUnMarkable = true;
        while (isUnMarkable) {
            isUnMarkable = false;
            for (int i = 0; i < lst.size(); i++) {
                DFAState srt = lst.get(i);
                map.put(srt.getHashNo(), srt);
                for (int j = 0; j < lst.size(); j++) {
                    if (i != j) {
                        DFAState fnl = lst.get(j);
                        //int markedValue = srt.getHashNo() * MOD + fnl.getHashNo();
                        int markedValue = doHash(srt, fnl);
                        if (!markeds.contains(markedValue)) {
                            for (String lbl : allTransitions) {
                                DFAState afSrt = srt.doTransit(lbl);
                                DFAState afFnl = fnl.doTransit(lbl);
                                //int hashValue = afSrt.getHashNo() * MOD + afFnl.getHashNo();
                                int hashValue = doHash(afSrt, afFnl);
                                if (markeds.contains(hashValue)) {
                                    markeds.add(markedValue);
                                    isUnMarkable = true;
                                    System.out.println(srt.getHashNo() + "|" + fnl.getHashNo());
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            System.out.println(markeds.size());
        }
        int count = markeds.size();
        List<Set<Integer>> distincts = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            //map.put(srt.getHashNo(), srt);
            for (int j = i + 1; j < lst.size(); j++) {
                int q = lst.get(i).getHashNo();
                int p = lst.get(j).getHashNo();
                Set<Integer> set = new HashSet<>();
                int hash = q * MOD + p;
                if (!markeds.contains(hash)) {
                    set.add(p);
                    set.add(q);
                    distincts.add(set);
                    System.out.println(q + "|" + p);
                }

            }
        }

        boolean isStop = true;
        while (isStop) {
            isStop = false;
            List<Set<Integer>> removed = new ArrayList<>();
            for (int i = 0; i < distincts.size(); i++) {
                for (int j = i + 1; j < distincts.size(); j++) {
                    Set<Integer> set1 = distincts.get(i);
                    Set<Integer> set2 = distincts.get(j);
                    Set<Integer> mutual = new HashSet<>(set1);
                    mutual.retainAll(set2);
                    if (!mutual.isEmpty()) {
                        set1.addAll(set2);
                        removed.add(set2);
                    }
                }
            }
            if (!removed.isEmpty()) {
                isStop = true;
                for (Set<Integer> set : removed) {
                    distincts.remove(set);
                }
            }
        }

        System.out.println(distincts.size());

        System.out.println("Quan");
    }
    //    public Map<Integer, Set<DFAState>> getReverseStates(DFAState state) {
    //        
    //    }
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

    public void doLabeling() {
        for (Cluster cluster : clusters) {
            for (RawMessage rm : cluster.getMessages()) {
                // treat:
                String lbl = "unknown";
                String payload = rm.getContent();
                String[] splited = payload.split(" ");
                if (splited.length > 0) {
                    lbl = splited[0].replace("\n", "").replace("\r", "");
                }
                rm.setCluterLabel(lbl);
                //rm.setCluterLabel(cluster.getLabel());
            }
        }
    }

    public String doSequence(Session s) {
        String unknownLbl = "unknown";
        String result = "" + s.getMessages().size();
        String srcIP = s.getSrcIP();

        result = result.concat("->" + srcIP);
        result = result.concat("->" + s.getDestIP());

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
