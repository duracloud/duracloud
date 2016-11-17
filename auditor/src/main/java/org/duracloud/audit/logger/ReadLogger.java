/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.logger;

import org.duracloud.common.queue.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bill Branan
 *         Date: 3/20/14
 */
public class ReadLogger extends TaskLogger {

    // Note that this logger is not named using the standard class name method
    // to keep the information sent to this log out of the primary logs.
    private Logger log = LoggerFactory.getLogger("storage-read");

    public void log(Task task) {
        if(log.isInfoEnabled()) {
            log.info(buildLogMessage(task));
        }
    }

}
