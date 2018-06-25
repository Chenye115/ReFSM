/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc.ds;

import nbl.tgr.pre.utils.MyUtils;

/**
 *
 * @author Quan-speedLab
 */
public class LCSDistance extends AbstractDistance{

    @Override
    public double distance(String str1, String m) {
       return 1 - ((MyUtils.longestSubstring(str1, m).length() * 1.0) / m.length());
    }
    
}
