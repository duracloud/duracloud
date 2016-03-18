/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.util.AccountIdUtil;

/**
 * This  http request filter parses the account id from X-FORWARDED-HOST attribute
 * and stores it in an http request attribute for use by downstream processes.
 * It is possible to override the account id by using the 
 * -Dorg.duracloud.accountid system property.  You may want to use this override when using 
 * an instance without a DNS name matching the duracloud convention (ie <accountid>.duracloud.org).
 *
 * @author Daniel Bernstein
 *
 */
public class DuraCloudRequestContextFilter implements Filter {
    
    static final String X_FORWARDED_HOST_HEADER = "X-FORWARDED-HOST";
    static final String ACCOUNT_ID_OVERRIDE_SYSTEM_PROP_KEY =
        Constants.ACCOUNT_ID_ATTRIBUTE;
    private String accountIdOverride = null;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        accountIdOverride = System.getProperty(ACCOUNT_ID_OVERRIDE_SYSTEM_PROP_KEY);
    }
    
    @Override
    public void destroy() {}
    
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
                             throws IOException,
                                 ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String accountId = accountIdOverride;
        String host = httpRequest.getHeader(X_FORWARDED_HOST_HEADER);
        if(host == null){
            host = request.getServerName();
        }

        if(accountId == null) {
            accountId = AccountIdUtil.extractAccountIdFromHost(host);
        }
        httpRequest.setAttribute(Constants.ACCOUNT_ID_ATTRIBUTE, accountId);
        httpRequest.setAttribute(Constants.SERVER_HOST, host);
        httpRequest.setAttribute(Constants.SERVER_PORT, httpRequest.getServerPort());
        chain.doFilter(request, response);
    }
    
}
