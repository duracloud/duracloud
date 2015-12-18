/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest;

import org.duracloud.common.constant.Constants;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class DuraCloudRequestContextUtil {
    /**
     * Retrieves the account associated with the current thread.
     * @return
     */
    public String getAccountId(){
        return (String)getAttribute(Constants.ACCOUNT_ID_ATTRIBUTE);
    }
    
    private Object getAttribute(String attributeName){
        return RequestContextHolder.currentRequestAttributes()
        .getAttribute(attributeName,
                      RequestAttributes.SCOPE_REQUEST);
    }


    public int  getPort(){
        return (Integer)getAttribute(Constants.SERVER_PORT);
    }

    public String  getHost(){
        return (String)getAttribute(Constants.SERVER_HOST);
    }

}
