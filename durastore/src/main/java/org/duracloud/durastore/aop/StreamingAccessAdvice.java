/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import java.lang.reflect.Method;
import java.util.Map;

import org.duracloud.StorageTaskConstants;
import org.duracloud.common.model.AclType;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.s3storageprovider.dto.GetUrlTaskParameters;
import org.duracloud.security.util.AuthorizationHelper;
import org.duracloud.storage.provider.TaskProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Verifies that the caller is authorized to make call to stream content.
 * See durastore/src/main/webapp/WEB-INF/config/aop-config.xml for
 * pointcut matching pattern. This pointcut is designed intercept calls to TaskProvider
 * instances.
 *
 * @author Daniel Bernstein Date: 12/07/2015
 */
public class StreamingAccessAdvice implements MethodBeforeAdvice, Ordered {
    private Logger log = LoggerFactory.getLogger(StreamingAccessAdvice.class);
    private int order = 0;
    private AuthorizationHelper authHelper;

    public StreamingAccessAdvice(AuthorizationHelper authHelper) {
        this.authHelper = authHelper;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void before(Method method, Object[] args, Object target)
        throws Throwable {
        String taskName = (String) args[0];

        if (!taskName.matches(StorageTaskConstants.GET_HLS_URL_TASK_NAME) &&
            !taskName.matches(StorageTaskConstants.GET_SIGNED_COOKIES_URL_TASK_NAME)) {
            return;
        }

        TaskProvider taskProvider = (TaskProvider) target;
        String storeId = taskProvider.getStoreId();

        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (this.authHelper.hasAdmin(auth)) {
            return;
        }

        String taskParams = (String) args[1];
        GetUrlTaskParameters params = GetUrlTaskParameters.deserialize(taskParams);
        String spaceId = params.getSpaceId();
        Map<String, AclType> acls =
            this.authHelper.getSpaceACLs(storeId, spaceId);
        if (!this.authHelper.hasReadAccess(auth.getName(), acls)
            && !this.authHelper.groupsHaveReadAccess(auth, acls)) {
            log.error(auth.getName() + " is not authorized to view content in " + spaceId);
            throw new UnauthorizedException("You are not authorized to access space "
                                            + spaceId + ".");
        } else {
            log.debug("successfully authorized {} to view {}",
                      auth.getName(),
                      spaceId);
        }

    }
}
