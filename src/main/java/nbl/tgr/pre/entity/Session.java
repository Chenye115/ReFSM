/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Quan-speedLab
 */
public class Session {

    private int sID;
    private String srcIP;
    private String destIP;
    private long startTime;
    private long endTime;
    private List<RawMessage> messages;

    public Session() {
        messages = new ArrayList<>();
    }

    public void addMessage(RawMessage mesg) {
        messages.add(mesg);
    }

    public int getsID() {
        return sID;
    }

    public void setsID(int sID) {
        this.sID = sID;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public String getDestIP() {
        return destIP;
    }

    public void setDestIP(String destIP) {
        this.destIP = destIP;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<RawMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<RawMessage> messages) {
        this.messages = messages;
    }

}
