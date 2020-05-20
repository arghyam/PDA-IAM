package com.socion.backend.dto;

public class ValidateOtpDto {

    private String phoneNumber;
    private String otp;
    private String typeOfOTP;
    private String countryCode;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getTypeOfOTP() {
        return typeOfOTP;
    }

    public void setTypeOfOTP(String typeOfOTP) {
        this.typeOfOTP = typeOfOTP;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
