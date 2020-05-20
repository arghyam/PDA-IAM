package com.socion.backend.dto;

import com.socion.backend.entity.SlimRegistryUser;

public class SlimRequest {
    private SlimRegistryUser person;

    public SlimRegistryUser getPerson() {
        return person;
    }

    public void setPerson(SlimRegistryUser person) {
        this.person = person;
    }
}
