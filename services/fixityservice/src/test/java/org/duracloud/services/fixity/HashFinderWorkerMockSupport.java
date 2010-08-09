/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.services.fixity.results.ServiceResult;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashFinderWorkerMockSupport {
    public static class ServiceResultEquals implements IArgumentMatcher {
        private ServiceResult expectedResult;

        public ServiceResultEquals(ServiceResult expected) {
            this.expectedResult = expected;
        }

        public boolean matches(Object actual) {
            if (!(actual instanceof ServiceResult)) {
                return false;
            }
            ServiceResult actualResult = (ServiceResult) actual;
            Assert.assertNotNull(actualResult.getEntry());
            Assert.assertEquals(expectedResult.getEntry(),
                                actualResult.getEntry());
            return true;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqResult(");
            buffer.append(expectedResult.getClass().getName());
            buffer.append(")");
        }
    }

    public static ServiceResult eqResult(ServiceResult serviceResult) {
        EasyMock.reportMatcher(new HashFinderWorkerMockSupport.ServiceResultEquals(
            serviceResult));
        return null;
    }
}
