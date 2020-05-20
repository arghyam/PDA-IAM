package com.socion.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext {

    @Value("${keycloak.auth-server-url}")
    private String keyCloakServiceUrl;

    @Value("${admin-user-username}")
    private String adminUserName;

    @Value("${admin-user-password}")
    private String adminUserpassword;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak-client-id}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${client.granttype}")
    private String grantType;

    @Value("${salt-value}")
    private String saltValue;

    @Value("${iv-value}")
    private String ivValue;

    @Value("${secret-key}")
    private String secretKey;

    @Value("${key-size}")
    private int keySize;

    @Value("${iteration-count}")
    private int iterationCount;

    @Value("${registry-base-url}")
    private String registryBaseUrl;

    @Value("${mail-smtp-auth}")
    private String smtpAuth;

    @Value("${mail-smtp-starttls-enable}")
    private String smtpMailTls;

    @Value("${mail-smtp-host}")
    private String smtpHost;

    @Value("${smtp-gmail-com}")
    private String smtpMail;

    @Value("${mail-smtp-port}")
    private String smtpPort;

    @Value("${port}")
    private String port;

    @Value("${sourcemailid}")
    private String sourceEmail;

    @Value("${sourceemailpassword}")
    private String sourceEmailPassword;

    @Value("${subject-reset-pwd}")
    private String emailSubjectForResetPwd;

    @Value("${email-content-reset-pwd}")
    private String emailContentForResetPwd;

    @Value("${subject-for-sign-up}")
    private String emailSubjectForSignup;

    @Value("${email-content-for-sign-up}")
    private String emailContentForSignup;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${notification.server.url}")
    private String notificationServerUrl;

    @Value("${register-url-text}")
    private String registrationUrlText;

    @Value("${link-expiration-time-text}")
    private String linkExpirationTimeText;

    @Value("${link-expiration-time}")
    private int linkExpirationTime;

    @Value("${subject-for-email-udpate}")
    private String emailSubjectForEmailUpdate;

    @Value("${email-content-for-email-update}")
    private String emailContentForEmailUpdate;

    @Value("${email-update-url-text}")
    private String emailUpdateUrlText;

    @Value("${aws-accesskey}")
    private String awsAccessKey;

    @Value("${aws-secretkey}")
    private String awsSecretKey;

    @Value("${aws-region}")
    private String awsRegion;

    @Value("${entity.server.url}")
    private String entityServerUrl;

    @Value("${aws-s3-bucket-name}")
    private String awsS3BucketName;
    @Value("${aws-s3-url}")
    private String awsS3Url;

    @Value("${AppversionIosParticipantForced}")
    private String AppversionIosParticipantForced;

    @Value("${AppversionIosTrainerForced}")
    private String AppversionIosTrainerForced;

    @Value("${AppVersionAndroidParticipantForced}")
    private String AppVersionAndroidParticipantForced;

    @Value("${AppVesionAndroidTrainerForced}")
    private String AppVesionAndroidTrainerForced;

    @Value("${AppversionIosParticipantReco}")
    private String AppversionIosParticipantReco;

    @Value("${AppversionIosTrainerReco}")
    private String AppversionIosTrainerReco;

    @Value("${AppVersionAndroidParticipantReco}")
    private String AppVersionAndroidParticipantReco;

    @Value("${AppVesionAndroidTrainerReco}")
    private String AppVesionAndroidTrainerReco;

    @Value("${email-template-path}")
    private String emailTemplatePath;

    @Value("${email-template-path-phone-update}")
    private String emailTemplatePathPhoneUpdate;

    @Value("${email-update-success}")
    private String emailUpdateSuccess;

    @Value("${email-update-unsuccessful}")
    private String emailUpdateUnSuccessFul;

    public String getSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(String smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public String getSmtpMailTls() {
        return smtpMailTls;
    }

    public void setSmtpMailTls(String smtpMailTls) {
        this.smtpMailTls = smtpMailTls;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpMail() {
        return smtpMail;
    }

    public void setSmtpMail(String smtpMail) {
        this.smtpMail = smtpMail;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSourceEmail() {
        return sourceEmail;
    }

    public void setSourceEmail(String sourceEmail) {
        this.sourceEmail = sourceEmail;
    }

    public String getSourceEmailPassword() {
        return sourceEmailPassword;
    }

    public void setSourceEmailPassword(String sourceEmailPassword) {
        this.sourceEmailPassword = sourceEmailPassword;
    }


    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getKeyCloakServiceUrl() {
        return keyCloakServiceUrl;
    }

    public void setKeyCloakServiceUrl(String keyCloakServiceUrl) {
        this.keyCloakServiceUrl = keyCloakServiceUrl;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminUserpassword() {
        return adminUserpassword;
    }

    public void setAdminUserpassword(String adminUserpassword) {
        this.adminUserpassword = adminUserpassword;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getSaltValue() {
        return saltValue;
    }

    public void setSaltValue(String saltValue) {
        this.saltValue = saltValue;
    }

    public String getIvValue() {
        return ivValue;
    }

    public void setIvValue(String ivValue) {
        this.ivValue = ivValue;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public String getRegistryBaseUrl() {
        return registryBaseUrl;
    }

    public void setRegistryBaseUrl(String registryBaseUrl) {
        this.registryBaseUrl = registryBaseUrl;
    }

    public String getEmailContentForSignup() {
        return emailContentForSignup;
    }

    public void setEmailContentForSignup(String emailContentForSignup) {
        this.emailContentForSignup = emailContentForSignup;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getEmailSubjectForResetPwd() {
        return emailSubjectForResetPwd;
    }

    public void setEmailSubjectForResetPwd(String emailSubjectForResetPwd) {
        this.emailSubjectForResetPwd = emailSubjectForResetPwd;
    }

    public String getEmailContentForResetPwd() {
        return emailContentForResetPwd;
    }

    public void setEmailContentForResetPwd(String emailContentForResetPwd) {
        this.emailContentForResetPwd = emailContentForResetPwd;
    }

    public String getEmailSubjectForSignup() {
        return emailSubjectForSignup;
    }

    public void setEmailSubjectForSignup(String emailSubjectForSignup) {
        this.emailSubjectForSignup = emailSubjectForSignup;
    }

    public String getRegistrationUrlText() {
        return registrationUrlText;
    }

    public void setRegistrationUrlText(String registrationUrlText) {
        this.registrationUrlText = registrationUrlText;
    }

    public String getLinkExpirationTimeText() {
        return linkExpirationTimeText;
    }

    public void setLinkExpirationTimeText(String linkExpirationTimeText) {
        this.linkExpirationTimeText = linkExpirationTimeText;
    }

    public int getLinkExpirationTime() {
        return linkExpirationTime;
    }

    public void setLinkExpirationTime(int linkExpirationTime) {
        this.linkExpirationTime = linkExpirationTime;
    }

    public String getEmailSubjectForEmailUpdate() {
        return emailSubjectForEmailUpdate;
    }

    public void setEmailSubjectForEmailUpdate(String emailSubjectForEmailUpdate) {
        this.emailSubjectForEmailUpdate = emailSubjectForEmailUpdate;
    }

    public String getEmailContentForEmailUpdate() {
        return emailContentForEmailUpdate;
    }

    public void setEmailContentForEmailUpdate(String emailContentForEmailUpdate) {
        this.emailContentForEmailUpdate = emailContentForEmailUpdate;
    }

    public String getEmailUpdateUrlText() {
        return emailUpdateUrlText;
    }

    public void setEmailUpdateUrlText(String emailUpdateUrlText) {
        this.emailUpdateUrlText = emailUpdateUrlText;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public String getNotificationServerUrl() {
        return notificationServerUrl;
    }

    public void setNotificationServerUrl(String notificationServerUrl) {
        this.notificationServerUrl = notificationServerUrl;
    }

    public String getEntityServerUrl() {
        return entityServerUrl;
    }

    public void setEntityServerUrl(String entityServerUrl) {
        this.entityServerUrl = entityServerUrl;
    }

    public String getAwsS3BucketName() {
        return awsS3BucketName;
    }

    public void setAwsS3BucketName(String awsS3BucketName) {
        this.awsS3BucketName = awsS3BucketName;
    }

    public String getAwsS3Url() {
        return awsS3Url;
    }

    public void setAwsS3Url(String awsS3Url) {
        this.awsS3Url = awsS3Url;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public String getAppversionIosParticipantForced() {
        return AppversionIosParticipantForced;
    }

    public void setAppversionIosParticipantForced(String appversionIosParticipantForced) {
        AppversionIosParticipantForced = appversionIosParticipantForced;
    }

    public String getAppversionIosTrainerForced() {
        return AppversionIosTrainerForced;
    }

    public void setAppversionIosTrainerForced(String appversionIosTrainerForced) {
        AppversionIosTrainerForced = appversionIosTrainerForced;
    }

    public String getAppVersionAndroidParticipantForced() {
        return AppVersionAndroidParticipantForced;
    }

    public void setAppVersionAndroidParticipantForced(String appVersionAndroidParticipantForced) {
        AppVersionAndroidParticipantForced = appVersionAndroidParticipantForced;
    }

    public String getAppVesionAndroidTrainerForced() {
        return AppVesionAndroidTrainerForced;
    }

    public void setAppVesionAndroidTrainerForced(String appVesionAndroidTrainerForced) {
        AppVesionAndroidTrainerForced = appVesionAndroidTrainerForced;
    }

    public String getAppversionIosParticipantReco() {
        return AppversionIosParticipantReco;
    }

    public void setAppversionIosParticipantReco(String appversionIosParticipantReco) {
        AppversionIosParticipantReco = appversionIosParticipantReco;
    }

    public String getAppversionIosTrainerReco() {
        return AppversionIosTrainerReco;
    }

    public void setAppversionIosTrainerReco(String appversionIosTrainerReco) {
        AppversionIosTrainerReco = appversionIosTrainerReco;
    }

    public String getAppVersionAndroidParticipantReco() {
        return AppVersionAndroidParticipantReco;
    }

    public void setAppVersionAndroidParticipantReco(String appVersionAndroidParticipantReco) {
        AppVersionAndroidParticipantReco = appVersionAndroidParticipantReco;
    }

    public String getAppVesionAndroidTrainerReco() {
        return AppVesionAndroidTrainerReco;
    }

    public void setAppVesionAndroidTrainerReco(String appVesionAndroidTrainerReco) {
        AppVesionAndroidTrainerReco = appVesionAndroidTrainerReco;
    }

    public String getEmailTemplatePath() {
        return emailTemplatePath;
    }

    public void setEmailTemplatePath(String emailTemplatePath) {
        this.emailTemplatePath = emailTemplatePath;
    }

    public String getEmailTemplatePathPhoneUpdate() {
        return emailTemplatePathPhoneUpdate;
    }

    public void setEmailTemplatePathPhoneUpdate(String emailTemplatePathPhoneUpdate) {
        this.emailTemplatePathPhoneUpdate = emailTemplatePathPhoneUpdate;
    }

    public String getEmailUpdateSuccess() {
        return emailUpdateSuccess;
    }

    public void setEmailUpdateSuccess(String emailUpdateSuccess) {
        this.emailUpdateSuccess = emailUpdateSuccess;
    }

    public String getEmailUpdateUnSuccessFul() {
        return emailUpdateUnSuccessFul;
    }

    public void setEmailUpdateUnSuccessFul(String emailUpdateUnSuccessFul) {
        this.emailUpdateUnSuccessFul = emailUpdateUnSuccessFul;
    }
}
