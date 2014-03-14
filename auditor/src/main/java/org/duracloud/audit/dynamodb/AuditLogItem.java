package org.duracloud.audit.dynamodb;

import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
/**
 * Representings a log event line in the database
 * @author Daniel Bernstein
 * 
 */
@DynamoDBTable(tableName = AuditLogItem.TABLE_NAME)
public class AuditLogItem {
    public static final String STORE_ID_ATTRIBUTE = "StoreId";
    public static final String CONTENT_ID_ATTRIBUTE = "ContentId";
    public static final String TABLE_NAME = "AuditLog";
    public static final String ACCOUNT_SPACE_ID_INDEX = "AccountSpaceIdIndex";
    
    public static final String ID_ATTRIBUTE = "ItemId";
    public static final String ACCOUNT_ATTRIBUTE = "Account";
    public static final String SPACE_ID_ATTRIBUTE = "SpaceId";
    public static final String ACCOUNT_SPACE_ID_HASH_ATTRIBUTE =
        "AccountSpaceIdHash";

    private String id,accountSpaceIdHash, account, storeId, spaceId, contentId, contentMd5,
        username, action;

    private Date timestamp;

    public AuditLogItem( String id, 
                            String accountSpaceIdHash,
                            String account, 
                            String storeId, 
                            String spaceId, 
                            String contentId,
                            String contentMd5, 
                            String username, 
                            String action, 
                            Date timestamp) {
        setId(id);
        setAccountSpaceIdHash(accountSpaceIdHash);
        setAccount(account);
        setStoreId(storeId);
        setSpaceId(spaceId);
        setContentId(contentId);
        setContentMd5(contentMd5);
        setUsername(username);
        setAction(action);
        setTimestamp(timestamp);
    }

    public AuditLogItem() {
    }

    @DynamoDBHashKey(attributeName = ID_ATTRIBUTE)
    public String getId() {
        return id;
    }

    @DynamoDBRangeKey(attributeName = ACCOUNT_ATTRIBUTE)
    public String getAccount() {
        return this.account;
    }

    @DynamoDBAttribute(attributeName = STORE_ID_ATTRIBUTE)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ACCOUNT_SPACE_ID_INDEX)
    public String getStoreId() {
        return this.storeId;
    }

    @DynamoDBAttribute(attributeName = SPACE_ID_ATTRIBUTE)
    public String getSpaceId() {
        return this.spaceId;
    }

    @DynamoDBAttribute(attributeName = CONTENT_ID_ATTRIBUTE)
    public String getContentId() {
        return contentId;
    }

    @DynamoDBAttribute(attributeName = "ContentMd5")
    public String getContentMd5() {
        return contentMd5;
    }

    @DynamoDBAttribute(attributeName = "Action")
    public String getAction() {
        return action;
    }


    @DynamoDBAttribute(attributeName = "Username")
    public String getUsername() {
        return this.username;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = ACCOUNT_SPACE_ID_INDEX, attributeName = ACCOUNT_SPACE_ID_HASH_ATTRIBUTE)
    public String
        getAccountSpaceIdHash() {
        return this.accountSpaceIdHash;
    }
 
    @DynamoDBAttribute(attributeName = "Timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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
}
