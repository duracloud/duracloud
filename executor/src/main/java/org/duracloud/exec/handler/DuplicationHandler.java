/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.exec.error.InvalidActionRequestException;

/**
 * Manages the running of the Duplication on Change service in DuraCloud.
 *
 * @author: Bill Branan
 * Date: 3/2/12
 */
public class DuplicationHandler extends BaseServiceHandler {

    private static final String HANDLER_NAME = "duplication-handler";

    public DuplicationHandler() {
        super();
        supportedActions.add(START_DUPLICATION);
        supportedActions.add(ADD_TO_DUPLICATION);
        supportedActions.add(REMOVE_FROM_DUPLICATION);
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
    public void performAction(String actionName, String actionParameters)
        throws InvalidActionRequestException {
        if(START_DUPLICATION.equals(actionName)) {
            // TODO: implement
        } else if(ADD_TO_DUPLICATION.equals(actionName)) {
            // TODO: implement
        } else if(REMOVE_FROM_DUPLICATION.equals(actionName)) {
            // TODO: implement
        } else {
            String err = actionName + " is not a valid action";
            throw new InvalidActionRequestException(err);
        }
    }

}
