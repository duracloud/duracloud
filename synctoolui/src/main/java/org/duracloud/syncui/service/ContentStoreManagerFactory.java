/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.error.ContentStoreException;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public interface ContentStoreManagerFactory {

    public ContentStoreManager create() throws ContentStoreException;
}
