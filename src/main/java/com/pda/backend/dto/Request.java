package com.pda.backend.dto;

import com.pda.backend.entity.RegistryUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {
    private RegistryUser person;

    public RegistryUser getPerson() {
        return person;
    }

    public void setPerson(RegistryUser person) {
        this.person = person;
    }
}
