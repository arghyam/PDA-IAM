package com.pda.backend.dto;

import com.pda.backend.utils.Constants;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class LogoutRequestBody {

    @NotEmpty
    @NotNull(message = Constants.FIELD_INVALID)
    public String accessToken;

    @NotEmpty
    @NotNull(message = Constants.FIELD_INVALID)
    public String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
