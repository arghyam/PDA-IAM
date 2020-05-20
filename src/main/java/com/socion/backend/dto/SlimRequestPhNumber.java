package com.socion.backend.dto;

import com.socion.backend.entity.PhoneNumberRegistryUser;

public class SlimRequestPhNumber {
    private PhoneNumberRegistryUser person;

    public PhoneNumberRegistryUser getPerson() {
        return person;
    }

    public void setPerson(PhoneNumberRegistryUser person) {
        this.person = person;
    }
}
