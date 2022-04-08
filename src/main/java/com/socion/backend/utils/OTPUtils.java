package com.socion.backend.utils;

import com.socion.backend.config.AppContext;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.costandusagereport.model.AWSRegion;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OTPUtils {

    @Autowired
    AppContext appContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(OTPUtils.class);

    public void sendOTP(String phoneNumber, String message) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(appContext.getAwsAccessKey(), appContext.getAwsSecretKey());
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(AWSRegion.UsWest2.toString())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();

        Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));

        PublishRequest request = new PublishRequest();
        request.withMessage(message)
                .withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes);
        PublishResult result = snsClient.publish(request);
        LOGGER.info(result.getMessageId());
    }
}

// AWS region was hard coded here to UsWest2 .It had to be replaced with ap-south-1.
