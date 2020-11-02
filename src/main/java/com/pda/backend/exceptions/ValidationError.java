package com.pda.backend.exceptions;

/**
 * Validation error provides with validation on specific fields and the error message for them.
 * <p>
 * Generates a no-args constructor.
 *
 * @NoArgsConstructor Annotation Will generate an error message if such a constructor cannot be written due to the existence of final fields.
 * </p>
 */
public class ValidationError {

    private String field;
    private String message;

    /**
     * Instantiates a new Validation error.
     *
     * @param field   the field
     * @param message the message
     */
    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    /**
     * Gets field.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * Sets field.
     *
     * @param field the field
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return this.field + message;
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
