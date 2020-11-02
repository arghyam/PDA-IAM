package com.pda.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlimRequestCountryCode {
    @JsonProperty(value="CountryCode")
    private RegistryUsers countryCode;

    public RegistryUsers getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(RegistryUsers countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        return "SlimRequestCountryCode{" +
                "countryCode=" + countryCode +
                '}';
    }
}
