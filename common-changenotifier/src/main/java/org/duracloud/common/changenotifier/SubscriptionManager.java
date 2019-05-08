package org.duracloud.common.changenotifier;

public interface SubscriptionManager {

    public void addListener(MessageListener listener);

    public void connect();

    public void disconnect();
}
