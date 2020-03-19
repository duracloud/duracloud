/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import org.duracloud.common.util.DuracloudConfigBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author: Daniel Bernstein
 * Date: March 21, 2010
 */
@Controller
public class LoginController {

    @Autowired
    private DuracloudConfigBean duracloudConfigBean;

    @RequestMapping("/login")
    public ModelAndView presentLogin() throws Exception {
        String amaUrl = duracloudConfigBean.getAmaUrl();

        ModelAndView login = new ModelAndView("login");
        login.addObject("amaUrl", amaUrl);
        return login;
    }

}
