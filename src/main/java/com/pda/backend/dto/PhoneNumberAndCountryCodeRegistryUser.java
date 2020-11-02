package com.pda.backend.dto;

public class PhoneNumberAndCountryCodeRegistryUser {
    private String phoneNumber;
    private String countryCode;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public PhoneNumberAndCountryCodeRegistryUser(String phoneNumber, String countryCode) {
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
    }
}
