/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.notification.NotificationType;
import org.duracloud.common.util.IOUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.exec.error.InvalidActionRequestException;
import org.duracloud.exec.runner.BitIntegrityRunner;
import org.duracloud.execdata.bitintegrity.BitIntegrityResults;
import org.duracloud.execdata.bitintegrity.SpaceBitIntegrityResult;
import org.duracloud.execdata.bitintegrity.serialize.BitIntegrityResultsSerializer;
import org.duracloud.serviceconfig.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages running the Bit Integrity service in DuraCloud.
 *
 * @author: Bill Branan
 * Date: 3/1/12
 */
public class BitIntegrityHandler extends BaseServiceHandler {

    private final Logger log =
        LoggerFactory.getLogger(BitIntegrityHandler.class);

    protected static final String HANDLER_NAME = "bit-integrity-handler";

    protected static final String BIT_INTEGRITY_NAME =
        "Bit Integrity Checker - Tools";
    protected static final String RESULTS_MIME_TYPE = "application/json";

    private Timer timer;
    private BitIntegrityRunner runner;
    private String resultsFileName;
    private BitIntegrityResults results;

    public BitIntegrityHandler(String resultsFileName) {
        super();
        supportedActions.add(START_BIT_INTEGRITY);
        supportedActions.add(CANCEL_BIT_INTEGRITY);
        this.timer = new Timer();
        this.resultsFileName = resultsFileName;
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public String getStatus() {
        return runner.getStatus();
    }

    @Override
    public void start() {
        log.info("Executor: Starting Bit Integrity Handler");

        ServiceInfo service;
        try {
            service = findAvailableServiceByName(BIT_INTEGRITY_NAME);
            initializeRunner(service);
        } catch(Exception e) {
            setError("Unable to find Bit Integrity Tools service due " +
                     "to " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    private void initializeRunner(ServiceInfo service) {
        if(null == this.runner) {
            this.runner = new BitIntegrityRunner(storeMgr,
                                                 servicesMgr,
                                                 manifestGenerator,
                                                 service,
                                                 this);
        }
    }

    /**
     * Intended to be used only for testing purposes.
     * @param runner
     */
    protected void setRunner(BitIntegrityRunner runner) {
        this.runner = runner;
    }

    @Override
    public void stop() {
        log.info("Executor: Stopping Bit Integrity");
        timer.cancel();
        timer = new Timer();
        runner.stop();
    }

    /**
     * @param actionName supported actions: start-bit-integrity,
     *                                      cancel-bit-integrity
     * @param actionParameters for start-bit-integrity:
     *        startTime in millis "," frequency in millis
     *        example, value of: "1577854800000,604800000" will start the next
     *        bit integrity check on Jan 1, 2020 and run weekly.
     */
    @Override
    public void performAction(String actionName, String actionParameters)
        throws InvalidActionRequestException {
        log.info("Executor: Performing action: " + actionName +
                 ", with parameters: " + actionParameters);

        if(START_BIT_INTEGRITY.equals(actionName)) {
            if(null == actionParameters || actionParameters.equals("")) {
                String err = "Parameters expected: 'start-time,frequency'";
                throw new InvalidActionRequestException(err);
            }

            String[] params = actionParameters.split(",");

            // Start a scheduled bit integrity check
            long startTime = Long.valueOf(params[0]);
            long frequency = Long.valueOf(params[1]);

            Date startDate = new Date(startTime);
            if(startDate.before(new Date())) {
                String err = "Start value provided is " + startDate.toString() +
                             ". Cannot set a schedule which starts in the past";
                throw new InvalidActionRequestException(err);
            }

            if(frequency < 600000) {
                String err = "The minimum frequency for the bit integrity " +
                             "schedule is 10 minutes, a value of: 600000";
                throw new InvalidActionRequestException(err);
            }

            BIHandlerTask handlerTask = new BIHandlerTask();
            timer.scheduleAtFixedRate(handlerTask, startDate, frequency);
        } else if(CANCEL_BIT_INTEGRITY.equals(actionName)) {
            stop();
        } else {
            String err = actionName + " is not a valid action";
            throw new InvalidActionRequestException(err);
        }
    }

    private class BIHandlerTask extends TimerTask {
        public void run() {
            if(!runner.isRunning()) {
                new Thread(runner).start();
            }
        }
    }

    public void storeResults(String storeId,
                             String spaceId,
                             SpaceBitIntegrityResult result) {
        retrieveResults();
        results.addSpaceResult(storeId, spaceId, result);
        storeResults(results);
    }

    protected BitIntegrityResults retrieveResults() {
        int attempts = 0;
        while(null == results && attempts < 3) {
            try {
                ContentStore store = storeMgr.getPrimaryContentStore();
                Content resultFile =
                    store.getContent(HANDLER_STATE_SPACE, resultsFileName);

                String resultValue =
                    IOUtil.readStringFromStream(resultFile.getStream());
                BitIntegrityResultsSerializer serializer =
                    new BitIntegrityResultsSerializer();
                results = serializer.deserialize(resultValue);
            } catch(ContentStoreException e) {
                log.warn("Not able to retrieve Bit Integrity results file " +
                         "due to ContentStoreException: " + e.getMessage());
            } catch(IOException e) {
                log.warn("Not able to retrieve Bit Integrity results file " +
                         "due to IOException: " + e.getMessage(), e);
            }
            ++attempts;
        }

        if(null == results) {
            results = new BitIntegrityResults();
        }
        return results;
    }

    protected void storeResults(BitIntegrityResults results) {
        try {
            BitIntegrityResultsSerializer serializer =
                new BitIntegrityResultsSerializer();
            String resultValue = serializer.serialize(results);
            storeFile(resultsFileName, resultValue, RESULTS_MIME_TYPE);
        } catch(Exception e) {
            log.error("Not able to store Bit Integrity results file " +
                      "due to " + e.getClass().getName() + ": " +
                      e.getMessage(), e);
        }
    }

    public void notify(String message) {
        String subject = "DuraCloud Bit Integrity failure on: " + host;
        notifier.sendAdminNotification(NotificationType.EMAIL,
                                       subject,
                                       message);
    }

    private void setError(String error, Exception e) {
        log.error(error, e);
        status = ERROR_PREFIX + error;
        throw new DuraCloudRuntimeException(error, e);
    }

}
