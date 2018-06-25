/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
public class GeneralApioriAlg {

    private List<Session> traces;

    public List<Session> getTraces() {
        return traces;
    }

    public void setTraces(List<Session> traces) {
        this.traces = traces;
    }

    public double getThresh_hold() {
        return thresh_hold;
    }

    public void setThresh_hold(double thresh_hold) {
        this.thresh_hold = thresh_hold;
    }
    private double thresh_hold;

    // NOTE THAT: apply to k_length; k>=2;
    public Map<String, Integer> generateNextCandidateSet(Map<String, Integer> candidateSet) {
        Map<String, Integer> result = new HashMap<>();
        for (String key1 : candidateSet.keySet()) {
            for (String key2 : candidateSet.keySet()) {

                char[] arrKey1 = key1.toCharArray();
                char[] arrKey2 = key2.toCharArray();
                boolean isPairUp = true;
                for (int i = 1; i < arrKey1.length; i++) {
                    if (arrKey1[i] != arrKey2[i - 1]) {
                        isPairUp = false;
                        break;
                    }
                }
                if (isPairUp) {
                    String newone = key1 + arrKey2[arrKey2.length - 1];
                    result.put(newone, 0);
                }

            }
        }

        return result;
    }

    public Map<String, Integer> generate2LengthCandidateSet(Map<String, Integer> candidateSet) {
        Map<String, Integer> result = new HashMap<>();
        for (String key1 : candidateSet.keySet()) {
            for (String key2 : candidateSet.keySet()) {

                String newone = key1 + key2;
                result.put(newone, 0);

            }
        }
        return result;
    }

    public Map<String, Integer> countFrequency(Map<String, Integer> candidateSet, int length) {
        Map<String, Integer> result = new HashMap<>();
        int process = 0;

        for (Session s : traces) {
            process++;
            if (process % 50 == 0) {
                System.out.print(".");
            }

            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                char[] splited = content.toCharArray();
                if (splited.length >= length) {
                    for (int i = 0; i < splited.length - length + 1; i++) {
                        char keyarr[] = Arrays.copyOfRange(splited, i, i + length);

                        String key = new String(keyarr);
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

    public Map<String, Integer> countFrequencyOnSession(Map<String, Integer> candidateSet, int length) {
        Map<String, Integer> result = new HashMap<>();
        int process = 0;

        for (Session s : traces) {
            process++;
            if (process % 50 == 0) {
                System.out.print(".");
            }
            Set<String> alreadyWords = new HashSet<String>();
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                char[] splited = content.toCharArray();
                if (splited.length >= length) {
                    for (int i = 0; i < splited.length - length + 1; i++) {
                        char keyarr[] = Arrays.copyOfRange(splited, i, i + length);
                        String key = new String(keyarr);
                        if (candidateSet.containsKey(key)) {
                            if (!alreadyWords.contains(key)) {
                                if (!result.containsKey(key)) {
                                    result.put(key, 0);
                                }
                                result.put(key, result.get(key) + 1);
                                alreadyWords.add(key);
                            }
                        }
                    }
                }

            }

        }
        //result.put(key, count);

        return result;
    }

//     public Map<String, Integer> generateNextCandidateSet(Map<String, Integer> candidateSet) {
//        Map<String, Integer> result = new HashMap<>();
//        for (String key1 : candidateSet.keySet()) {
//            for (String key2 : candidateSet.keySet()) {
//                if (key1 == null ? key2 != null : !key1.equals(key2)) {
//                    String subStr = key2.substring(0, key2.lastIndexOf(DELIMITER));
//                    if (key1.contains(subStr)) {
//                        String newone = key1.concat(DELIMITER).concat(key2.substring(key2.lastIndexOf(DELIMITER) + 1));
//                        if (!result.containsKey(newone)) {
//                            result.put(newone, 0);
//                        }
//                    }
//                }
//            }
//        }
//        return result;
//    }
//
//    public Map<String, Integer> generate2LengthCandidateSet(Map<String, Integer> candidateSet) {
//        Map<String, Integer> result = new HashMap<>();
//        for (String key1 : candidateSet.keySet()) {
//            for (String key2 : candidateSet.keySet()) {
//                if (key1 == null ? key2 != null : !key1.equals(key2)) {
//                    String newone = key1.concat(DELIMITER).concat(key2);
//                    if (!result.containsKey(newone)) {
//                        result.put(newone.trim(), 0);
//                    }
//
//                }
//            }
//        }
//        return result;
//    }
//
//    public Map<String, Integer> countFrequency(Map<String, Integer> candidateSet, int length) {
//        Map<String, Integer> result = new HashMap<>();
//        int process = 0;
//        int count = 0;
//
//        for (Session s : traces) {
//            process++;
//            if (process % 50 == 0) {
//                System.out.print(".");
//            }
//            for (RawMessage rm : s.getMessages()) {
//                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
//                content.replaceAll("[\\t\\n\\r]+", DELIMITER);
//
//                String[] splited = content.split(DELIMITER);
//                if (splited.length >= length) {
//                    for (int i = 0; i < splited.length - length; i++) {
//                        String key = splited[i];
//                        for (int j = 1; j < length; j++) {
//                            key = key.concat(DELIMITER).concat(splited[i + j]);
//                        }
//                        if (candidateSet.containsKey(key)) {
//                            if (!result.containsKey(key)) {
//                                result.put(key, 0);
//                            }
//                            result.put(key, result.get(key) + 1);
//                        }
//
//                    }
//                }
//
//            }
//        }
//        //result.put(key, count);
//
//        return result;
//    }
}
