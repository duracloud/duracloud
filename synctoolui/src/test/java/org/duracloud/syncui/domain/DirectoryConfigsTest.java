/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import junit.framework.Assert;

import org.duracloud.syncui.AbstractTest;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class DirectoryConfigsTest extends AbstractTest {

    
    @Test
    public void testRemovePath(){
        DirectoryConfigs dc = new DirectoryConfigs();
        String testPath = "path";
        dc.add(new DirectoryConfig(testPath));
        Assert.assertEquals(1, dc.size());
        dc.removePath(testPath);
        Assert.assertEquals(0, dc.size());
        
    }

}
