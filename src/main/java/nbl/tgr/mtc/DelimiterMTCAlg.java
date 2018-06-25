/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;
import java.util.stream.Collectors;
import nbl.tgr.mtc.kal.apriori.DelimiterBasedApriori;
import nbl.tgr.pre.utils.MyUtils;

/**
 *
 * @author Quan-speedLab
 */
public class DelimiterMTCAlg implements IMTCAlgorithm {

    private static String UNLABELED = "unlabeled";

    private double supportRate = 1;

    /**
     * Get the value of supportRate
     *
     * @return the value of supportRate
     */
    public double getSupportRate() {
        return supportRate;
    }

    /**
     * Set the value of supportRate
     *
     * @param supportRate new value of supportRate
     */
    public void setSupportRate(double supportRate) {
        this.supportRate = supportRate;
    }

    private String demiliter;

    /**
     * Get the value of demiliter
     *
     * @return the value of demiliter
     */
    public String getDemiliter() {
        return demiliter;
    }

    /**
     * Set the value of demiliter
     *
     * @param demiliter new value of demiliter
     */
    public void setDemiliter(String demiliter) {
        this.demiliter = demiliter;
    }

    public DelimiterMTCAlg() {

    }

    public DelimiterMTCAlg(String demiliter) {
        this.demiliter = demiliter;
    }

