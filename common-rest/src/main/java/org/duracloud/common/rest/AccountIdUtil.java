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
public class AccountIdUtil {
    /**
     * Retrieves the account associated with the current thread.
     * @return
     */
    public String getAccountId(){
        String accountId = (String)RequestContextHolder.currentRequestAttributes()
            .getAttribute(Constants.ACCOUNT_ID_ATTRIBUTE,
                          RequestAttributes.SCOPE_REQUEST);
        return accountId;
    }
}
