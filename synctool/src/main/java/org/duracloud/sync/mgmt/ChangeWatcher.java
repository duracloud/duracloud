/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for new items on the ChangedList.
 *
 * @author: Bill Branan
 * Date: Mar 17, 2010
 */
public class ChangeWatcher implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ChangeWatcher.class);    

    private boolean continueWatch;
    private ChangedList changedList;
    private ChangeHandler handler;
    private long watchFrequency;
    private StatusManager status;

    /**
     * Creates a ChangeWatcher which watches for changes to the ChangedList
     * and notifies the ChangeHandler.
     *
     * @param changedList the ChangedList to watch
     * @param handler the ChangeHandler to notify
     * @param watchFrequency how often to check for changes
     */
    public ChangeWatcher(ChangedList changedList,
                         ChangeHandler handler,
                         long watchFrequency) {
        this.changedList = changedList;
        this.handler = handler;
        this.watchFrequency = watchFrequency;
        this.status = StatusManager.getInstance();
        continueWatch = true;
    }

    public void run() {
        while (continueWatch) {
            ChangedFile changedFile = changedList.reserve();
            if (changedFile != null) {
                boolean success = handler.handleChangedFile(changedFile);
                if(success) {
                    status.startingWork();
                } else {
                    changedFile.unreserve();
                }
            } else {
                // List is empty or handler not ready, wait before next check
                sleep(watchFrequency);
            }
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.warn("ChangeWatcher thread interrupted");
        }
    }

    public void endWatch() {
        continueWatch = false;
    }
}
