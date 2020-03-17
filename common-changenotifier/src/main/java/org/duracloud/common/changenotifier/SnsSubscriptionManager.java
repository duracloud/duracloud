/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.changenotifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueNameExistsException;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.WaitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Bernstein
 */
public class SnsSubscriptionManager implements SubscriptionManager {
    private Logger log = LoggerFactory.getLogger(SnsSubscriptionManager.class);

    private AmazonSQS sqsClient;
    private AmazonSNS snsClient;
    private String topicArn;
    private String queueName;
    private String queueUrl;
    private String subscriptionArn;
    private boolean initialized = false;
    private List<MessageListener> messageListeners = new ArrayList<>();

    public SnsSubscriptionManager(AmazonSQS sqsClient,
                                  AmazonSNS snsClient,
                                  String topicArn,
                                  String queueName) {
        this.topicArn = topicArn;
        this.queueName = queueName;
        this.sqsClient = sqsClient;
        this.snsClient = snsClient;
    }

    @Override
    public void addListener(MessageListener listener) {
        this.messageListeners.add(listener);
    }

    @Override
    public synchronized void connect() {
        if (initialized) {
            throw new DuraCloudRuntimeException("this manager is already connected");
        }

        //create sqs queue
        log.info("creating sqs queue");
        CreateQueueRequest request = new CreateQueueRequest(this.queueName);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("ReceiveMessageWaitTimeSeconds", "20");
        request.setAttributes(attributes);
        CreateQueueResult result;
        try {
            result = sqsClient.createQueue(request);
            this.queueUrl = result.getQueueUrl();
            log.info("sqs queue created: {}", this.queueUrl);
        } catch (QueueNameExistsException ex) {
            log.info("queue with name {} already exists.");
            GetQueueUrlResult queueUrlResult = sqsClient.getQueueUrl(this.queueName);
            this.queueUrl = queueUrlResult.getQueueUrl();
            log.info("sqs queue url retrieved: {}", this.queueUrl);
        }

        String queueArnKey = "QueueArn";
        GetQueueAttributesResult getQueueAttrResult =
            sqsClient.getQueueAttributes(this.queueUrl,
                                         Arrays.asList(queueArnKey));
        log.info("subscribing {} to {}", queueUrl, topicArn);

        String queueArn = getQueueAttrResult.getAttributes().get(queueArnKey);

        SubscribeResult subscribeResult = this.snsClient.subscribe(topicArn,
                                                                   "sqs",
                                                                   queueArn);
        this.subscriptionArn = subscribeResult.getSubscriptionArn();

        Map<String, String> queueAttributes = new HashMap<String, String>();
        queueAttributes.put("Policy", generateSqsPolicyForTopic(queueArn, topicArn));

        sqsClient.setQueueAttributes(new SetQueueAttributesRequest(queueUrl, queueAttributes));

        log.info("subscription complete: {}", this.subscriptionArn);

        //subscribe queue to topic
        this.initialized = true;

        startPolling();

    }

    private String generateSqsPolicyForTopic(String queueArn, String topicArn) {
        String policy =
            "{ " +
            "  \"Version\":\"2008-10-17\"," +
            "  \"Id\":\"" + queueArn + "/policyId\"," +
            "  \"Statement\": [" +
            "    {" +
            "        \"Sid\":\"" + queueArn + "/statementId\"," +
            "        \"Effect\":\"Allow\"," +
            "        \"Principal\":{\"AWS\":\"*\"}," +
            "        \"Action\":\"SQS:SendMessage\"," +
            "        \"Resource\": \"" + queueArn + "\"," +
            "        \"Condition\":{" +
            "            \"StringEquals\":{\"aws:SourceArn\":\"" + topicArn + "\"}" +
            "        }" +
            "    }" +
            "  ]" +
            "}";

        return policy;
    }

    private void startPolling() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (initialized) {
                    try {
                        ReceiveMessageResult result = sqsClient.receiveMessage(queueUrl);
                        List<Message> messages = result.getMessages();
                        for (Message message : messages) {
                            dispatch(message);
                            log.debug("{} dispatched", message);
                            sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
                            log.debug("{} deleted", message);
                        }

                    } catch (Exception ex) {
                        log.warn("failed to poll queue: " + ex.getMessage(), ex);
                    }
                }
            }

        }, "sqs-long-poller").start();
    }

    private void dispatch(Message message) {
        log.debug("dispatching message {}", message);
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessage(message);
            } catch (Exception ex) {
                log.error("failed to dispatch message " + message
                          + " to "
                          + listener
                          + "due to "
                          + ex.getMessage(),
                          ex);
            }
        }
    }

    @Override
    public void disconnect() {
        if (!this.initialized) {
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
        //Redeploys will fail due to amazon sqs requirement to wait
        //60 seconds before recreating a queue exit without waiting.
        WaitUtil.wait(60);
        log.info("disconnection complete");
    }
}
