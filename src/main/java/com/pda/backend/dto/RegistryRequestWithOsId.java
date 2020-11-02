package com.pda.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistryRequestWithOsId extends RegistryRequest {

    @JsonProperty(value = "request")
    private RequestWithOsId requestWithOsId;


    public RegistryRequestWithOsId(RequestParams params, RequestWithOsId requestWithOsId, String id) {

        this.ver = "1.0";
        this.ets = System.currentTimeMillis();
        this.params = params;
        this.requestWithOsId = requestWithOsId;
        this.id = id;
    }

    public RequestWithOsId getRequestWithOsId() {
        return requestWithOsId;
    }

    public void setRequestWithOsId(RequestWithOsId requestWithOsId) {
        this.requestWithOsId = requestWithOsId;
    }
}
