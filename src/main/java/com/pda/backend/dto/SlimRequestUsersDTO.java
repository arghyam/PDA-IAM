package com.pda.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlimRequestUsersDTO {
    private SlimRequestUsers request;
    private String id;
    private String ver;

    public SlimRequestUsers getRequest() {
        return request;
    }

    public void setRequest(SlimRequestUsers request) {
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
