/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import static org.duracloud.security.vote.VoterUtil.*;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.duracloud.security.domain.HttpVerb;
import org.duracloud.security.util.SecurityUtil;
import org.duracloud.snapshot.SnapshotConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

/**
 * This class makes security decisions for Task related calls.
 *
 * @author Daniel Bernstein
 *         Date: 07/27/2015
 */
public class TaskAccessVoter implements AccessDecisionVoter {

    private final Logger log =
        LoggerFactory.getLogger(TaskAccessVoter.class);

    /**
     * This method always returns true because all configAttributes are able
     * to be handled by this voter.
     *
     * @param configAttribute any att
     * @return true
     */
    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    /**
     * This methods returns true if the arg class is an instance of or
     * subclass of FilterInvocation.
     * No other classes can be handled by this voter.
     *
     * @param aClass to be analyized for an AuthZ vote.
     * @return true if is an instance of or subclass of FilterInvocation
     */
    @Override
    public boolean supports(Class aClass) {
        return FilterInvocation.class.isAssignableFrom(aClass);
    }

    /**
     * 
     *
     * @param auth     principal seeking AuthZ
     * @param resource that is under protection
     * @param config   access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    public int vote(Authentication auth,
                    Object resource,
                    Collection config) {
        String label = "TaskAccessVoter";
        if (resource != null && !supports(resource.getClass())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        FilterInvocation invocation = (FilterInvocation) resource;
        HttpServletRequest httpRequest = invocation.getHttpRequest();
        if (null == httpRequest) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }
        
        HttpVerb verb = HttpVerb.valueOf(httpRequest.getMethod());
        String path = httpRequest.getPathInfo();
        
        
        if((!path.startsWith("/task/") && !path.startsWith("task/"))){
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }
        
        
        //do not allow non-root users to execute snapshot restorations.
        if(HttpVerb.POST.equals(verb) && 
            path.endsWith("/" + SnapshotConstants.RESTORE_SNAPSHOT_TASK_NAME) &&
            !SecurityUtil.isRoot(auth)){
             log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        } else {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_GRANTED;
        }
    }
}
