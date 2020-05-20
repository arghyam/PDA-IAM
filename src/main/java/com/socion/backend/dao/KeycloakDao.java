package com.socion.backend.dao;

import com.socion.backend.dto.AccessTokenResponseDTO;
import com.socion.backend.utils.Constants;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.bind.annotation.ResponseBody;
import retrofit2.Call;

import retrofit2.http.*;

import javax.ws.rs.Consumes;
import java.io.IOException;
import java.util.List;

public interface KeycloakDao {

    @POST(Constants.KEYCLOAK_REGISTER_API)
    Call<Void> registerUser(@Path("realm") String realm,
                            @Header("Authorization") String token,
                            @Body UserRepresentation userss) throws IOException;


    @POST(Constants.GENERATE_ACCESS_TOKEN)
    @FormUrlEncoded
    Call<AccessTokenResponseDTO> generateAccessTokenUsingCredentials(@Path("realm") String realm, @Field("username") String username,
                                                                     @Field("password") String password,
                                                                     @Field("client_id") String clientId,
                                                                     @Field("grant_type") String grantType,
                                                                     @Field("client_secret") String clientSecret);

    @POST(Constants.GENERATE_ACCESS_TOKEN)
    @FormUrlEncoded
    Call<AccessTokenResponseDTO> generateAccessTokenUsingRefreshToken(@Path("realm") String realm, @Field("refresh_token") String refreshToken,
                                                                      @Field("client_id") String clientId,
                                                                      @Field("grant_type") String grantType,
                                                                      @Field("client_secret") String clientSecret);

    @GET(Constants.GET_USER_API)
    Call<UserRepresentation> getUser(@Path("realm") String realm,
                                     @Header("Authorization") String token,
                                     @Path("id") String id) throws IOException;


    @POST(Constants.LOGOUT_API)
    Call<Void> logout(@Header("Authorization") String token, @Path("realm") String realm, @Path("id") String id);

    @GET(Constants.SEARCH_USERS)
    Call<List<UserRepresentation>> searchUsersByUserName(@Path("realm") String realm,
                                                         @Header("Authorization") String token,
                                                         @Query("username") String username) throws IOException;

    @GET(Constants.SEARCH_USERS)
    Call<List<UserRepresentation>> searchUsersByEmail(@Path("realm") String realm,
                                                      @Header("Authorization") String token,
                                                      @Query("email") String email) throws IOException;

    @PUT(Constants.UPDATE_USER)
    Call<ResponseBody> updateUser(@Header("Authorization") String adminAccessToken,
                                  @Path("id") String id,
                                  @Body UserRepresentation user,
                                  @Path("realm") String realm) throws IOException;

    @PUT(Constants.RESET_PASSWORD)
    @Consumes("application/json")
    Call<Void> resetPassword(@Header("Authorization") String token,
                             @Path("id") String id,
                             @Body List<String> actions,
                             @Path("realm") String realm) throws IOException;


    @PUT(Constants.SEND_VERIFICATION_EMAIL)
    @Consumes("application/json")
    Call<Void> sendVerificationEmail(@Header("Authorization") String token,
                                     @Path("id") String id,
                                     @Path("realm") String realm) throws IOException;

    @DELETE(Constants.DELETE_USER)
    Call<Void> deleteUser(@Header("Authorization") String token,
                          @Path("id") String id,
                          @Path("realm") String realm) throws IOException;
}
