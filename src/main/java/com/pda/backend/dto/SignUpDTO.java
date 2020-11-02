package com.pda.backend.dto;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class SignUpDTO {

    @NotEmpty
    @NotNull
    @Pattern(regexp = "[^?,]*$", message = " Please insert a valid name")
    public String name;

    @NotNull
    @NotEmpty
    @Pattern(regexp = "[\\s]*[0-9]*[0-9]+", message = " Please Check valid phone number")
    public String phoneNumber;

    @NotNull
    @NotEmpty
    public String countryCode;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
