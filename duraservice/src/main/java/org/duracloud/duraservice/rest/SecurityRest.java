/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.security.DuracloudUserDetailsService;
import static org.duracloud.security.xml.SecurityUsersDocumentBinding.createSecurityUsersFrom;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.common.rest.RestUtil;

import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
@Path("/security")
public class SecurityRest extends BaseRest {

    private DuracloudUserDetailsService userDetailsService;

    public SecurityRest(DuracloudUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @POST
    public Response initializeUsers() {
        RestUtil.RequestContent content = null;
        try {
            RestUtil restUtil = new RestUtil();
            content = restUtil.getRequestContent(request, headers);
            List<SecurityUserBean> users = createSecurityUsersFrom(content.getContentStream());
            userDetailsService.setUsers(users);

            String responseText = "Initialization Successful\n";
            return Response.ok(responseText, BaseRest.TEXT_PLAIN).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}