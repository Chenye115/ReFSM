/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre.prs;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.packet.Packet;
import io.pkts.packet.impl.TcpPacketImpl;
import io.pkts.protocol.Protocol;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class FTPParser extends TraceParser {

    /* public FTPParser() {
    }*/
    public FTPParser(String path) {
        super(path);
    }

    public Map<String, Integer> extractKeywords(List<Session> traces) {
        Map<String, Integer> result = new HashMap<>();
        for (Session s : traces) {
            for (RawMessage rm : s.getMessages()) {
                String payload = rm.getContent();
                String[] splited = payload.split("\\s+");
                if (splited.length > 0) {
                    String k = splited[0].trim();
                    if (k.contains("-")) {
                        k = k.substring(0, k.indexOf("-"));
                    }
                    if (!result.containsKey(k)) {
                        result.put(k, 0);
                    }
                    result.put(k, result.get(k) + 1);
                }
            }
        }
        return result;
    }

    @Override
    public List<Session> doParse() throws IOException {
        System.out.println("---------------------------------------------------");
        System.out.println("Start session extraction:");
        System.out.println("Step 1: extract raw session");
        List<Session> rawSessions = extractSession();
        System.out.println("Step 2: split sessions");
        List<Session> splitedSession = splitSession(rawSessions);
        System.out.println("Step 3: re-assemble packet in session");
        List<Session> result = reassemble(splitedSession);
        System.out.println("Step 4: clean data");
        List<Session> afterCleanning = clean(result);
        System.out.println("---------------------------------------------------");

        return result;
    }

    private List<Session> extractSession() throws IOException {
        final Pcap pcap = Pcap.openStream(getTracePath());
        Map<String, List<RawMessage>> maps = new HashMap<>();

        //int no_packets = 0;
        System.out.println("Start read pcap file...");
        pcap.loop((final Packet packet) -> {
            // group by srcIP, destIP

            // Step 3 - For every new packet the PacketHandler will be
            //          called and you can examine this packet in a few
            //          different ways. You can e.g. check whether the
            //          packet contains a particular protocol, such as UDP.
            if (packet.hasProtocol(Protocol.TCP)) {

                // Step 4 - Now that we know that the packet contains
                //          a UDP packet we get ask to get the UDP packet
                //          and once we have it we can just get its
                //          payload and print it, which is what we are
                //          doing below.
                //System.out.println(packet.getPacket(Protocol.TCP).getPayload());
                TcpPacketImpl tcp = (TcpPacketImpl) packet.getPacket(Protocol.TCP);

                String key = tcp.getDestinationIP() + "|" + tcp.getSourceIP();
                if (!maps.containsKey(key)) {
                    maps.put(key, new ArrayList<RawMessage>());
                }
                List<RawMessage> lst = maps.get(key);
                RawMessage message = new RawMessage();
                message.setDestIP(tcp.getDestinationIP());
                message.setSrcIP(tcp.getSourceIP());
                message.setTimestamp(tcp.getArrivalTime());

                if (tcp.getPayload() != null) {
                    message.setPayload(tcp.getPayload().getArray());
                    lst.add(message);
                }
                if (tcp.isFIN()) {
                    message.setCluterLabel("FIN");
                    lst.add(message);
                }
                if (tcp.isSYN() && !tcp.isACK()) {
                    message.setCluterLabel("SYN");
                    lst.add(message);
                }
                

            }
            return true;
        });

        System.out.println("Reading completed.");
        System.out.println("Preparing to extract session...");
        Map<String, List<RawMessage>> map_of_src_dest = new HashMap<>();
        while (maps.size() > 0) {
            String key = maps.keySet().iterator().next();
            String[] splited = key.split("\\|");
            String reverseKey = splited[1] + "|" + splited[0];

            List<RawMessage> clientMessages = maps.get(key);
            List<RawMessage> serverMessages = maps.get(reverseKey);
            if (serverMessages != null) {
                clientMessages.addAll(serverMessages);
            }
            Collections.sort(clientMessages);
            map_of_src_dest.put(key, clientMessages);
            maps.remove(key);
            maps.remove(reverseKey);
        }
        System.out.println("Number of pair dest and src: " + map_of_src_dest.size());
        System.out.println("Start extract session...");
        List<Session> lstSessions = new ArrayList<>();
        int no_of_message = 0;
        int auto_increased_id = 0;
        long thesh_hold_inter_gap = 1000;
        for (String key : map_of_src_dest.keySet()) {
            if (auto_increased_id % 50 == 0) {
                System.out.print(".");
            }

            auto_increased_id++;
            List<RawMessage> rawMessages = map_of_src_dest.get(key);
            no_of_message += rawMessages.size();
            List<Integer> anchorACKs = new ArrayList<>();
            List<Integer> anchorFINs = new ArrayList<>();

            String srcIP = "";
            String destIP = "";
            if (rawMessages.size() > 0) {
                srcIP = rawMessages.get(0).getSrcIP();
                destIP = rawMessages.get(0).getDestIP();
            }

            int startIndex = -1;
            int endIndex = -1;
            boolean isSearching = false;
            for (int i = 0; i < rawMessages.size(); i++) {
                String type = rawMessages.get(i).getCluterLabel();
                if (type != null && type.equals("SYN")) {
                    startIndex = i;
                    isSearching = true;
                }
                if (type != null && type.equals("FIN") && isSearching) {
                    endIndex = i;
                    isSearching = false;
                    anchorACKs.add(startIndex);
                    anchorFINs.add(endIndex);
                }
            }
            if (isSearching) {
                endIndex = rawMessages.size() - 1;
                isSearching = false;
                anchorACKs.add(startIndex);
                anchorFINs.add(endIndex);
            }

            for (int index = 0; index < anchorACKs.size(); index++) {
                int first = anchorACKs.get(index) + 1;
                int end = anchorFINs.get(index) - 1;
                Session session = new Session();
                RawMessage firstPacket = rawMessages.get(first);
                RawMessage lastPacket = rawMessages.get(end);
//                session.setSrcIP(firstPacket.getDestIP());
//                session.setDestIP(firstPacket.getSrcIP());
                session.setSrcIP(srcIP);
                session.setDestIP(destIP);
                try {
                    session.setMessages(rawMessages.subList(first, end));
                } catch (Exception ex) {
                    System.out.println("");
                }

                session.setStartTime(firstPacket.getTimestamp());
                session.setEndTime(lastPacket.getTimestamp());
                session.setsID(auto_increased_id);

                lstSessions.add(session);

            }

//            Session session = new Session();
//            RawMessage firstPacket = rawMessages.get(0);
//            RawMessage lastPacket = rawMessages.get(rawMessages.size() - 1);
//            session.setSrcIP(firstPacket.getDestIP());
//            session.setDestIP(firstPacket.getSrcIP());
//            session.setMessages(rawMessages);
//            session.setStartTime(firstPacket.getTimestamp());
//            session.setEndTime(lastPacket.getTimestamp());
//            session.setsID(auto_increased_id);
//
//            lstSessions.add(session);
        }
        System.out.println("End...");
        return lstSessions;
    }

    private List<Session> splitSession(List<Session> traces) {
        int thesh_hold_inter_gap = 5000;
        int thesh_hold_session_gap = 300000000; //30s
        int count = 0;
        List<Session> result = new ArrayList<>();
        for (Session s : traces) {
            if (s.getMessages().size() > 1) {
                List<RawMessage> rawMessages = s.getMessages();
                Collections.sort(rawMessages);
                List<Integer> anchors = new ArrayList<>();
                anchors.add(0);
                for (int i = 1; i < rawMessages.size() - 1; i++) {
                    long gap = rawMessages.get(i).getTimestamp() - rawMessages.get(i - 1).getTimestamp();
                    if (gap > thesh_hold_session_gap) {
                        anchors.add(i);
                    }
                }
                anchors.add(rawMessages.size() - 1);
                for (int j = 0; j < anchors.size() - 1; j++) {
                    Session sess = new Session();
                    sess.setDestIP(s.getDestIP());
                    sess.setSrcIP(s.getSrcIP());
                    sess.setStartTime(rawMessages.get(anchors.get(j)).getTimestamp());
                    sess.setEndTime(rawMessages.get(anchors.get(j + 1) - 1).getTimestamp());
                    List<RawMessage> rms = new ArrayList<>(rawMessages.subList(anchors.get(j), anchors.get(j + 1) - 1));
                    sess.setMessages(rms);
                    result.add(sess);
                }
            }

        }
        return result;
    }

    private List<Session> reassemble(List<Session> traces) throws IOException {
        List<Session> result = new ArrayList<>();
        for (Session s : traces) {
            List<RawMessage> rawMessages = s.getMessages();
            List<RawMessage> clone = new ArrayList<>();
            Collections.sort(rawMessages);
            int startIndex = 0;
            while (startIndex < rawMessages.size()) {
                int endIndex = startIndex + 1;
                RawMessage anchor = rawMessages.get(startIndex);
                for (int j = startIndex + 1; j < rawMessages.size() - 1; j++) {
                    long gap = rawMessages.get(j).getTimestamp() - rawMessages.get(j - 1).getTimestamp();
                    if (!rawMessages.get(j).getSrcIP().equals(rawMessages.get(startIndex).getSrcIP())) {
                        endIndex = j;
                        break;
                    }
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(anchor.getPayload());
                for (int i = startIndex + 1; i < endIndex - 1; i++) {
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

    private List<Session> clean(List<Session> sessions) {
        List<Session> result = new ArrayList<>();
        List<String> serverIP = Arrays.asList("128.3.28.48", "131.243.1.10", "128.3.12.44", "131.243.2.12", "242.70.74.100", "215.94.144.11", "203.174.5.11", "131.243.193.93", "163.86.146.154", "131.243.1.83", "93.154.168.114", "254.105.15.81", "62.159.77.154", "86.110.60.6", "178.4.51.120", "128.3.180.73", "81.245.216.94", "123.47.104.102", "128.3.32.63", "128.3.20.245", "103.106.164.29", "117.255.33.187", "250.252.151.246", "189.246.241.212", "174.178.109.66", "78.56.58.89", "131.243.16.51", "30.159.200.190");
        List<Session> removeList = new ArrayList<>();

        
        //result = sessions;
        return result;
    }
}
