/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nbl.tgr.pre.entity.RawMessage;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public interface IMTCAlgorithm {

    // Assign label to messages.
    Map<String, Set<RawMessage>> clustering(List<Session> traces);
}
