/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.duracloud.security.xml.SecurityUsersDocumentBinding.createSecurityUsersFrom;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
@Path("/security")
@Component
public class SecurityRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(SecurityRest.class);

    private DuracloudUserDetailsService userDetailsService;
    private RestUtil restUtil;

    @Autowired
    public SecurityRest(DuracloudUserDetailsService userDetailsService, RestUtil restUtil) {
        this.userDetailsService = userDetailsService;
        this.restUtil = restUtil;
    }

    @POST
    public Response initializeUsers() {
        RestUtil.RequestContent content = null;
        try {
            content = restUtil.getRequestContent(request, headers);
            List<SecurityUserBean> users = createSecurityUsersFrom(content.getContentStream());
            userDetailsService.setUsers(users);

            String responseText = "Initialization Successful\n";
            return Response.ok(responseText, TEXT_PLAIN).build();
            
        } catch (Exception e) {
            log.error("Error: initializing users.", e);
            String entity = e.getMessage() == null ? "null" : e.getMessage();
            return Response.serverError().entity(entity).build();
        }
    }
}
