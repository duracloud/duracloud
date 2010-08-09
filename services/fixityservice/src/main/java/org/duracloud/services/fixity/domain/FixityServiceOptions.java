/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.domain;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class FixityServiceOptions {

    private String mode;
    private String hashApproach;
    private String salt;
    private Boolean failFast;
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
                                Boolean failFast,
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

    public boolean needsToCompare() {
        Mode mode = getMode();
        return mode.equals(Mode.ALL_IN_ONE_LIST) ||
            mode.equals(Mode.ALL_IN_ONE_SPACE) || mode.equals(Mode.COMPARE);
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
        return failFast;
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
}
