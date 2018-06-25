/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.fex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nbl.tgr.pre.entity.RawMessage;

/**
 *
 * @author Quan-speedLab
 */
public class KeywordBasedFEX extends FieldExtractor {

    private List<String> keywords;

    @Override
    public List<String> doExtract(RawMessage mesg) {
        List<String> fields = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        String content = mesg.getContent();
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                int idx = content.indexOf(keyword);
                idx += keyword.length();
                indexes.add(idx);
            }
        }
        int l=content.length();
        if (!indexes.contains(l-1)) {
            indexes.add(l-1);
        }
        Collections.sort(indexes);
        for (int i = 0; i < indexes.size()-1; i++) {
            try {
                String field = content.substring(indexes.get(i), indexes.get(i + 1));
                if (!field.isEmpty()) {
                    fields.add(field);
                }
            } catch (Exception ex) {
                System.err.println("Quan");
            }
        }

        return fields;
    }

    public KeywordBasedFEX(List<String> kws) {
        keywords = kws;
    }

}
