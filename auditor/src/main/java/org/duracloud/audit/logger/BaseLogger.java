/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.logger;

import java.util.Map;

/**
 * @author Bill Branan
 *         Date: 3/21/14
 */
public abstract class BaseLogger {

    protected String buildLogMessage(Map<String,String> props) {
        StringBuilder builder = new StringBuilder();
        for(String key : props.keySet()) {
            add(builder, key, props.get(key));
        }
        return builder.toString();
    }

    protected void add(StringBuilder builder, String key, String value) {
        builder.append(key).append("=").append(value).append(" ");
    }

}
