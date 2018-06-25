/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc.kal.apriori;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public class DelimiterBasedApriori extends AbstractApriori {

    private static String DELIMITER = " ";
    private boolean isBinaryAnalysis;
//    private List<Session> traces;
//    private double thresh_hold;
//
//    public List<Session> getTraces() {
//        return traces;
//    }
//
//    public void setTraces(List<Session> traces) {
//        this.traces = traces;
//    }
//
//    public double getThresh_hold() {
//        return thresh_hold;
//    }
//
//    public void setThresh_hold(double thresh_hold) {
//        this.thresh_hold = thresh_hold;
//    }

    @Override
    public Map<String, Integer> initialize() {
        return null;
    }



    public Map<String, Integer> generateNextCandidateSet(Map<String, Integer> candidateSet) {
        Map<String, Integer> result = new HashMap<>();
        for (String key1 : candidateSet.keySet()) {
            for (String key2 : candidateSet.keySet()) {
                if (key1 == null ? key2 != null : !key1.equals(key2)) {
                    String subStr = key2.substring(0, key2.lastIndexOf(DELIMITER));
                    if (key1.contains(subStr)) {
                        String newone = key1.concat(DELIMITER).concat(key2.substring(key2.lastIndexOf(DELIMITER) + 1));
                        if (!result.containsKey(newone)) {
                            result.put(newone, 0);
                        }
                    }
                }
            }
        }
        return result;
    }

    public Map<String, Integer> generate2LengthCandidateSet(Map<String, Integer> candidateSet) {
        Map<String, Integer> result = new HashMap<>();
        for (String key1 : candidateSet.keySet()) {
            for (String key2 : candidateSet.keySet()) {
                if (key1 == null ? key2 != null : !key1.equals(key2)) {
                    String newone = key1.concat(DELIMITER).concat(key2);
                    if (!result.containsKey(newone)) {
                        result.put(newone.trim(), 0);
                    }

                }
            }
        }
        return result;
    }

    public Map<String, Integer> countFrequency(Map<String, Integer> candidateSet, int length) {
        Map<String, Integer> result = new HashMap<>();
        int process = 0;
        int count = 0;

        for (Session s : getTraces()) {
            process++;
            if (process % 50 == 0) {
                System.out.print(".");
            }
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                content.replaceAll("[\\t\\n\\r]+", DELIMITER);

                String[] splited = content.split(DELIMITER);
                if (splited.length >= length) {
                    for (int i = 0; i < splited.length - length + 1; i++) {
                        String key = splited[i];
                        for (int j = 1; j < length; j++) {
                            key = key.concat(DELIMITER).concat(splited[i + j]);
                        }
                        if (candidateSet.containsKey(key)) {
                            if (!result.containsKey(key)) {
                                result.put(key, 0);
                            }
                            result.put(key, result.get(key) + 1);
                        }

                    }
                }

            }
        }
        //result.put(key, count);

        return result;
    }

    @Override
    public Map<String, Integer> countFrequencyOnSession(Map<String, Integer> candidateSet, int length) {
        return null;
    }

//    @Override
//    public Map<String, Integer> generateClosedWord(List<Map<String, Integer>> lst_l_length) {
//        Map<String, Integer> finalClosedWords = new HashMap<>();
//        for (int k = lst_l_length.size() - 1; k > -1; k--) {
//            Map<String, Integer> l_length = lst_l_length.get(k);
//            for (String key : l_length.keySet()) {
//                boolean isAdd = true;
//                for (String str : finalClosedWords.keySet()) {
//                    if (str.contains(key)) {
//                        isAdd = false;
//                    }
//                }
//                if (isAdd) {
//                    finalClosedWords.put(key, l_length.get(key));
//                }
//            }
//        }
//        return finalClosedWords;
//    }
    @Override
    public Map<String, Integer> generate1lengthCandidateSet() {
        Map<String, Integer> result = new HashMap<String, Integer>();

        for (Session s : getTraces()) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                content.replaceAll("[\\t\\n\\r]+", " ");
                String[] words = content.split(DELIMITER);
                Set<String> existingWords = new HashSet<>();
                for (String word : words) {
                    if (!existingWords.contains(word)) {
                        if (!word.isEmpty()) {
                            if (!result.containsKey(word)) {
                                result.put(word, 0);
                            }
                            result.put(word, result.get(word) + 1);
                        }
                        existingWords.add(word);
                    }
                }
            }
        }
        return result;
    }
}
