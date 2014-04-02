/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

/**
 * @author Andrew Woods
 *         Date: Mar 14, 2010
 */
public class VoterUtil {

    /**
     * This is small debug utility available to voters in this package.
     */
    protected static String debugText(String heading,
                                      Authentication auth,
                                      Collection<ConfigAttribute> config,
                                      Object resource,
                                      int decision) {
        StringBuilder sb = new StringBuilder(heading);
        sb.append(": ");
        if (auth != null) {
            sb.append(auth.getName());
        }
        if (config != null) {
            Collection<ConfigAttribute> atts = config;
            if (atts != null && atts.size() > 0) {
                sb.append(" [");
                for (ConfigAttribute att : atts) {
                    sb.append(att.getAttribute());
                    sb.append(",");
                }
                sb.replace(sb.length() - 1, sb.length(), "]");
            }
        }
        if (resource != null) {
            sb.append(" resource: [");
            sb.append(resource.toString());
            sb.append("]");
        }
        sb.append(" => decision: " + decision);

        return sb.toString();
    }
}
