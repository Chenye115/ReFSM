/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.tgr.pre.entity;

/**
 *
 * @author Quan-speedLab
 */
public class InterGap {

    private long min;

    private long max;

    private double mean;

    /**
     * Get the value of mean
     *
     * @return the value of mean
     */
    public double getMean() {
        return mean;
    }

    /**
     * Set the value of mean
     *
     * @param mean new value of mean
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * Get the value of max
     *
     * @return the value of max
     */
    public long getMax() {
        return max;
    }

    /**
     * Set the value of max
     *
     * @param max new value of max
     */
    public void setMax(long max) {
        this.max = max;
    }

    /**
     * Get the value of min
     *
     * @return the value of min
     */
    public long getMin() {
        return min;
    }

    /**
     * Set the value of min
     *
     * @param min new value of min
     */
    public void setMin(long min) {
        this.min = min;
    }

}
