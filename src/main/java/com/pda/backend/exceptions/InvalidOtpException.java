package com.pda.backend.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOtpException extends RuntimeException {

    /**
     * Instantiates a new InvalidOtpException exception.
     *
     * @param message the message
     */
    public InvalidOtpException(String message) {
        super(message);
    }

}
