package org.duracloud.audit.dynamodb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.duracloud.audit.AuditLogItem;
import org.duracloud.common.error.DuraCloudRuntimeException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
/**
 * Representings a log event line in the database
 * @author Daniel Bernstein
 * 
 */
@DynamoDBTable(tableName = DynamoDBAuditLogItem.TABLE_NAME)
public class DynamoDBAuditLogItem implements AuditLogItem {
    public static final String STORE_ID_ATTRIBUTE = "StoreId";
    public static final String CONTENT_ID_ATTRIBUTE = "ContentId";
    public static final String TABLE_NAME = "AuditLog";
    public static final String ACCOUNT_SPACE_ID_INDEX = "AccountSpaceIdIndex";
    
    public static final String ID_ATTRIBUTE = "ItemId";
    public static final String ACCOUNT_ATTRIBUTE = "Account";
    public static final String MIMETYPE_ATTRIBUTE = "Mimetype";
    public static final String CONTENT_SIZE_ATTRIBUTE = "ContentSize";

    public static final String SPACE_ID_ATTRIBUTE = "SpaceId";
    public static final String ACCOUNT_SPACE_ID_HASH_ATTRIBUTE =
        "AccountSpaceIdHash";
    public static final String TIMESTAMP_ATTRIBUTE = "TimeStamp";
    public static final String PROPERTIES_ATTRIBUTE = "Properties";

    private String id;
    private String accountSpaceIdHash;
    private String account;
    private String storeId;
    private String spaceId;
    private String contentId;
    private String contentMd5;
    private String username;
    private String action;
    private String mimetype;
    private String contentSize;
    private String propertiesStr; //a json string representation of a Map<String,String>.
    
    private Long timestamp;

    public DynamoDBAuditLogItem( String id, 
                            String accountSpaceIdHash,
                            String account, 
                            String storeId, 
                            String spaceId, 
                            String contentId,
                            String contentMd5, 
                            String mimetype, 
                            String contentSize, 
                            String username, 
                            String action, 
                            Map<String,String> properties,
                            long timestamp) {
        setId(id);
        setAccountSpaceIdHash(accountSpaceIdHash);
        setAccount(account);
        setStoreId(storeId);
        setSpaceId(spaceId);
        setContentId(contentId);
        setContentMd5(contentMd5);
        setMimetype(mimetype);
        setContentSize(contentSize);
        setUsername(username);
        setAction(action);
        setProperties(properties);
        setTimestamp(timestamp);
    }

    public DynamoDBAuditLogItem() {
    }

    @DynamoDBHashKey(attributeName = ID_ATTRIBUTE)
    public String getId() {
        return id;
    }
    
    @DynamoDBRangeKey(attributeName = TIMESTAMP_ATTRIBUTE)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ACCOUNT_SPACE_ID_INDEX)
    public long getTimestamp() {
        return timestamp;
    }


    @DynamoDBAttribute(attributeName = ACCOUNT_ATTRIBUTE)
    @Override
    public String getAccount() {
        return this.account;
    }

    @DynamoDBAttribute(attributeName = STORE_ID_ATTRIBUTE)
    @Override
    public String getStoreId() {
        return this.storeId;
    }

    @DynamoDBAttribute(attributeName = SPACE_ID_ATTRIBUTE)
    @Override
    public String getSpaceId() {
        return this.spaceId;
    }

    @DynamoDBAttribute(attributeName = CONTENT_ID_ATTRIBUTE)
    @Override
    public String getContentId() {
        return contentId;
    }

    @DynamoDBAttribute(attributeName = "ContentMd5")
    @Override
    public String getContentMd5() {
        return contentMd5;
    }
    
    @DynamoDBAttribute(attributeName = CONTENT_SIZE_ATTRIBUTE)
    @Override
    public String getContentSize() {
        return this.contentSize;
    }

    @DynamoDBAttribute(attributeName = MIMETYPE_ATTRIBUTE)
    @Override
    public String getMimetype() {
        return this.mimetype;
    }

    @DynamoDBAttribute(attributeName = "Action")
    @Override
    public String getAction() {
        return action;
    }

    @DynamoDBAttribute(attributeName = "Username")
    @Override
    public String getUsername() {
        return this.username;
    }

    @DynamoDBAttribute(attributeName = PROPERTIES_ATTRIBUTE)
    public String getPropertiesStr() {
        return propertiesStr;
    }

    @DynamoDBIgnore
    @Override
    public Map<String, String> getProperties() {
        return convertProperties(this.propertiesStr);
    }
    
    private Map<String, String> convertProperties(String properties) {
        Map<String, String> result = null;
        if(properties != null){
            try {
                result = new ObjectMapper().readValue(properties,
                                                 HashMap.class);
            } catch (Exception e) {
                throw new DuraCloudRuntimeException(e.getMessage(),e);
            }        
        }
        return result;
    }

    public void setProperties(Map<String,String> properties){
        try {
            this.propertiesStr = new ObjectMapper().writeValueAsString(properties);
        } catch (Exception e) {
            throw new DuraCloudRuntimeException(e.getMessage(),e);
        }
    }
    
    @DynamoDBIndexHashKey(globalSecondaryIndexName = ACCOUNT_SPACE_ID_INDEX, attributeName = ACCOUNT_SPACE_ID_HASH_ATTRIBUTE)
    public String
        getAccountSpaceIdHash() {
        return this.accountSpaceIdHash;
    }
 
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAccountSpaceIdHash(String accountSpaceIdHash) {
        this.accountSpaceIdHash = accountSpaceIdHash;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }
    
    public void setContentSize(String contentSize) {
        this.contentSize = contentSize;
    }


    public void setPropertiesStr(String propertiesStr) {
        //verifies that properties are valid json.
        convertProperties(propertiesStr);
        this.propertiesStr = propertiesStr;
    }
}
