/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Daniel Bernstein 
 * 
 */
public class MaxFileSizeForm {
    private int maxFileSizeInGB = 1;

    
    public List<Integer> getValues(){
        return Arrays.asList(new Integer[]{1,2,3,4,5});
    }


    public int getMaxFileSizeInGB() {
        return maxFileSizeInGB;
    }


    public void setMaxFileSizeInGB(int maxFileSizeInGB) {
        this.maxFileSizeInGB = maxFileSizeInGB;
    }
    
   
}
