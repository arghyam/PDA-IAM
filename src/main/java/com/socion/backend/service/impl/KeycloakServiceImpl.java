package com.socion.backend.service.impl;

import com.socion.backend.config.AppContext;
import com.socion.backend.config.CacheConfiguration;
import com.socion.backend.dao.KeycloakDao;
import com.socion.backend.dao.KeycloakService;
import com.socion.backend.dto.AccessTokenResponseDTO;
import com.socion.backend.dto.LoginDTO;
import com.socion.backend.dto.ResponseDTO;
import com.socion.backend.exceptions.NotFoundException;
import com.socion.backend.exceptions.UnauthorizedException;
import com.socion.backend.exceptions.UserCreateException;
import com.socion.backend.utils.Constants;
import com.socion.backend.utils.HttpUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class KeycloakServiceImpl implements KeycloakService {

    @Autowired
    KeycloakDao keycloakDao;

    @Autowired
    AppContext appContext;

    @Autowired
    CacheConfiguration cacheConfiguration;

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakServiceImpl.class);

    @Override
    public void register(String token, UserRepresentation userRepresentation) {

        Call<Void> loginResponseDTOCall = null;
        try {
            loginResponseDTOCall = keycloakDao.registerUser(appContext.getRealm(), Constants.BEARER + token, userRepresentation);
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        Response loginResponseDTOResponse = null;
        try {
            loginResponseDTOResponse = loginResponseDTOCall.execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (!loginResponseDTOResponse.isSuccessful()) {
            if (loginResponseDTOResponse.code() == HttpStatus.UNAUTHORIZED.value()) {
                throw new UnauthorizedException(Constants.USER_NOT_AUTHORIZED);
            } else if (loginResponseDTOResponse.code() == HttpStatus.CONFLICT.value()) {
                throw new UserCreateException("User already exists");
            }
        }
    }

    @Override
    public UserRepresentation getUserById(String token, String id, String realm) {

        Call<UserRepresentation> userRepresentationCall = null;
        try {
            userRepresentationCall = keycloakDao.getUser(realm, token, id);
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        Response<UserRepresentation> response = null;
        try {
            response = userRepresentationCall.execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (!response.isSuccessful()) {
            if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                throw new UnauthorizedException(Constants.USER_NOT_AUTHORIZED);
            } else if (response.code() == HttpStatus.NOT_FOUND.value()) {
                throw new UserCreateException("User not found");
            }
        }
        return response.body();
    }

    @Override
    public ResponseDTO logout(String id) {
        ResponseDTO response = new ResponseDTO();
        String adminAccessToken = null;
        adminAccessToken = this.generateAccessToken(appContext.getAdminUserName());
        Call<Void> logoutCall = keycloakDao.logout(Constants.BEARER + adminAccessToken, appContext.getRealm(), id);
        Response logoutResponse = null;
        try {
            logoutResponse = logoutCall.execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        if (logoutResponse.isSuccessful()) {
            response.setResponseCode(HttpStatus.OK.value());
        } else {
            response.setResponseCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(logoutResponse.message());
        }
        return response;
    }


    @Override
    public UserRepresentation getUserByUsername(String token, String username, String realm) {

        Response<List<UserRepresentation>> response = null;
        LOGGER.debug("Retriving user from keycloak. User is not cached");
        Call<List<UserRepresentation>> userRepresentationCall = null;
        String adminAccessToken = null;
        adminAccessToken = generateAccessToken(appContext.getAdminUserName());
        if (username.matches(".+@.+\\.[a-z]+")) {
            try {
                userRepresentationCall = keycloakDao.searchUsersByEmail(realm, Constants.BEARER + adminAccessToken, username);
            } catch (IOException e) {
                LOGGER.error(Constants.ERRORLOG, e);
            }
        } else {
            try {
                userRepresentationCall = keycloakDao.searchUsersByUserName(realm, Constants.BEARER + adminAccessToken, username);
            } catch (IOException e) {
                LOGGER.error(Constants.ERRORLOG, e);
            }
        }

        try {
            response = userRepresentationCall.execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (response.body().size() == 0) {
            throw new NotFoundException(Constants.USER_DOES_NOT_EXIST);
        } else if (!response.isSuccessful()) {
            if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                throw new UnauthorizedException(Constants.USER_NOT_AUTHORIZED);
            } else if (response.code() == HttpStatus.NOT_FOUND.value()) {
                throw new UserCreateException("User not found");
            }

        }
        return response.body().get(0);
    }

    @Override
    public void updateUser(String token, String id, UserRepresentation user, String realm) {

        Call<ResponseBody> responseBodyCall = null;
        try {
            responseBodyCall = keycloakDao.updateUser(token, id, user, realm);
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        try {
            responseBodyCall.execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
    }

    @Override
    public AccessTokenResponseDTO refreshAccessToken(LoginDTO loginDTO) {

        LOGGER.info("Refreshing Access Token");
        Call<AccessTokenResponseDTO> loginResponseDTOCall;
        Response<AccessTokenResponseDTO> loginResponseDTO = null;

        if (Constants.REFRESH_TOKEN.equalsIgnoreCase(loginDTO.getGrantType())) {
            loginResponseDTOCall = keycloakDao.generateAccessTokenUsingRefreshToken(appContext.getRealm(), loginDTO.getRefreshToken(),
                    appContext.getClientId(), loginDTO.getGrantType(), appContext.getClientSecret());
            try {
                loginResponseDTO = loginResponseDTOCall.execute();
            } catch (IOException e) {
                LOGGER.error(Constants.ERRORLOG, e);
            }
        } else {
            loginResponseDTOCall = keycloakDao.generateAccessTokenUsingCredentials(appContext.getRealm(), loginDTO.getUserName(), loginDTO.getPassword(), appContext.getClientId(),
                    Constants.PASSWORD, appContext.getClientSecret());
            try {
                loginResponseDTO = loginResponseDTOCall.execute();
            } catch (IOException e) {
                LOGGER.error(Constants.ERRORLOG, e);
            }
        }
        return loginResponseDTO.body();
    }

    @Override
    public ResponseDTO forgotPassword(String emailId) {

        ResponseDTO responseDTO = new ResponseDTO();
        try {

            String adminAccessToken = this.generateAccessToken(appContext.getAdminUserName());
            UserRepresentation userRepresentation = getUserByUsername(adminAccessToken, emailId, appContext.getRealm());
            List<String> actions = new ArrayList<>();
            actions.add("UPDATE_PASSWORD");
            Call<Void> resetPasswordCall = keycloakDao.resetPassword(Constants.BEARER + adminAccessToken, userRepresentation.getId(), actions, appContext.getRealm());

            Response<Void> keycloakResponse = resetPasswordCall.execute();
            if (keycloakResponse.isSuccessful()) {
                responseDTO.setResponseCode(HttpStatus.OK.value());
                responseDTO.setMessage("Open your Mail and reset new Password");
            } else {
                responseDTO = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Invalid request." + keycloakResponse.message());
            }
        } catch (Exception e) {
            LOGGER.error("Exception while trying to reset password : {} ", e);
            responseDTO = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Invalid request." + e);
        }
        return responseDTO;
    }

    @Override
    public ResponseDTO sendVerificationEmail(String accessToken, String id, String realm) {

        Response<Void> verifyEmail = null;
        try {
            verifyEmail = keycloakDao.sendVerificationEmail(Constants.BEARER + accessToken, id, appContext.getRealm()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        ResponseDTO response = new ResponseDTO();

        if (verifyEmail.isSuccessful()) {
            response = HttpUtils.onSuccess(null, Constants.USER_CREATED_SUCCESSFULLY);
        } else {
            response = HttpUtils.onFailure(HttpStatus.GATEWAY_TIMEOUT.value(), Constants.ERROR_SENDING_EMAIL);
        }
        return response;
    }


    @Override
    public String generateAccessToken(String username) {
        AccessTokenResponseDTO adminAccessTokenResponse = null;
        LOGGER.debug("Generating access Token for user : {} ", username);
            try {

                Call<AccessTokenResponseDTO> accessTokenResponseDTOCall = keycloakDao.generateAccessTokenUsingCredentials(appContext.getRealm(), appContext.getAdminUserName(),
                        appContext.getAdminUserpassword(), appContext.getClientId(), appContext.getGrantType(), appContext.getClientSecret());
                Response<AccessTokenResponseDTO> accessTokenResponseDTOResponse = accessTokenResponseDTOCall.execute();
                adminAccessTokenResponse = accessTokenResponseDTOResponse.body();
            } catch (IOException e) {
                LOGGER.error(Constants.ERRORLOG, e);
            }
            return adminAccessTokenResponse.getAccessToken();
}}
