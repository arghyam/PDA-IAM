package com.pda.backend.dto;

public class UserLoginResponseDTO {

    private AccessTokenResponseDTO accessTokenResponseDTO;
    private UserDTO userDetails;
    private Boolean emailVerified;


    public AccessTokenResponseDTO getAccessTokenResponseDTO() {
        return accessTokenResponseDTO;
    }

    public void setAccessTokenResponseDTO(AccessTokenResponseDTO accessTokenResponseDTO) {
        this.accessTokenResponseDTO = accessTokenResponseDTO;
    }

    public UserDTO getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDTO userDetails) {
        this.userDetails = userDetails;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

}
