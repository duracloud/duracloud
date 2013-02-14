/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

/**
 * A spring controller for directory creation.
 * 
 * @author Daniel Bernstein
 * 
 */
@Controller
@RequestMapping(ConfigurationController.CONFIGURATION_MAPPING)
public class DirectoryController {
    private static Logger log =
        LoggerFactory.getLogger(DirectoryController.class);

    private SyncConfigurationManager syncConfigurationManager;

    @Autowired
    public DirectoryController(SyncConfigurationManager syncConfigurationManager) {
        this.syncConfigurationManager = syncConfigurationManager;
    }

    @ModelAttribute("directoryConfigForm")
    public DirectoryConfigForm directoryConfigForm() {
        return new DirectoryConfigForm();
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String get() {
        log.debug("accessing new directory page");
        return "directory";
    }

    @RequestMapping(value = { "/add" }, method=RequestMethod.POST)
    public String add(@Valid DirectoryConfigForm directoryConfigForm) {
        log.debug("adding new directory");
        DirectoryConfigs directoryConfigs =
            this.syncConfigurationManager.retrieveDirectoryConfigs();

        String path = directoryConfigForm.getDirectoryPath();
        directoryConfigs.add(new DirectoryConfig(path));
        
        this.syncConfigurationManager.persistDirectoryConfigs(directoryConfigs);

        return "success";
    }



}
