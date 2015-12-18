/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.db;

import java.text.MessageFormat;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class DataSourceConfigurer {
    public DataSourceConfigurer(BasicDataSource dataSource, String databaseName, String host, String port, String username, String password){
        dataSource.setUrl(MessageFormat.format("jdbc:mysql://{0}:{1}/{2}" +
            "?characterEncoding=utf8" +
            "&characterSetResults=utf8",
           host,
           port,
           databaseName));
        dataSource.setUsername(username);
        dataSource.setPassword(password);
    }
}
