package com.socion.backend.dto;

public class UserResponseDTO {

    private UserLoginResponseDTO response;
    private String message;
    private int responseCode;

    public UserLoginResponseDTO getResponse() {
        return response;
    }

    public void setResponse(UserLoginResponseDTO response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
