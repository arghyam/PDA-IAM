package com.socion.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RegistryCountryCode {

    @NotEmpty
    private String code;

    @NotEmpty
    private String country;

    public RegistryCountryCode() {
    }

    public RegistryCountryCode(@NotEmpty String code, @NotEmpty String country) {
        this.code = code;
        this.country = country;
    }

    @JsonCreator
    public RegistryCountryCode(Map<String, Object> map) {
        this.code = (String) map.get("code");
        this.country = (String) map.get("country");
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}