/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.changenotifier;

/**
 * Enumerator that defines supported Notifier types.
 *
 * @author Andy Foster
 * Date: March 10th, 2020
 */
public enum NotifierType {
    RABBITMQ("RabbitMQ"), SNS("SNS");

    private final String text;

    private NotifierType(String t) {
        text = t;
    }

    /**
     * Returns the NotifierType from a string value. Defaults to SNS.
     *
     * @param string
     * @return NotifierType
     */
    public static NotifierType fromString(String t) {
        for (NotifierType nType : values()) {
            if (nType.text.equalsIgnoreCase(t) ||
                nType.name().equalsIgnoreCase(t)) {
                return nType;
            }
        }
        return SNS;
    }

    @Override
    public String toString() {
        return text;
    }
}
