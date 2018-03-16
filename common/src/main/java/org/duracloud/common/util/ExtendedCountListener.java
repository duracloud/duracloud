/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

/**
 * @author: Bill Branan
 * Date: Jan 18, 2011
 */
public interface ExtendedCountListener {
    public void setCount(long count);

    public void setIntermediaryCount(long count);

    public void setCountComplete();
}
