/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc;
import nbl.tgr.mtc.Cluster;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import nbl.tgr.mtc.ds.AbstractDistance;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;
import nbl.tgr.pre.utils.MyUtils;

/**
 *
 * @author Quan-speedLab
 */
public class KeywordBasedMTCAlg implements IMTCAlgorithm{

    private AbstractDistance dstAlg;

    public void setDstAlg(AbstractDistance dstAlg) {
        this.dstAlg = dstAlg;
    }

    
    private static String UNLABELED = "unlabeled";
    
    @Override
    public Map<String, Set<RawMessage>> clustering(List<Session> traces) {
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
        System.out.println("Step 1: find the number of clustering...");
        System.out.println("Start");
        Map<String, Set<RawMessage>> clusters = new HashMap<>();
        traces.forEach((s) -> {
            s.getMessages().forEach((rm) -> {
                List<String> kws = new ArrayList<>();
                String content = new String(rm.getPayload(), StandardCharsets.UTF_8);

                for (String keyword : sortedKeywords.keySet()) {
                    if (content.contains(keyword)) {
                        kws.add(keyword);
                    }
                }
                Collections.sort(kws);

                String signature = String.join(";", kws);
                if (kws.isEmpty()) {
                    signature = UNLABELED;
                }
                if (!clusters.containsKey(signature)) {
                    clusters.put(signature, new HashSet<>());
                }
                clusters.get(signature).add(rm);
             });
        });
        System.out.println("End");
        System.out.println("---------------------------------------------------");

        Map<String, Set<RawMessage>> firstClusters = clusters.entrySet().stream().filter(en -> en.getValue().size() > 100).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        SortedSet<String> keys = new TreeSet<String>(firstClusters.keySet());
        System.out.println("No of final clusters: " + firstClusters.size());
//        for (String sign : keys) {
//            System.out.println("signature: " + sign + "||" + "No of messages: " + firstClusters.get(sign).size());
//        }

        Set<RawMessage> unlabeledCluster = firstClusters.get(UNLABELED);
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
                    double distance = dstAlg.distance(mesg.getContent(), m);
                    totalDistance += distance;
                }
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
        System.out.println("Unlabeled:" + stillUnlabeledCluster.size());
        List<String> lstContent= new ArrayList<>();
        stillUnlabeledCluster.forEach((msg) -> {
            lstContent.add(msg.getContent());
        });
        Collections.sort(lstContent);
        
        lstContent.forEach((msg) -> {
            System.out.println(msg);
        });

        for (RawMessage rw : pairClusters.keySet()) {
            if (firstClusters.containsKey(pairClusters.get(rw))) {
                firstClusters.get(pairClusters.get(rw)).add(rw);
            }
        }
        
        
        firstClusters.put(UNLABELED, stillUnlabeledCluster);
        
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
        
        return firstClusters;
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
}
