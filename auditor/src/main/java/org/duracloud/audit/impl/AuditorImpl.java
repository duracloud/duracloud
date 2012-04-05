/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.impl;

import org.duracloud.audit.AuditListener;
import org.duracloud.audit.AuditLogStore;
import org.duracloud.audit.Auditor;
import org.duracloud.audit.error.AuditLogNotFoundException;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.HttpHeaders;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.aop.ContentMessage;
import org.duracloud.error.NotFoundException;
import org.duracloud.storage.aop.IngestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_CHECKSUM;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MD5;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MODIFIED;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_SIZE;

/**
 * @author Andrew Woods
 *         Date: 3/17/12
 */
public class AuditorImpl implements Auditor {

    private final Logger log = LoggerFactory.getLogger(AuditorImpl.class);

    private ContentStoreManager storeManager;

    private AuditListener listener;
    private AuditLogStore logStore;

    private boolean isStopped;

    public AuditorImpl(AuditListener listener, AuditLogStore logStore) {
        this.listener = listener;
        this.logStore = logStore;
        this.isStopped = false;
    }

    @Override
    public void initialize(ContentStoreManager storeManager) {
        log.debug("initialize() the Auditor");

        this.storeManager = storeManager;
        this.logStore.initialize(getPrimaryStore());
        this.listener.initialize();
        this.isStopped = false;
    }

    /**
     * This methods puts all stores in a single audit log. However, each space
     * has its own log.
     *
     * @param async true if executing asynchronously
     */
    @Override
    public void createInitialAuditLogs(boolean async) {
        AuditLogRunner runner = new AuditLogRunner();
        if (async) {
            new Thread(runner).start();

        } else {
            runner.run();
        }
    }

    /**
     * This private class run the auditor.
     */
    private class AuditLogRunner implements Runnable {
        @Override
        public void run() {
            doCreateInitialAuditLogs();
        }
    }

    private void doCreateInitialAuditLogs() {
        log.debug("creating initial audit log");
        checkInitialized();

        // Collect all spaces across all stores.
        Map<String, Set<ContentStore>> spaceStores =
            new HashMap<String, Set<ContentStore>>();

        for (ContentStore store : allStores()) {
            for (String space : spaces(store)) {
                if (!isSystemEvent(space)) {
                    addToSpaceStore(space, store, spaceStores);
                }
            }
        }

        for (String space : spaceStores.keySet()) {
            // Pause current audit logging.
            listener.waitToWrite(space, true);

            // Remove existing logs.
            Iterator<String> logs = logStore.logs(space);
            while (logs.hasNext()) {
                remove(logs.next());
            }
        }

        // Build initial logs from existing content.
        for (String space : spaceStores.keySet()) {
            for (ContentStore store : spaceStores.get(space)) {
                createLog(store, space);
            }

            // Un-pause audit logging.
            listener.waitToWrite(space, false);
        }
    }

    private boolean isSystemEvent(String spaceId) {
        return Constants.SYSTEM_SPACES.contains(spaceId);
    }

    private void addToSpaceStore(String space,
                                 ContentStore store,
                                 Map<String, Set<ContentStore>> spaceStores) {
        Set<ContentStore> stores = spaceStores.get(space);
        if (null == stores) {
            stores = new HashSet<ContentStore>();
        }

        stores.add(store);
        spaceStores.put(space, stores);
    }

    private void createLog(ContentStore store, String space) {
        try {
            if (!isStopped) {
                doCreateLog(store, space);

            } else {
                log.info("Not creating log, auditor is stopping, {}:{}",
                         store.getStoreId(),
                         space);
            }

        } catch (Exception e) {
            log.error("Error creating log: store:{}, space:{}",
                      store.getStoreId(),
                      space);
        }
    }

    private void doCreateLog(ContentStore store, String space) {
        List<ContentMessage> events = new ArrayList<ContentMessage>();

        Iterator<String> contents = getSpaceContents(store, space);
        while (contents.hasNext() && !isStopped) {
            String contentId = contents.next();
            ContentMessage event = buildEvent(store, space, contentId);
            events.add(event);

            if (events.size() % 499 == 0) {
                logStore.write(events);
                events.clear();
            }
        }

        // Write final set of events.
        if (events.size() > 0) {
            logStore.write(events);
        }
    }

    private ContentMessage buildEvent(ContentStore store,
                                      String space,
                                      String contentId) {
        try {
            return doBuildEvent(store, space, contentId);

        } catch (Exception e) {
            String storeId = store.getStoreId();
            log.error("Error building event for, {}:{}//{}",
                      new Object[]{storeId, space, contentId},
                      e);
            ContentMessage msg = new ContentMessage();
            msg.setStoreId(storeId);
            msg.setSpaceId(space);
            msg.setContentId(contentId);
            msg.setAction(ContentMessage.ACTION.ERROR.name());
            return msg;
        }
    }

