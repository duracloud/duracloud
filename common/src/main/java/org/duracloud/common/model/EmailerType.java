/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

/**
 * Enumerator that defines supported Emailer types.
 *
 * @author Andy Foster
 * Date: March 10th, 2020
 */
public enum EmailerType {
    SMTP("SMTP"), SES("SES");

    private final String text;

    private EmailerType(String t) {
        text = t;
    }

    /**
     * Returns the EmailerType from a string value. Defaults to SES.
     *
     * @param string
     * @return EmailerType
     */
    public static EmailerType fromString(String t) {
        for (EmailerType eType : values()) {
            if (eType.text.equalsIgnoreCase(t) ||
                eType.name().equalsIgnoreCase(t)) {
                return eType;
            }
        }
        return SES;
    }

    @Override
    public String toString() {
        return text;
    }
}
