/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import org.duracloud.common.model.Securable;
import org.duracloud.common.model.Credential;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.error.NoUserLoggedInException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * This class wraps calls to securable objects and logs them in if there is
 * a valid credential in the security context.
 * Otherwise, it logs the object out.
 *
 * @author Andrew Woods
 *         Date: Mar 28, 2010
 */
public class SecurityAdvice implements MethodBeforeAdvice {

    private final Logger log = LoggerFactory.getLogger(SecurityAdvice.class);

    private SecurityContextUtil securityContextUtil;

    public SecurityAdvice(SecurityContextUtil util) {
        this.securityContextUtil = util;
    }

    public void before(Method method, Object[] objects, Object o)
        throws Throwable {
        String methodClass = method.getDeclaringClass().getCanonicalName();
        String methodName = method.getName();
        log.debug("securing call: '" + methodClass + "." + methodName + "'");

        if (!Securable.class.isAssignableFrom(o.getClass())) {
            log.warn("Unexpected object filtered: " + o.getClass().getName());
            return;
        }

        Securable securable = (Securable) o;
        try {
            Credential credential = securityContextUtil.getCurrentUser();
            securable.login(credential);

        } catch (NoUserLoggedInException e) {
            log.info("No user currently logged in.");
            securable.logout();
        }
    }
}
