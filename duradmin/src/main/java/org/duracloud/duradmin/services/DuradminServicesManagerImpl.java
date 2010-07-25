/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.services;

import org.duracloud.duradmin.config.DuradminConfig;

/**
 * @author Andrew Woods
 *         Date: Mar 25, 2010
 */
public class DuradminServicesManagerImpl extends org.duracloud.client.ServicesManagerImpl {

    public DuradminServicesManagerImpl() {
        super(DuradminConfig.getDuraServiceHost(),
              DuradminConfig.getDuraServicePort(),
              DuradminConfig.getDuraServiceContext());
    }
}
