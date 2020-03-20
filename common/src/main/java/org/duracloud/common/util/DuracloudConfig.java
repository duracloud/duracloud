/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * This class provides access to Duracloud configuration properties.
 *
 * @author Nicholas Woodward
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@PropertySource("${duracloud.config.file}") // this references the system property.
public class DuracloudConfig {
    private Logger log = LoggerFactory.getLogger(DuracloudConfig.class);

    @Autowired
    Environment env;

    @Bean
    public DuracloudConfigBean duracloudConfigBean() {
        return new DuracloudConfigBean(env);
    }

}
