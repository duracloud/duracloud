/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("${duracloud.config.file}") // this references the system property.
/**
 * @author Nicholas Woodward
 */
public class DuracloudConfig {
    private Logger log = LoggerFactory.getLogger(DuracloudConfig.class);

    @Value("${mc.host}")
    private String mcHost;

    @Value("${mc.port}")
    private String mcPort;

    public String getMcHost() {
        log.info("TDL mcHost: {}", mcHost);
        return mcHost;
    }

    public String getMcPort() {
        log.info("TDL mcPort: {}", mcPort);
        return mcPort;
    }

    public String getAmaUrl() {
        String mcSchema = (getMcPort() == "443") ? "https" : "http";
        log.info("TDL mcSchema: {}", mcSchema);
        return mcSchema + "://" + getMcHost();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
        return p;
    }

}
