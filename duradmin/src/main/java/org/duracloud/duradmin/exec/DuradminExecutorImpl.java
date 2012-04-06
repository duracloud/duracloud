/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.exec;

import org.duracloud.client.exec.ExecutorImpl;
import org.duracloud.common.model.SystemUserCredential;
import org.duracloud.duradmin.config.DuradminConfig;

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 *
 */
public class DuradminExecutorImpl extends ExecutorImpl {
    
    public DuradminExecutorImpl(){
        super(DuradminConfig.getDuraBossHost(),
              DuradminConfig.getDuraBossPort(),
              DuradminConfig.getDuraBossContext(),
              new SystemUserCredential());
    }
    
}
