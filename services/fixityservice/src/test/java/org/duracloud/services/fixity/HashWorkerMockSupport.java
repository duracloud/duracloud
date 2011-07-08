/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.services.fixity.results.HashVerifierResult;
import org.duracloud.services.fixity.results.ServiceResult;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashWorkerMockSupport {

    /**
     * This nested class matches true if the actual ServiceResult has the same
     * "getEntry()" value as the expected ServiceResult.
     * <p/>
     * It is used by including the eqResult(...) method below in the EasyMock
     * expectation.
     */
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
        EasyMock.reportMatcher(new HashWorkerMockSupport.ServiceResultEquals(
            serviceResult));
        return null;
    }

    /**
     * This nested class matches true if the actual ServiceResult contains
     * spaceId/status elements that match either the expected validCase or
     * invalidCase maps.
     * <p/>
     * It is used by including the matchesResult(...) method below in the
     * EasyMock expectation.
     */
    public static class HashVerifierResultMatches implements IArgumentMatcher {
        private Map<String, String> validCase;
        private Map<String, String> invalidCase;

        public HashVerifierResultMatches(Map<String, String> validCase,
                                         Map<String, String> invalidCase) {
            this.validCase = validCase;
            this.invalidCase = invalidCase;
        }

        public boolean matches(Object actual) {
            if (!(actual instanceof ServiceResult)) {
                return false;
            }
            ServiceResult actualResult = (ServiceResult) actual;
            Assert.assertNotNull(actualResult.getEntry());

            Map<String, String> spaceToStatus = new HashMap<String, String>();
            BufferedReader br = new BufferedReader(new StringReader(actualResult.getEntry()));
            String line = null;
            String[] parts;
            try {
                while ((line = br.readLine()) != null) {
                    parts = line.split("\t");
                    Assert.assertEquals(5, parts.length);
                    spaceToStatus.put(parts[0].trim(), parts[4].trim());
                }

            } catch (IOException e) {
                System.err.println("Error reading: " + actualResult.getEntry());
            }

            Assert.assertEquals(validCase.size(), spaceToStatus.size());
            int validCount = 0;
            int invalidCount = 0;
            for (String spaceId : spaceToStatus.keySet()) {
                String validStatus = validCase.get(spaceId);
                String invalidStatus = invalidCase.get(spaceId);
                String status = spaceToStatus.get(spaceId);

                Assert.assertNotNull(validStatus);
                Assert.assertNotNull(invalidStatus);
                Assert.assertNotNull(status);

                if (status.equals(validStatus)) {
                    validCount++;
                }
                if (status.equals(invalidStatus)) {
                    invalidCount++;
                }
            }

            Assert.assertTrue(validCount + " or " + invalidCount,
                              validCount == validCase.size() ||
                                  invalidCount == invalidCase.size());

            Assert.assertFalse(validCount + " and " + invalidCount,
                               validCount == validCase.size() &&
                                   invalidCount == invalidCase.size());

            if (validCount == validCase.size()) {
                Assert.assertEquals(true, actualResult.isSuccess());
            } else {
                Assert.assertEquals(false, actualResult.isSuccess());
            }

            return true;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("matchesResult(");
            buffer.append(HashVerifierResult.class.getName());
            buffer.append(")");
        }
    }

    public static ServiceResult matchesResult(Map<String, String> validCase,
                                              Map<String, String> invalidCase) {
        HashVerifierResultMatches matcher = new HashVerifierResultMatches(
            validCase,
            invalidCase);
        EasyMock.reportMatcher(matcher);
        return null;
    }
}
