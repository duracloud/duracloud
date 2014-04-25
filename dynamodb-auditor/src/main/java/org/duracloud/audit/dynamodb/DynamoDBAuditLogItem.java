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
    public static final String ACCOUNT_ATTRIBUTE = "Account";
    public static final String STORE_ID_ATTRIBUTE = "StoreId";
    public static final String SPACE_ID_ATTRIBUTE = "SpaceId";
    public static final String CONTENT_ID_ATTRIBUTE = "ContentId";
    private static final String CONTENT_MD5_ATTRIBUTE = "ContentMd5";
    public static final String MIMETYPE_ATTRIBUTE = "Mimetype";
    public static final String CONTENT_SIZE_ATTRIBUTE = "ContentSize";
    private static final String USERNAME_ATTRIBUTE = "Username";
    private static final String ACTION_ATTRIBUTE = "Action";
    public static final String CONTENT_PROPERTIES_ATTRIBUTE = "ContentProperties";
    private static final String SPACE_ACLS_PROPERTY_ATTRIBUTE = "SpaceACLs";

    public static final String ID_ATTRIBUTE = "ItemId";
    public static final String TIMESTAMP_ATTRIBUTE = "TimeStamp";
    public static final String ACCOUNT_SPACE_ID_HASH_ATTRIBUTE =
        "AccountSpaceIdHash";

    
    public static final String TABLE_NAME = "AuditLog";
    public static final String ACCOUNT_SPACE_ID_INDEX = "AccountSpaceIdIndex";
    public static final String ID_TIMESTAMP_INDEX = "IdTimeStamp";
    private static final String SOURCE_SPACE_ID_ATTRIBUTE = "SourceSpaceId";
    private static final String SOURCE_CONTENT_ID_ATTRIBUTE = "SourceContentId";
    
    
    public static String[] PROJECTED_ATTRIBUTES = {
        ACCOUNT_ATTRIBUTE,
        STORE_ID_ATTRIBUTE,
        SPACE_ID_ATTRIBUTE,
        CONTENT_ID_ATTRIBUTE,
        CONTENT_MD5_ATTRIBUTE,
        MIMETYPE_ATTRIBUTE,
        CONTENT_SIZE_ATTRIBUTE,
        USERNAME_ATTRIBUTE, 
        ACTION_ATTRIBUTE
    };
    
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
    private String contentProperties; 
    private String spaceAcls;
    private String sourceSpaceId;
    private String sourceContentId;
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
                            String contentProperties,
                            String spaceAcls,
                            String sourceSpaceId,
                            String sourceContentId,
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
        setContentProperties(contentProperties);
        setSourceSpaceId(sourceSpaceId);
        setSourceContentId(sourceContentId);
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

    @DynamoDBAttribute(attributeName = CONTENT_MD5_ATTRIBUTE)
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

    @DynamoDBAttribute(attributeName = ACTION_ATTRIBUTE)
    @Override
    public String getAction() {
        return action;
    }

    @DynamoDBAttribute(attributeName = USERNAME_ATTRIBUTE)
    @Override
    public String getUsername() {
        return this.username;
    }

    @DynamoDBAttribute(attributeName = CONTENT_PROPERTIES_ATTRIBUTE)
    public String getContentProperties() {
        return contentProperties;
    }

    @DynamoDBAttribute(attributeName = SPACE_ACLS_PROPERTY_ATTRIBUTE)
    public String getSpaceAcls() {
        return spaceAcls;
    }

    @DynamoDBAttribute(attributeName = SOURCE_SPACE_ID_ATTRIBUTE)
    @Override
    public String getSourceSpaceId() {
        return this.sourceSpaceId;
    }

    @DynamoDBAttribute(attributeName = SOURCE_CONTENT_ID_ATTRIBUTE)
    @Override
    public String getSourceContentId() {
        return this.sourceContentId;
    }

    public void setContentProperties(String contentProperties) {
        this.contentProperties = contentProperties;
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
    
    public void setSourceSpaceId(String sourceSpaceId) {
        this.sourceSpaceId = sourceSpaceId;
    }
    
    public void setSourceContentId(String sourceContentId) {
        this.sourceContentId = sourceContentId;
    }

}
