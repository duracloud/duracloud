/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;


public class SnsSubscriptionManager {
    private Logger log = LoggerFactory.getLogger(SnsSubscriptionManager.class);

    private AmazonSQSClient sqsClient;
    private AmazonSNSClient snsClient;
    private String topicArn;
    private String queueName;
    private String queueUrl;
    private String subscriptionArn;
    private boolean initialized = false;
    private List<MessageListener> messageListeners = new ArrayList<>();
    public SnsSubscriptionManager(AmazonSQSClient sqsClient,
                                  AmazonSNSClient snsClient,
                                  String topicArn,
                                  String queueName) {
        this.topicArn = topicArn;
        this.queueName = queueName;
        this.sqsClient = sqsClient;
        this.snsClient = snsClient;
    }
    
    public void addListener(MessageListener listener){
        this.messageListeners.add(listener);
    }
    
    public synchronized void connect(){
        if(initialized){
            throw new DuraCloudRuntimeException("this manager is already connected");
        }
        //create sqs queue
        log.info("creating sqs queue");
        CreateQueueRequest request = new CreateQueueRequest(this.queueName);
        Map<String,String> attributes = new HashMap<String,String>();
        attributes.put("ReceiveMessageWaitTimeSeconds", "20");
        request.setAttributes(attributes);
        CreateQueueResult result = sqsClient.createQueue(request);
        this.queueUrl = result.getQueueUrl();
        log.info("sqs queue created: {}", this.queueUrl);
        String queueArnKey = "QueueArn";
        GetQueueAttributesResult getQueueAttrResult =
            sqsClient.getQueueAttributes(this.queueUrl,
                                         Arrays.asList(queueArnKey));
        log.info("subscribing {} to {}", queueUrl, topicArn);
        SubscribeResult subscribeResult = this.snsClient.subscribe(topicArn,
                                 "sqs",
                                 getQueueAttrResult.getAttributes()
                                                   .get(queueArnKey));
        this.subscriptionArn = subscribeResult.getSubscriptionArn();
        log.info("subscription complete: {}", this.subscriptionArn);
        
        //subscribe queue to topic
        this.initialized = true;
        
        startPolling();
        
        
    }
    
    private void startPolling() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(initialized){
                    try {
                        ReceiveMessageResult result = sqsClient.receiveMessage(queueUrl);
                        List<Message> messages = result.getMessages();
                        for(Message message : messages){
                            dispatch(message);
                        }
                    }catch(Exception ex){
                        log.warn("failed to receive messages: " + ex.getMessage(), ex);
                    }
                }
            }

        }, "sqs-long-poller").start();
    }

    private void dispatch(Message message) {
        for(MessageListener listener : messageListeners){
            try{
                listener.onMessage(message);
            }catch(Exception ex){
                log.error("failed to dispatch message " + message
                          + " to "
                          + listener
                          + "due to "
                          + ex.getMessage(),
                          ex);
            }
        }
    }

    public void disconnect(){
        if(!this.initialized){
            throw new DuraCloudRuntimeException("this manager is already disconnected");
        }
        log.info("disconnecting");
        log.info("unsubscribing {}", this.subscriptionArn);
        this.snsClient.unsubscribe(this.subscriptionArn);
        log.info("unsubscribed {}", this.subscriptionArn);
        log.info("deleting queue {}", this.subscriptionArn);
        this.sqsClient.deleteQueue(this.queueUrl);
        log.info("deleted queue {}", this.subscriptionArn);
        this.initialized = false;
        log.info("disconnection complete");
    }
}
