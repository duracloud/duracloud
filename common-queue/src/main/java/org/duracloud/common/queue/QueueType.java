/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue;

/**
 * Enumerator that defines supported Queue types.
 *
 * @author Andy Foster
 * Date: March 10th, 2020
 */
public enum QueueType {
    RABBITMQ("RabbitMQ"), SQS("SQS");

    private final String text;

    private QueueType(String t) {
        text = t;
    }

    /**
     * Returns the QueueType from a string value. Defaults to SQS.
     *
     * @param string
     * @return QueueType
     */
    public static QueueType fromString(String t) {
        for (QueueType qType : values()) {
            if (qType.text.equalsIgnoreCase(t) ||
                qType.name().equalsIgnoreCase(t)) {
                return qType;
            }
        }
        return SQS;
    }

    @Override
    public String toString() {
        return text;
    }
}
