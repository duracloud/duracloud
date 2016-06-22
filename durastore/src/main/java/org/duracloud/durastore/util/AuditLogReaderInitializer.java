/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.account.db.repo.DuracloudMillRepo;
import org.duracloud.audit.reader.AuditLogReader;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class AuditLogReaderInitializer {
    private AuditLogReader auditLogReader;
    private DuracloudMillRepo millRepo;
    
    public AuditLogReaderInitializer(AuditLogReader auditLogReader, DuracloudMillRepo millRepo){
        this.auditLogReader = auditLogReader;
        this.millRepo = millRepo;
    }
    
    public void init(){
        this.auditLogReader.initialize(new AuditConfigBuilder(millRepo).build());
    }
}
