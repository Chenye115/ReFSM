/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Quan-speedLab
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //Bittorent
        //int[] a = {184, 48, 92, 92, 52, 672, 120, 152, 56};
        int[] a = {92, 60, 152, 357, 152, 152, 48, 475, 45, 45};
        int tp = 0;
        for (int i : a) {
            tp = tp + ((i - 1) * i) / 2;
        }
        System.out.println(tp);
    }

}
