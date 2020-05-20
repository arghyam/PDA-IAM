package com.socion.backend.dto;

import com.socion.backend.utils.Constants;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class LoginRequestBody {

    @NotNull(message = Constants.FIELD_INVALID)
    @Size(min = 0, max = 10)
    @NotEmpty
    @Pattern(regexp = "(^$|[0-9]{10})")
    public String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
