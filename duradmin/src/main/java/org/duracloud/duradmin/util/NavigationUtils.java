/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecutionOutcome;

public class NavigationUtils {

    public static void setReturnTo(HttpServletRequest request,
                                   MutableAttributeMap map) {
        String returnTo = request.getParameter(NavigationUtils.RETURN_TO_KEY);
        map.put(NavigationUtils.RETURN_TO_KEY, returnTo);
    }

    public static String getReturnTo(FlowExecutionOutcome outcome) {
        String returnTo =
                outcome.getOutput().getString(NavigationUtils.RETURN_TO_KEY);
        if (returnTo != null) {
            return "serverRelative:" + returnTo;
        } else {
            return null;
        }
    }

    public static String getReturnTo(HttpServletRequest request) {
        Object returnTo = request.getParameter(RETURN_TO_KEY);
        return (returnTo != null ? returnTo.toString() : "");
    }

    public static final String RETURN_TO_KEY = "returnTo";

}
