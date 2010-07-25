/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import org.duracloud.common.error.DuraCloudRuntimeException;
import static org.duracloud.common.util.ExceptionUtil.getStackTraceAsString;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import static org.duracloud.security.xml.SecurityUsersDocumentBinding.createSecurityUsersFrom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import java.io.IOException;
import java.util.List;

/**
 * This class initializes the application security users based on the xml
 * body of the servlet request.
 *
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class InitSecurityUsersController extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(InitSecurityUsersController.class);

    private DuracloudUserDetailsService userDetailsService;

    public InitSecurityUsersController(DuracloudUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
        throws Exception {

        String method = request.getMethod();
        if (!method.equalsIgnoreCase("POST")) {
            respond(response, "unsupported: " + method, SC_METHOD_NOT_ALLOWED);
            return null;
        }

        ServletInputStream xml = request.getInputStream();
        if (xml != null) {
            try {
                List<SecurityUserBean> users = createSecurityUsersFrom(xml);
                userDetailsService.setUsers(users);
                respond(response, "Initialization Successful\n", SC_OK);

            } catch (Exception e) {
                respond(response,
                        getStackTraceAsString(e),
                        SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            respond(response, "no users in request\n", SC_BAD_REQUEST);
        }
        return null;
    }

    private void respond(HttpServletResponse response, String msg, int status) {
        ServletOutputStream output;
        try {
            output = response.getOutputStream();

        } catch (IOException e) {
            String err = "Error getting servlet output stream";
            log.error(err, e);
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            throw new DuraCloudRuntimeException(err, e);
        }

        try {
            output.write(msg.getBytes());
            output.flush();

        } catch (IOException e) {
            String err = "Error writing to servlet output stream";
            log.error(err, e);
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            throw new DuraCloudRuntimeException(err, e);
        }

        response.setStatus(status);
    }

}
