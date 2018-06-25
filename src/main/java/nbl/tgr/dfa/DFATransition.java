/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.dfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Quan-speedLab
 */
public class DFATransition {

    private Map<String, String> data;

    private String label;

    public String getLabel() {
        label = input + "|" + output;
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    private DFAState currentState;

    public DFAState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DFAState currentState) {
        this.currentState = currentState;
    }

    public DFAState getNextState() {
        return nextState;
    }

    public void setNextState(DFAState nextState) {
        this.nextState = nextState;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
    private DFAState nextState;
    private String input;
    private String output;

    public DFATransition(DFAState currentState, DFAState nextState, String input, String output) {
        this.currentState = currentState;
        this.nextState = nextState;
        this.input = input;
        this.output = output;
        this.data = new HashMap<>();
    }

    public void addData(String dt, String lbl) {
        data.put(dt, lbl);
    }

}
