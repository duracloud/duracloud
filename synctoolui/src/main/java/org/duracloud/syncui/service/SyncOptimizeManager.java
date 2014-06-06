/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.duracloud.syncoptimize.SyncOptimizeDriver;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.duracloud.syncoptimize.config.SyncOptimizeConfigParser;
import org.duracloud.syncoptimize.status.SyncTestEvent;
import org.duracloud.syncoptimize.status.SyncTestStatus;
import org.duracloud.syncui.controller.SyncOptimizeManagerResultCallBack;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class wraps the SyncOptimizeDriver for use as an asynchronous business
 * service.
 * 
 * @author Daniel Bernstein
 * 
 */
@Component("syncOptimizeManager")
public class SyncOptimizeManager {
    private SyncOptimizeDriver syncOptimizeDriver;
    private SyncConfigurationManager syncConfigurationManager;
    private Date startTime = null;
    private String status = null;
    private boolean running = false;
    private boolean failed = false;
    private static final Logger log =
        LoggerFactory.getLogger(SyncOptimizeManager.class);

    public SyncOptimizeManager(
        SyncConfigurationManager syncConfigurationManager,
        SyncOptimizeDriver syncOptimizeDriver) {
        this.syncConfigurationManager = syncConfigurationManager;
        this.syncOptimizeDriver = syncOptimizeDriver;
        reset();
    }

    @Autowired
    public SyncOptimizeManager(SyncConfigurationManager syncConfigurationManager) {
        this(syncConfigurationManager,new SyncOptimizeDriver(false));
    }
    
    private void reset() {
        status = "";
        failed = false;
        startTime = new Date();
    }

    public boolean isRunning() {
        return running;
    }

    public String getStatus() {
        if (running) {
            updateRunningStatus();
        }
        return status;

    }

    public boolean isFailed() {
        return failed;
    }

    protected void updateRunningStatus() {
        SyncTestStatus syncTestStatus = syncOptimizeDriver.getSyncTestStatus();
        List<SyncTestEvent> events = syncTestStatus.getSyncEvents();

        if (events.size() > 0) {
            Collections.sort(events, new Comparator<SyncTestEvent>() {
                @Override
                public int compare(SyncTestEvent o1, SyncTestEvent o2) {
                    return Long.valueOf(o1.getElapsed())
                               .compareTo(Long.valueOf(o1.getElapsed()));
                }
            });

            SyncTestEvent best = events.get(0);
            status =
                MessageFormat.format("Transfer rate optimization underway. "
                                         + "Current test started at {0}. {1} tests so far run. Best run so far: {2}",
                                     startTime,
                                     events.size(),
                                     best.toString());
        } else {
            status = startTime + ": Started, but no tests have completed yet.";
        }
    }

    /**
     * Starts the sync optimization process. On success, the thread count will be updated automatically.
     * It is up to to caller to detect a successful run (using the callback interface) and responding 
     * with an autostart of the SyncProcessManager if appropriate.
     * @param callback on success
     */
    public void start(final SyncOptimizeManagerResultCallBack callback) {
        if (isRunning()) {
            throw new IllegalStateException("The start() method cannot be called when the sync optimize process is running.");
        }

        running = true;

        new Thread(new Runnable() {
            public void run() {
                
                SyncOptimizeManager.this.reset();
                
                SyncOptimizeConfig config = new SyncOptimizeConfig();
                DuracloudConfiguration duracloudConfig =
                    syncConfigurationManager.retrieveDuracloudConfiguration();
                config.setHost(duracloudConfig.getHost());
                config.setPort(duracloudConfig.getPort());
                config.setNumFiles(10);
                config.setSizeFiles(5);
                config.setSpaceId(duracloudConfig.getSpaceId());
                config.setUsername(duracloudConfig.getUsername());
                config.setPassword(duracloudConfig.getPassword());
                config.setContext(SyncOptimizeConfigParser.DEFAULT_CONTEXT);
                try {
                    int threadCount =
                        syncOptimizeDriver.getOptimalThreads(config);
                    syncConfigurationManager.setThreadCount(threadCount);
                    status =
                        MessageFormat.format("The optimizer last ran to completion at {0}. "
                                                 + "The optimal thread count under the prevailing system conditions at that time was {1}.  "
                                                 + "  The system has been updated with the new thread count.",
                                             new Date(),
                                             threadCount);
                    
                    running = false;

                    try{
                        callback.onSuccess();
                    }catch(Exception e){
                        log.error(e.getMessage());
                    }
                    
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    status =
                        "The optimizer failed: "
                            + e.getMessage()
                            + ". Please try again or contact DuraCloud support.";
                    failed = true;
                    running = false;
                    callback.onFailure(e, status);
                }
            }
        }).start();

    }

}
