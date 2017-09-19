/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;

/**
 * @author Andrew Woods
 *         Date: 3/11/11
 */
public class AmazonNotificationFactory implements NotificationFactory {

    private static final Logger log = LoggerFactory.getLogger(
        AmazonNotificationFactory.class);

    private AmazonSimpleEmailService emailService;
    private Map<String, Emailer> emailerMap = new HashMap<String, Emailer>();

    @Override
    public void initialize(String accessKey, String secretKey) {
		if (StringUtils.isNotBlank(accessKey)) {
			log.debug("initialize email service with provided credentials");
			emailService = AmazonSimpleEmailServiceAsyncClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
					.build();
		} else {
			log.debug("initialize email service using the default AWS Default Credentials Chain provider");
			emailService = AmazonSimpleEmailServiceAsyncClientBuilder.defaultClient();
		}
    }

    @Override
    public Emailer getEmailer(String fromAddress) {
        if (null == fromAddress ||
            !EmailValidator.getInstance().isValid(fromAddress)) {
            String msg = "fromAddress not valid notification: " + fromAddress;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (null == emailService) {
            String msg = "Emailer service !initialized.";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        Emailer emailer = emailerMap.get(fromAddress);
        if (null == emailer) {
            emailer = new AmazonEmailer(emailService, fromAddress);
            emailerMap.put(fromAddress, emailer);
        }

        return emailer;
    }

}
