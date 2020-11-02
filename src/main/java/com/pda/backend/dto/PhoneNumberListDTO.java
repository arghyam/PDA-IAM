package com.pda.backend.dto;

import java.util.List;

public class PhoneNumberListDTO {
    private List<PhoneNumberAndCountryCodeDTO> phoneNumbers;


    public List<PhoneNumberAndCountryCodeDTO> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<PhoneNumberAndCountryCodeDTO> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
