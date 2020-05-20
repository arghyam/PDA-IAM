package com.socion.backend.dto;


import java.util.Map;

public class CountryCode {

    String code;
    String country;
    int phoneNumberLength;


    public CountryCode() {
    }

    public CountryCode(String code, String country, int phoneNumberLength) {
        this.code = code;
        this.country = country;
        this.phoneNumberLength = phoneNumberLength;
    }

    public CountryCode(Map<String, Object> map) {
        this.code = (String) map.get("code");
        this.country = (String) map.get("country");
        this.phoneNumberLength=Integer.parseInt(map.get("phoneNumberLength").toString());

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getPhoneNumberLength() {
        return phoneNumberLength;
    }

    public void setPhoneNumberLength(int phoneNumberLength) {
        this.phoneNumberLength = phoneNumberLength;
    }
}
