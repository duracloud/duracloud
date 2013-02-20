/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.apache.commons.lang.StringUtils;
import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.InputStream;
import java.util.Properties;

/**
 * A controller for initializing the app programmatically. It is intended to be
 * used by tests.
 * 
 * @author Daniel Bernstein
 * 
 */
@Controller
public class InitController {
    public static final String INIT_MAPPING = "/init";

    private static Logger log = LoggerFactory.getLogger(InitController.class);

    private SyncConfigurationManager syncConfigurationManager;

    @Autowired
    public InitController(SyncConfigurationManager syncConfigurationManager) {
        this.syncConfigurationManager = syncConfigurationManager;
    }

    @RequestMapping(value = { INIT_MAPPING }, method = RequestMethod.POST)
    public ResponseEntity<String> initialize(InputStream request) {
        log.debug("initializing the application programmatically...");

        Properties properties = new Properties();
        String text = "initialization successful";
        HttpStatus status = HttpStatus.OK;
        try {
            properties.load(request);
            String[] uploadDirectories =
                properties.getProperty("uploadDirectories", "").split(",");

            DirectoryConfigs configs = new DirectoryConfigs();
            for (String dir : uploadDirectories) {
                configs.add(new DirectoryConfig(dir));
            }

            String configXmlLocation =
                properties.getProperty("configXmlLocation");
            if (!StringUtils.isBlank(configXmlLocation)) {
                this.syncConfigurationManager.setConfigXmlPath(configXmlLocation);
            }

            this.syncConfigurationManager.persistDuracloudConfiguration(properties.getProperty("username"),
                                                                        properties.getProperty("password"),
                                                                        properties.getProperty("host"),
                                                                        properties.getProperty("port",
                                                                                               "443"),
                                                                        properties.getProperty("spaceId"));

            this.syncConfigurationManager.persistDirectoryConfigs(configs);

            log.debug(text);
        } catch (Exception e) {
            text = "initialization failed: " + e.getMessage();
            log.error(text);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<String>(text, status);
    }
}
