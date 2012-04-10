/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

/**
 * @author Andrew Woods
 *         Date: 4/09/12
 */
public class SpaceCreateAdviceTest extends SpaceAdviceTestBase {

    @Override
    protected BaseContentStoreAdvice getSpaceAdvice() {
        return new SpaceCreateAdvice();
    }
}
