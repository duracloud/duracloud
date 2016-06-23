/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;


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
                StoreRest.class,
                SpaceRest.class,
                ManifestRest.class,
                BitIntegrityReportRest.class,
                AuditLogRest.class,
                TaskRest.class,
                ContentRest.class, 
                StorageStatsRest.class);
            
        }
    }
