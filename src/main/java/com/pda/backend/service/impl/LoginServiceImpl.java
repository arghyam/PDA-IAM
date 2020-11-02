package com.pda.backend.service.impl;

import com.pda.backend.AesUtil;
import com.pda.backend.dao.RegistryDao;
import com.pda.backend.dto.UserDTO;
import com.pda.backend.config.AppContext;
import com.pda.backend.dao.KeycloakDao;
import com.pda.backend.dao.KeycloakService;
import com.pda.backend.dto.UserResponseDTO;
import com.pda.backend.dto.LoginDTO;
import com.pda.backend.dto.UserLoginResponseDTO;
import com.pda.backend.dto.AccessTokenResponseDTO;
import com.pda.backend.dto.ResponseDTO;

import com.pda.backend.exceptions.NotFoundException;
import com.pda.backend.service.LoginService;
import com.pda.backend.service.UserService;
import com.pda.backend.utils.Constants;
import com.pda.backend.utils.HttpUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private KeycloakDao keycloakDao;

    @Autowired
    UserService userService;

    @Autowired
    AppContext appContext;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    RegistryDao registryDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServiceImpl.class);
    private static String FALSE = "False";

    @Override
    public UserResponseDTO login(LoginDTO loginDTO, BindingResult bindingResult)  {


        userService.valiadtePojo(bindingResult);
        UserResponseDTO responseDTO = new UserResponseDTO();

        UserLoginResponseDTO userLoginResponseDTO = new UserLoginResponseDTO();

        try {
            String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
            UserRepresentation userRepresentation = keycloakService.getUserByUsername(adminAccessToken, loginDTO.getUserName(), appContext.getRealm());

            AccessTokenResponseDTO accessTokenResponseDTO = userLogin(loginDTO, responseDTO);

            if (responseDTO.getResponseCode() != Constants.TWO_HUNDRED) {
                userLoginResponseDTO.setAccessTokenResponseDTO(accessTokenResponseDTO);
                return responseDTO;
            }

            userLoginResponseDTO.setAccessTokenResponseDTO(accessTokenResponseDTO);

            if (userRepresentation.isEmailVerified() == Boolean.FALSE) {
                responseDTO.setMessage(Constants.EMAIL_NOT_VERIFIED);
                responseDTO.setResponseCode(HttpStatus.BAD_REQUEST.value());
                return responseDTO;
            }

            UserDTO user = new UserDTO();
            user.setEmailId(userRepresentation.getEmail());
            user.setUserName(userRepresentation.getUsername());
            user.setUserId(userRepresentation.getId());
            user.setName(userRepresentation.getFirstName());

            userLoginResponseDTO.setUserDetails(user);
            userLoginResponseDTO.setEmailVerified(userRepresentation.isEmailVerified());
            responseDTO.setResponse(userLoginResponseDTO);
        } catch (Exception e) {
            LOGGER.info(Constants.ERRORLOG+e);
            responseDTO.setMessage(e.getMessage());
            responseDTO.setResponseCode(HttpStatus.UNAUTHORIZED.value());
            return responseDTO;
        }

        return responseDTO;
    }

    public AccessTokenResponseDTO userLogin(LoginDTO loginDTO, UserResponseDTO responseDTO) {
        AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO();

        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        String decryptedPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), loginDTO.getPassword());

        loginDTO.setGrantType(appContext.getGrantType());
        loginDTO.setClientId(appContext.getClientId());
        try {
            Call<AccessTokenResponseDTO> loginResponseDTOCall = keycloakDao.generateAccessTokenUsingCredentials(appContext.getRealm(), loginDTO.getUserName(),
                    decryptedPassword, appContext.getClientId(), loginDTO.getGrantType(), appContext.getClientSecret());
            Response<AccessTokenResponseDTO> loginResponseDTOResponse = loginResponseDTOCall.execute();



            if (loginResponseDTOResponse.code() == Constants.TWO_HUNDRED) {
                accessTokenResponseDTO = loginResponseDTOResponse.body();
            } else {

                responseDTO.setMessage(Constants.INVALID_PASSWORD);
                responseDTO.setResponseCode(HttpStatus.FORBIDDEN.value());
            }
            responseDTO.setResponseCode(loginResponseDTOResponse.code());

        } catch (IOException e) {
            LOGGER.info(Constants.ERRORLOG +e);
            responseDTO.setResponseCode(HttpStatus.FORBIDDEN.value());
            responseDTO.setMessage(Constants.INVALID_PASSWORD);
        }
        return accessTokenResponseDTO;
    }

    @Override
    public UserResponseDTO refreshAccessToken(LoginDTO loginDTO) {
        UserLoginResponseDTO userLoginResponseDTO = new UserLoginResponseDTO();
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        try {
            AccessTokenResponseDTO accessTokenResponseDTO = keycloakService.refreshAccessToken(loginDTO);
            userLoginResponseDTO.setAccessTokenResponseDTO(accessTokenResponseDTO);

            if (accessTokenResponseDTO != null) {
                userResponseDTO.setResponseCode(HttpStatus.OK.value());
                userResponseDTO.setResponse(userLoginResponseDTO);
            } else {
                userResponseDTO.setResponseCode(HttpStatus.BAD_REQUEST.value());
            }
        } catch (Exception e) {
            LOGGER.info(Constants.ERRORLOG+e);
            userResponseDTO.setMessage(e.getMessage());
        }
        return userResponseDTO;
    }

    @Override

    public ResponseDTO logout(String id)  {
        LOGGER.info("============================================API CALL:/log-out============================================");
        return keycloakService.logout(id);
    }

    @Override
    public ResponseDTO resendVerifyEmail(String email)  {

        ResponseDTO responseDTO = new ResponseDTO();
        Keycloak adminUser = userService.getKeycloak();
        String accessToken = adminUser.tokenManager().getAccessTokenString();
        try {
            UserRepresentation userByUsername = keycloakService.getUserByUsername(accessToken, email, appContext.getRealm());
            LOGGER.info("Fetched user"+userByUsername);
            CompletableFuture.runAsync(() -> {
                try {
                    userService.sendVerificationEmailForCompletingSignup(email);
                    LOGGER.info("Successfully resent the email");
                } catch (Exception e) {
                    LOGGER.error("Error resending email"+e);
                }
            });
            responseDTO.setResponseCode(HttpStatus.OK.value());
            responseDTO.setMessage(Constants.EMAIL_SENT_SUCCESSFULLY);
        } catch (NotFoundException e) {
            LOGGER.info(Constants.ERRORLOG+e);
            responseDTO = HttpUtils.onFailure(HttpStatus.NOT_FOUND.value(), Constants.USER_DOES_NOT_EXIST);
        }
        return responseDTO;
    }
}
