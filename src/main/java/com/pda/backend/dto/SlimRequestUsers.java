package com.pda.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlimRequestUsers {
    private RegistryUsers person;

    public RegistryUsers getPerson() {
        return person;
    }

    public void setPerson(RegistryUsers person) {
        this.person = person;
    }
}
