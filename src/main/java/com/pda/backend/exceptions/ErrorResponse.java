package com.pda.backend.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private String message;

    /**
     * Instantiates a new Response message.
     */
    public ErrorResponse() {
        //Default Constructor
    }

    /**
     * Instantiates a new Response message.
     *
     * @param message the message
     */
    public ErrorResponse(String message) {
        this.message = message;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
