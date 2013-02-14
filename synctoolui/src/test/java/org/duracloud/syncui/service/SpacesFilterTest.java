/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.duracloud.common.constant.Constants;
import org.duracloud.error.ContentStoreException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SpacesFilterTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFilter() throws ContentStoreException {
        SpacesFilter filter = new SpacesFilter();
        
       String[] spaces = {"space1", "space2", "space3"};
       
       List<String> spacesList = Arrays.asList(spaces);
       
       List<String> result = filter.filter(spacesList);
       
       Assert.assertNotNull(result);
       
       Assert.assertEquals(spaces.length, result.size());

       spacesList = new LinkedList<String>();
       
       for(String systemSpace : Constants.SYSTEM_SPACES){
           spacesList.add(systemSpace);
       }
       
       result = filter.filter(spacesList);
       Assert.assertEquals(0, result.size());

    }
}
