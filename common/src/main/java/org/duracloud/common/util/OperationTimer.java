/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A handy class that lets you wrap any operation in a stopwatch that will output
 * elapsed time  and an arbitrary message to the log. 
 * @author Daniel Bernstein
 * 
 * @param <T>
 */
public abstract class OperationTimer<T> {
    private Logger logger = LoggerFactory.getLogger(OperationTimer.class);
    private StopWatch stopWatch = new StopWatch();
    private String message = ""; 
    
    public OperationTimer(String message){
        this.message = message;
    }

    public T execute() throws Exception {
        stopWatch.start();
        T result = executeImpl();
        stopWatch.stop();
        logger.debug("Message={} - Elapsed time: {}", message, stopWatch.toString());
        return result;
    }
    
    /**
     * Implements the execution of the operation to be timed.
     * @return
     * @throws Exception
     */
    public abstract T executeImpl() throws Exception;
}
