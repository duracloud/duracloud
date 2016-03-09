/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.appconfig.domain.Application;
import org.duracloud.duradmin.config.DuradminConfig;
import org.duracloud.duradmin.domain.SecurityUserCommand;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Andrew Woods
 *         Date: Apr 23, 2010
 * @deprecated This class may no longer be needed as the "Administration" tab
 *             is now read-only.
 */
@Deprecated
@Controller
public class ManageSecurityUsersController  {

    private final Logger log = LoggerFactory.getLogger(
        ManageSecurityUsersController.class);

    private DuracloudUserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public ManageSecurityUsersController(
        @Qualifier("userDetailsSvc") DuracloudUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    
    @RequestMapping(value="/admin")
    public ModelAndView handle(HttpServletRequest request,
                                  HttpServletResponse response,
                                  SecurityUserCommand cmd,
                                  BindingResult result) throws Exception {
        cmd.setUsers(this.userDetailsService.getUsers());
        String verb = cmd.getVerb();
        String username = cmd.getUsername();
        String password = passwordEncoder.encodePassword(cmd.getPassword(), null);
        
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
        user.setPassword("*********");
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

    }

    private Application getDuraStoreApp() {
        String host = DuradminConfig.getDuraStoreHost();
        String port = DuradminConfig.getDuraStorePort();
        String ctxt = DuradminConfig.getDuraStoreContext();
        return new Application(host, port, ctxt);
    }

    public DuracloudUserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(DuracloudUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
