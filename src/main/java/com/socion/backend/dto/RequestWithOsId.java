package com.socion.backend.dto;

import com.socion.backend.entity.RegistryUserWithOsId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestWithOsId {
    private RegistryUserWithOsId person;

    public RegistryUserWithOsId getPerson() {
        return person;
    }

    public void setPerson(RegistryUserWithOsId person) {
        this.person = person;
    }
}
