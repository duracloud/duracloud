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
 * Manages running the Bit Integrity service in DuraCloud.
 *
 * @author: Bill Branan
 * Date: 3/1/12
 */
public class BitIntegrityHandler extends BaseServiceHandler {

    private static final String HANDLER_NAME = "bit-integrity-handler";
    private static final String START_BIT_INTEGRITY = "start-bit-integrity";
    private static final String CANCEL_BIT_INTEGRITY = "cancel-bit-integrity";

    public BitIntegrityHandler() {
        super();
        supportedActions.add(START_BIT_INTEGRITY);
        supportedActions.add(CANCEL_BIT_INTEGRITY);
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
        if(START_BIT_INTEGRITY.equals(actionName)) {
            // TODO: implement
        } else if(CANCEL_BIT_INTEGRITY.equals(actionName)) {
            // TODO: implement
        } else {
            throw new UnsupportedActionException(actionName);
        }
    }

}
