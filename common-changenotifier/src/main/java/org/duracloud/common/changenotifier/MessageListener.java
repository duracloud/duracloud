/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.changenotifier;

import com.amazonaws.services.sqs.model.Message;

/**
 * @author Daniel Bernstein
 */
public interface MessageListener {
    void onMessage(Message message);

    void onMessage(String message);
}
