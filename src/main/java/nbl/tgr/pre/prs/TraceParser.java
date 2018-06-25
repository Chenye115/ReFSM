/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre.prs;

import java.io.IOException;
import java.util.List;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public abstract class TraceParser {
    private String tracePath;

    public final String getTracePath() {
        return tracePath;
    }

    public final void setTracePath(String tracePath) {
        this.tracePath = tracePath;
    }

    public TraceParser(String tracePath) {
        this.tracePath = tracePath;
    }
    
    
    public abstract List<Session> doParse() throws IOException;
}
