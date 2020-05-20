package com.socion.backend.dto;

public class SlimRegistryUserPhnumberAndCountryCodeDto {

    private SlimRequestPhoneNumber request;
    private String id;
    private String ver;

    public SlimRequestPhoneNumber getRequest() {
        return request;
    }

    public void setRequest(SlimRequestPhoneNumber request) {
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