    @Override
    public Map<String, Set<RawMessage>> clustering(List<Session> traces) {
        System.out.println("Splitting and couting words...");
        int cut_off = (int) (traces.size() * supportRate);

        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        int count = 0;
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                content.replaceAll("[\\t\\n\\r]+", " ");
//                if (content.contains("privacy")) {
//                    count++;
//                    if (count < 50) {
//                        System.out.println(content);
//                    }
//                }
                String[] words = content.split(demiliter);
                Set<String> existingWords = new HashSet<>();
                for (String word : words) {
                    if (!existingWords.contains(word)) {
                        if (!word.isEmpty()) {
                            if (!wordCount.containsKey(word)) {
                                wordCount.put(word, 0);
                            }
                            wordCount.put(word, wordCount.get(word) + 1);
                        }
                        existingWords.add(word);
                    }
                }
            }
        }
        System.out.println("Number of words: " + wordCount.size());

        //System.err.println("Number of words: " + orderedWord);
        Map<String, Integer> filtered = wordCount.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        DelimiterBasedApriori alg = new DelimiterBasedApriori();
        alg.setTraces(traces);
        Map<String, Integer> generation = alg.generate2LengthCandidateSet(filtered);
        System.out.println("Number of 2 length generation: " + generation.size());
        Map<String, Integer> generation2 = alg.countFrequency(generation, 2);
        Map<String, Integer> g2Filtered = generation2.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        System.out.println("2-length generation before cutoff: " + generation2.size());
        System.out.println("2-length generation after cutoff: " + g2Filtered.size());

        List<Map<String, Integer>> lst_l_length = new ArrayList<>();
        lst_l_length.add(filtered);
        lst_l_length.add(g2Filtered);

        boolean stop = false;
        int i = 3;
        Map<String, Integer> startGen = g2Filtered;
        while (!stop) {
            Map<String, Integer> length_i_Gen = alg.generateNextCandidateSet(startGen);
            System.out.println("Number of " + i + " length generation: " + length_i_Gen.size());
            Map<String, Integer> generation_i = alg.countFrequency(length_i_Gen, i);
            Map<String, Integer> g_i_Filtered = generation_i.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            System.out.println(i + "-length generation before cutoff: " + generation_i.size());
            System.out.println(i + "-length generation after cutoff: " + g_i_Filtered.size());
            System.out.println(i + "-length generation after cutoff 's value: " + g_i_Filtered);
            startGen = g_i_Filtered;
            i++;
            if (startGen.size() == 0) {
                stop = true;
            }
            lst_l_length.add(g_i_Filtered);
        }

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

        ValueComparator bvc = new ValueComparator(finalClosedWords);
        Map<String, Integer> sortedClosedWords = new TreeMap<String, Integer>(bvc);
        sortedClosedWords.putAll(finalClosedWords);

        System.out.println("No of final closed words: " + sortedClosedWords.size());
        System.out.println("Final closed words: " + sortedClosedWords);
        Iterator it = sortedClosedWords.entrySet().iterator();
        int NO_MESSAGES = 217281;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int value = Integer.parseInt(pair.getValue().toString());
            System.out.println(pair.getKey() + ":" + (double) (value * 1.0 / NO_MESSAGES));

        }
        String DELIMITER = " ";
        System.out.println("Start clustering...");
        Map<String, List<RawMessage>> clusters = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                List<String> kws = new ArrayList<>();
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                content.replaceAll("[\\t\\n\\r]+", DELIMITER);
                for (String keyword : sortedClosedWords.keySet()) {
                    if (content.contains(keyword)) {
                        kws.add(keyword);
                    }
                }
                Collections.sort(kws);
                String signature = String.join(";", kws);
                if (!clusters.containsKey(signature)) {
                    clusters.put(signature, new ArrayList<>());
                }
                clusters.get(signature).add(rm);
            }
        }

        System.out.println("No of clusters: " + clusters.size());
        for (String signature : clusters.keySet()) {
            System.out.println("signature: " + signature + "|||" + "No of messages: " + clusters.get(signature).size());
        }
        return null;
    }

    public void clustering2(List<Session> traces) {

        Map<String, Integer> filtered = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            byte bytes[] = new byte[1];
            bytes[0] = (byte) i;
            String str = new String(bytes);
            filtered.put(str, 0);
        }
        int cut_off = (int) (traces.size() * supportRate);
        double position_thresh_hold = 0.0000;
        System.out.println("1_length generation: " + filtered);

        GeneralApioriAlg alg = new GeneralApioriAlg();
        alg.setTraces(traces);
        Map<String, Integer> generation = alg.generate2LengthCandidateSet(filtered);
        System.out.println("Number of 2 length generation: " + generation.size());
        Map<String, Integer> generation2 = alg.countFrequency(generation, 2);

        Map<String, Integer> g2Filtered = generation2.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        System.out.println("2-length generation before cutoff: " + generation2.size());
        System.out.println("2-length generation after cutoff: " + g2Filtered.size());
        //System.out.println("2-length generation: " + g2Filtered);
        System.out.println("---------------------------------------------------");
        List<Map<String, Integer>> lst_l_length = new ArrayList<>();
        //lst_l_length.add(filtered);
        lst_l_length.add(g2Filtered);

        boolean stop = false;
        int i = 3;
        Map<String, Integer> startGen = g2Filtered;
        while (!stop) {
            Map<String, Integer> length_i_Gen = alg.generateNextCandidateSet(startGen);
            System.out.println("Number of " + i + " length generation: " + length_i_Gen.size());
            Map<String, Integer> generation_i = alg.countFrequency(length_i_Gen, i);
            Map<String, Integer> g_i_Filtered = generation_i.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            System.out.println(i + "-length generation before cutoff: " + generation_i.size());
            System.out.println(i + "-length generation after cutoff: " + g_i_Filtered.size());

            System.out.println("---------------------------------------------------");
            // System.out.println(i + "-length generation after cutoff 's value: " + g_i_Filtered);
            startGen = g_i_Filtered;
            i++;
            if (startGen.size() == 0) {
                stop = true;
            }
            lst_l_length.add(g_i_Filtered);
        }

        Map<String, Integer> finalClosedWords = new HashMap<>();
        for (Map<String, Integer> l_length : lst_l_length) {
            for (String key : l_length.keySet()) {
                finalClosedWords.put(key, l_length.get(key));
            }
        }
        System.out.println("Find the closed words");
        System.out.println("---------------------------------------------------");
        Map<String, Integer> keywords = new HashMap<>();
        Map<String, Double> keywordPosition = new HashMap<>();
        for (String word : finalClosedWords.keySet()) {
            //Map<String, Integer> relatedWords = new HashMap<>();
            boolean isAdded = true;
            for (String other : finalClosedWords.keySet()) {
                if (word != other) {
                    if (other.contains(word)) {
                        //relatedWords.put(other, finalClosedWords.get(other));
                        isAdded = false;
                        break;
                    }
                }
            }
            if (isAdded) {
                keywords.put(word, finalClosedWords.get(word));
                keywordPosition.put(word, Double.MAX_VALUE);
            }
        }
        System.out.println("Calculate position variance and normalization ");
        System.out.println("---------------------------------------------------");
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                for (String keyword : keywords.keySet()) {
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

        KeyComparator bvc2 = new KeyComparator(keywords);
        Map<String, Integer> sortedKeywords2 = new TreeMap<String, Integer>(bvc2);
        sortedKeywords2.putAll(keywords);

        System.out.println("No of keywords before merging: " + sortedKeywords2.size());
        System.out.println("Keywords before merging: \n");
        System.out.println("---------------------------------------------------");

        Iterator it2 = sortedKeywords2.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pair = (Map.Entry) it2.next();
            int value = Integer.parseInt(pair.getValue().toString());
            System.out.println(pair.getKey() + ":" + value);

        }

        Map<String, Integer> startKeywords = keywords.entrySet().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        boolean isStop = false;
        while (!isStop) {
            boolean isNoMoreMerge = false;
            Map<String, Integer> iterativeKeywords = new HashMap<>();

            for (String kw : startKeywords.keySet()) {
                boolean isKeep = true;
                for (String kw2 : startKeywords.keySet()) {
                    if (kw != kw2) {
                        String subKW = MyUtils.longestSubstringFromStart(kw, kw2);
                        if (subKW.length() >= 4) {
                            if (!iterativeKeywords.containsKey(subKW)) {
                                iterativeKeywords.put(subKW, 0);
                            }
                            isKeep = false;
                            isNoMoreMerge = true;
                            iterativeKeywords.put(subKW, startKeywords.get(kw) > startKeywords.get(kw2) ? startKeywords.get(kw) : startKeywords.get(kw2));
                        }
                    }
                }
                if (isKeep) {
                    iterativeKeywords.put(kw, startKeywords.get(kw));
                }
            }
            isStop = !isNoMoreMerge;
            if (!isStop) {
                startKeywords = iterativeKeywords;
            }

        }

        // recalculate frequency:
        Map<String, Integer> finalKeywords = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                for (String kw : startKeywords.keySet()) {
                    if (content.contains(kw)) {
                        if (!finalKeywords.containsKey(kw)) {
                            finalKeywords.put(kw, 0);
                        }
                        finalKeywords.put(kw, finalKeywords.get(kw) + 1);
                    }
                }
            }
        }

        KeyComparator bvc = new KeyComparator(finalKeywords);
        Map<String, Integer> sortedKeywords = new TreeMap<String, Integer>(bvc);
        sortedKeywords.putAll(finalKeywords);

        System.out.println("No of final closed words: " + sortedKeywords.size());
        System.out.println("Final keywords: \n");
        System.out.println("---------------------------------------------------");
