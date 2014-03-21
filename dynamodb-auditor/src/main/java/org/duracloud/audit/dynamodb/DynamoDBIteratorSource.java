package org.duracloud.audit.dynamodb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.duracloud.common.collection.IteratorSource;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

/**
 * An {code IteratorSource} for DynamoDB queries.
 * @author Daniel Bernstein
 *
 * @param <T>
 */
public class DynamoDBIteratorSource<T> implements IteratorSource<T> {
    
    private AmazonDynamoDBClient client;
    private QueryRequest request;
    private DynamoDBMapper mapper;
    private Class<? extends T> clazz;
    
    public DynamoDBIteratorSource(AmazonDynamoDBClient client, QueryRequest request, Class<? extends T> clazz) {
        super();
        this.client = client;
        this.request = request;
        mapper = new DynamoDBMapper(this.client);
        this.clazz = clazz;
    }

    @Override
    public Collection<T> getNext() {
        if(request == null){
            return null;
        }
        QueryResult result = this.client.query(this.request);
        if(result.getCount() > 0){
            List<? extends T> list =  mapper.marshallIntoObjects(clazz, result.getItems());
            
            Map<String,AttributeValue> lastEvaluatedKey = result.getLastEvaluatedKey();
            if(lastEvaluatedKey != null){
                this.request.setExclusiveStartKey(lastEvaluatedKey);
            }else{
                this.request = null;
            }
            
            return (Collection<T>)list;
            
        }else{
            request = null;
            return null;
        }
    }
}
