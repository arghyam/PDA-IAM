package com.pda.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryResponse extends  RegistryRequest{
 @JsonProperty(value = "params")
    private ResponseParams responseParams;
    private String responseCode;
    private Object result;

    public enum API_ID {
        CREATE("open-saber.registry.create"), READ("open-saber.registry.read"), UPDATE(
                "open-saber.registry.update"), AUDIT("open-saber.registry.audit"), HEALTH(
                "open-saber.registry.health"), DELETE("open-saber.registry.delete"), SEARCH(
                "open-saber.registry.search"), SIGN("open-saber.utils.sign"), VERIFY(
                "open-saber.utils.verify"), KEYS("open-saber.utils.keys"), ENCRYPT(
                "open-saber.utils.encrypt"), DECRYPT(
                "open-saber.utils.decrypt"), NONE("");
        private String id;

        private API_ID(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public enum Status {
        SUCCESSFUL, UNSUCCESSFUL;
    }


    public ResponseParams getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(ResponseParams responseParams) {
        this.responseParams = responseParams;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }





}
