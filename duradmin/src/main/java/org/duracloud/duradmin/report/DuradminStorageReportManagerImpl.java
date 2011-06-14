package org.duracloud.duradmin.report;

import org.duracloud.client.report.StorageReportManagerImpl;
import org.duracloud.duradmin.config.DuradminConfig;

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 *
 */
public class DuradminStorageReportManagerImpl extends StorageReportManagerImpl{
    
    public DuradminStorageReportManagerImpl(){
        super(DuradminConfig.getDuraReportHost(),
              DuradminConfig.getDuraReportPort(),
              DuradminConfig.getDuraReportContext());
    }
    
}