    /**
     * This method creates the ContentMessage structure.
     *
     * @param store     of event
     * @param spaceId   of event
     * @param contentId of event
     * @return {@link ContentMessage}
     */
    private ContentMessage doBuildEvent(ContentStore store,
                                        String spaceId,
                                        String contentId) {
        Map<String, String> props = getContentProperties(store,
                                                         spaceId,
                                                         contentId);

        IngestMessage event = new IngestMessage();
        String storeId = store.getStoreId();
        String username = null;
        String action = ContentMessage.ACTION.INGEST.name();
        String datetime = props.get(HttpHeaders.LAST_MODIFIED);
        String contentMimeType = props.get(HttpHeaders.CONTENT_TYPE);
        String contentMd5 = props.get(HttpHeaders.CONTENT_MD5);
        String contentSize = props.get(HttpHeaders.CONTENT_LENGTH);

        if (null == datetime) {
            datetime = props.get(PROPERTIES_CONTENT_MODIFIED);
        }
        if (null == contentMimeType) {
            contentMimeType = props.get(PROPERTIES_CONTENT_MIMETYPE);
        }
        if (null == contentMd5) {
            contentMd5 = props.get(PROPERTIES_CONTENT_MD5);
        }
        if (null == contentMd5) {
            contentMd5 = props.get(PROPERTIES_CONTENT_CHECKSUM);
        }
        if (null == contentSize) {
            contentSize = props.get(PROPERTIES_CONTENT_SIZE);
        }

        event.setStoreId(storeId);
        event.setSpaceId(spaceId);
        event.setContentId(contentId);
        event.setUsername(username);
        event.setAction(action);
        event.setDatetime(datetime);
        event.setContentMimeType(contentMimeType);
        event.setContentMd5(contentMd5);
        event.setContentSize(Long.valueOf(contentSize));

        return event;
    }

    private Map<String, String> getContentProperties(ContentStore store,
                                                     String space,
                                                     String contentId) {
        Map<String, String> props = null;
        boolean done = false;
        int tries = 0;
        final int maxTries = 3;
        while (!done && tries++ < maxTries) {
            try {
                props = doGetContentProperties(store, space, contentId);
                done = true;
            } catch (Exception e) {
                // do nothing
            }
        }

        if (!done) {
            StringBuilder err = new StringBuilder("Unable to get content ");
            err.append("properties: ");
            err.append(store.getStoreId());
            err.append(", ");
            err.append(space);
            err.append("//");
            err.append(contentId);
            err.append(", after ");
            err.append(tries);
            err.append(" tries");
            log.error(err.toString());
            throw new DuraCloudRuntimeException(err.toString());
        }
        return props;
    }

    private Map<String, String> doGetContentProperties(ContentStore store,
                                                       String space,
                                                       String contentId)
        throws ContentStoreException {
        try {
            return store.getContentProperties(space, contentId);

        } catch (ContentStoreException e) {
            StringBuilder err = new StringBuilder("Error ");
            err.append("getting content properties: ");
            err.append(store.getStoreId());
            err.append(", ");
            err.append(space);
            err.append("//");
            err.append(contentId);
            log.warn(err.toString(), e);
            throw e;
        }
    }

    private Iterator<String> getSpaceContents(ContentStore store,
                                              String space) {
        try {
            return store.getSpaceContents(space);

        } catch (NotFoundException nfe) {
            log.info("space: {} not found for store: {}",
                     space,
                     store.getStoreId());
        } catch (ContentStoreException e) {
            log.warn("Error get space: {} contents for store: {}",
                     new Object[]{space, store.getStoreId(), e});
        }
        return new ArrayList<String>().iterator();
    }

    private void remove(String logContentId) {
        try {
            logStore.removeLog(logContentId);
        } catch (Exception e) {
            log.warn("Error removing log: {}", logContentId);
        }
    }

    private Collection<ContentStore> allStores() {
        try {
            return storeManager.getContentStores().values();

        } catch (ContentStoreException e) {
            String err = "Error getting content stores.";
            log.error(err, e);
            throw new DuraCloudRuntimeException(err, e);
        }
    }

    private List<String> spaces(ContentStore store) {
        try {
            return store.getSpaces();

        } catch (ContentStoreException e) {
            throw new DuraCloudRuntimeException(
                "Error getting spaces: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        log.debug("stop() the Auditor");
        isStopped = true;
        listener.stop();
    }

    @Override
    public List<String> getAuditLogs(String spaceId)
        throws AuditLogNotFoundException {
        List<String> logs = new ArrayList<String>();

        Iterator<String> itr = logStore.logs(spaceId);
        while (itr.hasNext()) {
            logs.add(itr.next());
        }

        if (0 == logs.size()) {
            throw new AuditLogNotFoundException(spaceId);
        }

        Collections.sort(logs);
        return logs;
    }

    private void checkInitialized() {
        if (null == storeManager) {
            throw new DuraCloudRuntimeException("Auditor must be initialized!");
        }
    }

    private ContentStore getPrimaryStore() {
        try {
            return storeManager.getPrimaryContentStore();

        } catch (ContentStoreException e) {
            throw new DuraCloudRuntimeException(
                "Error getting primary content store: " + e.getMessage(),
                e);
        }
    }

}