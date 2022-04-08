package com.socion.backend.utils;

import com.socion.backend.AesUtil;
import com.socion.backend.config.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;

public class EmailUtils {



    private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtils.class);

    private EmailUtils() {
    }

    public static void sendEmail(AppContext appContext, String emailId, String action, String userId, String name, String otp,String emailUpdateId) throws IOException, MessagingException {
        LOGGER.debug("Sending email by setting required smtp properties.");
        Properties props = new Properties();
        props.put(appContext.getSmtpAuth(), Constants.TRUE);
        props.put(appContext.getSmtpMailTls(), Constants.TRUE);
        props.put(appContext.getSmtpHost(), appContext.getSmtpMail());
        props.put(appContext.getSmtpPort(), appContext.getPort());
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(appContext.getSourceEmail(), appContext.getSourceEmailPassword());
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(appContext.getSourceEmail(), false));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailId));
        if (Constants.EMAIL_ACTION_RESET_PWD.equals(action)) {
            LOGGER.debug("Resetting the password for user : {}", emailId);
            msg.setSubject(appContext.getEmailSubjectForResetPwd());
            msg.setContent(appContext.getEmailContentForResetPwd(), Constants.TEXTFORMAT);
        } else if (Constants.EMAIL_ACTION_VERIFY_ACCOUNT.equals(action)) {
            LOGGER.debug("Sign up Verification Email sent for user : {}", emailId);
            String url = appContext.getServerUrl() + Constants.USER_REGISTRATION_COMPLETE_SIGN_UP + emailId;

            StringBuilder content = new StringBuilder("<html><body> <p> " + appContext.getEmailContentForSignup() + "</p>");
            content.append("<p><a href=" + url + ">" + appContext.getRegistrationUrlText() + "</a></p>");
            content.append("<p>" + appContext.getLinkExpirationTimeText() + "</p>");
            msg.setSubject(appContext.getEmailSubjectForSignup());
            msg.setContent(content.toString(), Constants.TEXTFORMAT);

        } else if (Constants.EMAIL_ACTION_UPDATE_EMAIL_ID.equals(action)) {
            LOGGER.debug("Verification email sent for user : {} ", emailId);
            AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
            String encryptedDateTime = aesUtil.encrypt32(appContext.getSaltValue(), appContext.getIvValue(),
                    appContext.getSecretKey(), LocalDateTime.now().toString());

            try {
                String emailContent = new String(Files.readAllBytes(Paths.get(appContext.getEmailTemplatePath())));
                String url = appContext.getServerUrl() + Constants.USER_EMAIL_UPDATE_V2 + emailId + '&' + "userId=" + userId+'&'+"date="+encryptedDateTime+'&'+"emailUpdateId="+emailUpdateId;

                emailContent = emailContent.replace("$url", url);
                emailContent = emailContent.replace("$name", name);

                msg.setSubject(appContext.getEmailSubjectForEmailUpdate());
                msg.setContent(emailContent, Constants.TEXTFORMAT);
            } catch (IOException e) {
                LOGGER.error("Error in sending email: {}", e);
            }


        } else if (Constants.EMAIL_ACTION_UPDATE_PHONE.equals(action)) {
            LOGGER.debug("Verification email sent for user : {} ", emailId);
            String emailContent = new String(Files.readAllBytes(Paths.get(appContext.getEmailTemplatePathPhoneUpdate())));

            emailContent = emailContent.replace("$otp", otp);
            emailContent = emailContent.replace("$name", name);
            msg.setSubject(appContext.getEmailSubjectForEmailUpdate());
            msg.setContent(emailContent, Constants.TEXTFORMAT);

        }
        msg.setSentDate(new Date());
        Transport.send(msg);
    }
}

// Reading the email templates failed since they couldn’t be accessed at runtime. This change is to fix this.
