package com.socion.backend.aws;

import com.socion.backend.config.AppContext;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;

@Component
@Service
public class AwsConfigServiceImpl implements AwsConfigService {

    @Autowired
    AppContext appContext;

    public AmazonS3 awsS3Configuration() {
        AWSCredentials credentials = new BasicAWSCredentials(
                appContext.getAwsAccessKey(),
                appContext.getAwsSecretKey()
        );

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_SOUTH_1)
                .build();

    }

    public PutObjectResult putProfileCardObjectInAwsS3(String pathOfCertificate, String userId, AmazonS3 amazonS3) {

        String bucketName = appContext.getAwsS3BucketName();

        PutObjectRequest request1 = new PutObjectRequest(bucketName + "/profile-card", userId, new File(pathOfCertificate));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("x-amz-meta-title", "UserCard");
        request1.setMetadata(metadata);
        return amazonS3.putObject(request1);


    }


}
