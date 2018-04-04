/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.spaces.controller;

import org.duracloud.common.util.ExtendedCountListener;

/**
 * @author Daniel Bernstein
 */
public class ItemCounter implements ExtendedCountListener {
    private Long count = null;
    private Long intermediaryCount = null;
    private boolean countComplete = false;

    public Long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Long getIntermediaryCount() {
        return intermediaryCount;
    }

    public void setIntermediaryCount(long intermediaryCount) {
        this.intermediaryCount = intermediaryCount;
    }

    public boolean isCountComplete() {
        return countComplete;
    }

    public void setCountComplete() {
        countComplete = true;
    }

}
