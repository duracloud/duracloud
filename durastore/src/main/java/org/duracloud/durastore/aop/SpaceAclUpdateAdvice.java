/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.duracloud.storage.aop.ContentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides AOP advice over SpaceACL update calls.
 *
 * @author Andrew Woods
 *         Date: 4/09/12
 */
public class SpaceAclUpdateAdvice extends SpaceUpdateAdvice {

    private final Logger log =
        LoggerFactory.getLogger(SpaceAclUpdateAdvice.class);

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    protected ContentMessage createMessage(Object[] methodArgs) {
        return super.createMessage(methodArgs);
    }

}