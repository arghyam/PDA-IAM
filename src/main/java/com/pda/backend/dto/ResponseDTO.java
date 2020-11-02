package com.pda.backend.dto;

public class ResponseDTO {

    private Object response;
    private String message;
    private int responseCode;

    public ResponseDTO(Object response, String message, int responseCode) {
        this.response = response;
        this.message = message;
        this.responseCode = responseCode;
    }

    public ResponseDTO() {
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
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
