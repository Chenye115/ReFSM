/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc.kal;

import com.sun.org.apache.xpath.internal.compiler.Keywords;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import nbl.tgr.mtc.DelimiterMTCAlg;
import nbl.tgr.mtc.GeneralApioriAlg;
import nbl.tgr.mtc.kal.apriori.AbstractApriori;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;
import nbl.tgr.pre.utils.MyUtils;

/**
 *
 * @author Quan-speedLab
 */
public class AprioriKAL extends AbstractKAL {

    private AbstractApriori alg;

    public AbstractApriori getAlg() {
        return alg;
    }

    public void setAlg(AbstractApriori alg) {
        this.alg = alg;
    }

    private double position_thresh_hold = 0.0;

    public double getPosition_thresh_hold() {
        return position_thresh_hold;
    }

    public void setPosition_thresh_hold(double position_thresh_hold) {
        this.position_thresh_hold = position_thresh_hold;
    }

    

    @Override
    public List<String> doKeywordAnalysis(List<Session> traces) {

        List<String> keywords = alg.doApriori();
        Map<String, Double> keywordPosition = new HashMap<>();
        keywords.forEach((word) -> {
            keywordPosition.put(word, Double.MAX_VALUE);
        });

        System.out.println("Calculate position variance and normalization ");
        System.out.println("---------------------------------------------------");
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;

        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                for (String keyword : keywords) {
                    if (content.contains(keyword)) {
                        int pos = content.indexOf(keyword);
                        if (pos < keywordPosition.get(keyword)) {
                            keywordPosition.put(keyword, pos * 1.0);
                            if (pos < minPos) {
                                minPos = pos;
                            }
                            if (pos > maxPos) {
                                maxPos = pos;
                            }
                        }
                    }
                }
            }
        }

        double range = maxPos - minPos;

        for (String keyword : keywordPosition.keySet()) {
            double posVar = (keywordPosition.get(keyword) - minPos) / range;
            keywordPosition.put(keyword, posVar);
            if (posVar > position_thresh_hold) {
                keywords.remove(keyword);
            }
        }

        System.out.println("Position variance analysis " + keywordPosition.size());
        Iterator itr = keywordPosition.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry pair = (Map.Entry) itr.next();
            double value = Double.parseDouble(pair.getValue().toString());
            System.out.println(pair.getKey() + ":" + value);

        }

        //Map<String, Integer> afterPositionClosedWords = keywords.entrySet().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        Map<String, Integer> afterPositionClosedWords = new HashMap<>();
        for (String kw : keywords) {
            afterPositionClosedWords.put(kw, 0);
        }

        boolean isStop = false;
        while (!isStop) {
            boolean isNoMoreMerge = false;
            Map<String, Integer> iterativeKeywords = new HashMap<>();

            for (String kw : afterPositionClosedWords.keySet()) {
                boolean isKeep = true;
                for (String kw2 : afterPositionClosedWords.keySet()) {
                    if (!kw.equals(kw2)) {
                        String subKW = MyUtils.longestSubstringFromStart(kw, kw2);
                        if (subKW.length() >= 4) {
                            if (!iterativeKeywords.containsKey(subKW)) {
                                iterativeKeywords.put(subKW, 0);
                            }
                            isKeep = false;
                            isNoMoreMerge = true;
                            iterativeKeywords.put(subKW, afterPositionClosedWords.get(kw) > afterPositionClosedWords.get(kw2) ? afterPositionClosedWords.get(kw) : afterPositionClosedWords.get(kw2));
                        }
                    }
                }
                if (isKeep) {
                    iterativeKeywords.put(kw, afterPositionClosedWords.get(kw));
                }
            }
            isStop = !isNoMoreMerge;
            if (!isStop) {
                afterPositionClosedWords = iterativeKeywords;
            }

        }

        // recalculate frequency:
        Map<String, Integer> finalKeywords = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                for (String kw : afterPositionClosedWords.keySet()) {
                    if (content.contains(kw)) {
                        if (!finalKeywords.containsKey(kw)) {
                            finalKeywords.put(kw, 0);
                        }
                        finalKeywords.put(kw, finalKeywords.get(kw) + 1);
                    }
                }
            }
        }

        return new ArrayList<>(finalKeywords.keySet());
//        DelimiterMTCAlg.KeyComparator bvc = new DelimiterMTCAlg.KeyComparator(finalKeywords);
//        Map<String, Integer> sortedKeywords = new TreeMap<String, Integer>(bvc);
//        sortedKeywords.putAll(finalKeywords);
//
//        System.out.println("No of final closed words: " + sortedKeywords.size());
//        System.out.println("Final keywords: \n");
//        System.out.println("---------------------------------------------------");
////        System.out.println("Final closed words: " + sortedKeywords);
//        Iterator it = sortedKeywords.entrySet().iterator();
//        int NO_MESSAGES = 217281;
//
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry) it.next();
//            int value = Integer.parseInt(pair.getValue().toString());
//            System.out.println(pair.getKey() + ":" + value);
//
//        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
}
