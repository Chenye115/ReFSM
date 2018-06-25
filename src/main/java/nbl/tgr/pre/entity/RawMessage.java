/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre.entity;

import io.pkts.buffer.Buffer;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Quan-speedLab
 */
public class RawMessage implements Comparable<RawMessage> {

    private long order;
    private long timestamp;
    private long sessionId;
    private String destIP;
    private String srcIP;
    private int length;
    private byte[] payload;
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String cluterLabel;

    /**
     * Get the value of cluterLabel
     *
     * @return the value of cluterLabel
     */
    public String getCluterLabel() {
        return cluterLabel;
    }

    /**
     * Set the value of cluterLabel
     *
     * @param cluterLabel new value of cluterLabel
     */
    public void setCluterLabel(String cluterLabel) {
        this.cluterLabel = cluterLabel;
    }

    public RawMessage() {
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        content = new String(this.payload, StandardCharsets.UTF_8);
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getDestIP() {
        return destIP;
    }

    public void setDestIP(String destIP) {
        this.destIP = destIP;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Get the value of srcIP
     *
     * @return the value of srcIP
     */
    public String getSrcIP() {
        return srcIP;
    }

    /**
     * Set the value of srcIP
     *
     * @param srcIP new value of srcIP
     */
    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    @Override
    public int compareTo(RawMessage t) {
        if (timestamp == t.timestamp) {
            return 0;
        } else if (timestamp < t.timestamp) {
            return -1;
        } else {
            return 1;
        }
    }

}
