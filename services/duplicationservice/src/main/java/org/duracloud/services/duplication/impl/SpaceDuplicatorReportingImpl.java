/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.services.duplication.SpaceDuplicator;
import org.duracloud.services.duplication.error.DuplicationException;
import org.duracloud.services.duplication.result.DuplicationEvent;
import org.duracloud.services.duplication.result.ResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements the SpaceDuplicator contract with the responsibility
 * of requeuing failed duplication attempts and reporting all successful and
 * failed duplications.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public class SpaceDuplicatorReportingImpl implements SpaceDuplicator {

    private final Logger log = LoggerFactory.getLogger(
        SpaceDuplicatorReportingImpl.class);

    public static final int MAX_RETRIES = 3;

    private SpaceDuplicator spaceDuplicator;

    private ResultListener listener;
    private boolean watchQ;
    private DelayQueue<DuplicationEvent> retryQ;
    private Map<DuplicationEvent, Integer> retryTally;

    private ExecutorService executor;
    private final long waitMillis;


    public SpaceDuplicatorReportingImpl(SpaceDuplicator spaceDuplicator,
                                        ResultListener listener) {
        this(spaceDuplicator, listener, 60000); // default waiting factor = 1min
    }

    public SpaceDuplicatorReportingImpl(SpaceDuplicator spaceDuplicator,
                                        ResultListener listener,
                                        long waitMillis) {
        this.spaceDuplicator = spaceDuplicator;
        this.listener = listener;
        this.watchQ = true;
        this.waitMillis = waitMillis;

        this.retryQ = new DelayQueue<DuplicationEvent>();
        this.retryTally = new HashMap<DuplicationEvent, Integer>();
        this.executor = Executors.newSingleThreadExecutor();

        executor.execute(new QWatcher());
    }

    @Override
    public String getFromStoreId() {
        return spaceDuplicator.getFromStoreId();
    }

    @Override
    public String getToStoreId() {
        return spaceDuplicator.getToStoreId();
    }

    private class QWatcher extends Thread {
        @Override
        public void run() {
            while (watchQ) {
                DuplicationEvent event = retryQ.poll();
                if (null != event) {
                    switch (event.getType()) {
                        case SPACE_CREATE:
                            createSpace(event.getSpaceId());
                            break;

                        case SPACE_DELETE:
                            deleteSpace(event.getSpaceId());
                            break;

                        case SPACE_UPDATE:
                            updateSpace(event.getSpaceId());
                            break;

                        case SPACE_UPDATE_ACL:
                            updateSpaceAcl(event.getSpaceId());
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
    public void createSpace(String spaceId) {
        try {
            spaceDuplicator.createSpace(spaceId);
            createSpaceSuccess(spaceId);

        } catch (DuplicationException e) {
            createSpaceFailure(spaceId);
        }
    }

    @Override
    public void updateSpace(String spaceId) {
        try {
            spaceDuplicator.updateSpace(spaceId);
            updateSpaceSuccess(spaceId);

        } catch (DuplicationException e) {
            updateSpaceFailure(spaceId);
        }
    }

    @Override
    public void updateSpaceAcl(String spaceId) {
        try {
            spaceDuplicator.updateSpaceAcl(spaceId);
            updateSpaceAclSuccess(spaceId);

        } catch (DuplicationException e) {
            updateSpaceAclFailure(spaceId);
        }
    }

    @Override
    public void deleteSpace(String spaceId) {
        try {
            spaceDuplicator.deleteSpace(spaceId);
            deleteSpaceSuccess(spaceId);

        } catch (DuplicationException e) {
            deleteSpaceFailure(spaceId);
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

        for (DuplicationEvent event : events) {
            processFailure(event, "service shutdown during retry");
        }
    }

    private void createSpaceSuccess(String spaceId) {
        processSuccess(spaceId, DuplicationEvent.TYPE.SPACE_CREATE);
    }

    private void updateSpaceSuccess(String spaceId) {
        processSuccess(spaceId, DuplicationEvent.TYPE.SPACE_UPDATE);
    }

    private void updateSpaceAclSuccess(String spaceId) {
        processSuccess(spaceId, DuplicationEvent.TYPE.SPACE_UPDATE_ACL);
    }

    private void deleteSpaceSuccess(String spaceId) {
        processSuccess(spaceId, DuplicationEvent.TYPE.SPACE_DELETE);
    }

    private void processSuccess(String spaceId, DuplicationEvent.TYPE type) {
        DuplicationEvent event = createDuplicationEvent(spaceId, type);
        log.debug("processing success: {}", event);

        // clear the tracking of this event.
        retryTally.remove(event);
        listener.processResult(event);
    }

    private DuplicationEvent createDuplicationEvent(String spaceId,
                                                    DuplicationEvent.TYPE type) {
        return new DuplicationEvent(spaceDuplicator.getFromStoreId(),
                                    spaceDuplicator.getToStoreId(), type,
                                    spaceId);
    }

    private void createSpaceFailure(String spaceId) {
        log.debug("createSpaceFailure({})", spaceId);
        spaceFailure(spaceId, DuplicationEvent.TYPE.SPACE_CREATE);
    }

    private void updateSpaceFailure(String spaceId) {
        log.debug("updateSpaceFailure({})", spaceId);
        spaceFailure(spaceId, DuplicationEvent.TYPE.SPACE_UPDATE);
    }

    private void updateSpaceAclFailure(String spaceId) {
        log.debug("updateSpaceAclFailure({})", spaceId);
        spaceFailure(spaceId, DuplicationEvent.TYPE.SPACE_UPDATE_ACL);
    }

    private void deleteSpaceFailure(String spaceId) {
        log.debug("deleteSpaceFailure({})", spaceId);
        spaceFailure(spaceId, DuplicationEvent.TYPE.SPACE_DELETE);
    }

    private void spaceFailure(String spaceId, DuplicationEvent.TYPE type) {
        DuplicationEvent event = createDuplicationEvent(spaceId, type);

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
        log.warn("processing failure: {}", event);

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
    protected boolean retriesExist() {
        return !retryTally.isEmpty() || !retryQ.isEmpty();
    }

}
