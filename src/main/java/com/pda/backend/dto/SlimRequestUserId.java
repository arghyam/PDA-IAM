package com.pda.backend.dto;

import com.pda.backend.entity.RegistryUserWithUserId;

public class SlimRequestUserId {
    private RegistryUserWithUserId person;

    public RegistryUserWithUserId getPerson() {
        return person;
    }

    public void setPerson(RegistryUserWithUserId person) {
        this.person = person;
    }
}
