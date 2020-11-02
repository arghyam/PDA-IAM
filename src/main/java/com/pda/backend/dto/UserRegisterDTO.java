package com.pda.backend.dto;

import com.pda.backend.utils.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties
public class UserRegisterDTO {

    @NotEmpty
    @NotNull(message = Constants.FIELD_INVALID)
    public String salutation;


    //  @Pattern(regexp=".+@.+\\.[a-z]+")
    @NotEmpty
    @NotNull(message = Constants.FIELD_INVALID)
    public String email;

    @NotEmpty
    @NotNull(message = Constants.FIELD_INVALID)
    public String password;

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
