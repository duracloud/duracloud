/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.services.duplication.ContentDuplicator;
import org.duracloud.services.duplication.error.DuplicationException;
import org.duracloud.services.duplication.result.DuplicationEvent;
import org.duracloud.services.duplication.result.ResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.CONTENT_CREATE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.CONTENT_DELETE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.CONTENT_UPDATE;

/**
 * @author Andrew Woods
 *         Date: 9/18/11
 */
public class ContentDuplicatorReportingImpl implements ContentDuplicator {

    private final Logger log = LoggerFactory.getLogger(
        ContentDuplicatorReportingImpl.class);

    public static final int MAX_RETRIES = 3;

    private ContentDuplicator contentDuplicator;

    private ResultListener listener;
    private boolean watchQ;
    private Queue<DuplicationEvent> inboxQ;
    private DelayQueue<DuplicationEvent> retryQ;
    private Map<DuplicationEvent, Integer> retryTally;

    private ExecutorService executor;
    private final long waitMillis;


    public ContentDuplicatorReportingImpl(ContentDuplicator contentDuplicator,
                                          ResultListener listener) {
        this(contentDuplicator,
             listener,
             60000); // default waiting factor = 1min
    }

    public ContentDuplicatorReportingImpl(ContentDuplicator contentDuplicator,
                                          ResultListener listener,
                                          long waitMillis) {
        this.contentDuplicator = contentDuplicator;
        this.listener = listener;
        this.waitMillis = waitMillis;

        this.watchQ = true;
        this.inboxQ = new ConcurrentLinkedQueue<DuplicationEvent>();
        this.retryQ = new DelayQueue<DuplicationEvent>();
        this.retryTally = new HashMap<DuplicationEvent, Integer>();
        this.executor = Executors.newFixedThreadPool(2);

        executor.execute(new QWatcher(inboxQ));
        executor.execute(new QWatcher(retryQ));
    }

    @Override
    public String getFromStoreId() {
        return contentDuplicator.getFromStoreId();
    }

    @Override
    public String getToStoreId() {
        return contentDuplicator.getToStoreId();
    }

    /**
     * This thread spins on the provided queue of DuplicationEvents, executing
     * the appropriate method.
     */
    private class QWatcher extends Thread {

        private Queue<DuplicationEvent> queue;

