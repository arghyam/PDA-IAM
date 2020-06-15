package com.socion.backend.controller;

import com.socion.backend.AesUtil;
import com.socion.backend.config.AppContext;
import com.socion.backend.dto.*;
import com.socion.backend.dto.*;
import com.socion.backend.entity.RegistryUserWithOsId;
import com.socion.backend.service.LoginService;
import com.socion.backend.service.LoginV2Service;
import com.socion.backend.utils.Constants;
import com.socion.backend.utils.HttpUtils;
import com.socion.backend.utils.KeycloakUtil;
import org.apache.commons.io.FileUtils;
import org.keycloak.common.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


@RestController
@RequestMapping(value = "api/v2/user", produces = {"application/json", "application/x-www-form-urlencoded"})
public class LoginController {

    @Autowired
    LoginV2Service loginV2Service;

    @Autowired
    AppContext appContext;

    @Autowired
    UserController userController;

    @Autowired
    LoginService loginService;


    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @PostMapping(path = "/register", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseDTO signup(@Validated @RequestBody SignUpDTO signUpDTO,
                              BindingResult bindingResult) throws IOException {
        return loginV2Service.register(bindingResult, signUpDTO);
    }


    @PostMapping(path = "/login", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String login(@Validated @RequestBody(required = false) LoginDTO loginDTO,
                        BindingResult bindingResult) throws IOException {

        UserResponseDTO responseDTO = loginV2Service.login(loginDTO, bindingResult);
        return HttpUtils.convertJsonObjectToString(responseDTO);

    }


    @GetMapping(value = "/send-otp")
    public ResponseDTO send(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("typeOfOTP") String typeOfOTP, @RequestParam(required = false, name = "countryCode") String countryCode) throws IOException {
        return loginV2Service.validateUserNameAndSendOTP(phoneNumber, typeOfOTP, countryCode);
    }

    @GetMapping(value = "/get-country-codes")
    public ResponseDTO getcountryCodes() throws IOException {
        return loginV2Service.getCountryCodes();
    }

    @PostMapping(value = "/change-password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseDTO changePassword(@RequestHeader("access-token") String accessToken,
                                      @RequestBody PasswordBodyDto passwordBody,
                                      BindingResult bindingResult) {

        ResponseDTO responseDTO = new ResponseDTO();
        try {
            responseDTO = loginV2Service.changeUserPassword(accessToken, passwordBody, bindingResult);
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            loginService.logout(userId);

        } catch (Exception exception) {
            responseDTO = userController.handleAccessTokenException(exception);
        }
        return responseDTO;
    }

    @PostMapping(value = "/set-password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String setPassword(@RequestBody SetPasswordDto passwordBody,
                              BindingResult bindingResult) throws IOException {
        UserResponseDTO responseDTO = loginV2Service.setUserPassword(passwordBody, bindingResult);
        return HttpUtils.convertJsonObjectToString(responseDTO);

    }

    @PostMapping(value = "/forgot-password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String forgotPassword(@RequestBody SetPasswordDto passwordBody,
                                 BindingResult bindingResult) throws IOException {
        UserResponseDTO responseDTO = loginV2Service.resetUserPassword(passwordBody, bindingResult);
        return HttpUtils.convertJsonObjectToString(responseDTO);

    }

    @PostMapping(value = "/validate-otp", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseDTO validateOtp(@RequestBody ValidateOtpDto otpDto,
                                   BindingResult bindingResult) throws IOException {
        return loginV2Service.validateOtp(otpDto, bindingResult);

    }

    @GetMapping(value = "/update-phone-number-verification")
    public ResponseDTO updatePhoneNumberVerification(@RequestParam("phoneNumber")
                                                             String phoneNumber, @RequestHeader("access-token") String accessToken, @RequestParam("countryCode") String countryCode) throws VerificationException, IOException {

        /*if (!PatternUtils.validatePhone(phoneNumber)) {
            return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Please Check valid phone number");
        }*/

        ResponseDTO responseDTO = loginV2Service.updatePhoneNumberVerification(phoneNumber, accessToken, countryCode);

        if (responseDTO.getResponseCode() == Constants.TWO_HUNDRED) {
            return responseDTO;
        } else {
            return HttpUtils.onFailure(responseDTO.getResponseCode(), Constants.PHONE_UPDATE_VERIFY_FAILURE + responseDTO.getMessage());
        }
    }

    @GetMapping(value = "/send-otp-new-phone")
    public ResponseDTO sendOtptoNewPhoneForUpdatePhone(@RequestParam("phoneNumber")
                                                               String phoneNumber, @RequestHeader("access-token") String accessToken, @RequestParam("countryCode") String countryCode) throws VerificationException, IOException {
        LOGGER.info("counrty code is {}", countryCode);
        return loginV2Service.updatePhoneNumberPostEmailVerification(phoneNumber, accessToken, countryCode);
    }

    @GetMapping(value = "/update-email-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO updateUserEmailId(@RequestParam("emailId") String emailId, @RequestHeader("access-token") String accessToken) throws VerificationException, IOException {
        String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
        ResponseDTO responseDTO = loginV2Service.updateEmailIdForUserProfile(emailId, userId, Boolean.FALSE, accessToken, null);
        if (responseDTO.getResponseCode() == Constants.TWO_HUNDRED) {
            return HttpUtils.onSuccess(null, "Please check your email to complete the new email verification process");
        } else {
            return HttpUtils.onFailure(responseDTO.getResponseCode(), "Error in Sending verification Email" + responseDTO.getMessage());
        }
    }

    @GetMapping(value = "/update-email-id-post-verification", produces = MediaType.TEXT_HTML_VALUE)
    public String updateUserEmailIdPostVerification(@RequestParam("emailId") String emailId, @RequestParam("userId") String userId, @RequestParam("date") String date, @RequestParam("emailUpdateId") String emailUpdateId) throws VerificationException, IOException {
        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        String decryptedDateTime = aesUtil.decrypt32(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), date);
        LOGGER.info("EMAIL TIME{}", decryptedDateTime);
        LOGGER.info("CURRENT TIME:{}", LocalDateTime.now().toString());
        if (LocalDateTime.now().minusMinutes(10l).isAfter(LocalDateTime.parse(decryptedDateTime))) {
            File htmlTemplateFile = new File(appContext.getEmailUpdateUnSuccessFul());
            String htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");
            htmlString = htmlString.replace("$error", "Link expired");
            return htmlString;
        }
        ResponseDTO responseDTO = loginV2Service.updateEmailIdForUserProfile(emailId, userId, Boolean.TRUE, null, emailUpdateId);
        if (responseDTO.getResponseCode() == Constants.TWO_HUNDRED) {
            RegistryUserWithOsId registryUserWithOsId = getUserById(userId);
            File htmlTemplateFile = new File(appContext.getEmailUpdateSuccess());
            String htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");
            htmlString = htmlString.replace("$name", registryUserWithOsId.getName());
            return htmlString;
        } else {
            File htmlTemplateFile = new File(appContext.getEmailUpdateUnSuccessFul());
            String htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");
            htmlString = htmlString.replace("$error", responseDTO.getMessage());
            return htmlString;
        }
    }

    @RequestMapping(value = "/generate-access-token", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String generateAccessToken(@RequestBody LoginDTO loginDTO) throws IOException {
        LOGGER.info("Fetching access token for : {}", loginDTO.getUserName());
        Boolean validation1 = (Constants.PASSWORD.equalsIgnoreCase(loginDTO.getGrantType()) && loginDTO.getUserName() != null && loginDTO.getPassword() != null);
        Boolean validation2 = (Constants.REFRESH_TOKEN.equalsIgnoreCase(loginDTO.getGrantType()) && loginDTO.getRefreshToken() != null);
        if (validation1 || validation2) {

            UserResponseDTO responseDTO = loginService.refreshAccessToken(loginDTO);

            return HttpUtils.convertJsonObjectToString(responseDTO);
        } else {
            throw new InvalidParameterException("Required params are missing. Combination of {username, password, grantType=password} OR {grantType=refresh_token, refreshToken} to be provided");
        }
    }

    @GetMapping(path = "/get-profile")
    public ResponseDTO getUserProfile(@RequestHeader("access-token") String accessToken) throws VerificationException, IOException {
        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            return loginV2Service.fetchUserProfileDetail(userId, accessToken);
        } catch (Exception exception) {
            return userController.handleAccessTokenException(exception);
        }
    }

    @PostMapping(path = "/get-profile-from-phoneNumber")
    public ResponseDTO getUserProfileFromNumber(@RequestHeader("access-token") String accessToken, @RequestBody PhoneNumberListDTO phoneNumberDTO) {
        try {
            KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            return loginV2Service.fetchUserProfileDetailFromPhoneNUmber(phoneNumberDTO, accessToken);
        } catch (Exception exception) {
            return userController.handleAccessTokenException(exception);
        }


    }

    @GetMapping(value = "/scan/user-detail")
    public ResponseDTO getUserDetailOnScanQrCode(@RequestHeader("access-token") String accessToken, @RequestParam("userId") String userId) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            ResponseDTO fetchedUseDetail = loginV2Service.fetchUserDetailOnScanQrCode(userId, accessToken);
            responseDTO.setResponse(fetchedUseDetail.getResponse());
            responseDTO.setResponseCode(HttpStatus.OK.value());
            responseDTO.setMessage(Constants.SCAN_USER_DETAIL);
        } catch (Exception exception) {
            responseDTO = userController.handleAccessTokenException(exception);

        }
        return responseDTO;
    }

