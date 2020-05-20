package com.socion.backend.dto;

import com.socion.backend.entity.RegistryUserWithOsId;

public class RegistryCustomResponse {
    private int responseCode;
    private RegistryUserWithOsId response;
    private String message;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public RegistryUserWithOsId getResponse() {
        return response;
    }

    public void setResponse(RegistryUserWithOsId response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