        public QWatcher(Queue<DuplicationEvent> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (watchQ) {
                DuplicationEvent event = queue.poll();
                if (null != event) {

                    switch (event.getType()) {
                        case CONTENT_CREATE:
                            createContent(event);
                            break;

                        case CONTENT_DELETE:
                            deleteContent(event);
                            break;

                        case CONTENT_UPDATE:
                            updateContent(event);
                            break;

                        default:
                            String msg = "Unexpected retry event: " + event;
                            log.error(msg);
                            throw new DuraCloudRuntimeException(msg);
                    }

                } else {
                    try {
                        sleep(waitMillis);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Override
    public String createContent(String spaceId, String contentId) {
        submitEvent(createDuplicationEvent(spaceId, contentId, CONTENT_CREATE));
        return "returned-string-not-used";
    }

    @Override
    public void updateContent(String spaceId, String contentId) {
        submitEvent(createDuplicationEvent(spaceId, contentId, CONTENT_UPDATE));
    }

    @Override
    public void deleteContent(String spaceId, String contentId) {
        submitEvent(createDuplicationEvent(spaceId, contentId, CONTENT_DELETE));
    }

    private DuplicationEvent createDuplicationEvent(String spaceId,
                                                    String contentId,
                                                    TYPE type) {
        return createDuplicationEvent(spaceId, contentId, null, type);
    }

    private DuplicationEvent createDuplicationEvent(String spaceId,
                                                    String contentId,
                                                    String md5,
                                                    TYPE type) {
        return new DuplicationEvent(contentDuplicator.getFromStoreId(),
                                    contentDuplicator.getToStoreId(), type,
                                    spaceId,
                                    contentId,
                                    md5);
    }

    private void submitEvent(DuplicationEvent event) {
        if (!inboxQ.contains(event)) {
            inboxQ.add(event);
            
        } else {
            log.debug("omitting submitEvent: {}, q: {}", event, inboxQ.size());
        }
    }

    private String createContent(DuplicationEvent event) {
        String spaceId = event.getSpaceId();
        String contentId = event.getContentId();

        String md5 = null;
        try {
            md5 = contentDuplicator.createContent(spaceId, contentId);
            createContentSuccess(spaceId, contentId, md5);

        } catch (DuplicationException e) {
            createContentFailure(spaceId, contentId);
        }
        return md5;
    }

    private void updateContent(DuplicationEvent event) {
        String spaceId = event.getSpaceId();
        String contentId = event.getContentId();

        try {
            contentDuplicator.updateContent(spaceId, contentId);
            updateContentSuccess(spaceId, contentId);

        } catch (DuplicationException e) {
            updateContentFailure(spaceId, contentId);
        }
    }

    private void deleteContent(DuplicationEvent event) {
        String spaceId = event.getSpaceId();
        String contentId = event.getContentId();

        try {
            contentDuplicator.deleteContent(spaceId, contentId);
            deleteContentSuccess(spaceId, contentId);

        } catch (DuplicationException e) {
            deleteContentFailure(spaceId, contentId);
        }
    }

    @Override
    public void stop() {
        log.info("Shutting down.");

        watchQ = false;
        executor.shutdown();

        Set<DuplicationEvent> events = new HashSet<DuplicationEvent>();
        events.addAll(retryTally.keySet());

        for (Object event : retryQ.toArray()) {
            events.add((DuplicationEvent) event);
        }
        retryQ.clear();

        for (Object event : inboxQ.toArray()) {
            events.add((DuplicationEvent) event);
        }
        inboxQ.clear();

        for (DuplicationEvent event : events) {
            processFailure(event, "service shutdown during retry");
        }
    }

    private void createContentSuccess(String spaceId,
                                      String contentId,
                                      String md5) {
        processSuccess(spaceId, contentId, md5, CONTENT_CREATE);
    }

    private void updateContentSuccess(String spaceId, String contentId) {
        processSuccess(spaceId, contentId, TYPE.CONTENT_UPDATE);
    }

    private void deleteContentSuccess(String spaceId, String contentId) {
        processSuccess(spaceId, contentId, TYPE.CONTENT_DELETE);
    }

    private void processSuccess(String spaceId,
                                String contentId,
                                String md5,
                                TYPE type) {
        processSuccess(createDuplicationEvent(spaceId, contentId, md5, type));
    }

    private void processSuccess(String spaceId, String contentId, TYPE type) {
        processSuccess(createDuplicationEvent(spaceId, contentId, type));
    }

    private void processSuccess(DuplicationEvent event) {
        log.debug("processing success: {}, q: {}", event, inboxQ.size());

        // clear the tracking of this event.
        retryTally.remove(event);
        listener.processResult(event);
    }

    private void createContentFailure(String spaceId, String contentId) {
        log.debug("createContentFailure({}, {})", spaceId, contentId);
        contentFailure(spaceId, contentId, CONTENT_CREATE);
    }

    private void updateContentFailure(String spaceId, String contentId) {
        log.debug("updateContentFailure({}, {})", spaceId, contentId);
        contentFailure(spaceId, contentId, TYPE.CONTENT_UPDATE);
    }

    private void deleteContentFailure(String spaceId, String contentId) {
        log.debug("deleteContentFailure({}, {})", spaceId, contentId);
        contentFailure(spaceId, contentId, TYPE.CONTENT_DELETE);
    }

    private void contentFailure(String spaceId, String contentId, TYPE type) {
        DuplicationEvent event =
            createDuplicationEvent(spaceId, contentId, type);

        Integer tally = retryTally.get(event);
        if (null == tally) {
            tally = 0;
        }

        if (tally >= MAX_RETRIES) {
            processFailure(event, "max retries exceeded: " + tally);

        } else {
            processRetry(event, tally + 1);
        }
    }

    private void processFailure(DuplicationEvent event, String error) {
        log.warn("processing failure: {} for: {}", error, event);

        event.fail(error);

        // clear tracking of this event.
        retryTally.remove(event);
        listener.processResult(event);
    }

    private void processRetry(DuplicationEvent result, Integer tally) {
        log.info("Schedule retry: {}, for {}", tally, result);

        Random r = new Random();
        long exponentialWait = r.nextInt((int) Math.pow(3, tally));

        long delayMillis = exponentialWait * waitMillis;
        result.setDelay(delayMillis);

        retryTally.put(result, tally);
        retryQ.put(result);
    }

    /**
     * This method is intended as a helper for UNIT TESTS ONLY.
     *
     * @return true if retries are in-progress
     */
    protected boolean eventsExist() {
        return !retryTally.isEmpty() || !retryQ.isEmpty() || !inboxQ.isEmpty();
    }

}