//        System.out.println("Final closed words: " + sortedKeywords);
        Iterator it = sortedKeywords.entrySet().iterator();
        int NO_MESSAGES = 217281;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int value = Integer.parseInt(pair.getValue().toString());
            System.out.println(pair.getKey() + ":" + value);

        }

        System.out.println("---------------------------------------------------");
        System.out.println("Start clustering...");
        Map<String, Set<RawMessage>> clusters = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                List<String> kws = new ArrayList<>();
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8);

                for (String keyword : sortedKeywords.keySet()) {
                    if (content.contains(keyword)) {
                        kws.add(keyword);
                    }
                }
                Collections.sort(kws);

                String signature = String.join(";", kws);
                if (kws.size() == 0) {
                    signature = UNLABELED;
                }
                if (!clusters.containsKey(signature)) {
                    clusters.put(signature, new HashSet<>());
                }
                clusters.get(signature).add(rm);
            }
        }
        System.out.println("End clustering...");
        System.out.println("---------------------------------------------------");

//        System.out.println("No of clusters: " + clusters.size());
//        Map<String, Set<RawMessage>> finalclusters = new HashMap<>();
//        for (String signature : clusters.keySet()) {
//            Set<String> elements = Arrays.stream(signature.split(";")).collect(Collectors.toSet());
//            boolean isAdd = true;
//            for (String other : clusters.keySet()) {
//                if (other != signature) {
//                    Set<String> other_elements = Arrays.stream(other.split(";")).collect(Collectors.toSet());
//                    if (other_elements.contains(elements)) {
//                        isAdd = false;
//                    }
//                }
//            }
//            if (isAdd && clusters.get(signature).size() > 100) {
//                finalclusters.put(signature, clusters.get(signature));
//            }
//
//        }
        Map<String, Set<RawMessage>> firstClusters = clusters.entrySet().stream().filter(en -> en.getValue().size() > 100).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        SortedSet<String> keys = new TreeSet<String>(firstClusters.keySet());
        System.out.println("No of final clusters: " + firstClusters.size());
        for (String sign : keys) {
            System.out.println("signature: " + sign + "||" + "No of messages: " + firstClusters.get(sign).size());
            if (sign == "") {

            }
        }

        Set<RawMessage> unlabeledCluster = firstClusters.get(UNLABELED);
        firstClusters.remove(UNLABELED);

        System.out.println("Start reinforcement clustering...");
        Map<RawMessage, String> pairClusters = new HashMap<>();
        for (RawMessage mesg : unlabeledCluster) {
            double maxSim = 0;
            String nearestCluster = null;
            String content = mesg.getContent();
            int length = content.length();
            for (String signature : firstClusters.keySet()) {
                double similarity = 0;
                String[] splitted = signature.split(";");
                for (String m : splitted) {
                    //String lcs = MyUtils.longestSubstring(mesg.getContent(), m);
                    //similarity += ((lcs.length() * 1.0) / length);
                    double sim = (MyUtils.longestSubKeyword(mesg.getContent(), m).length() * 1.0);
                    similarity += sim;
                }
                double mean_sim = similarity / splitted.length;
                if (mean_sim > maxSim) {
                    maxSim = mean_sim;
                    nearestCluster = signature;
                }
            }
            pairClusters.put(mesg, nearestCluster);
        }

        for (RawMessage rw : pairClusters.keySet()) {
            if (firstClusters.containsKey(pairClusters.get(rw))) {
                firstClusters.get(pairClusters.get(rw)).add(rw);
            }

        }

        System.out.println("After reinforcement:");
        for (String sign : firstClusters.keySet()) {
            System.out.println("signature: " + sign + "||" + "No of messages: " + firstClusters.get(sign).size());
            if (sign == "") {

            }
        }

    }

    private String readAllBytesJava7(String filePath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public void onlyCluster(List<Session> traces) {
        String keyword_path = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\ftp_keyword.txt";
        String strKw = readAllBytesJava7(keyword_path);

        Map<String, Integer> sortedKeywords = new HashMap<>();
        for (String kw : strKw.split("\\|")) {
            sortedKeywords.put(kw, 0);
        }
        System.out.println("Loading keyword from file.");
        System.out.println("Number of keywords: " + sortedKeywords.size());
        System.out.println("Keywords: " + sortedKeywords);
        System.out.println("---------------------------------------------------");
        System.out.println("Start clustering...");
        Map<String, Set<RawMessage>> clusters = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                List<String> kws = new ArrayList<>();
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8);

                for (String keyword : sortedKeywords.keySet()) {
                    if (content.contains(keyword)) {
                        kws.add(keyword);
                    }
                }
                Collections.sort(kws);

                String signature = String.join(";", kws);
                if (kws.size() == 0) {
                    signature = UNLABELED;
                }
                if (!clusters.containsKey(signature)) {
                    clusters.put(signature, new HashSet<>());
                }
                clusters.get(signature).add(rm);
            }
        }
        System.out.println("End clustering...");
        System.out.println("---------------------------------------------------");

        Map<String, Set<RawMessage>> firstClusters = clusters.entrySet().stream().filter(en -> en.getValue().size() > 100).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        SortedSet<String> keys = new TreeSet<String>(firstClusters.keySet());
        System.out.println("No of final clusters: " + firstClusters.size());
        for (String sign : keys) {
            System.out.println("signature: " + sign + "||" + "No of messages: " + firstClusters.get(sign).size());
            if (sign == "") {

            }
        }

        Set<RawMessage> unlabeledCluster = firstClusters.get(UNLABELED);
