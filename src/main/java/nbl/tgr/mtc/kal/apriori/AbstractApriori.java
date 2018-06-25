/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc.kal.apriori;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nbl.tgr.mtc.GeneralApioriAlg;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public abstract class AbstractApriori {

    private List<Session> traces;

    public final List<Session> getTraces() {
        return traces;
    }

    public final void setTraces(List<Session> traces) {
        this.traces = traces;
    }

    public final double getThresh_hold() {
        return thresh_hold;
    }

    public final void setThresh_hold(double thresh_hold) {
        this.thresh_hold = thresh_hold;
    }
    private double thresh_hold;

    public abstract Map<String, Integer> initialize();

    public abstract Map<String, Integer> generateNextCandidateSet(Map<String, Integer> candidateSet);

    public abstract Map<String, Integer> generate1lengthCandidateSet();

    public abstract Map<String, Integer> generate2LengthCandidateSet(Map<String, Integer> candidateSet);

    public abstract Map<String, Integer> countFrequency(Map<String, Integer> candidateSet, int length);

    public abstract Map<String, Integer> countFrequencyOnSession(Map<String, Integer> candidateSet, int length);

    public Map<String, Integer> generateClosedWord(List<Map<String, Integer>> lst_l_length) {
        Map<String, Integer> finalClosedWords = new HashMap<>();
        for (int k = lst_l_length.size() - 1; k > -1; k--) {
            Map<String, Integer> l_length = lst_l_length.get(k);
            for (String key : l_length.keySet()) {
                boolean isAdd = true;
                for (String str : finalClosedWords.keySet()) {
                    if (str.contains(key)) {
                        isAdd = false;
                    }
                }
                if (isAdd) {
                    finalClosedWords.put(key, l_length.get(key));
                }
            }
        }
        return finalClosedWords;
    }

    /**
     * doApriori: find the closed words.
     * @return 
     */
    public List<String> doApriori() {
        int cut_off = (int) (traces.size() * getThresh_hold());
        
        Map<String, Integer> filtered = initialize();

        //double position_thresh_hold = 0.0000;
        System.out.println("1_length generation: " + filtered);

        Map<String, Integer> generation = generate2LengthCandidateSet(filtered);
        System.out.println("Number of 2 length generation: " + generation.size());
        Map<String, Integer> generation2 = countFrequencyOnSession(generation, 2);

        Map<String, Integer> g2Filtered = generation2.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        System.out.println("2-length generation before cutoff: " + generation2.size());
        System.out.println("2-length generation after cutoff: " + g2Filtered.size());
        System.out.println("---------------------------------------------------");
        List<Map<String, Integer>> lst_l_length = new ArrayList<>();
        //lst_l_length.add(filtered);
        lst_l_length.add(g2Filtered);

        boolean stop = false;
        int i = 3;
        Map<String, Integer> startGen = g2Filtered;
        while (!stop) {
            Map<String, Integer> length_i_Gen = generateNextCandidateSet(startGen);
            System.out.println("Number of " + i + " length generation: " + length_i_Gen.size());
            Map<String, Integer> generation_i = countFrequencyOnSession(length_i_Gen, i);
            Map<String, Integer> g_i_Filtered = generation_i.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            System.out.println(i + "-length generation before cutoff: " + generation_i.size());
            System.out.println(i + "-length generation after cutoff: " + g_i_Filtered.size());
            
            System.out.println("---------------------------------------------------");
            // System.out.println(i + "-length generation after cutoff 's value: " + g_i_Filtered);
            startGen = g_i_Filtered;
            i++;
            if (startGen.isEmpty()) {
                stop = true;
            }
            lst_l_length.add(g_i_Filtered);
        }

        Map<String, Integer> mergeWords = new HashMap<>();
        for (Map<String, Integer> l_length : lst_l_length) {
            for (String key : l_length.keySet()) {
                mergeWords.put(key, l_length.get(key));
            }
        }
        System.out.println("Find the closed words");
        System.out.println("---------------------------------------------------");
        Map<String, Integer> closedWords = new HashMap<>();
        for (String word : mergeWords.keySet()) {
            //Map<String, Integer> relatedWords = new HashMap<>();
            boolean isAdded = true;
            for (String other : mergeWords.keySet()) {
                if (word != other) {
                    if (other.contains(word)) {
                        //relatedWords.put(other, finalClosedWords.get(other));
                        isAdded = false;
                        break;
                    }
                }
            }
            if (isAdded) {
                closedWords.put(word, mergeWords.get(word));
            }
        }

        return new ArrayList<>(closedWords.keySet());
    }
}
