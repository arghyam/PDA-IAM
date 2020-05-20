package com.socion.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlimRequestCountryCodeDTO {
    private SlimRequestCountryCode request;
    private String id;
    private String ver;

    public SlimRequestCountryCode getRequest() {
        return request;
    }

    public void setRequest(SlimRequestCountryCode request) {
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

    @Override
    public String toString() {
        return "SlimRequestCountryCodeDTO{" +
                "request=" + request +
                ", id='" + id + '\'' +
                ", ver='" + ver + '\'' +
                '}';
    }
}
