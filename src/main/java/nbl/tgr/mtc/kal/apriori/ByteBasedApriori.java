/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc.kal.apriori;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public class ByteBasedApriori extends AbstractApriori {

    @Override
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

    @Override
    public Map<String, Integer> generate1lengthCandidateSet() {
        Map<String, Integer> filtered = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            byte bytes[] = new byte[1];
            bytes[0] = (byte) i;
            String str = new String(bytes);
            filtered.put(str, 0);
        }
        return filtered;
    }

    @Override
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

    @Override
    public Map<String, Integer> countFrequency(Map<String, Integer> candidateSet, int length) {
        Map<String, Integer> result = new HashMap<>();
        int process = 0;

        for (Session s : getTraces()) {
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

    @Override
    public Map<String, Integer> countFrequencyOnSession(Map<String, Integer> candidateSet, int length) {
        Map<String, Integer> result = new HashMap<>();
        for (Session s : getTraces()) { 
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
    public Map<String, Integer> initialize() {
        Map<String, Integer> filtered = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            byte bytes[] = new byte[1];
            bytes[0] = (byte) i;
            String str = new String(bytes);
            filtered.put(str, 0);
        }
        return filtered;
    }

}