    @CrossOrigin
    @RequestMapping(value = "/update-profile", method = RequestMethod.PUT)
    public ResponseDTO updateUserProfile(@RequestHeader("access-token") String accessToken, @RequestBody RegistryUserWithOsId registryUser) throws VerificationException, IOException {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String phoneNumber = KeycloakUtil.fetchPhoneNumberFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            UpdateUserProfileDTO updateUserProfileDTO = new UpdateUserProfileDTO();
            updateUserProfileDTO.setAccessToken(accessToken);
            LocalDateTime localDateTime = LocalDateTime.now();
            registryUser.setUpdtDttm((localDateTime.toString()));
            updateUserProfileDTO.setUpdatedRegistryUserBody(registryUser);
            updateUserProfileDTO.setEmail(null);
            updateUserProfileDTO.setPhoneNumber(phoneNumber);
            updateUserProfileDTO.setUpdateEmail(Boolean.FALSE);
            updateUserProfileDTO.setUpdatedEmailVerified(Boolean.FALSE);
            updateUserProfileDTO.setNewemail(null);
            updateUserProfileDTO.setUserId(null);
            ResponseDTO updateUserInfo = loginV2Service.updateUserProfile(updateUserProfileDTO);
            if (updateUserInfo.getResponseCode() != Constants.TWO_HUNDRED) {
                return updateUserInfo;
            }
            responseDTO.setResponse(updateUserInfo.getResponse());
            responseDTO.setMessage("Successfully updated User Data");
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_OK);
        } catch (Exception exception) {
            responseDTO = userController.handleAccessTokenException(exception);

        }
        return responseDTO;
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO logout(@RequestHeader("access-token") String accessToken) throws IOException, VerificationException {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            responseDTO = loginService.logout(userId);

        } catch (Exception exception) {
            responseDTO = userController.handleAccessTokenException(exception);
        }
        return responseDTO;
    }

    @PostMapping(path = "/change-status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO changeActiveStatus(@RequestHeader("access-token") String accessToken, @RequestBody ChangeStatusDto changeStatusDto) {
        return loginV2Service.changeActiveStatus(accessToken, changeStatusDto);
    }

    @PostMapping(value = "/update-photo")
    public ResponseDTO updateProfilePhoto(@RequestHeader("access-token") String accessToken, @RequestParam("isRemovePhoto") boolean isRemovePhoto) {
        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
            return loginV2Service.updateUserProfilePhoto(userId, isRemovePhoto);
        } catch (Exception exception) {
            return userController.handleAccessTokenException(exception);
        }


    }

    @PostMapping(value = "/private/all", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    private List<LinkedHashMap> getUsers(@RequestBody UserRequestTDO userRequestTDO) throws IOException {
        return loginV2Service.getUsersByUserIds(userRequestTDO.getUserIds());
    }

    @GetMapping(value = "/private/details/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    private RegistryUserWithOsId getUserById(@PathVariable(value = "userId") String userIds) throws IOException {
        return loginV2Service.getUsersByUserId(userIds);
    }

    @GetMapping(value = "/private/details/list", produces = MediaType.APPLICATION_JSON_VALUE)
    private List<RegistryUserWithOsId> getUserById(@RequestParam(value = "userId") List<String> userIds) throws IOException {
        List<RegistryUserWithOsId> allUserList = new ArrayList<>();
        for (String userId : userIds) {
           try{
            allUserList.add(loginV2Service.getUsersByUserId(userId));
           }catch (Exception e){
               LOGGER.info("User Not found with userId:{}",userId);
           }
        }
        return allUserList;
    }

}
