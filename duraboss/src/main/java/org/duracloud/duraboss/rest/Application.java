/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest;

import org.duracloud.duraboss.rest.report.StorageReportRest;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.web.filter.RequestContextFilter;


/**
 * The entry point for the jax-rs application.
 * This class is referenced in the web.xml.
 * @author Daniel Bernstein
 *         Date: Oct 2, 2014
 */
public class Application extends ResourceConfig{

        /**
         * Register JAX-RS application components.
         */
        public Application () {
            super(
                RequestContextFilter.class,
                InitRest.class,
                StorageReportRest.class,
                SecurityRest.class
                );
            
        }
    }
