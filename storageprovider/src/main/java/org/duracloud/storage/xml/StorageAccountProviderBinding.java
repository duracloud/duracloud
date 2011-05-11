/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml;

import org.duracloud.storage.domain.StorageAccount;
import org.jdom.Element;

/**
 * This interface defines the contract for Storage Account xml bindings.
 *
 * @author Andrew Woods
 *         Date: 5/9/11
 */
public interface StorageAccountProviderBinding {

    /**
     * This method builds a StorageAccount object from the arg xml.
     *
     * @param xml serialization of StorageAccount
     * @return StorageAccount
     * @throws Exception
     */
    public StorageAccount getAccountFromXml(Element xml) throws Exception;
}
