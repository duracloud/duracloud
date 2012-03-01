/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.report;

import org.duracloud.client.report.ServiceReportManagerImpl;
import org.duracloud.duradmin.config.DuradminConfig;

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 *
 */
public class DuradminServiceReportManagerImpl extends ServiceReportManagerImpl{
    
    public DuradminServiceReportManagerImpl(){
        super(DuradminConfig.getDuraBossHost(),
              DuradminConfig.getDuraBossPort(),
              DuradminConfig.getDuraBossContext());
    }
    
}
