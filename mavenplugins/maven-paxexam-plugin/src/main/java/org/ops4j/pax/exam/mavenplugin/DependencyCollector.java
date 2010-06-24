/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.ops4j.pax.exam.mavenplugin;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;

public interface DependencyCollector {

    public abstract List<Dependency> getDependencies()
            throws MojoExecutionException;

}