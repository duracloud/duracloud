/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.exec.error.UnsupportedActionException;

/**
 * Manages the running of the Media Streaming service in DuraCloud.
 *
 * @author: Bill Branan
 * Date: 3/2/12
 */
public class MediaStreamingHandler extends BaseServiceHandler {

    private static final String HANDLER_NAME = "media-streaming-handler";
    private static final String START_STREAMING = "start-streaming";
    private static final String STOP_STREAMING = "stop-streaming";

    public MediaStreamingHandler() {
        super();
        supportedActions.add(START_STREAMING);
        supportedActions.add(STOP_STREAMING);
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public String getStatus() {
        // TODO: implement
        return "Everything is fine!";
    }

    @Override
    public void start() {
        // TODO: implement
    }

    @Override
    public void stop() {
        // TODO: implement
    }

    @Override
    public void performAction(String actionName, String actionParameters) {
        if(START_STREAMING.equals(actionName)) {
            // TODO: implement
        } else if(STOP_STREAMING.equals(actionName)) {
            // TODO: implement
        } else {
            throw new UnsupportedActionException(actionName);
        }
    }

}