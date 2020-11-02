package com.pda.backend.dto;

import com.pda.backend.entity.PhoneNumberRegistryUser;

public class SlimRequestPhNumber {
    private PhoneNumberRegistryUser person;

    public PhoneNumberRegistryUser getPerson() {
        return person;
    }

    public void setPerson(PhoneNumberRegistryUser person) {
        this.person = person;
    }
}
