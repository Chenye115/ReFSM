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

/**
 *
 * @author Quan-speedLab
 */
public class DFAState {

    private String name;
    private boolean root;
    private boolean endState;

    public boolean isEndState() {
        return endState;
    }

    public void setEndState(boolean endState) {
        this.endState = endState;
    }
    private int no_of_seqs;

    private int hashNo;

    public int getHashNo() {
        return hashNo;
    }

    public void setHashNo(int hashNo) {
        this.hashNo = hashNo;
    }

    public boolean isRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DFATransition> getLstTransitions() {
        return lstTransitions;
    }

    public void setLstTransitions(List<DFATransition> lstTransitions) {
        this.lstTransitions = lstTransitions;
    }

    public Map<String, DFAState> getNextStates() {
        return nextStates;
    }

    public void setNextStates(Map<String, DFAState> nextStates) {
        this.nextStates = nextStates;
    }
    private List<DFATransition> lstTransitions;
    private Map<String, DFAState> nextStates;
    private Map<String, DFAState> previousStates;

    public Map<String, DFAState> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(Map<String, DFAState> previousStates) {
        this.previousStates = previousStates;
    }

    public DFAState(String name) {
        this.name = name;
    }

    public DFAState() {
        root = false;
        endState = false;
        nextStates = new HashMap<>();
        previousStates = new HashMap<>();
        lstTransitions = new LinkedList<>();
        no_of_seqs = 0;
    }

    public DFAState(boolean isRoot) {
        root = isRoot;
        endState = false;
        nextStates = new HashMap<>();
        previousStates = new HashMap<>();
        lstTransitions = new LinkedList<>();
        no_of_seqs = 0;
    }

    public int getNo_of_seqs() {
        return no_of_seqs;
    }

    public void removeState(String key) {
        nextStates.remove(key);
    }

    public void visit() {
        no_of_seqs++;
    }

    public DFAState doTransit(String input) {
        if (nextStates.containsKey(input)) {
            return nextStates.get(input);
        }
        return this;
    }

    public DFAState doTransitWithoutSefl(String input) {
        if (nextStates.containsKey(input)) {
            return nextStates.get(input);
        }
        return null;
    }

    public String doOutput(String input) {
        for (DFATransition trans : lstTransitions) {
            if (trans.getInput().equals(input)) {
                return trans.getOutput();
            }
        }
        return null;
    }

    public Set<DFAState> getAllState() {
        Set<DFAState> result = new HashSet<>();
        result.add(this);
        for (DFAState state : nextStates.values()) {
            // result.add(state);
            result.addAll(state.getAllState());
        }
        return result;
    }

    public Set<DFATransition> getAllTransition() {
        Set<DFATransition> result = new HashSet<>();
        result.addAll(lstTransitions);
        for (String trans : nextStates.keySet()) {
            DFAState state = nextStates.get(trans);
            if (state != null) {
                result.addAll(state.getAllTransition());
            }
        }
        return result;
    }

    public Set<String> getAllIOSymbols() {
        Set<String> result = new HashSet<>();
        for (String trans : nextStates.keySet()) {
            if (trans != null) {
                result.add(trans);
            }
            DFAState state = nextStates.get(trans);
            if (state != null) {
                result.addAll(state.getAllIOSymbols());
            }

        }
        return result;
    }

    public void addTransition(String lbl, DFAState nextState) {
        if (!nextStates.containsKey(lbl)) {
            nextStates.put(lbl, nextState);
            DFATransition trans = new DFATransition(this, nextState, lbl, lbl);
            lstTransitions.add(trans);
        }
    }

    public void addTransition(String input, String output, DFAState nextState) {
        if (!nextStates.containsKey(input)) {
            nextStates.put(input, nextState);
            DFATransition trans = new DFATransition(this, nextState, input, output);
            lstTransitions.add(trans);
        }
    }

    public void replaceTransition(String lbl, DFAState n) {
        nextStates.put(lbl, n);
        for (DFATransition trans : lstTransitions) {
            if (trans.getInput().equals(lbl)) {
                trans.setNextState(n);
            }
        }
    }

}
