package com.socion.backend.dto;

import com.socion.backend.entity.RegistryUserWithUserId;

public class SlimRequestUserId {
    private RegistryUserWithUserId person;

    public RegistryUserWithUserId getPerson() {
        return person;
    }

    public void setPerson(RegistryUserWithUserId person) {
        this.person = person;
    }
}
