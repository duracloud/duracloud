/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.domain;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class FixityServiceOptions {
    private final Logger log = LoggerFactory.getLogger(FixityServiceOptions.class);

    private String mode;
    private String hashApproach;
    private String salt;
    private String failFast;
    private String storeId;
    private String providedListingSpaceIdA;
    private String providedListingSpaceIdB;
    private String providedListingContentIdA;
    private String providedListingContentIdB;
    private String targetSpaceId;
    private String outputSpaceId;
    private String outputContentId;
    private String reportContentId;


    public enum Mode {
        ALL_IN_ONE_LIST("all-in-one-for-list"),
        ALL_IN_ONE_SPACE("all-in-one-for-space"),
        GENERATE_LIST("generate-for-list"),
        GENERATE_SPACE("generate-for-space"),
        COMPARE("compare"),
        UNKNOWN("unknown");

        private String key;

        private Mode(String key) {
            this.key = key;
        }

        public static Mode fromString(String arg) {
            for (Mode m : values()) {
                if (m.key.equalsIgnoreCase(arg) ||
                    m.name().equalsIgnoreCase(arg)) {
                    return m;
                }
            }
            throw new DuraCloudRuntimeException("Invalid mode: '" + arg + "'");
        }

        public String getKey() {
            return key;
        }
    }

    public enum HashApproach {
        STORED, GENERATED, SALTED;

        private static HashApproach fromString(String ha) {
            if (ha != null) {
                for (HashApproach approach : HashApproach.values()) {
                    if (approach.name().equalsIgnoreCase(ha)) {
                        return approach;
                    }
                }
            }
            throw new DuraCloudRuntimeException("Invalid hash-approach: " + ha);
        }
    }

    public FixityServiceOptions(String mode,
                                String hashApproach,
                                String salt,
                                String failFast,
                                String storeId,
                                String providedListingSpaceIdA,
                                String providedListingSpaceIdB,
                                String providedListingContentIdA,
                                String providedListingContentIdB,
                                String targetSpaceId,
                                String outputSpaceId,
                                String outputContentId,
                                String reportContentId) {
        this.mode = mode;
        this.hashApproach = hashApproach;
        this.salt = salt;
        this.failFast = failFast;
        this.storeId = storeId;
        this.providedListingSpaceIdA = providedListingSpaceIdA;
        this.providedListingSpaceIdB = providedListingSpaceIdB;
        this.providedListingContentIdA = providedListingContentIdA;
        this.providedListingContentIdB = providedListingContentIdB;
        this.targetSpaceId = targetSpaceId;
        this.outputSpaceId = outputSpaceId;
        this.outputContentId = outputContentId;
        this.reportContentId = reportContentId;

    }

    public boolean needsToHash() {
        Mode mode = getMode();
        return mode.equals(Mode.ALL_IN_ONE_LIST) ||
            mode.equals(Mode.ALL_IN_ONE_SPACE) ||
            mode.equals(Mode.GENERATE_LIST) || mode.equals(Mode.GENERATE_SPACE);
    }

    public boolean needsToAutoGenerateHashListing() {
        return getMode().equals(Mode.ALL_IN_ONE_SPACE);
    }

    public boolean needsToCompare() {
        Mode mode = getMode();
        return mode.equals(Mode.ALL_IN_ONE_LIST) ||
            mode.equals(Mode.ALL_IN_ONE_SPACE) || mode.equals(Mode.COMPARE);
    }

    public void verify() {
        // must have a mode
        Mode mode = getMode();
        if (mode == null || mode.equals(Mode.UNKNOWN)) {
            throwInvalidOptions("Invalid mode");
        }

        switch (mode) {
            case ALL_IN_ONE_LIST:
                verifyNotNull(hashApproach, "hashApproach");
                //verifyNotNull(salt, "salt");
                //verifyNotNull(failFast, "failFast");
                verifyNotNull(storeId, "storeId");
                verifyNotNull(providedListingSpaceIdA, "proListingSpaceIdA");
                verifyNotNull(providedListingContentIdA, "prListingContentIdA");
                verifyNull(providedListingSpaceIdB, "providedListingSpaceIdB");
                verifyNull(providedListingContentIdB, "proListingContentIdB");
                verifyNull(targetSpaceId, "targetSpaceId");
                verifyNotNull(outputSpaceId, "outputSpaceId");
                verifyNotNull(outputContentId, "outputContentId");
                verifyNotNull(reportContentId, "reportContentId");
                break;
            case ALL_IN_ONE_SPACE:
                verifyNotNull(hashApproach, "hashApproach");
                //verifyNotNull(salt, "salt");
                //verifyNotNull(failFast, "failFast");
                verifyNotNull(storeId, "storeId");
                verifyNotNull(providedListingSpaceIdA, "proListingSpaceIdA");
                verifyNotNull(providedListingContentIdA, "prListingContentIdA");
                verifyNull(providedListingSpaceIdB, "providedListingSpaceIdB");
                verifyNull(providedListingContentIdB, "proListingContentIdB");
                verifyNotNull(targetSpaceId, "targetSpaceId");
                verifyNotNull(outputSpaceId, "outputSpaceId");
                verifyNotNull(outputContentId, "outputContentId");
                verifyNotNull(reportContentId, "reportContentId");
                break;
            case GENERATE_LIST:
                verifyNotNull(hashApproach, "hashApproach");
                //verifyNotNull(salt, "salt");
                //verifyNull(failFast, "failFast");
                verifyNotNull(storeId, "storeId");
                verifyNotNull(providedListingSpaceIdA, "proListingSpaceIdA");
                verifyNotNull(providedListingContentIdA, "prListingContentIdA");
                verifyNull(providedListingSpaceIdB, "providedListingSpaceIdB");
                verifyNull(providedListingContentIdB, "proListingContentIdB");
                verifyNull(targetSpaceId, "targetSpaceId");
                verifyNotNull(outputSpaceId, "outputSpaceId");
                verifyNotNull(outputContentId, "outputContentId");
                verifyNull(reportContentId, "reportContentId");
                break;
            case GENERATE_SPACE:
                verifyNotNull(hashApproach, "hashApproach");
                //verifyNotNull(salt, "salt");
                //verifyNull(failFast, "failFast");
                verifyNotNull(storeId, "storeId");
                verifyNull(providedListingSpaceIdA, "providedListingSpaceIdA");
                verifyNull(providedListingContentIdA, "proListingContentIdA");
                verifyNull(providedListingSpaceIdB, "providedListingSpaceIdB");
                verifyNull(providedListingContentIdB, "proListingContentIdB");
                verifyNotNull(targetSpaceId, "targetSpaceId");
                verifyNotNull(outputSpaceId, "outputSpaceId");
                verifyNotNull(outputContentId, "outputContentId");
                verifyNull(reportContentId, "reportContentId");
                break;
            case COMPARE:
                verifyNull(hashApproach, "hashApproach");
                //verifyNull(salt, "salt");
                //verifyNotNull(failFast, "failFast");
                verifyNotNull(storeId, "storeId");
                verifyNotNull(providedListingSpaceIdA, "proListingSpaceIdA");
                verifyNotNull(providedListingContentIdA, "prListingContentIdA");
                verifyNotNull(providedListingSpaceIdB, "proListingSpaceIdB");
                verifyNotNull(providedListingContentIdB, "prListingContentIdB");
                verifyNull(targetSpaceId, "targetSpaceId");
                verifyNotNull(outputSpaceId, "outputSpaceId");
                verifyNull(outputContentId, "outputContentId");
                verifyNotNull(reportContentId, "reportContentId");
                break;
            default:
                throwInvalidOptions("Unexpected mode");
        }

    }

    private void verifyNull(String value, String name) {
        log.info("verifyNull(" + name + ") is currently disabled.");
//        if (null != value) {
//            throwInvalidOptions(name + " should be null");
//        }
    }

    private void verifyNotNull(String value, String name) {
        if (null == value) {
            throwInvalidOptions(name + " should not be null");
        }
    }

    private void throwInvalidOptions(String msg) {
        StringBuilder sb = new StringBuilder("Invalid FixityServiceOptions: ");
        sb.append(msg);
        sb.append(System.getProperty("line.separator"));
        sb.append(toString());
        log.error(sb.toString());
        throw new DuraCloudRuntimeException(sb.toString());
    }

    public Mode getMode() {
        return Mode.fromString(mode);
    }

    public HashApproach getHashApproach() {
        return HashApproach.fromString(hashApproach);
    }

    public String getSalt() {
        return salt;
    }

    public Boolean isFailFast() {
        return new Boolean(failFast);
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProvidedListingSpaceIdA() {
        return providedListingSpaceIdA;
    }

    public String getProvidedListingSpaceIdB() {
        return providedListingSpaceIdB;
    }

    public String getProvidedListingContentIdA() {
        return providedListingContentIdA;
    }

    public String getProvidedListingContentIdB() {
        return providedListingContentIdB;
    }

    public String getTargetSpaceId() {
        return targetSpaceId;
    }

    public String getOutputSpaceId() {
        return outputSpaceId;
    }

    public String getOutputContentId() {
        return outputContentId;
    }

    public String getReportContentId() {
        return reportContentId;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceOptions: [");
        sb.append("mode=");
        sb.append(mode);
        sb.append(", ");
        sb.append("hashApproach=");
        sb.append(hashApproach);
        sb.append(", ");
        sb.append("salt=");
        sb.append(salt);
        sb.append(", ");
        sb.append("failFast=");
        sb.append(failFast);
        sb.append(", ");
        sb.append("storeId=");
        sb.append(storeId);
        sb.append(", ");
        sb.append("providedListingSpaceIdA=");
        sb.append(providedListingSpaceIdA);
        sb.append(", ");
        sb.append("providedListingSpaceIdB=");
        sb.append(providedListingSpaceIdB);
        sb.append(", ");
        sb.append("providedListingContentIdA=");
        sb.append(providedListingContentIdA);
        sb.append(", ");
        sb.append("providedListingContentIdB=");
        sb.append(providedListingContentIdB);
        sb.append(", ");
        sb.append("targetSpaceId=");
        sb.append(targetSpaceId);
        sb.append(", ");
        sb.append("outputSpaceId=");
        sb.append(outputSpaceId);
        sb.append(", ");
        sb.append("outputContentId=");
        sb.append(outputContentId);
        sb.append(", ");
        sb.append("reportContentId=");
        sb.append(reportContentId);
        sb.append("]");
        return sb.toString();
    }
}
