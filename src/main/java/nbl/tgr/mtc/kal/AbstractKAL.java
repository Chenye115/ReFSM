/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc.kal;

import java.util.List;
import nbl.tgr.pre.entity.Session;

/**
 *
 * @author Quan-speedLab
 */
public abstract class AbstractKAL {
    public abstract List<String> doKeywordAnalysis(List<Session> traces);
}
