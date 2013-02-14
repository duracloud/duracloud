/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;

/**
 * A abstract base test class to facilitate writing clean test classes.
 * @author Daniel Bernstein
 *
 */
public abstract class AbstractTest {

    private List<Object> mocks;

    @Before
    public void setup() {
        this.mocks = new LinkedList<Object>();
    }

    @After
    public void tearDown() {
        verify();
    }

    private void verify() {
        for (Object o : mocks) {
            EasyMock.verify(o);
        }
    }

    /**
     * Creates the mock with a simple name, adds it to the internal list of
     * mocks to be replayed and verified.
     * 
     * @param clazz
     * @return
     */
    protected <T> T createMock(Class<T> clazz) {
        T mock = EasyMock.createMock(clazz.getSimpleName(), clazz);
        mocks.add(mock);
        return mock;
    }

    protected void replay() {
        for (Object o : mocks) {
            EasyMock.replay(o);
        }
    }

    protected void sleep() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