//        System.out.println("Test reinforce keyword analysis...");
//        List<RawMessage> list = new ArrayList<RawMessage>(unlabeledCluster);
//        Session s= new Session();
//        s.setMessages(list);
//        List<Session> tr= new ArrayList<>();
//        tr.add(s);
//        doKeywordAnalysis(tr);
//        System.out.println("End Test reinforce keyword analysis...");

        firstClusters.remove(UNLABELED);

        System.out.println("Start reinforcement clustering...");
        Map<RawMessage, String> pairClusters = new HashMap<>();
        Set<RawMessage> stillUnlabeledCluster = new HashSet<>();
        for (RawMessage mesg : unlabeledCluster) {
            double minDistance = Integer.MAX_VALUE;
            String nearestCluster = null;
            String content = mesg.getContent();
            int length = content.length();
            for (String signature : firstClusters.keySet()) {
                double totalDistance = 0;
                String[] splitted = signature.split(";");
                for (String m : splitted) {
                    //String lcs = MyUtils.longestSubstring(mesg.getContent(), m);
                    //similarity += ((lcs.length() * 1.0) / length);
                    double distance = 1 - ((MyUtils.longestSubstring(mesg.getContent(), m).length() * 1.0) / m.length());
                    totalDistance += distance;
                }
                //double mean_sim = totalDistance / splitted.length;
                if (totalDistance < minDistance) {
                    minDistance = totalDistance;
                    nearestCluster = signature;
                }
            }
            if (minDistance <= 0.35) {
                pairClusters.put(mesg, nearestCluster);
            } else {
                stillUnlabeledCluster.add(mesg);
            }

        }
        System.out.println("Un:" + stillUnlabeledCluster.size());
        for (RawMessage msg : stillUnlabeledCluster) {
            System.out.println(msg.getContent());
        }

        for (RawMessage rw : pairClusters.keySet()) {
            if (firstClusters.containsKey(pairClusters.get(rw))) {
                firstClusters.get(pairClusters.get(rw)).add(rw);
            }
        }

        System.out.println("After reinforcement:");
        int count = 0;
        for (String sign : firstClusters.keySet()) {
            System.out.println("signature: " + sign + "||" + "No of messages: " + firstClusters.get(sign).size());
            for (RawMessage rm : firstClusters.get(sign)) {
                String payload = rm.getContent();
                String[] splited = payload.split("\\s+");
                if (splited.length > 0) {
                    String k = splited[0].trim();
                    if (k.contains("-")) {
                        k = k.substring(0, k.indexOf("-"));
                    }
                    if (sign.contains(k)) {
                        count++;
                    }
                }
            }
        }
        System.out.println(count);
    }

    public void doKeywordAnalysis(List<Session> traces) {
        Map<String, Integer> filtered = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            byte bytes[] = new byte[1];
            bytes[0] = (byte) i;
            String str = new String(bytes);
            filtered.put(str, 0);
        }
        int cut_off = (int) (831 * supportRate);
        double position_thresh_hold = 0.0000;
        System.out.println("1_length generation: " + filtered);

        GeneralApioriAlg alg = new GeneralApioriAlg();
        alg.setTraces(traces);
        Map<String, Integer> generation = alg.generate2LengthCandidateSet(filtered);
        System.out.println("Number of 2 length generation: " + generation.size());
        Map<String, Integer> generation2 = alg.countFrequency(generation, 2);

        Map<String, Integer> g2Filtered = generation2.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        System.out.println("2-length generation before cutoff: " + generation2.size());
        System.out.println("2-length generation after cutoff: " + g2Filtered.size());
        //System.out.println("2-length generation: " + g2Filtered);
        System.out.println("---------------------------------------------------");
        List<Map<String, Integer>> lst_l_length = new ArrayList<>();
        //lst_l_length.add(filtered);
        lst_l_length.add(g2Filtered);

        boolean stop = false;
        int i = 3;
        Map<String, Integer> startGen = g2Filtered;
        while (!stop) {
            Map<String, Integer> length_i_Gen = alg.generateNextCandidateSet(startGen);
            System.out.println("Number of " + i + " length generation: " + length_i_Gen.size());
            Map<String, Integer> generation_i = alg.countFrequency(length_i_Gen, i);
            Map<String, Integer> g_i_Filtered = generation_i.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            System.out.println(i + "-length generation before cutoff: " + generation_i.size());
            System.out.println(i + "-length generation after cutoff: " + g_i_Filtered.size());
            if (i > 50) {
                System.out.println(g_i_Filtered);
            }
            System.out.println("---------------------------------------------------");
            // System.out.println(i + "-length generation after cutoff 's value: " + g_i_Filtered);
            startGen = g_i_Filtered;
            i++;
            if (startGen.size() == 0) {
                stop = true;
            }
            lst_l_length.add(g_i_Filtered);
        }

        Map<String, Integer> finalClosedWords = new HashMap<>();
        for (Map<String, Integer> l_length : lst_l_length) {
            for (String key : l_length.keySet()) {
                finalClosedWords.put(key, l_length.get(key));
            }
        }
        System.out.println("Find the closed words");
        System.out.println("---------------------------------------------------");
        Map<String, Integer> keywords = new HashMap<>();
        Map<String, Double> keywordPosition = new HashMap<>();
        for (String word : finalClosedWords.keySet()) {
            //Map<String, Integer> relatedWords = new HashMap<>();
            boolean isAdded = true;
            for (String other : finalClosedWords.keySet()) {
                if (word != other) {
                    if (other.contains(word)) {
                        //relatedWords.put(other, finalClosedWords.get(other));
                        isAdded = false;
                        break;
                    }
                }
            }
            if (isAdded) {
                keywords.put(word, finalClosedWords.get(word));
                keywordPosition.put(word, Double.MAX_VALUE);
            }
        }
        System.out.println("Calculate position variance and normalization ");
        System.out.println("---------------------------------------------------");
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                for (String keyword : keywords.keySet()) {
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

        KeyComparator bvc2 = new KeyComparator(keywords);
        Map<String, Integer> sortedKeywords2 = new TreeMap<String, Integer>(bvc2);
        sortedKeywords2.putAll(keywords);

        System.out.println("No of keywords before merging: " + sortedKeywords2.size());
        System.out.println("Keywords before merging: \n");
        System.out.println("---------------------------------------------------");

        Iterator it2 = sortedKeywords2.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry pair = (Map.Entry) it2.next();
            int value = Integer.parseInt(pair.getValue().toString());
            System.out.println(pair.getKey() + ":" + value);

        }

        Map<String, Integer> startKeywords = keywords.entrySet().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        boolean isStop = false;
        while (!isStop) {
            boolean isNoMoreMerge = false;
            Map<String, Integer> iterativeKeywords = new HashMap<>();

            for (String kw : startKeywords.keySet()) {
                boolean isKeep = true;
                for (String kw2 : startKeywords.keySet()) {
                    if (kw != kw2) {
                        String subKW = MyUtils.longestSubstringFromStart(kw, kw2);
                        if (subKW.length() >= 4) {
                            if (!iterativeKeywords.containsKey(subKW)) {
                                iterativeKeywords.put(subKW, 0);
                            }
                            isKeep = false;
                            isNoMoreMerge = true;
                            iterativeKeywords.put(subKW, startKeywords.get(kw) > startKeywords.get(kw2) ? startKeywords.get(kw) : startKeywords.get(kw2));
                        }
                    }
                }
                if (isKeep) {
                    iterativeKeywords.put(kw, startKeywords.get(kw));
                }
            }
            isStop = !isNoMoreMerge;
            if (!isStop) {
                startKeywords = iterativeKeywords;
            }

        }

        // recalculate frequency:
        Map<String, Integer> finalKeywords = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                for (String kw : startKeywords.keySet()) {
                    if (content.contains(kw)) {
                        if (!finalKeywords.containsKey(kw)) {
                            finalKeywords.put(kw, 0);
                        }
                        finalKeywords.put(kw, finalKeywords.get(kw) + 1);
                    }
                }
            }
        }

        KeyComparator bvc = new KeyComparator(finalKeywords);
        Map<String, Integer> sortedKeywords = new TreeMap<String, Integer>(bvc);
        sortedKeywords.putAll(finalKeywords);

        System.out.println("No of final closed words: " + sortedKeywords.size());
        System.out.println("Final keywords: \n");
        System.out.println("---------------------------------------------------");
