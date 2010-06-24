/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.ops4j.pax.exam.mavenplugin;

/**
 * Just extend the SeviceMix mojo to generate the depends file.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @extendsPlugin depends-maven-plugin
 * @goal generate-depends-file
 * @phase generate-resources
 * @requiresDependencyResolution test
 * @since 0.5.0, April 23, 2009
 */
public class GenerateDependsFileMojo
    extends org.apache.servicemix.tooling.depends.GenerateDependsFileMojo
{

}
