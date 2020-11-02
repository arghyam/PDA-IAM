package com.pda.backend.dto;

import com.pda.backend.entity.SlimRegistryUser;

public class SlimRequest {
    private SlimRegistryUser person;

    public SlimRegistryUser getPerson() {
        return person;
    }

    public void setPerson(SlimRegistryUser person) {
        this.person = person;
    }
}
