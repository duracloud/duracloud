/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import java.util.Dictionary;

import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;

/**
 * This class is the entry point for performing on-demand bit-integrity
 * verification.
 *
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class FixityService extends BaseService
        implements ComputeService, ManagedService {

    private final Logger log = LoggerFactory.getLogger(FixityService.class);

    private String text;

    @Override
    public void start() throws Exception {
        log.info("FixityService is Starting");
        super.start();
    }

    @Override
    public void stop() throws Exception {
        log.info("FixityService is Stopping");
        super.stop();
    }

    @Override
    public String describe() throws Exception {
        log.info("FixityService: Calling describe().");
        String baseDescribe = super.describe();
        return baseDescribe + "; Service message: '" + text + "'";
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        log.info("FixityService updating config: ");
        if (config != null) {
            Enumeration keys = config.keys();
            {
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String val = (String) config.get(key);
                    log.info(" [" + key + "|" + val + "] ");
                }
            }
        } else {
            log.info("config is null.");
        }

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        log.info("FixityService: setText (" + text + ")");
        this.text = text;
    }

}
