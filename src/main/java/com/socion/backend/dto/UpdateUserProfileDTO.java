package com.socion.backend.dto;

import com.socion.backend.entity.RegistryUserWithOsId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserProfileDTO {
    private String accessToken;
    private RegistryUserWithOsId updatedRegistryUserBody;
    private String phoneNumber;
    private String email;
    private Boolean isUpdatedEmailVerified;
    private Boolean updateEmail;
    private String newemail;
    private String userId;
    private String emailUpdateId;

    public String getEmailUpdateId() {
        return emailUpdateId;
    }

    public void setEmailUpdateId(String emailUpdateId) {
        this.emailUpdateId = emailUpdateId;
    }

    public Boolean getUpdateEmail() {
        return updateEmail;
    }

    public void setUpdateEmail(Boolean updateEmail) {
        this.updateEmail = updateEmail;
    }

    public String getNewemail() {
        return newemail;
    }

    public void setNewemail(String newemail) {
        this.newemail = newemail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public RegistryUserWithOsId getUpdatedRegistryUserBody() {
        return updatedRegistryUserBody;
    }

    public void setUpdatedRegistryUserBody(RegistryUserWithOsId updatedRegistryUserBody) {
        this.updatedRegistryUserBody = updatedRegistryUserBody;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getUpdatedEmailVerified() {
        return isUpdatedEmailVerified;
    }

    public void setUpdatedEmailVerified(Boolean updatedEmailVerified) {
        isUpdatedEmailVerified = updatedEmailVerified;
    }


}
