package org.duracloud.audit.dynamodb;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
/**
 * A utility class for creating and dropping the audit log table and indices.
 * 
 * @author Daniel Bernstein
 *
 */
public class DatabaseUtil {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);
    
    public static final String DEFAULT_LOCAL_ENDPOINT = "http://localhost:8000";

    private static final String STRING_ATTRIBUTE_TYPE = "S";

    public static void drop(AmazonDynamoDBClient client) {
        try {

            DeleteTableRequest request =
                new DeleteTableRequest().withTableName(DynamoDBAuditLogItem.TABLE_NAME);

            DeleteTableResult result = client.deleteTable(request);

            log.info(result.toString());
        } catch (AmazonServiceException ase) {
            System.err.println("Failed to delete table "
                + DynamoDBAuditLogItem.TABLE_NAME + " " + ase);
        }
    }

    public static void create(AmazonDynamoDBClient client) {
        String tableName = DynamoDBAuditLogItem.TABLE_NAME;

        try {
            String hashKeyName = DynamoDBAuditLogItem.ID_ATTRIBUTE;
            String rangeKeyName = DynamoDBAuditLogItem.TIMESTAMP_ATTRIBUTE;
            long readCapacityUnits = 10;
            long writeCapacityUnits = 5;
            log.info("Creating table " + tableName);
            ArrayList<KeySchemaElement> ks = new ArrayList<KeySchemaElement>();
            ArrayList<AttributeDefinition> attributeDefinitions =
                new ArrayList<AttributeDefinition>();

            ks.add(new KeySchemaElement().withAttributeName(hashKeyName)
                                         .withKeyType(KeyType.HASH));
            attributeDefinitions.add(new AttributeDefinition().withAttributeName(hashKeyName)
                                                              .withAttributeType(STRING_ATTRIBUTE_TYPE));

            ks.add(new KeySchemaElement().withAttributeName(rangeKeyName)
                                         .withKeyType(KeyType.RANGE));
            attributeDefinitions.add(new AttributeDefinition().withAttributeName(rangeKeyName)
                                                              .withAttributeType("N"));

            // Provide initial provisioned throughput values as Java long data
            // types
            ProvisionedThroughput provisionedthroughput =
                new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                                           .withWriteCapacityUnits(writeCapacityUnits);

            CreateTableRequest request =
                new CreateTableRequest().withTableName(tableName)
                                        .withKeySchema(ks)
                                        .withProvisionedThroughput(provisionedthroughput);

            ArrayList<GlobalSecondaryIndex> globalSecondaryIndexes =
                new ArrayList<GlobalSecondaryIndex>();

            attributeDefinitions.add(new AttributeDefinition().withAttributeName(DynamoDBAuditLogItem.ACCOUNT_SPACE_ID_HASH_ATTRIBUTE)
                                                              .withAttributeType(STRING_ATTRIBUTE_TYPE));

            request.setAttributeDefinitions(attributeDefinitions);

            addGlobalSecondaryIndex(DynamoDBAuditLogItem.ACCOUNT_SPACE_ID_INDEX,
                                    DynamoDBAuditLogItem.ACCOUNT_SPACE_ID_HASH_ATTRIBUTE,
                                    DynamoDBAuditLogItem.TIMESTAMP_ATTRIBUTE,
                                    provisionedthroughput,
                                    globalSecondaryIndexes);

            addGlobalSecondaryIndex(DynamoDBAuditLogItem.ID_TIMESTAMP_INDEX,
                                    DynamoDBAuditLogItem.ID_ATTRIBUTE,
                                    DynamoDBAuditLogItem.TIMESTAMP_ATTRIBUTE,
                                    provisionedthroughput,
                                    globalSecondaryIndexes);


            request.setGlobalSecondaryIndexes(globalSecondaryIndexes);

            CreateTableResult result = client.createTable(request);
            log.info("result: " + result.getTableDescription());
        } catch (AmazonServiceException ase) {
            System.err.println("Failed to create table "
                + tableName + " " + ase);
        }

    }

    private static void
        addGlobalSecondaryIndex(String indexName,
                                String hashKey,
                                String rangeKey,
                                ProvisionedThroughput provisionedthroughput,
                                ArrayList<GlobalSecondaryIndex> indexes) {
        GlobalSecondaryIndex gsi =
            new GlobalSecondaryIndex().withIndexName(indexName)
                                      .withProvisionedThroughput(provisionedthroughput)
                                      .withProjection(new Projection().withProjectionType(ProjectionType.INCLUDE)
                                                                      .withNonKeyAttributes(Arrays.asList(DynamoDBAuditLogItem.PROJECTED_ATTRIBUTES)))
                                      .withKeySchema(new KeySchemaElement(hashKey,
                                                                          KeyType.HASH),
                                                     new KeySchemaElement(rangeKey,
                                                                          KeyType.RANGE));
        indexes.add(gsi);

    }

}
