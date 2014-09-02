package org.duracloud.audit;

import org.duracloud.common.error.DuraCloudCheckedException;

import java.lang.Exception; 
/**
 * 
 * @author Daniel Bernstein
 *         March 11, 2014
 *
 */
public class AuditLogWriteFailedException extends DuraCloudCheckedException {
    
    private static final long serialVersionUID = 1L;
    private AuditLogItem logItem;
    
    public AuditLogWriteFailedException(
        Exception ex, AuditLogItem logItem) {
        super(ex);
        this.logItem = logItem;
    }
    
    public AuditLogWriteFailedException(
        String message, AuditLogItem logItem) {
        super(message);
	this.logItem = logItem;
    }

    public AuditLogItem getLogItem() {
        return logItem;
    }
}
