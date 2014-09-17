/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.db.repo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
@Configuration
@ImportResource("classpath:/org/duracloud/mill/jpa/mill-jpa-config.xml")
public class MillJpaRepoConfig {
}
