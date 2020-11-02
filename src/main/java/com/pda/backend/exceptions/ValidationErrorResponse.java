package com.pda.backend.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * The type Validation error response.
 *
 * @AllArgsConstructor annotation Generates an all-args constructor.
 * An all-args constructor requires one argument for every field in the class.
 * <p>
 * Generates a no-args constructor.
 * @NoArgsConstructor Annotation will generate an error message if such a constructor cannot be written due to the existence of final fields.
 * </p>
 * @JsonIgnoreProperties is used to either suppress serialization of properties (during
 * serialization), or ignore processing of JSON properties read (during deserialization). </p>
 * <p>
 */
@JsonIgnoreProperties("message")
public class ValidationErrorResponse extends ResponseMessage {


    private List<ValidationError> errors;

    ValidationErrorResponse(List<ValidationError> errors) {
        this.errors = errors;
    }

    /**
     * Gets errors.
     *
     * @return the errors
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Sets errors.
     *
     * @param errors the errors
     */
    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}
