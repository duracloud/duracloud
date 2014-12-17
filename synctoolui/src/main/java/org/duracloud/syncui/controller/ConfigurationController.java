/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import javax.validation.Valid;

import org.duracloud.syncui.domain.AdvancedForm;
import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.duracloud.syncui.domain.DuracloudCredentialsForm;
import org.duracloud.syncui.domain.PrefixForm;
import org.duracloud.syncui.domain.SyncProcessState;
import org.duracloud.syncui.domain.ThreadCountForm;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.duracloud.syncui.service.SyncOptimizeManager;
import org.duracloud.syncui.service.SyncProcessException;
import org.duracloud.syncui.service.SyncProcessManager;
import org.duracloud.syncui.util.UpdatePolicyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    private SyncOptimizeManager syncOptimizeManager;

    @Autowired
    public ConfigurationController(
        SyncConfigurationManager syncConfigurationManager,
        SyncProcessManager syncProcessManager,
        SyncOptimizeManager syncOptimizeManager) {
        this.syncConfigurationManager = syncConfigurationManager;
        this.syncProcessManager = syncProcessManager;
        this.syncOptimizeManager = syncOptimizeManager;
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

    @ModelAttribute("advancedForm")
    public AdvancedForm advancedForm(){
        AdvancedForm f = new AdvancedForm();
        f.setSyncDeletes(this.syncConfigurationManager.isSyncDeletes());
        f.setUpdatePolicy(UpdatePolicyHelper.get(this.syncConfigurationManager).name());
        f.setJumpStart(this.syncConfigurationManager.isJumpStart());
        return f;
    }


    @RequestMapping(value = { "" }, method= RequestMethod.GET)
    public String get(Model model) {
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

    @RequestMapping(value = { "/advanced" }, method = RequestMethod.POST)
    public View updateOptions(
                                AdvancedForm form,
                                RedirectAttributes redirectAttributes) {

        boolean syncDeletes = form.isSyncDeletes();
        log.debug("updating sync deletes to : {}", syncDeletes);
        this.syncConfigurationManager.setSyncDeletes(syncDeletes);

        String up = form.getUpdatePolicy();
        log.debug("modifying update policy to  {}", up);
        UpdatePolicyHelper.set(this.syncConfigurationManager, UpdatePolicy.valueOf(up));

        boolean jumpstart = form.isJumpStart();
        log.debug("updating  jump start to : {}", jumpstart);
        this.syncConfigurationManager.setJumpStart(jumpstart);

        return createConfigUpdatedRedirectView(redirectAttributes);
    }
    
    @ModelAttribute("threadCountForm")
    public ThreadCountForm threadCountForm(){
        ThreadCountForm f = new ThreadCountForm();
        f.setThreadCount(this.syncConfigurationManager.getThreadCount());
        return f;
    }
    
    @ModelAttribute("syncOptimizeManager")
    public SyncOptimizeManager syncOptimizeManager(){
        return this.syncOptimizeManager;
    }


 

    @RequestMapping(value = { "/thread-count" }, method = RequestMethod.POST)
    public View updateThreadCount(
                                ThreadCountForm form,
                                RedirectAttributes redirectAttributes) {

        int threadCount = form.getThreadCount();
        log.debug("updating thread count  to : {}", threadCount);
        this.syncConfigurationManager.setThreadCount(threadCount);
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

    

    @ModelAttribute("prefixForm")
    public PrefixForm prefixForm(){
        PrefixForm f = new PrefixForm();
        f.setPrefix(this.syncConfigurationManager.getPrefix());
        return f;
    }

    @RequestMapping(value = { "/prefix" }, method = RequestMethod.POST)
    public View updatePrefix(
                                PrefixForm form,
                                RedirectAttributes redirectAttributes) {

        String prefix = form.getPrefix();
        log.debug("updating prefix  to : {}", prefix);
        this.syncConfigurationManager.setPrefix(prefix);
        return createConfigUpdatedRedirectView(redirectAttributes);
    }

    @RequestMapping(value = { "/optimize" }, method = RequestMethod.GET)
    public String optimize() {
        return "optimize";
    }

    @RequestMapping(value = { "/optimize" }, method = RequestMethod.POST)
    public View
        optimize(@RequestParam(value = "autoStart", defaultValue = "false") final boolean autoStart,
                 RedirectAttributes redirectAttributes) {
        
        if (!syncProcessManager.getProcessState()
            .equals(SyncProcessState.STOPPED)) {
                throw new IllegalStateException("The  optimizer cannot run when the sync process is running.");
        }

        
        this.syncOptimizeManager.start(new SyncOptimizeManagerResultCallBack(){
            public void onSuccess(){
                if(autoStart){
                    try {
                        syncProcessManager.start();
                    } catch (SyncProcessException e) {
                        log.error("failed to start sync process manager: "
                                      + e.getMessage(),
                                  e);
                    }
                }
            }
            
            @Override
            public void onFailure(Exception ex, String status) {
                //do nothing.
            }
        });


        
        return createRedirect(redirectAttributes, "syncOptimizeStarted");
    }

    private View
        createConfigUpdatedRedirectView(RedirectAttributes redirectAttributes) {
        String include = "configUpdated";
        return createRedirect(redirectAttributes, include);
    }

    protected View createRedirect(RedirectAttributes redirectAttributes,
                                  String include) {
        RedirectView view =
            new RedirectView(CONFIGURATION_MAPPING, true, true, false);
        redirectAttributes.addFlashAttribute("messageInclude", include);

        return view;
    }

    public static enum UpdatePolicy {
        NONE,
        PRESERVE,
        OVERWRITE;
    }
}
