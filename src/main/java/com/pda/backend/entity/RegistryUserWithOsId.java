package com.pda.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RegistryUserWithOsId extends RegistryUser {
    @NotEmpty
    private String osid;


    public RegistryUserWithOsId() {
    }

    @JsonCreator
    public RegistryUserWithOsId(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.emailId = (String) map.get("emailId");
        this.salutation = (String) map.get("salutation");
        this.phoneNumber = (String) map.get("phoneNumber");
        this.photo = (String) map.get("photo");
        this.userId = (String) map.get("userId");
        this.osid = (String) map.get("osid");
        this.crtdDttm = (String) map.get("crtdDttm");
        this.updtDttm = (String) map.get("updtDttm");
        Object isActive = map.get("active");
        this.active = null == isActive ? Boolean.FALSE : (Boolean) isActive;
        this.profileCardUrl = (String) map.get("profileCardUrl");
        this.countryCode = (String) map.get("countryCode");
    }

    public String getOsid() {
        return osid;
    }

    public void setOsid(String osid) {
        this.osid = osid;
    }

}