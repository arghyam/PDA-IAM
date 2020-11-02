package com.pda.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

public class RegistryRequest {

    protected String id;
    protected String ver;
    protected Long ets;
    protected RequestParams params;
    protected Request request;
    @JsonIgnore
    protected String requestMapString;
    @JsonIgnore
    protected JsonNode requestMapNode;

    public RegistryRequest() {
    }

    public RegistryRequest(RequestParams params, Request request, String id) {
        this.ver = "1.0";
        this.ets = System.currentTimeMillis();
        this.params = params;
        this.request = request;
        this.id = id;
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

    public Long getEts() {
        return ets;
    }

    public void setEts(Long ets) {
        this.ets = ets;
    }

    public RequestParams getParams() {
        return params;
    }

    public void setParams(RequestParams params) {
        this.params = params;
    }


    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getRequestMapString() {
        return requestMapString;
    }

    public void setRequestMapString(String requestMapString) {
        this.requestMapString = requestMapString;
    }

    public JsonNode getRequestMapNode() {
        return requestMapNode;
    }

    public void setRequestMapNode(JsonNode requestMapNode) {
        this.requestMapNode = requestMapNode;
    }
}
