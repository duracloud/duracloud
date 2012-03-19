/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.impl;

import org.duracloud.audit.Auditor;
import org.duracloud.audit.error.AuditLogNotFoundException;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: 3/17/12
 */
public class AuditorImpl implements Auditor {

    private final Logger log = LoggerFactory.getLogger(AuditorImpl.class);

    private ContentStore contentStore;
    private String auditLogSpaceId;
    private String auditLogPrefix;

    public AuditorImpl(String auditLogSpaceId, String auditLogPrefix) {
        this.auditLogSpaceId = auditLogSpaceId;
        this.auditLogPrefix = auditLogPrefix;
    }

    @Override
    public void initialize(ContentStoreManager storeMgr) {
        log.debug("initialize() the Auditor");

        try {
            this.contentStore = storeMgr.getPrimaryContentStore();

        } catch (ContentStoreException e) {
            throw new DuraCloudRuntimeException(
                "Error getting primary content store: " + e.getMessage(),
                e);
        }

        start();
    }

    private void start() {
        checkInitialized();

        // listen for content messages

        // temporarily store message in-memory

        // if message received, start writer thread.
        //  writer, pushes to contentStore after x-seconds

    }

    @Override
    public void createInitialAuditLogs() {
        log.debug("creating initial audit log");
        checkInitialized();

        for (String space : allSpaces()) {
            // create audit log
        }

    }

    private List<String> allSpaces() {
        try {
            return contentStore.getSpaces();

        } catch (ContentStoreException e) {
            throw new DuraCloudRuntimeException(
                "Error getting spaces: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        log.debug("stop() the Auditor");
        checkInitialized();
    }

    @Override
    public String getAuditLogs(String spaceId)
        throws AuditLogNotFoundException {
        StringBuilder logs = new StringBuilder();

        Iterator itr = logIterator(spaceId);
        while (itr.hasNext()) {
            logs.append(itr.next());
            logs.append('\n');
        }

        if (0 == logs.length()) {
            throw new AuditLogNotFoundException(spaceId);
        }

        return logs.toString();
    }

    private Iterator<String> logIterator(String spaceId) {
        try {
            return contentStore.getSpaceContents(auditLogSpaceId,
                                                 auditLogPrefix + spaceId);

        } catch (ContentStoreException e) {
            return new ArrayList<String>().iterator();
        }
    }

    private void checkInitialized() {
        if (null == contentStore) {
            throw new DuraCloudRuntimeException("Auditor must be initialized!");
        }
    }

}