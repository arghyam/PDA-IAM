package com.socion.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlimRegistryUserPhnumberDto {

    private SlimRequestPhNumber request;
    private String id;
    private String ver;

    public SlimRequestPhNumber getRequest() {
        return request;
    }

    public void setRequest(SlimRequestPhNumber request) {
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
