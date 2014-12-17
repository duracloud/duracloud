/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.test;

import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.runner.RunWith;

/**
 * @author Daniel Bernstein
 *         Date: Sep 3, 2014
 */
@RunWith(EasyMockRunner.class)
public class AbstractTestBase extends EasyMockSupport {
    
    @After
    public void tearDown(){
        verifyAll();
    }
}
