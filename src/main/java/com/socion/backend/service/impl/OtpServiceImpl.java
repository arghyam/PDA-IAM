package com.socion.backend.service.impl;

import com.socion.backend.config.AppContext;
import com.socion.backend.service.OtpService;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Component
public class OtpServiceImpl implements OtpService {

    @Autowired
    AppContext appContext;

    @Override
    public void sendOtp() {

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(appContext.getAwsAccessKey(), appContext.getAwsSecretKey());
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();

        Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));

        PublishRequest request = new PublishRequest();
        request.withMessage("test")
                .withPhoneNumber("")
                .withMessageAttributes(smsAttributes);
        snsClient.publish(request);

    }
}
