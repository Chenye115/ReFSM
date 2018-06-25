/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.mtc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nbl.tgr.fex.FieldExtractor;
import nbl.tgr.fex.KeywordBasedFEX;
import nbl.tgr.pre.entity.RawMessage;

/**
 *
 * @author Quan-speedLab
 */
public class Cluster {

    public FieldExtractor extractor;
    private String label;

    public String getLabel() {
        return label;
    }
    private List<RawMessage> messages;

    public List<RawMessage> getMessages() {
        return messages;
    }

    public Cluster() {
        messages = new ArrayList<>();
    }

    public Cluster(String lbl, List<RawMessage> mesgs) {
        label = lbl;
        messages = mesgs;
    }

    public void addRawMessage(RawMessage rm) {
        messages.add(rm);
    }

    public FieldExtractor findExtractor() {
        // just hacked
        List<String> kws = new ArrayList<>(Arrays.asList(label.split(";")));
        return new KeywordBasedFEX(kws);
    }

    public void findTheExtractor() {
        // just hacked
        List<String> kws = new ArrayList<>(Arrays.asList(label.split(";")));
        extractor = new KeywordBasedFEX(kws);
    }
    
    
    public List<String> getFields(RawMessage rm){
        return extractor.doExtract(rm);
    }

}
