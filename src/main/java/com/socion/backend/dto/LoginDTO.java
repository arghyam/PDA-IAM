package com.socion.backend.dto;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LoginDTO {

    private String clientId;
    private String grantType;
    @NotEmpty
    @NotNull
    private String userName;
    @NotEmpty
    @NotNull
    private String password;

    private String countryCode;
    private String refreshToken;

    public LoginDTO(String clientId, String grantType, String userName, String password, String refreshToken) {
        this.clientId = clientId;
        this.grantType = grantType;
        this.userName = userName;
        this.password = password;
        this.refreshToken = refreshToken;
    }

    public LoginDTO() {

    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }


    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
