/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Quan-speedLab
 */
public class MyUtils {

    public static String longestSubstring(String str1, String str2) {

        StringBuilder sb = new StringBuilder();
        if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty()) {
            return "";
        }

// java initializes them already with 0
        int[][] num = new int[str1.length()][str2.length()];
        int maxlen = 0;
        int lastSubsBegin = 0;

        for (int i = 0; i < str1.length(); i++) {
            for (int j = 0; j < str2.length(); j++) {
                if (str1.charAt(i) == str2.charAt(j)) {
                    if ((i == 0) || (j == 0)) {
                        num[i][j] = 1;
                    } else {
                        num[i][j] = 1 + num[i - 1][j - 1];
                    }

                    if (num[i][j] > maxlen) {
                        maxlen = num[i][j];
                        // generate substring from str1 => i
                        int thisSubsBegin = i - num[i][j] + 1;
                        if (lastSubsBegin == thisSubsBegin) {
                            //if the current LCS is the same as the last time this block ran
                            sb.append(str1.charAt(i));
                        } else {
                            //this block resets the string builder if a different LCS is found
                            lastSubsBegin = thisSubsBegin;
                            sb = new StringBuilder();
                            sb.append(str1.substring(lastSubsBegin, i + 1));
                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    public static String longestSubstringFromStart(String str1, String str2) {

        StringBuilder sb = new StringBuilder();
        if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty()) {
            return "";
        }

// java initializes them already with 0
        int length = (str1.length() >= str2.length()) ? str2.length() : str1.length();
        for (int i = 0; i < length; i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                sb.append(str1.charAt(i));
            } else {
                break;
            }
        }

        return sb.toString();
    }

    public static String longestSubKeyword(String str, String keyword) {
        String result = "";
        for (int i = keyword.length() - 1; i > -1; i--) {
            String subKeyword = keyword.substring(0, i);
            if (str.contains(subKeyword)) {
                result = subKeyword;
                break;
            }
        }
        return result;
    }

    public static double calculateJaccardIndex(String str1, String str2) {
        Set<Character> charsSet1 = str1.chars().mapToObj(e -> (char) e).collect(Collectors.toSet());
        Set<Character> charsSet2 = str2.chars().mapToObj(e -> (char) e).collect(Collectors.toSet());
        Set<Character> combined = new HashSet<>();
        combined.addAll(charsSet1);
        combined.addAll(charsSet2);

        Set<Character> intersection = new HashSet<Character>(charsSet1); // use the copy constructor
        intersection.retainAll(charsSet2);
        if (combined.size() == 0) {
            return 0;
        } else {
            return (intersection.size() * 1.0 / combined.size());
        }
    }

    public static void main(String[] args) throws IOException {
        String str1 = "SIZE /";
        String str2 = "OPTS utf8 on\r\n";
        String str3 = "226 Transfer complete.\r\n227 Entering Passive Mode (128,3,28,48,;227 Entering Passive Mode (1";
        System.out.println((MyUtils.longestSubKeyword(str1, str2).length() * 1.0));
        System.out.println((MyUtils.longestSubKeyword(str1, str3).length() * 1.0));

    }

}
