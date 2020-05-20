package com.socion.backend.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;

public interface AwsConfigService {

    public AmazonS3 awsS3Configuration();

    public PutObjectResult putProfileCardObjectInAwsS3(String pathOfCard, String userId, AmazonS3 amazonS3);

}

