package com.pda.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlimRegistryUserUserIdDto {

    private SlimRequestUserId request;
    private String id;
    private String ver;

    public SlimRequestUserId getRequest() {
        return request;
    }

    public void setRequest(SlimRequestUserId request) {
        this.request = request;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

}
