/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.filter;

import org.duracloud.syncui.service.SyncConfigurationManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The root application configuration class.
 * 
 * @author Daniel Bernstein
 * 
 */
@Component("setupCheckFilter")
public class SetupCheckFilter implements Filter, ApplicationContextAware {

    private static final String SETUP_PATH = "/setup";
    private static final String[] EXEMPT_PATHS =
        { SETUP_PATH, "/init", "/ajax" };

    private SyncConfigurationManager syncConfigurationManager;

    private ApplicationContext applicationContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException,
            ServletException {

        if (this.syncConfigurationManager == null) {
            this.syncConfigurationManager =
                applicationContext.getBean(SyncConfigurationManager.class);
        }

        HttpServletRequest hrequest = (HttpServletRequest) request;

        if (this.syncConfigurationManager.isConfigurationComplete()
            || isExemptPath((hrequest))) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse hresponse = (HttpServletResponse) response;
            hresponse.sendRedirect(hrequest.getContextPath() + SETUP_PATH);
        }

    }

    private boolean isExemptPath(HttpServletRequest r) {
        String path = r.getRequestURI();
        String c = r.getContextPath();

        for (String p : EXEMPT_PATHS) {
            if (path.startsWith(c + p)) {
                return true;
            }
        }

        return false;

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;

    }

}
