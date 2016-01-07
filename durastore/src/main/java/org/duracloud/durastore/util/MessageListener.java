package org.duracloud.durastore.util;

import com.amazonaws.services.sqs.model.Message;

public interface MessageListener {
    void onMessage(Message message);
}
