package com.pda.backend.dao;

import com.pda.backend.dto.*;


import com.pda.backend.utils.Constants;
import org.springframework.stereotype.Repository;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.io.IOException;

@Repository
public interface RegistryDao {

    @POST(Constants.REGISRY_ADD_USER)
    Call<RegistryResponse> createUser(@Header("x-authenticated-user-token") String adminAccessToken,
                                      @Body RegistryRequest registryRequest) throws IOException;

    @POST(Constants.REGISRY_SEARCH_USER)
    Call<RegistryResponse> searchUserByPhoneNumber(@Header("x-authenticated-user-token") String accessToken,
                                                   @Body SlimRegistryUserPhnumberDto slimRegistryUserDto) throws IOException;
    @POST(Constants.REGISRY_SEARCH_USER)
    Call<RegistryResponse> searchUserByPhoneNumberAndCountryCode(@Header("x-authenticated-user-token") String accessToken,
                                                   @Body SlimRegistryUserPhnumberAndCountryCodeDto slimRegistryUserDto) throws IOException;

    @POST(Constants.REGISRY_SEARCH_USER)
    Call<RegistryResponse> searchUserByUserId(@Header("x-authenticated-user-token") String accessToken,
                                              @Body SlimRegistryUserUserIdDto slimRegistryUserUserIdDto) throws IOException;


    @POST(Constants.REGISRY_SEARCH_USER)
    Call<RegistryResponse> searchUser(@Header("x-authenticated-user-token") String accessToken,
                                      @Body SlimRegistryUserDto slimRegistryUserDto) throws IOException;

    @POST(Constants.REGISTRY_UPDATE_USER)
    Call<RegistryResponse> updateUser(@Header("x-authenticated-user-token") String accessToken,
                                      @Body RegistryRequestWithOsId registryRequestWithOsId) throws IOException;


    @POST(Constants.REGISRY_SEARCH_USER)
    Call<RegistryResponse> getAllUsers(@Header("x-authenticated-user-token") String accessToken,
                                       @Body SlimRequestUsersDTO slimRequestUsersDTO) throws IOException;

    @POST(Constants.REGISRY_SEARCH)
    Call<RegistryResponse> getAllCountryCodes(@Header("x-authenticated-user-token") String accessToken,
                                              @Body SlimRequestCountryCodeDTO slimRequestCountryCodeDTO) throws IOException;
}
