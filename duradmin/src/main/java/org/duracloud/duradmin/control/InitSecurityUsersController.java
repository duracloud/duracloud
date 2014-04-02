/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.duracloud.common.util.ExceptionUtil.getStackTraceAsString;
import static org.duracloud.security.xml.SecurityUsersDocumentBinding.createSecurityUsersFrom;

import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class initializes the application security users based on the xml
 * body of the servlet request.
 *
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */

@Controller
public class InitSecurityUsersController {

    private final Logger log = LoggerFactory.getLogger(InitSecurityUsersController.class);

    private DuracloudUserDetailsService userDetailsService;
    
    @Autowired
    public InitSecurityUsersController(DuracloudUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @RequestMapping(value="/security", method=RequestMethod.POST)
    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
        throws Exception {

        ServletInputStream xml = request.getInputStream();
        if (xml != null) {
            try {
                List<SecurityUserBean> users = createSecurityUsersFrom(xml);
                userDetailsService.setUsers(users);
                return respond(response, "Initialization Successful\n", SC_OK);

            } catch (Exception e) {
                return respond(response,
                        getStackTraceAsString(e),
                        SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            return respond(response, "no users in request\n", SC_BAD_REQUEST);
        }
    }

    private ModelAndView respond(HttpServletResponse response, String msg, int status) {
        response.setStatus(status);
        log.info("writing response: status = " + status + "; msg = " + msg);
        return new ModelAndView("jsonView", "response", msg);
    }


}
