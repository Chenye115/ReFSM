/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.packet.Packet;
import io.pkts.packet.impl.TcpPacketImpl;
import io.pkts.protocol.Protocol;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nbl.tgr.dfa.EFSMInferor;
import nbl.tgr.dfa.EFSMResconstructor;
import nbl.tgr.dfa.PPLiveInferror;
import nbl.tgr.mtc.Cluster;
import nbl.tgr.mtc.DelimiterMTCAlg;
import nbl.tgr.mtc.KeywordBasedMTCAlg;
import nbl.tgr.mtc.ds.LCSDistance;
import nbl.tgr.mtc.kal.AprioriKAL;
import nbl.tgr.mtc.kal.apriori.ByteBasedApriori;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;
import nbl.tgr.pre.prs.BtrParser;
import nbl.tgr.pre.prs.FTPParser;
import nbl.tgr.pre.prs.HTTPParser;
import nbl.tgr.pre.utils.MyUtils;

/**
 *
 * @author Quan-speedLab
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        EFSMInferor inferor = new EFSMInferor();
        inferor.doLoading();
        inferor.doResconstruct();
        //inferor.doFieldInfer();
        //doBitReverse();

    }

    private static boolean tryParse(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void doFTPReverse() throws IOException {
        String path = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\lbnl.anon-ftp.03-01-10.tcpdump";
        //String path="C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\HTTP\\http_pcap.pcap";
        FTPParser parser = new FTPParser(path);
        List<Session> lstSessions = parser.doParse();

        System.out.println("\nFinish");
        System.out.println("Here is basic statistic: ");
        System.out.println("- Number of sessions: " + lstSessions.size());

        System.out.println("Start keyword analysis... ");

        LCSDistance dstAlg = new LCSDistance();
        KeywordBasedMTCAlg mtcAlg = new KeywordBasedMTCAlg();
        mtcAlg.setDstAlg(dstAlg);
        Map<String, Set<RawMessage>> result = mtcAlg.clustering(lstSessions);
        List<Cluster> clusters = new ArrayList<>();
        for (String ks : result.keySet()) {
            List<RawMessage> rms = new ArrayList<>(result.get(ks));
            Cluster cl = new Cluster(ks, rms);
            clusters.add(cl);
        }

        for (Cluster cluster : clusters) {
            cluster.findTheExtractor();
            for (RawMessage rm : cluster.getMessages()) {
                List<String> fields = cluster.getFields(rm);
            }
        }

        Set<String> serverIPs = new HashSet<>();
        for (Session s : lstSessions) {
            if (s.getDestIP().contains("131.243")) {
                serverIPs.add(s.getDestIP());
            }
            if (s.getSrcIP().contains("131.243")) {
                serverIPs.add(s.getSrcIP());
            }
        }

//        EFSMResconstructor constructors = new EFSMResconstructor(lstSessions, clusters);
//        constructors.setServerIPs(new ArrayList<>(serverIPs));
//        constructors.doResconstruct();
        doFieldsExtraction(lstSessions);
    }

    public static void doFieldsExtraction(List<Session> lstSessions) throws FileNotFoundException, IOException {

        Map<String, List<String>> dataset = new HashMap<>();
        for (Session s : lstSessions) {
            int count = 0;
            for (RawMessage rm : s.getMessages()) {
                count++;
                String lbl = "unknown";
                String value = null;
                String payload = rm.getContent();
                String[] splited = payload.split(" ");
                if (splited.length > 1) {
                    lbl = splited[0].replace("\n", "").replace("\r", "");
                    if (lbl.contains("-")) {
                        lbl = lbl.substring(0, lbl.indexOf("-"));
                    }
                    value = splited[1].replace("\n", "").replace("\r", "");;

                }
                if (!lbl.isEmpty() && value != null && !tryParse(lbl)) {
                    if (!dataset.containsKey(lbl)) {
                        dataset.put(lbl, new ArrayList<>());
                    }
                    List<String> data = dataset.get(lbl);
                    data.add(value);
                }

            }
        }

        File fout = new File("C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\ftp_field.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
            for (String key : dataset.keySet()) {
                List<String> data = dataset.get(key);
                String inline = String.join(";", data);
                String printed = key + "|" + inline;
                bw.write(printed);
                bw.newLine();
            }
        }

    }

    public static void doSequence(List<Session> lstSessions) throws FileNotFoundException, IOException {
        File fout = new File("C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\ftp_sequence.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
            for (Session s : lstSessions) {

                String srcIP = s.getSrcIP();
                StringBuilder sb = new StringBuilder();
                sb.append("START");
                int count = 0;
                for (RawMessage rm : s.getMessages()) {
                    count++;
                    String lbl = "unknown";
                    String payload = rm.getContent();
                    String[] splited = payload.split(" ");
                    if (splited.length > 0) {
                        lbl = splited[0].replace("\n", "").replace("\r", "");
                        if (lbl.contains("-")) {
                            lbl = lbl.substring(0, lbl.indexOf("-"));
                        }
                    }
                    sb.append(";");
                    sb.append(lbl);
                    if (rm.getSrcIP() == null ? srcIP == null : rm.getSrcIP().equals(srcIP)) {
                        count++;
                    }
                }
                String sequence = sb.toString();
                if (sequence.contains("START;220;USER;331;PASS;230") || sequence.contains("START;220-;USER;331;PASS;230")) {
                    bw.write(sb.toString());
                    bw.newLine();
                }
            }
        }
    }

    public static List<Session> reassembly(List<Session> traces) throws IOException {
        List<Session> result = new ArrayList<>();
        int thesh_hold_inter_gap = 5000;
        int thesh_hold_session_gap = 10000000;
        int count = 0;
        List<Session> nTraces = new ArrayList<>();
        for (Session s : traces) {
            if (s.getMessages().size() > 1) {
                List<RawMessage> rawMessages = s.getMessages();
                Collections.sort(rawMessages);
                List<Integer> anchors = new ArrayList<>();
                anchors.add(0);
                for (int i = 1; i < rawMessages.size() - 1; i++) {
                    if (rawMessages.get(i).getContent().contains("This computer is a Federal computer")) {
                        count++;
                        anchors.add(i);
                    }
                }
                anchors.add(rawMessages.size() - 1);
                for (int j = 0; j < anchors.size() - 1; j++) {
                    Session sess = new Session();
                    sess.setDestIP(s.getDestIP());
                    sess.setSrcIP(s.getSrcIP());
                    sess.setStartTime(rawMessages.get(anchors.get(j)).getTimestamp());
                    try {
                        sess.setEndTime(rawMessages.get(anchors.get(j + 1) - 1).getTimestamp());
                    } catch (Exception ex) {
                    }

                    List<RawMessage> rms = new ArrayList<>(rawMessages.subList(anchors.get(j), anchors.get(j + 1) - 1));
                    sess.setMessages(rms);
                    nTraces.add(sess);
                }
            }

        }

        for (Session s : nTraces) {
            List<RawMessage> rawMessages = s.getMessages();
            List<RawMessage> clone = new ArrayList<>();
            Collections.sort(rawMessages);
            int startIndex = 0;

            while (startIndex < rawMessages.size()) {
                int endIndex = startIndex + 1;
                RawMessage anchor = rawMessages.get(startIndex);
                for (int j = startIndex + 1; j < rawMessages.size() - 1; j++) {
                    if (!rawMessages.get(j).getSrcIP().equals(rawMessages.get(startIndex).getSrcIP())) {
                        endIndex = j;
                        break;
                    }
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(anchor.getPayload());
                for (int i = startIndex; i < endIndex - 1; i++) {
                    RawMessage merge = rawMessages.get(i);
                    outputStream.write(merge.getPayload());
                }
                byte[] combined = outputStream.toByteArray();
                anchor.setPayload(combined);
                clone.add(anchor);
                startIndex = endIndex;

            }
            s.setMessages(clone);
            result.add(s);
        }
        return result;
    }
    
    public static void doBitReverse() throws IOException {
        String path = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\BitTorrent\\Bittorrent_revised.pcap";
        BtrParser parser= new BtrParser(path);
        List<Session> lstSessions = parser.doParse();
        System.out.println(lstSessions.size());
    }

}
