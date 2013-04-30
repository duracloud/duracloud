/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import javax.validation.Valid;

import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.duracloud.syncui.domain.DuracloudCredentialsForm;
import org.duracloud.syncui.domain.SyncProcessState;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.duracloud.syncui.service.SyncProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * A spring controller for configuration related functions.
 * 
 * @author Daniel Bernstein
 * 
 */
@Controller
@RequestMapping(ConfigurationController.CONFIGURATION_MAPPING)
public class ConfigurationController {
    public static final String CONFIGURATION_MAPPING = "/configuration";
    private static final String FEEDBACK_MESSAGE = "user-feedback";
    private static Logger log =
        LoggerFactory.getLogger(ConfigurationController.class);

    private SyncConfigurationManager syncConfigurationManager;
    private SyncProcessManager syncProcessManager;

    @Autowired
    public ConfigurationController(
        SyncConfigurationManager syncConfigurationManager,
        SyncProcessManager syncProcessManager) {
        this.syncConfigurationManager = syncConfigurationManager;
        this.syncProcessManager = syncProcessManager;
    }

    @ModelAttribute("directoryConfigs")
    public DirectoryConfigs directoryConfigs() {
        return this.syncConfigurationManager.retrieveDirectoryConfigs();
    }

    @ModelAttribute("duracloudConfiguration")
    public DuracloudConfiguration duracloudConfiguration() {
        return this.syncConfigurationManager.retrieveDuracloudConfiguration();
    }

    @ModelAttribute("duracloudCredentialsForm")
    public DuracloudCredentialsForm duracloudCredentials() {
        return new DuracloudCredentialsForm();
    }

    @ModelAttribute("syncProcessState")
    public SyncProcessState syncProcessState() {
        return syncProcessManager.getProcessState();
    }

    @RequestMapping(value = { "" })
    public String get() {
        log.debug("accessing configuration page");
        return "configuration";
    }

    @RequestMapping(value = { "/remove" }, method = RequestMethod.POST)
    public View removeDirectory(@Valid DirectoryConfigForm directoryConfigForm,
                                RedirectAttributes redirectAttributes) {
        String path = directoryConfigForm.getDirectoryPath();

        log.debug("removing path: {}", path);
        DirectoryConfigs directoryConfigs =
            this.syncConfigurationManager.retrieveDirectoryConfigs();

        directoryConfigs.removePath(path);
        this.syncConfigurationManager.persistDirectoryConfigs(directoryConfigs);

        return createConfigUpdatedRedirectView(redirectAttributes);
    }

    @ModelAttribute("directoryConfigForm")
    public DirectoryConfigForm directoryConfigForm() {
        return new DirectoryConfigForm();
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String getAdd() {
        log.debug("accessing new directory page");
        return "directory";
    }

    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    public View add(@Valid DirectoryConfigForm directoryConfigForm,
                    RedirectAttributes redirectAttributes) {
        log.debug("adding new directory");
        DirectoryConfigs directoryConfigs =
            this.syncConfigurationManager.retrieveDirectoryConfigs();

        String path = directoryConfigForm.getDirectoryPath();
        directoryConfigs.add(new DirectoryConfig(path));

        this.syncConfigurationManager.persistDirectoryConfigs(directoryConfigs);

        return createConfigUpdatedRedirectView(redirectAttributes);
    }

    private View
        createConfigUpdatedRedirectView(RedirectAttributes redirectAttributes) {
        RedirectView view =
            new RedirectView("/configuration", true, true, false);
        redirectAttributes.addFlashAttribute("messageInclude", "configUpdated");

        return view;
    }

}
