/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.fex;

import java.util.List;
import nbl.tgr.pre.entity.RawMessage;

/**
 *
 * @author Quan-speedLab
 */
public abstract class FieldExtractor {
     public abstract List<String> doExtract(RawMessage mesg);
}
