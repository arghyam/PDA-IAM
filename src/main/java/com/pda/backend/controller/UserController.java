package com.pda.backend.controller;

import com.pda.backend.config.AppContext;
import com.pda.backend.dao.KeycloakService;
import com.pda.backend.dto.ResponseDTO;
import com.pda.backend.dto.UserRegisterDTO;
import com.pda.backend.dto.LoginDTO;
import com.pda.backend.dto.UserResponseDTO;
import com.pda.backend.dto.PasswordBodyDto;
import com.pda.backend.entity.RegistryUserWithOsId;
import com.pda.backend.exceptions.UserCreateException;
import com.pda.backend.service.LoginService;
import com.pda.backend.service.UserService;
import com.pda.backend.utils.Constants;
import com.pda.backend.utils.HttpUtils;
import com.pda.backend.utils.KeycloakUtil;
import org.keycloak.exceptions.TokenNotActiveException;
import org.keycloak.exceptions.TokenSignatureInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import javax.ws.rs.QueryParam;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RequestMapping(value = "api/v1/user", produces = {"application/json", "application/x-www-form-urlencoded"}, consumes = {"application/json", "application/x-www-form-urlencoded"})
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    LoginService loginService;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    AppContext appContext;

    @Autowired
    CacheManager cacheManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    public ResponseDTO signup(@Validated @RequestBody UserRegisterDTO userRegisterDTO,
                              BindingResult bindingResult)  {
        return userService.register(userRegisterDTO, bindingResult);
    }

    @PostMapping("/login")
    public String login(@Validated @RequestBody(required = false) LoginDTO loginDTO,
                        BindingResult bindingResult)  {
        UserResponseDTO responseDTO = loginService.login(loginDTO, bindingResult);
        return HttpUtils.convertJsonObjectToString(responseDTO);

    }

    @RequestMapping(value = "/generate-access-token", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String generateAccessToken(@RequestBody LoginDTO loginDTO)  {
        LOGGER.info("Fetching access token for : {}", loginDTO.getUserName());
        Boolean validation1=(Constants.PASSWORD.equalsIgnoreCase(loginDTO.getGrantType()) && loginDTO.getUserName() != null && loginDTO.getPassword() != null);
        Boolean validation2 =(Constants.REFRESH_TOKEN.equalsIgnoreCase(loginDTO.getGrantType()) && loginDTO.getRefreshToken() != null);
        if (validation1||validation2) {

            UserResponseDTO responseDTO = loginService.refreshAccessToken(loginDTO);
            return HttpUtils.convertJsonObjectToString(responseDTO);

        } else {
            throw new InvalidParameterException("Required params are missing. Combination of {username, password, grantType=password} OR {grantType=refresh_token, refreshToken} to be provided");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/logout", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE, produces = MediaType.ALL_VALUE)
    public ResponseDTO logout(@RequestHeader("access-token") String accessToken) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            responseDTO = loginService.logout(userId);

        } catch (Exception exception) {
            responseDTO = handleAccessTokenException(exception);
        }
        return responseDTO;

    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE, produces = MediaType.ALL_VALUE)
    public ResponseDTO resetPassword(@QueryParam("emailId") String emailId) {
        return keycloakService.forgotPassword(emailId);

    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public ResponseDTO changePassword(@RequestHeader("access-token") String accessToken, @RequestBody PasswordBodyDto passwordBody)  {

        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String emailId = KeycloakUtil.fetchEmailIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            responseDTO = userService.changeUserPassword(emailId, passwordBody);
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            loginService.logout(userId);

        } catch (Exception exception) {
            responseDTO = handleAccessTokenException(exception);
        }
        return responseDTO;

    }

    @RequestMapping(value = "/resend-verify-email", method = RequestMethod.POST)
    public ResponseDTO resendVerificationEmail(@QueryParam("emailId") String emailId) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            if (emailId == null) {
                responseDTO.setResponseCode(HttpStatus.BAD_REQUEST.value());
                responseDTO.setMessage("EmailId is blank. Please enter a valid EmailId ");
                return responseDTO;
            } else {
                return loginService.resendVerifyEmail(emailId);
            }

        } catch (Exception exception) {
            return handleAccessTokenException(exception);

        }
    }


    @RequestMapping(value = "/get-profile", method = RequestMethod.POST)
    public ResponseDTO getUserProfile(@RequestHeader("access-token") String accessToken){
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String emailId = KeycloakUtil.fetchEmailIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            return userService.fetchUserProfileDetail(emailId, accessToken);
        } catch (Exception exception) {
            responseDTO = handleAccessTokenException(exception);

        }
        return responseDTO;

    }

    @RequestMapping(value = "/update-profile", method = RequestMethod.POST)
    public ResponseDTO updateUserProfile(@RequestHeader("access-token") String accessToken, @RequestBody RegistryUserWithOsId registryUser) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String userEmail = registryUser.getEmailId();
            String emailId = KeycloakUtil.fetchEmailIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            ResponseDTO updateUserInfo = userService.updateUserProfile(accessToken, registryUser, emailId, Boolean.FALSE);
            responseDTO.setResponse(updateUserInfo.getResponse());
            responseDTO.setMessage("SuccessFully updated user Data");
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_OK);
            if (!userEmail.equalsIgnoreCase(emailId)) {
                responseDTO.setMessage("Successfully updated user data. You're trying to update your email. To complete the process, please click on email verification link received on your new email Id: " + userEmail);

            }
        } catch (Exception exception) {
            responseDTO = handleAccessTokenException(exception);

        }
        return responseDTO;
    }

    @RequestMapping(value = "/update-email-id", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.ALL_VALUE)
    public String updateUserEmailIdPostVerification(@QueryParam("emailId") String emailId, @QueryParam("userId") String userId) {

        ResponseDTO responseDTO = userService.updateEmailIdForUserProfile(emailId, userId, Boolean.TRUE);

        if (responseDTO.getResponseCode() == Constants.TWO_HUNDRED) {
            return appContext.getEmailUpdateSuccess();
        } else {
            return appContext.getEmailUpdateUnSuccessFul() + responseDTO.getMessage();
        }
    }

    @RequestMapping(value = "/complete-sign-up", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.ALL_VALUE)
    public String completeSignUp(@QueryParam("emailId") String emailId)  {
        ResponseDTO responseDTO = userService.createEntryInRegistryAfterEmailVerification(emailId);

        if (responseDTO.getResponseCode() == Constants.TWO_HUNDRED) {
            return Constants.USER_REGISTRATION_COMPLETE;
        } else {
            return Constants.EMAIL_VERIFICATION_LINK_EXPIRED;
        }
    }


    @RequestMapping(value = "/scan/user-detail", method = RequestMethod.POST)
    public ResponseDTO getUserDetailOnScanQrCode(@RequestHeader("access-token") String accessToken, @QueryParam("userId") String userId) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            ResponseDTO fetchedUseDetail = userService.fetchUserDetailOnScanQrCode(userId, accessToken);
            responseDTO.setResponse(fetchedUseDetail.getResponse());
            responseDTO.setResponseCode(HttpStatus.OK.value());
            responseDTO.setMessage(Constants.SCAN_USER_DETAIL);
        } catch (Exception exception) {
            responseDTO = handleAccessTokenException(exception);

        }
        return responseDTO;

    }

    private PublicKey toPublicKey(String publicKeyString) {
        try {
            byte[] bytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpecification = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpecification);
        } catch (Exception e) {
            LOGGER.error("Error Creating public key,Exception:",e);
            return null;
        }
    }


    public static ResponseDTO handleAccessTokenException(Exception accessTokenException) {
        ResponseDTO responseDTO = new ResponseDTO();

        if (accessTokenException instanceof TokenSignatureInvalidException) {
            LOGGER.error("Signature of  access token is improper. Missed some content of Access Token : {}", accessTokenException.getMessage());
            responseDTO.setMessage("Signature of  access token is improper. Missed some content of Access Token ");
            responseDTO.setResponseCode(HttpStatus.UNAUTHORIZED.value());
        } else if (accessTokenException instanceof TokenNotActiveException) {
            LOGGER.error("Inactive access token. Please try with new  access token : {}", accessTokenException.getMessage());
            responseDTO.setMessage("Inactive access token. Please try with new  access token ");
            responseDTO.setResponseCode(HttpStatus.UNAUTHORIZED.value());
        } else if (accessTokenException instanceof UserCreateException) {
            LOGGER.error("user not found with this EmailId : {}", accessTokenException.getMessage());
            responseDTO.setMessage("User not found with this EmailId.");
            responseDTO.setResponseCode(HttpStatus.UNAUTHORIZED.value());
        } else {
            LOGGER.error("Issue with Access token, please try again");
            responseDTO.setMessage("Issue with Access token, please try again");
            responseDTO.setResponseCode(HttpStatus.UNAUTHORIZED.value());
        }
        return responseDTO;
    }

}
