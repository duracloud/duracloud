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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.duracloud.common.model.AclType;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.security.util.AuthorizationHelper;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Verifies that the caller is authorized to make call to retrieve snapshot
 * detail info. See durastore/src/main/webapp/WEB-INF/config/aop-config.xml for
 * pointcut matching pattern. This pointcut is designed intercept calls to TaskProvider
 * instances.
 *
 * @author Daniel Bernstein Date: 11/16/2015
 */
public class SnapshotAccessAdvice implements MethodBeforeAdvice, Ordered {
    private Logger log = LoggerFactory.getLogger(SnapshotAccessAdvice.class);
    private int order = 0;
    private AuthorizationHelper authHelper;

    public SnapshotAccessAdvice(AuthorizationHelper authHelper) {
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

        if (!taskName.matches("get-snapshot[^s]?(-.+)?")) {
            return;
        }

        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (this.authHelper.hasAdmin(auth)) {
            return;
        }

        String taskParams = (String) args[1];

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser =
            mapper.getJsonFactory().createJsonParser(taskParams);
        JsonNode node = parser.readValueAsTree();
        String rawSnapshotId = node.get("snapshotId").asText();
        SnapshotIdentifier snapshotId =
            SnapshotIdentifier.parseSnapshotId(rawSnapshotId);
        String sourceSpaceId = snapshotId.getSpaceId();
        String sourceStoreId = snapshotId.getStoreId();

        Map<String, AclType> acls =
            this.authHelper.getSpaceACLs(sourceStoreId, sourceSpaceId);
        if (!this.authHelper.hasReadAccess(auth.getName(), acls)
            && !this.authHelper.groupsHaveReadAccess(auth, acls)) {
            log.error(auth.getName() + " is not authorized to view " + rawSnapshotId);
            throw new UnauthorizedException("You are not authorized to access snapshot "
                                            + rawSnapshotId + ".");
        } else {
            log.debug("successfully authorized {} to view {}",
                      auth.getName(),
                      rawSnapshotId);
        }

    }
}
