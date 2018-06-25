/**
 *
 */
package io.pkts.examples.core;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.packet.Packet;
import io.pkts.packet.impl.TcpPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;

/**
 * A very simple example that just loads a pcap and prints out the content of
 * all UDP packets.
 *
 * @author jonas@jonasborjesson.com
 */
public class CoreExample001 {

    public static void main(final String[] args) throws IOException {

        // Step 1 - obtain a new Pcap instance by supplying an InputStream that points
        //          to a source that contains your captured traffic. Typically you may
        //          have stored that traffic in a file so there are a few convenience
        //          methods for those cases, such as just supplying the name of the
        //          file as shown below.
        String path = "C:\\Users\\Quan-speedLab\\Dropbox\\Thesis\\TestCasesExtractor\\Example traces\\FTP\\lbnl.anon-ftp.03-01-10.tcpdump\\lbnl.anon-ftp.03-01-10.tcpdump";
        final Pcap pcap = Pcap.openStream(path);

        // Step 2 - Once you have obtained an instance, you want to start 
        //          looping over the content of the pcap. Do this by calling
        //          the loop function and supply a PacketHandler, which is a
        //          simple interface with only a single method - nextPacket
        int no_packets = 0;
        pcap.loop(new PacketHandler() {
            @Override
            public boolean nextPacket(final Packet packet) throws IOException {
                // group by srcIP, destIP

                Map<String, Map<String, List<Session>>> maps = new HashMap<>();

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
                    if (!maps.containsKey(tcp.getSourceIP())) {
                        Map<String, List<Session>> gbDest = new HashMap<>();
                        maps.put(tcp.getSourceIP(), gbDest);

                    }
                    Map<String, List<Session>> gbDest = maps.get(tcp.getSourceIP());
                    if (!gbDest.containsKey(tcp.getDestinationIP())) {
                        List<Session> lstSession = new ArrayList<>();
                        gbDest.put(tcp.getDestinationIP(), lstSession);
                    }
                    List<Session> lst = gbDest.get(tcp.getDestinationIP());
                    boolean isFound = false;
                    for (Session s : lst) {
                        if (s.getEndTime()-tcp.getArrivalTime()< 1) {
                            //s.addMessage(mesg);
                        }
                    }

                    Session session = new Session();
                    session.setSrcIP(tcp.getSourceIP());
                    session.setDestIP(tcp.getDestinationIP());
                    session.setStartTime(tcp.getArrivalTime());
                    session.setEndTime(tcp.getArrivalTime());
                    System.out.println(tcp.getPayload());
                }

                return true;
            }
        });
    }

}
