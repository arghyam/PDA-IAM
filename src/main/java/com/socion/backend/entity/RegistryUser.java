package com.socion.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryUser {

    @NotEmpty
    protected String name;

    protected String emailId;

    protected String salutation;

    @NotEmpty
    protected String phoneNumber;

    @NotEmpty
    protected String photo;

    @NotEmpty
    protected String userId;


    @NotEmpty
    protected String crtdDttm;

    @NotEmpty
    protected String updtDttm;


    @NotEmpty
    protected boolean active;

    protected String profileCardUrl;

    protected String countryCode;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCrtdDttm() {
        return crtdDttm;
    }

    public void setCrtdDttm(String crtdDttm) {
        this.crtdDttm = crtdDttm;
    }

    public String getUpdtDttm() {
        return updtDttm;
    }

    public void setUpdtDttm(String updtDttm) {
        this.updtDttm = updtDttm;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    public String getProfileCardUrl() {
        return profileCardUrl;
    }

    public void setProfileCardUrl(String profileCardUrl) {
        this.profileCardUrl = profileCardUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


}