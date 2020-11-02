package com.pda.backend.dto;

public class SlimRequestPhoneNumber {
    private PhoneNumberAndCountryCodeRegistryUser person;

    public PhoneNumberAndCountryCodeRegistryUser getPerson() {
        return person;
    }

    public void setPerson(PhoneNumberAndCountryCodeRegistryUser person) {
        this.person = person;
    }
}
