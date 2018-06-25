/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre.prs;

import io.pkts.Pcap;
import io.pkts.packet.Packet;
import io.pkts.packet.impl.TcpPacketImpl;
import io.pkts.protocol.Protocol;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public class HTTPParser extends TraceParser{

    public HTTPParser(String tracePath) {
        super(tracePath);
    }

    @Override
    public List<Session> doParse() throws IOException {
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
                    maps.put(key, new ArrayList<>());
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
            Session session = new Session();
            RawMessage firstPacket = rawMessages.get(0);
            RawMessage lastPacket = rawMessages.get(rawMessages.size() - 1);
            session.setSrcIP(firstPacket.getSrcIP());
            session.setDestIP(firstPacket.getDestIP());
            session.setMessages(rawMessages);
            session.setStartTime(firstPacket.getTimestamp());
            session.setEndTime(lastPacket.getTimestamp());
            session.setsID(auto_increased_id);

            lstSessions.add(session);
        }
        System.out.println("End...");
        return lstSessions;
        
    }
    
}
