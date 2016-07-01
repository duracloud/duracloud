/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.event;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.json.JaxbJsonSerializer;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class AccountChangeEvent {
    public static enum EventType {
        ACCOUNT_CHANGED,
        USERS_CHANGED,
        STORAGE_PROVIDERS_CHANGED,
        ALL_ACCOUNTS_CHANGED, 
        STORAGE_PROVIDER_CACHE_ON_NODE_CHANGED;   //indicates that storage provider data cached on a node changed
    };
    
    private EventType eventType;
    private String accountId;
    /*
     * The host of originator of the event.
     */
    private String sourceHost;
    
    public AccountChangeEvent(){}

    /**
     * 
     * @param eventType
     * @param accountId
     */
    public AccountChangeEvent(EventType eventType, String accountId, String sourceHost){
        this.eventType = eventType;
        this.accountId = accountId;
        this.sourceHost = sourceHost;
    }
    
    public EventType getEventType(){
        return this.eventType;
    }
    
    public String getAccountId(){
        return this.accountId;
    }
    
    public String getSourceHost() {
        return sourceHost;
    }

    
    public static String serialize(AccountChangeEvent accountChangeEvent) {
        JaxbJsonSerializer<AccountChangeEvent> serializer = new JaxbJsonSerializer<>(
                AccountChangeEvent.class);
        try {
            return serializer.serialize(accountChangeEvent);
        }catch(Exception ex){
                throw new DuraCloudRuntimeException(ex);
        }
    }

    public static AccountChangeEvent deserialize(String eventString) {
        JaxbJsonSerializer<AccountChangeEvent> serializer = new JaxbJsonSerializer<>(
                AccountChangeEvent.class);
        try {
            return serializer.deserialize(eventString);
        }catch(Exception ex){
                throw new DuraCloudRuntimeException(ex);
        }
    }
}
