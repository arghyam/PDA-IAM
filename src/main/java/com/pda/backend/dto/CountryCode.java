package com.pda.backend.dto;

import java.util.Map;

public class CountryCode {

    private String code;
    private String country;
    private int phoneNumberLength;
    private int phoneNumberSizeMin;
    private int phoneNumberSizeMax;

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
        this.phoneNumberLength = (int) map.get("phoneNumberLength");
        this.phoneNumberSizeMax = (int) map.get("phoneNumberSizeMax");
        this.phoneNumberSizeMin = (int) map.get("phoneNumberSizeMin");

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

    public int getPhoneNumberSizeMin() {
        return phoneNumberSizeMin;
    }

    public void setPhoneNumberSizeMin(int phoneNumberSizeMin) {
        this.phoneNumberSizeMin = phoneNumberSizeMin;
    }

    public int getPhoneNumberSizeMax() {
        return phoneNumberSizeMax;
    }

    public void setPhoneNumberSizeMax(int phoneNumberSizeMax) {
        this.phoneNumberSizeMax = phoneNumberSizeMax;
    }
}