//        System.out.println("Final closed words: " + sortedKeywords);
        Iterator it = sortedKeywords.entrySet().iterator();
        int NO_MESSAGES = 217281;

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int value = Integer.parseInt(pair.getValue().toString());
            System.out.println(pair.getKey() + ":" + value);

        }
    }

    public void clustering3(List<Session> traces) {

        Map<String, Integer> filtered = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            byte bytes[] = new byte[1];
            bytes[0] = (byte) i;
            String str = new String(bytes);
            filtered.put(str, 0);
        }
        int cut_off = (int) (traces.size() * supportRate);
        double position_thresh_hold = 0.0000;
        System.out.println("1_length generation: " + filtered);

        GeneralApioriAlg alg = new GeneralApioriAlg();
        alg.setTraces(traces);
        Map<String, Integer> generation = alg.generate2LengthCandidateSet(filtered);
        System.out.println("Number of 2 length generation: " + generation.size());
        Map<String, Integer> generation2 = alg.countFrequencyOnSession(generation, 2);

        Map<String, Integer> g2Filtered = generation2.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        System.out.println("2-length generation before cutoff: " + generation2.size());
        System.out.println("2-length generation after cutoff: " + g2Filtered.size());
        //System.out.println("2-length generation: " + g2Filtered);
        System.out.println("---------------------------------------------------");
        List<Map<String, Integer>> lst_l_length = new ArrayList<>();
        //lst_l_length.add(filtered);
        lst_l_length.add(g2Filtered);

        boolean stop = false;
        int i = 3;
        Map<String, Integer> startGen = g2Filtered;
        while (!stop) {
            Map<String, Integer> length_i_Gen = alg.generateNextCandidateSet(startGen);
            System.out.println("Number of " + i + " length generation: " + length_i_Gen.size());
            Map<String, Integer> generation_i = alg.countFrequencyOnSession(length_i_Gen, i);
            Map<String, Integer> g_i_Filtered = generation_i.entrySet().stream().filter(entry -> entry.getValue() > cut_off).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            System.out.println(i + "-length generation before cutoff: " + generation_i.size());
            System.out.println(i + "-length generation after cutoff: " + g_i_Filtered.size());
            System.out.println("---------------------------------------------------");
            // System.out.println(i + "-length generation after cutoff 's value: " + g_i_Filtered);
            startGen = g_i_Filtered;
            i++;
            if (startGen.size() == 0) {
                stop = true;
            }
            lst_l_length.add(g_i_Filtered);
        }

        Map<String, Integer> finalClosedWords = new HashMap<>();
        for (Map<String, Integer> l_length : lst_l_length) {
            for (String key : l_length.keySet()) {
                finalClosedWords.put(key, l_length.get(key));
            }
        }
        System.out.println("Find the closed words");
        System.out.println("---------------------------------------------------");
        Map<String, Integer> keywords = new HashMap<>();
        Map<String, Double> keywordPosition = new HashMap<>();
        for (String word : finalClosedWords.keySet()) {
            //Map<String, Integer> relatedWords = new HashMap<>();
            boolean isAdded = true;
            for (String other : finalClosedWords.keySet()) {
                if (word != other) {
                    if (other.contains(word)) {
                        //relatedWords.put(other, finalClosedWords.get(other));
                        isAdded = false;
                        break;
                    }
                }
            }
            if (isAdded) {
                keywords.put(word, finalClosedWords.get(word));
                keywordPosition.put(word, Double.MAX_VALUE);
            }
        }
        System.out.println("Calculate position variance and normalization ");
        System.out.println("---------------------------------------------------");
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8).trim();
                for (String keyword : keywords.keySet()) {
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

        Map<String, Integer> finalKeywords = new HashMap<>();
        for (String kw : keywords.keySet()) {
            for (String kw2 : keywords.keySet()) {
                if (kw != kw2) {
                    String subKW = MyUtils.longestSubstring(kw, kw2);
                    if (subKW.length() >= 4) {
                        if (!finalKeywords.containsKey(subKW)) {
                            finalKeywords.put(subKW, 0);
                        }
                        finalKeywords.put(subKW, keywords.get(kw) > keywords.get(kw2) ? keywords.get(kw) : keywords.get(kw2));
                    }
                }
            }
        }

        KeyComparator bvc = new KeyComparator(finalKeywords);
        Map<String, Integer> sortedKeywords = new TreeMap<String, Integer>(bvc);
        sortedKeywords.putAll(finalKeywords);

        System.out.println("No of final closed words: " + sortedKeywords.size());
        System.out.println("Final keywords: \n");
        System.out.println("---------------------------------------------------");
//        System.out.println("Final closed words: " + sortedKeywords);
        Iterator it = sortedKeywords.entrySet().iterator();
        int NO_MESSAGES = 217281;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int value = Integer.parseInt(pair.getValue().toString());
            System.out.println(pair.getKey() + ":" + value);

        }

        System.out.println("---------------------------------------------------");
        System.out.println("Start clustering...");
        Map<String, Set<RawMessage>> clusters = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                List<String> kws = new ArrayList<>();
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8);

                for (String keyword : sortedKeywords.keySet()) {
                    if (content.contains(keyword)) {
                        kws.add(keyword);
                    }
                }
                Collections.sort(kws);
                String signature = String.join(";", kws);
                if (!clusters.containsKey(signature)) {
                    clusters.put(signature, new HashSet<>());
                }
                clusters.get(signature).add(rm);
            }
        }

        System.out.println("End clustering...");
        System.out.println("---------------------------------------------------");

        System.out.println("No of clusters: " + clusters.size());
        Map<String, Set<RawMessage>> finalclusters = new HashMap<>();
        for (String signature : clusters.keySet()) {
            Set<String> elements = Arrays.stream(signature.split(";")).collect(Collectors.toSet());
            boolean isAdd = true;
            for (String other : clusters.keySet()) {
                if (other != signature) {
                    Set<String> other_elements = Arrays.stream(other.split(";")).collect(Collectors.toSet());
                    if (other_elements.contains(elements)) {
                        isAdd = false;
                    }
                }
            }
            if (isAdd) {
                finalclusters.put(signature, clusters.get(signature));
            }

        }

        System.out.println("No of final clusters: " + finalclusters.size());
        for (String sign : finalclusters.keySet()) {
            System.out.println("signature: " + sign + "||" + "No of messages: " + clusters.get(sign).size());
        }
    }

    class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -11;
            } else {
                return 11;
            } // returning 0 would merge keys
        }
    }

    class KeyComparator implements Comparator<String> {

        Map<String, Integer> base;

        public KeyComparator(Map<String, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(String a, String b) {
            return a.compareTo(b); // returning 0 would merge keys
        }
    }

//    class Apriori
//    {
//        public Dictionary<int, int> GenerateL1(List<HashSet<int>> transactions, double minSup)
//        {
//            int cutOff = (int)Math.Round(minSup * transactions.Count());
//            Dictionary<int, int> l1Map = new Dictionary<int, int>();
//            foreach (HashSet<int> tx in transactions)
//            {
//                foreach (int it in tx)
//                {
//                    if (!l1Map.ContainsKey(it))
//                    {
//                        l1Map.Add(it, 0);
//                    }
//                    l1Map[it]++;
//                }
//            }
//
//            return l1Map.Where(item => item.Value >= cutOff).ToDictionary(i => i.Key, i => i.Value);
//        }
//
//        //public Dictionary<List<int>,int> Ge
//        public Dictionary<HashSet<int>, int> Count_Support(List<HashSet<int>> candidates, List<HashSet<int>> transactions)
//        {
//            Dictionary<HashSet<int>, int> countMap = new Dictionary<HashSet<int>, int>();
//            foreach (HashSet<int> tx in transactions)
//            {
//                foreach (HashSet<int> c in candidates)
//                {
//                    if (Utils.IsContain(tx, c))
//                    {
//                        if (!countMap.ContainsKey(c))
//                        {
//                            countMap.Add(c, 0);
//                        }dd
//                        countMap[c]++;
//                    }
//                }
//            }
//            return countMap;
//        }
//
//        public List<HashSet<int>> GenerateCandidate(List<HashSet<int>> candidate, int itrCount)
//        {
//            // Check size before doing it.
//            List<HashSet<int>> result = new List<HashSet<int>>();
//            for (int i = 0; i < candidate.Count(); i++)
//            {
//                for (int j = i; j < candidate.Count(); j++)
//                {
//                    var union = new HashSet<int>(candidate[i]);
//                    union.UnionWith(candidate[j]);
//                    if (union.Count() == itrCount)
//                    {
//                        if (!result.Any(u => u.SetEquals(union)))
//                        {
//                            result.Add(union);
//                        }
//
//                    }
//                }
//            }
//            return result;
//        }
//
//        public Dictionary<HashSet<int>, int> GenerateL2(List<HashSet<int>> transactions, double minSup)
//        {
//            int cutOff = (int)Math.Round(minSup * transactions.Count());
//            Dictionary<int, int> l2Map = new Dictionary<int, int>();
//            foreach (HashSet<int> tx in transactions)
//            {
//                var toList = tx.ToList();
//                for (int i = 0; i < toList.Count(); i++)
//                {
//                    for (int j = i + 1; j < toList.Count(); j++)
//                    {
//                        //map[i,j]++;
//                        int hashValue = toList[i] * 1000 + toList[j];
//                        if (!l2Map.ContainsKey(hashValue))
//                        {
//                            l2Map.Add(hashValue, 0);
//                        }
//                        l2Map[hashValue]++;
//                    }
//                }
//            }
//            var l2_cutoff = l2Map.Where(item => item.Value >= cutOff);
//            Dictionary<HashSet<int>, int> l2 = new Dictionary<HashSet<int>, int>();
//            foreach (var pair in l2_cutoff)
//            {
//                var newone = new HashSet<int> { pair.Key / 1000, pair.Key % 1000 };
//                l2.Add(newone, pair.Value);
//            }
//            return l2;
//        }
//
//        public Dictionary<HashSet<int>, int> DoApriori(List<HashSet<int>> transactions, double minSup)
//        {
//            int cutOff = (int)Math.Round(minSup * transactions.Count());
//            Dictionary<HashSet<int>, int> result = new Dictionary<HashSet<int>, int>();
//            var l1 = GenerateL1(transactions, minSup);
//            result = l1.ToDictionary(i => new HashSet<int> { i.Key }, i => i.Value);
//
//
//            var l2 = GenerateL2(transactions, minSup);
//            var candidates = new List<HashSet<int>>();
//            foreach (var l2_items in l2)
//            {
//                candidates.Add(l2_items.Key);
//                result.Add(l2_items.Key, l2_items.Value);
//            }
//
//            int itrCount = 3;
//            candidates = GenerateCandidate(candidates, itrCount);
//            // Do Apriori from 3nd iteration
//            while (candidates.Count() > 1)
//            {
//                var countMap = Count_Support(candidates, transactions);
//                // Cutoff the candidates set.
//                var l_itr = countMap.Where(item => item.Value >= cutOff);
//                // Store result and generate candidate set.
//                foreach (var c in l_itr)
//                {
//                    result.Add(c.Key, c.Value);
//                }
//
//                var candidate_l = l_itr.Select(i => i.Key).ToList();
//                itrCount = itrCount + 1;
//                candidates = GenerateCandidate(candidate_l, itrCount);
//            }
//
//
//
//            return result;
//        }
//
//        public List<Rule> GenerateRule(Dictionary<HashSet<int>, int> allFrequentItems, double minConf)
//        {
//            List<Rule> result = new List<Rule>();
//            var keys = allFrequentItems.Keys.ToList();
//            for (int i = 0; i < keys.Count; i++)
//            {
//                var items = keys[i];
//                var support = allFrequentItems[items];
//                for (int j = 0; j < keys.Count; j++)
//                {
//                    if (j != i)
//                    {
//                        var subItem = keys[j];
//                        if (items.IsProperSupersetOf(subItem))
//                        {
//                            var conf = (double)support / allFrequentItems[subItem];
//                            if (conf > minConf)
//                            {
//                                result.Add(new Rule() { First = items, Second = subItem, Confidence = conf });
//                            }
//                        }
//                    }
//
//                }
//            }
//            return result;
//        }
//
//        public List<Rule> DoRule(List<HashSet<int>> transactions, double minSup, double minConf)
//        {
//            var allFrequentItems = DoApriori(transactions, minSup);
//            List<Rule> result = new List<Rule>();
//            var keys = allFrequentItems.Keys.ToList();
//            for (int i = 0; i < keys.Count; i++)
//            {
//                var items = keys[i];
//                var support = allFrequentItems[items];
//                for (int j = 0; j < keys.Count; j++)
//                {
//                    if (j != i)
//                    {
//                        var subItem = keys[j];
//                        if (items.IsProperSupersetOf(subItem))
//                        {
//                            var conf = (double)support / allFrequentItems[subItem];
//                            if (conf > minConf)
//                            {
//                                result.Add(new Rule() { First = items, Second = subItem, Confidence = conf });
//                            }
//                        }
//                    }
//
//                }
//            }
//            return result;
//        }
//
//        private List<HashSet<int>> Subset(HashSet<int> set)
//        {
//            return null;
//        }
}
