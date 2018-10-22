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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("${duracloud.config.file}") // this references the system property.
/**
 * @author Nicholas Woodward
 */
public class DuracloudConfig2 {
    private Logger log = LoggerFactory.getLogger(DuracloudConfig.class);

    @Autowired
    Environment env;

    public String getMcHost() {
        log.info("TDL mcHost: {}", env.getProperty("mc.host"));
        return env.getProperty("mc.host");
    }

    public String getMcPort() {
        log.info("TDL mcPort: {}", env.getProperty("mc.port"));
        return env.getProperty("mc.port");
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
