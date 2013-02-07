/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author: Andrew Woods
 * Date: Aug 12, 2010
 */
public class HashVerifierResult implements ServiceResult {

    private static final Logger log =
        LoggerFactory.getLogger(HashVerifierResult.class);

    private boolean success;
    private String text;
    private Collection<ServiceResultItem> items;

    public HashVerifierResult(boolean success,
                              String text,
                              Collection<ServiceResultItem> items) {
        this.success = success;
        this.text = text;
        this.items = items;
    }

    @Override 
    public Collection<ServiceResultItem> getItems(){
        return this.items;
    }
    
    @Override
    public String getEntry() {
        return text;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
