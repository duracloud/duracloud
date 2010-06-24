/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


public class BaseTestSpring extends AbstractDependencyInjectionSpringContextTests {

    /*
     * This class is not yet used.
     * ...will be for integration testing.
     *
     */

    @Override
    protected String[] getConfigLocations()
    {
        setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
        return new String[]{"file:src/main/webapp/WEB-INF/config/duracloud-app-config.xml"};
    }

}
