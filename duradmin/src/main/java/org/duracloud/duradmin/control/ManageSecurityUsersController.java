/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.appconfig.domain.Application;
import org.duracloud.controller.AbstractRestController;
import org.duracloud.duradmin.config.DuradminConfig;
import org.duracloud.duradmin.domain.SecurityUserCommand;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * @author Andrew Woods
 *         Date: Apr 23, 2010
 */
public class ManageSecurityUsersController extends AbstractCommandController {

    private final Logger log = LoggerFactory.getLogger(
        ManageSecurityUsersController.class);

    private DuracloudUserDetailsService userDetailsService;

    public ManageSecurityUsersController() {
        setCommandClass(SecurityUserCommand.class);
        setCommandName("users");
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Object command,
                                  BindException errors) throws Exception {
        SecurityUserCommand cmd = (SecurityUserCommand) command;
        cmd.setUsers(this.userDetailsService.getUsers());
        String verb = cmd.getVerb();
        String username = cmd.getUsername();
        String password = cmd.getPassword();

        if (verb.equalsIgnoreCase("add")) {
            List<String> grants = new ArrayList<String>();
            grants.add("ROLE_USER");
            SecurityUserBean user = new SecurityUserBean(username,
                                                         password,
                                                         grants);
            cmd.addUser(user);
            log.info("added user {}", user.getUsername());
            return saveAndReturnModel(cmd, user);

        } else if (verb.equalsIgnoreCase("remove")) {
            SecurityUserBean user = getUser(cmd);
            cmd.removeUser(user.getUsername());
            log.info("removed user {}", username);
            return saveAndReturnModel(cmd, user);

        } else if (verb.equalsIgnoreCase("modify")) {
            SecurityUserBean user = getUser(cmd);
            user.setPassword(password);
            log.info("updated password for user {}", username);
            return saveAndReturnModel(cmd, user);

        } else {
            return new ModelAndView("admin-manager", "users", cmd.getUsers());
        }
    }

    private SecurityUserBean getUser(SecurityUserCommand cmd) {
        for (SecurityUserBean user : cmd.getUsers()) {
            if (user.getUsername().equals(cmd.getUsername())) {
                return user;
            }
        }
        return null;
    }

    private ModelAndView saveAndReturnModel(SecurityUserCommand cmd,
                                            SecurityUserBean user)
        throws Exception {
        pushUpdates(cmd.getUsers());
        return new ModelAndView("jsonView", "user", user);
    }

    private void pushUpdates(List<SecurityUserBean> users) throws Exception {
        // update duradmin.
        userDetailsService.setUsers(users);
        log.debug("pushed updates to user details service");

        // update durastore.
        Application durastore = getDuraStoreApp();
        durastore.setSecurityUsers(users);
        log.debug("pushed updates to durastore");

        // update duraservice
        Application duraservice = getDuraServiceApp();
        duraservice.setSecurityUsers(users);
        log.debug("pushed updates to duraservice");
    }

    private Application getDuraStoreApp() {
        String host = DuradminConfig.getDuraStoreHost();
        String port = DuradminConfig.getDuraStorePort();
        String ctxt = DuradminConfig.getDuraStoreContext();
        return new Application(host, port, ctxt);
    }

    private Application getDuraServiceApp() {
        String host = DuradminConfig.getDuraServiceHost();
        String port = DuradminConfig.getDuraServicePort();
        String ctxt = DuradminConfig.getDuraServiceContext();
        return new Application(host, port, ctxt);
    }

    public DuracloudUserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(DuracloudUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}