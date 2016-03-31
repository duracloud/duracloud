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
        ALL_ACCOUNTS_CHANGED
    };
    
    private EventType eventType;
    private String accountId;
    
    public AccountChangeEvent(){}
    /**
     * 
     * @param eventType
     * @param accountId
     */
    public AccountChangeEvent(EventType eventType, String accountId){
        this.eventType = eventType;
        this.accountId = accountId;
    }
    
    public AccountChangeEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType(){
        return this.eventType;
    }
    
    public String getAccountId(){
        return this.accountId;
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
