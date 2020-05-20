package com.socion.backend.dto;

import com.socion.backend.entity.RegistryUser;
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
