/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.logger;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Bernstein
 *         Date: 11/17/2016
 */
public class ClientInfoLogger extends BaseLogger {
    // Note that this logger is not named using the standard class name method
    // to keep the information sent to this log out of the primary logs.
    private Logger log = LoggerFactory.getLogger("client-info");

    public void log(Map<String,String> props) {
        if(log.isInfoEnabled()) {
            log.info(buildLogMessage(props));
        }
    }
}
