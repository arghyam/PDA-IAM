package com.socion.backend.dto;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ChangeStatusDto implements Serializable {

    @NotNull(message = "active can not be null")
    public Boolean active;

    public ChangeStatusDto() {
    }

    public ChangeStatusDto(Boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


}
