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
import org.duracloud.storage.aop.ContentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public class AuditListenerImpl implements AuditListener {

    private final Logger log = LoggerFactory.getLogger(AuditListenerImpl.class);

    private AuditLogStore logStore;

    private List<ContentMessage> events;
    private Set<String> writeWait;
    private Set<String> writeScheduled;

    private long delay;
    private boolean isStopped;


    public AuditListenerImpl(AuditLogStore logStore, long delay) {
        this.logStore = logStore;
        this.delay = delay;
        initialize();
    }

    public void initialize() {
        this.events =
            Collections.synchronizedList(new LinkedList<ContentMessage>());
        this.writeWait = new HashSet<String>();
        this.writeScheduled =
            Collections.synchronizedSet(new HashSet<String>());
        this.isStopped = false;
    }

    @Override
    public void onContentEvent(ContentMessage message) {
        log.info("AuditListenerImpl.onContentEvent({})", message);

        if (isStopped) {
            log.debug("Not logging event, auditor is stopped");
            return;
        }

        if (null == message) {
            String error = "Arg ContentMessage is null!";
            log.error(error);
            throw new IllegalArgumentException(error);
        }

        // Ignore system events.
        if (isSystemEvent(message)) {
            log.debug("Not logging internal system event.");
            return;
        }

        events.add(message);
        scheduleWrite(message.getSpaceId());
    }

    private boolean isSystemEvent(ContentMessage message) {
        String spaceId = message.getSpaceId();
        return Auditor.systemSpaces.contains(spaceId);
    }

    private void scheduleWrite(String spaceId) {
        if (!writeScheduled.contains(spaceId)) {
            writeScheduled.add(spaceId);

            EventWriter eventWriter = new EventWriter(spaceId);
            new Thread(eventWriter).start();
        }
    }

    @Override
    public void waitToWrite(String spaceId, boolean flag) {
        if (flag) {
            writeWait.add(spaceId);
        } else {
            writeWait.remove(spaceId);
        }
    }

    @Override
    public void stop() {
        isStopped = true;
    }

    /**
     * This class is a helper for writing audit log events.
     */
    private class EventWriter implements Runnable {

        private String spaceId;

        public EventWriter(String spaceId) {
            this.spaceId = spaceId;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            while (waitingToWrite(start)) {
                sleep();
            }
            write();
        }

        private boolean waitingToWrite(long start) {
            long now = System.currentTimeMillis();
            boolean wait = writeWait.contains(spaceId) || (start + delay) > now;
            log.debug("waiting on {}? {}", spaceId, wait);
            return wait;
        }

        private synchronized void write() {
            log.debug("writing events for space: {}", spaceId);

            List<ContentMessage> msgs = new LinkedList<ContentMessage>();
            synchronized (events) {

                Iterator<ContentMessage> itr = events.iterator();
                while (itr.hasNext()) {

                    ContentMessage event = itr.next();
                    if (spaceId.equals(event.getSpaceId())) {
                        msgs.add(event);
                        itr.remove();
                    }
                }
            }

            logStore.write(msgs);
            writeScheduled.remove(spaceId);
        }

        private void sleep() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

}
