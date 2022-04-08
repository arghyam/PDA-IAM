package com.socion.backend.service.impl;

import com.socion.backend.AesUtil;
import com.socion.backend.config.AppContext;
import com.socion.backend.config.CacheConfiguration;
import com.socion.backend.dao.KeycloakDao;
import com.socion.backend.dao.KeycloakService;
import com.socion.backend.dao.RegistryDao;
import com.socion.backend.dto.*;
import com.socion.backend.entity.PhoneNumberRegistryUser;
import com.socion.backend.entity.RegistryUser;
import com.socion.backend.entity.RegistryUserWithOsId;
import com.socion.backend.entity.RegistryUserWithUserId;
import com.socion.backend.exceptions.NotFoundException;
import com.socion.backend.service.LoginService;
import com.socion.backend.service.LoginV2Service;
import com.socion.backend.service.UserService;
import com.socion.backend.utils.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socion.backend.dto.*;
import com.socion.backend.utils.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;


@Service
@Component
public class LoginV2ServiceImpl implements LoginV2Service {

    @Autowired
    UserService userService;


    @Autowired
    KeycloakDao keycloakDao;

    @Autowired
    LoginService loginService;

    @Autowired
    AppContext appContext;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    CacheConfiguration cacheConfiguration;

    @Autowired
    RegistryDao registryDao;

    @Autowired
    OTPUtils utils;

    @Autowired
    ProfileCardUtils profileCardUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginV2ServiceImpl.class);

    private static String REGISTRY_SUCCESS_RESPONSE = "SUCCESSFUL";


    @Override
    public ResponseDTO register(BindingResult bindingResult, SignUpDTO signUpDTO) throws IOException {
        LOGGER.info("============================================API CALL:/Register============================================");

        LOGGER.info("NAME{}:",signUpDTO.getName());
        userService.valiadtePojo(bindingResult);
        Keycloak kc = userService.getKeycloak();
        ResponseDTO response = new ResponseDTO();

        LOGGER.info("User not present");
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("dummyPassword");
        credential.setTemporary(false);


        ResponseDTO responseDTO = getCountryCodes();
        List<CountryCode> countryCodes = (List<CountryCode>) responseDTO.getResponse();
        for (CountryCode code : countryCodes) {
            if (signUpDTO.getCountryCode().equals(code.getCode()) && signUpDTO.getPhoneNumber().length() != code.getPhoneNumberLength()) {
                LOGGER.error("ERROR:phone number {} is invalid", signUpDTO.getPhoneNumber());
                return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), Constants.INVALID_PHONE_NO_LENGTH);
            }
        }


        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(signUpDTO.getPhoneNumber());

        userRepresentation.setCredentials(asList(credential));
        userRepresentation.setFirstName(signUpDTO.getName());
        userRepresentation.setEnabled(Boolean.TRUE);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(Constants.REG_ENTRY_CREATED, asList(Boolean.FALSE.toString()));
        attributes.put(Constants.PHONE_NUMBER, asList(signUpDTO.getCountryCode() + signUpDTO.getPhoneNumber()));
        LOGGER.info("new attributes put with country code {},{} ", signUpDTO.getCountryCode(), signUpDTO.getPhoneNumber());
        attributes.put(Constants.COUNTRY_CODE, asList(signUpDTO.getCountryCode()));
        attributes.put(Constants.IS_USER_VALIDATED, asList(Boolean.FALSE.toString()));

        userRepresentation.setAttributes(attributes);

        Response result = kc.realm(appContext.getRealm()).users().create(userRepresentation);

        if (result.getStatus() != Constants.TWO_ZERO_ONE && result.getStatus() == Constants.FOUR_ZERO_NINE) {
            UserRepresentation keycloakUser = keycloakService.getUserByUsername(kc.tokenManager().getAccessTokenString(),
                    signUpDTO.getPhoneNumber(), appContext.getRealm());
            if (keycloakUser.getAttributes().get(Constants.IS_USER_VALIDATED).get(0).equalsIgnoreCase(Boolean.FALSE.toString())) {
                String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
                keycloakDao.deleteUser(Constants.BEARER + adminAccessToken, keycloakUser.getId(), appContext.getRealm()).execute();
                kc.realm(appContext.getRealm()).users().create(userRepresentation);
            }

            if (keycloakUser.getAttributes().get(Constants.IS_USER_VALIDATED).get(0).equalsIgnoreCase(Boolean.TRUE.toString()) && keycloakUser.getAttributes().get("registry_entry_created").get(0).equalsIgnoreCase(Boolean.TRUE.toString())) {

                LOGGER.error("User already exists.");
                response = HttpUtils.onFailure(HttpStatus.CONFLICT.value(), Constants.USER_ALREADY_EXISTS);
                return response;
            }
        }

        UserRepresentation keycloakUser = keycloakService.getUserByUsername(kc.tokenManager().getAccessTokenString(),
                signUpDTO.getPhoneNumber(), appContext.getRealm());


        LOGGER.debug("Keycloak user with firstname {}  returned", keycloakUser.getFirstName());

        String otp = generateOtp();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletableFuture.runAsync(
                () -> {
                    try {

                        keycloakUser.setFirstName(signUpDTO.getName());
                        keycloakUser.setUsername(signUpDTO.getPhoneNumber());
                        LOGGER.info("======first name of the user is {}" + signUpDTO.getName());
                        LOGGER.info("======username of the user is {}" + signUpDTO.getPhoneNumber());

                        sendOTP(keycloakUser, Constants.REGISTRATION_KEYWORD, "Thanks for  registering with Socion. Your verification code is " + otp + ". Kindly verify your mobile number.", otp);
                        executorService.shutdown();
                        LOGGER.info("Done  Executing");
                    } catch (Exception e) {
                        LOGGER.error("Error  sending OTP And/or creating registry entry", e);
                    } finally {
                        if (executorService.isTerminated()) {
                            LOGGER.debug("Already  terminated");
                        }
                        if (executorService.isShutdown()) {
                            LOGGER.debug("Executor  shutdown");
                        } else {
                            executorService.shutdownNow();
                            LOGGER.debug("Shutting  down  now");
                        }
                    }
                }, executorService);

        response = HttpUtils.onSuccess(null, Constants.USER_CREATED_SUCCESSFULLY);
        return response;

    }

    private void sendOTP(UserRepresentation userRepresentation, String typeOfOTP, String message, String otp) {
        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        String encryptedOtp = aesUtil.encrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), otp);
        userRepresentation.getAttributes().put(Constants.OTP, asList(encryptedOtp));
        userRepresentation.getAttributes().put(Constants.TYPE_OF_OTP, asList(typeOfOTP));
        userRepresentation.getAttributes().put(Constants.OTP_EXPIRY_TIME, asList(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT))));
        keycloakService.updateUser(Constants.BEARER + keycloakService.generateAccessToken(appContext.getAdminUserName()), userRepresentation.getId(), userRepresentation, appContext.getRealm());

        LOGGER.debug("OTP sending to : {} ", userRepresentation.getAttributes().get(Constants.PHONE_NUMBER).get(0));
        if (Constants.UPDATE_PHONE_KEYWORD.equalsIgnoreCase(typeOfOTP)) {
            try {
                EmailUtils.sendEmail(appContext, userRepresentation.getEmail(), Constants.EMAIL_ACTION_UPDATE_PHONE, userRepresentation.getId(), userRepresentation.getFirstName(), otp,null);
            } catch (Exception e) {
                LOGGER.error("Error with exception", e);
            }
        } else {
            utils.sendOTP(userRepresentation.getAttributes().get(Constants.PHONE_NUMBER).get(0), message);
            LOGGER.debug("OTP sent");
        }
    }

    @Override
    public UserResponseDTO login(LoginDTO loginDTO, BindingResult bindingResult) {
        LOGGER.info("============================================API CALL:/Login============================================");
        userService.valiadtePojo(bindingResult);

        ResponseDTO responseDTO = getCountryCodes();
        List<CountryCode> countryCodes = (List<CountryCode>) responseDTO.getResponse();
        for (CountryCode code : countryCodes) {
            if (loginDTO.getCountryCode().equals(code.getCode()) && loginDTO.getUserName().length() != code.getPhoneNumberLength()) {
                LOGGER.error("ERROR:phone number {} is invalid", loginDTO.getUserName());
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setResponseCode(400);
                userResponseDTO.setResponse(null);
                userResponseDTO.setMessage(Constants.INVALID_PHONE_NO_LENGTH);
                return userResponseDTO;
            }
        }

        return getUserLogin(loginDTO);

    }

    private String generateOtp() {
        int randomPIN = (int) (Math.random() * Constants.NINE_LAKH) + Constants.ONE_LAKH;
        return String.valueOf(randomPIN);
    }

    public ResponseDTO changeUserPassword(String accessToken, PasswordBodyDto passwordBodyDto, BindingResult bindingResult) {
        LOGGER.info("============================================API CALL:/change-password============================================");
        userService.valiadtePojo(bindingResult);
        String phoneNumber = null;
        try {
            phoneNumber = KeycloakUtil.fetchPhoneNumberFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm());
        } catch (VerificationException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        ResponseDTO response = new ResponseDTO();

        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());

        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());

        UserRepresentation keycloakUser = keycloakService.getUserByUsername(adminAccessToken, phoneNumber, appContext.getRealm());

        if (keycloakUser.getUsername() == null) {
            response = HttpUtils.onSuccess(null, "User with this PhoneNumber does not exist");
            return response;
        }

        String decryptedCurrentPassword;
        String decryptedUpdatedPassword;
        try {
            decryptedCurrentPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                    appContext.getSecretKey(), passwordBodyDto.getCurrentPassword());

            decryptedUpdatedPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                    appContext.getSecretKey(), passwordBodyDto.getNewPassword());
        } catch (Exception e) {
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Please send the Passwords in the encrypted format. Error -> " + e);
            LOGGER.debug(Constants.PASSWORD_NOT_ENCRYPTED_MESSAGE);
            return response;
        }


        if (decryptedCurrentPassword.equals(decryptedUpdatedPassword)) {
            LOGGER.debug("Current password and updated password should be different");
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Current password and updated password should be different");
            return response;
        }

        retrofit2.Response<AccessTokenResponseDTO> userAccessToken = null;
        try {
            userAccessToken = keycloakDao.generateAccessTokenUsingCredentials(appContext.getRealm(), keycloakUser.getUsername(),
                    decryptedCurrentPassword, appContext.getClientId(), appContext.getGrantType(), appContext.getClientSecret()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (!userAccessToken.isSuccessful()) {
            LOGGER.debug("Current password value is incorrect");
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Current password value is incorrect");
            return response;
        }

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(decryptedUpdatedPassword);
        credential.setTemporary(false);
        keycloakUser.setCredentials(asList(credential));
        retrofit2.Response<ResponseBody> passwordupdated = null;
        try {
            passwordupdated = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, keycloakUser.getId(), keycloakUser, appContext.getRealm()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (!passwordupdated.isSuccessful()) {
            LOGGER.debug("Password was not successfully Set.");
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Updating Password Failed.");
            return response;
        }
        response = HttpUtils.onSuccess(null, "Password successfully updated.");
        LocalDateTime now = LocalDateTime.now();
        saveNotification(new NotificationDTO(null, keycloakUser.getId(), Constants.CHANGE_PASSWORD, NotificationEvents.USER.toString(), now.toLocalDate().toString() + " " + now.toLocalTime().toString(), false));
        return response;
    }


    @Override
    public ResponseDTO validateOtp(ValidateOtpDto otpDto, BindingResult bindingResult) {

        LOGGER.info("============================================API CALL:/validate-otp============================================");
        LOGGER.info("otp country code from the dto is {}", otpDto.getCountryCode());

        userService.valiadtePojo(bindingResult);
// Country code validation was incorrect and this is to fix it.
        if (!otpDto.getCountryCode().substring(0, 1).equalsIgnoreCase("+")) {
            String countryCode = "+" + otpDto.getCountryCode().substring(0, otpDto.getCountryCode().length());
            otpDto.setCountryCode(countryCode);
        }

        if (!isOtpTypeValid(otpDto.getTypeOfOTP())) {
            return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Invalid Otp Type");
        }
        Keycloak keycloak = userService.getKeycloak();
        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        ResponseDTO response = new ResponseDTO();
        String token = keycloak.tokenManager().getAccessTokenString();

        UserRepresentation userRepresentation = keycloakService.getUserByUsername(token, otpDto.getPhoneNumber(), appContext.getRealm());

        String typeOfOTP = userRepresentation.getAttributes().get(Constants.TYPE_OF_OTP).get(0);
        if (!Constants.NEW_PHONE_KEYWORD_OTP.equalsIgnoreCase(typeOfOTP) && !AppUtils.compareCountryCode(otpDto.getCountryCode(), AppUtils.getCountryCode(userRepresentation))) {
            return HttpUtils.onFailure(HttpStatus.NOT_FOUND.value(), Constants.USER_DOES_NOT_EXIST);
        }

        if (userRepresentation.getUsername() == null) {
            LOGGER.debug(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            response.setResponseCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            return response;
        }

        String decryptedOtpReceived;
        try {
            decryptedOtpReceived = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                    appContext.getSecretKey(), otpDto.getOtp());
        } catch (Exception e) {
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Please send the OTP in the encrypted format. Error -> " + e);
            LOGGER.debug("OTP Validation Failed");
            return response;
        }
        boolean otpvalidation = (userRepresentation.getAttributes().get(Constants.OTP_EXPIRY_TIME) == null
                || userRepresentation.getAttributes().get(Constants.OTP) == null
                || userRepresentation.getAttributes().get(Constants.OTP).get(0) == null);
        boolean otpvalidation1 = (userRepresentation.getAttributes().get(Constants.OTP_EXPIRY_TIME).get(0) == null
                || userRepresentation.getAttributes().get(Constants.TYPE_OF_OTP) == null
                || userRepresentation.getAttributes().get(Constants.TYPE_OF_OTP).get(0) == null);
        if (otpvalidation || otpvalidation1) {
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Otp Validation Failed. Please resend the OTP.");
            LOGGER.debug("OTP Validation Failed");
            return response;
        }

        String encryptedOtpStored = userRepresentation.getAttributes().get(Constants.OTP).get(0);
        String decryptedOtpStored = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), encryptedOtpStored);


        String otpGeneratedTimestamp = userRepresentation.getAttributes().get(Constants.OTP_EXPIRY_TIME).get(0);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT));

        Date otpDate = null;
        try {
            otpDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(otpGeneratedTimestamp);
        } catch (ParseException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        Date currentDate = null;
        try {
            currentDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(now);
        } catch (ParseException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        int differenceInSecs = (int) (currentDate.getTime() - otpDate.getTime()) / Constants.THOUSAND;
        double differenceInMinutes = differenceInSecs / Constants.SIXTY;

        if (differenceInMinutes > Constants.FIVE) {
            LOGGER.debug("OTP Expired");
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "OTP has been expired");
            return response;
        }

        if (!decryptedOtpStored.equalsIgnoreCase(decryptedOtpReceived) || !(typeOfOTP.equalsIgnoreCase(otpDto.getTypeOfOTP()))) {
            LOGGER.debug("Incorrect OTP");
            response = HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Incorrect OTP");
            return response;
        }

        if (Constants.NEW_PHONE_KEYWORD_OTP.equalsIgnoreCase(typeOfOTP)) {
            List<String> list = userRepresentation.getAttributes().get(Constants.NEW_PHONE_NUMBER);
            if (null != list && !list.isEmpty()) {
                if (doesUserExists(list.get(0), token)) {
                    return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), Constants.USER_ALREADY_EXISTS);
                }
                userRepresentation.getAttributes().put(Constants.PHONE_NUMBER, asList(otpDto.getCountryCode() + list.get(0)));

                userRepresentation.getAttributes().put(Constants.COUNTRY_CODE, asList(AppUtils.getNewCountryCode(userRepresentation)));
                updatePhone(list.get(0), otpDto.getCountryCode(), userRepresentation.getId(), token, userRepresentation);
                userRepresentation.setUsername(list.get(0));

                userRepresentation.getAttributes().remove(Constants.NEW_COUNTRY_CODE);
                userRepresentation.getAttributes().remove(Constants.NEW_PHONE_NUMBER);
                LOGGER.info("++++++++++++++++++++++++New Phone number:{}", userRepresentation.getAttributes().get("phone_number"));
            }
        }

        userRepresentation.getAttributes().remove(Constants.OTP);
        userRepresentation.getAttributes().remove(Constants.TYPE_OF_OTP);
        userRepresentation.getAttributes().remove(Constants.OTP_EXPIRY_TIME);

        userRepresentation.getAttributes().put(Constants.OTP_VALIDATED_FOR, asList(typeOfOTP));


        keycloakService.updateUser(Constants.BEARER + token, userRepresentation.getId(), userRepresentation, appContext.getRealm());

        response = HttpUtils.onSuccess(null, "Otp Successfully Validated.");
        return response;
    }

    private boolean isOtpTypeValid(String typeOfOTP) {
        boolean otpvalid = (Constants.REGISTRATION_KEYWORD.equals(typeOfOTP) || Constants.FORGOT_PASSWORD_KEYWORD.equals(typeOfOTP));
        boolean otpvalid1 = (Constants.UPDATE_PHONE_KEYWORD.equals(typeOfOTP)
                || Constants.NEW_PHONE_KEYWORD_OTP.equals(typeOfOTP) || Constants.UPDATE_EMAIL_KEYWORD.equals(typeOfOTP));
        return otpvalid || otpvalid1;
    }

    @Override
    public UserResponseDTO setUserPassword(SetPasswordDto passwordBody, BindingResult bindingResult) {
        LOGGER.info("============================================API CALL:/set-password============================================");
        userService.valiadtePojo(bindingResult);

        ResponseDTO responseDTO = getCountryCodes();
        List<CountryCode> countryCodes = (List<CountryCode>) responseDTO.getResponse();
        for (CountryCode code : countryCodes) {
            LOGGER.error("ERROR:phone number {} is invalid", passwordBody.getUserName());
            if (passwordBody.getCountryCode().equals(code.getCode()) && passwordBody.getUserName().length() != code.getPhoneNumberLength()) {
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setResponseCode(400);
                userResponseDTO.setResponse(null);
                userResponseDTO.setMessage(Constants.INVALID_PHONE_NO_LENGTH);
                return userResponseDTO;
            }
        }

        UserResponseDTO response = new UserResponseDTO();
        Keycloak keycloak = userService.getKeycloak();
        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());

        UserRepresentation keycloakUser = keycloakService.getUserByUsername(keycloak.tokenManager().getAccessTokenString(),
                passwordBody.getUserName(), appContext.getRealm());

        if (keycloakUser.getUsername() == null) {
            LOGGER.debug(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            response.setResponseCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            return response;
        }

        String decryptedPassword;

        try {
            decryptedPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                    appContext.getSecretKey(), passwordBody.getPassword());
        } catch (Exception e) {
            response.setMessage(Constants.SEND_PASSWORD_IN_ENCRYPTD_FORMAT + e);
            response.setResponseCode(HttpStatus.BAD_REQUEST.value());
            LOGGER.debug(Constants.PASSWORD_NOT_ENCRYPTED_MESSAGE);
            return response;
        }

        if (keycloakUser.getAttributes().get(Constants.OTP_VALIDATED_FOR) == null
                || keycloakUser.getAttributes().get(Constants.OTP_VALIDATED_FOR).get(0) == null
                || !keycloakUser.getAttributes().get(Constants.OTP_VALIDATED_FOR).get(0).equalsIgnoreCase(Constants.REGISTRATION_KEYWORD)) {
            LOGGER.debug("OTP not validated before setting the password,");
            response.setResponseCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Please validate the OTP before setting the password.");
            return response;
        }

        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(decryptedPassword);
        credential.setTemporary(false);
        keycloakUser.setCredentials(asList(credential));

        retrofit2.Response<ResponseBody> passwordupdated = null;
        try {
            passwordupdated = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, keycloakUser.getId(), keycloakUser, appContext.getRealm()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (!passwordupdated.isSuccessful()) {
            LOGGER.debug("Password was not Successfully Set.");
            response.setResponseCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            return response;
        }

        LOGGER.info("Password Set Successfully.");


        keycloakUser.getAttributes().remove(Constants.OTP_VALIDATED_FOR);
        keycloakUser.getAttributes().put(Constants.IS_USER_VALIDATED, asList(Boolean.TRUE.toString()));
        retrofit2.Response<ResponseBody> userUpdated = null;
        try {
            userUpdated = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, keycloakUser.getId(), keycloakUser, appContext.getRealm()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        LOGGER.info(userUpdated.message());

        UserResponseDTO registryResponseDto = null;
        try {
            registryResponseDto = createEntryInRegistry(passwordBody.getUserName());
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (registryResponseDto.getResponseCode() != Constants.TWO_ZERO_ONE) {
            return registryResponseDto;
        }

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUserName(passwordBody.getUserName());
        loginDTO.setPassword(passwordBody.getPassword());
        loginDTO.setCountryCode(passwordBody.getCountryCode());
        response = getUserLogin(loginDTO);
        return response;
    }


    @Override
    public ResponseDTO validateUserNameAndSendOTP(String phoneNumber, String typeOfOTP, String countryCode) throws IOException {
        LOGGER.info("============================================API CALL:/send-otp============================================");
        if (!isOtpTypeValid(typeOfOTP)) {
            return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), "Invalid Otp Type");
        }
        LOGGER.info("PhoneNumber:{} , CountryCode:{}", phoneNumber, countryCode);


        if (!countryCode.substring(1, 2).equalsIgnoreCase("+")) {
            countryCode = "+" + countryCode.substring(1, countryCode.length());
        }


        ResponseDTO responseDTO = getCountryCodes();
        List<CountryCode> countryCodes = (List<CountryCode>) responseDTO.getResponse();
        for (CountryCode code : countryCodes) {
            if (countryCode.equals(code.getCode()) && phoneNumber.length() != code.getPhoneNumberLength()) {
                LOGGER.error("ERROR:phone number {} is invalid", phoneNumber);
                return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), Constants.INVALID_PHONE_NO_LENGTH);
            }
        }

        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        ResponseDTO response = new ResponseDTO();

        if (!doesUserExistsInRegistry(phoneNumber, adminAccessToken)) {

            response = HttpUtils.onFailure(HttpStatus.NOT_FOUND.value(), "User with the PhoneNumber does not exist");
            return response;
        }

        RegistryUserWithOsId registryUserWithOsId = getUserFromRegistry(null, phoneNumber, adminAccessToken);
        if (!registryUserWithOsId.getCountryCode().equalsIgnoreCase(countryCode)) {
            return HttpUtils.onFailure(HttpStatus.NOT_FOUND.value(), "User with the PhoneNumber does not exist");
        }

        Keycloak keycloak = userService.getKeycloak();
        UserRepresentation keycloakUser = keycloakService.getUserByUsername(keycloak.tokenManager().getAccessTokenString(),
                phoneNumber, appContext.getRealm());



        if (keycloakUser == null || keycloakUser.getUsername() == null) {
            LOGGER.debug(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);

            response = HttpUtils.onFailure(HttpStatus.NOT_FOUND.value(), "User with the PhoneNumber does not exist");
            return response;
        }

        if (Constants.FORGOT_PASSWORD_KEYWORD.equalsIgnoreCase(typeOfOTP) && (keycloakUser.getAttributes().get("registry_entry_created") == null || (("false").equals(keycloakUser.getAttributes().get("registry_entry_created").get(0))))) {
            LOGGER.debug(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            response = HttpUtils.onFailure(HttpStatus.NOT_FOUND.value(), "User with the PhoneNumber does not exist");
            return response;
        }
        String otp = generateOtp();

        try {
            if (typeOfOTP.equalsIgnoreCase(Constants.UPDATE_EMAIL_KEYWORD)) {
                sendOTP(keycloakUser, typeOfOTP, otp + " is your verification code to update the Email.", otp);
            } else {
                sendOTP(keycloakUser, typeOfOTP, otp + " is your verification code to reset the password.", otp);
            }
        } catch (Exception e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        response = HttpUtils.onSuccess(null, "OTP Sent successfully");

        return response;
    }

    private UserResponseDTO getUserLogin(LoginDTO loginDTO) {
        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());

        UserResponseDTO responseDTO = new UserResponseDTO();

        try {
            aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                    appContext.getSecretKey(), loginDTO.getPassword());
        } catch (Exception e) {
            responseDTO.setMessage(Constants.SEND_PASSWORD_IN_ENCRYPTD_FORMAT + e);
            responseDTO.setResponseCode(HttpStatus.BAD_REQUEST.value());
            LOGGER.debug(Constants.PASSWORD_NOT_ENCRYPTED_MESSAGE);
            return responseDTO;
        }

        loginDTO.setPassword(loginDTO.getPassword());


        UserLoginResponseDTO userLoginResponseDTO = new UserLoginResponseDTO();

        try {
            String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
            UserRepresentation userRepresentation = keycloakService.getUserByUsername(adminAccessToken, loginDTO.getUserName(), appContext.getRealm());

            if (userRepresentation.getAttributes().get(Constants.IS_USER_VALIDATED).get(0).equalsIgnoreCase(Boolean.FALSE.toString())) {
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setMessage(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
                userResponseDTO.setResponseCode(HttpStatus.NOT_FOUND.value());
                return userResponseDTO;
            }

            AccessTokenResponseDTO accessTokenResponseDTO = loginService.userLogin(loginDTO, responseDTO);

            if (responseDTO.getResponseCode() != Constants.TWO_HUNDRED) {
                return responseDTO;
            }
            userLoginResponseDTO.setAccessTokenResponseDTO(accessTokenResponseDTO);

            RegistryUserWithOsId registryUserWithOsId = getUserFromRegistryByUserID(userRepresentation.getId(), accessTokenResponseDTO.getAccessToken());
            if (registryUserWithOsId == null) {
                responseDTO.setMessage(Constants.USER_NOT_FOUND);
                responseDTO.setResponseCode(HttpStatus.NOT_FOUND.value());
            }

            if (!AppUtils.compareCountryCode(loginDTO.getCountryCode(), registryUserWithOsId.getCountryCode())) {
                responseDTO.setMessage(Constants.USER_DOES_NOT_EXIST);
                responseDTO.setResponseCode(HttpStatus.NOT_FOUND.value());
                return responseDTO;
            }

            UserDTO user = new UserDTO();
            user.setEmailId(userRepresentation.getEmail());
            user.setPhoneNumber(userRepresentation.getAttributes().get(Constants.PHONE_NUMBER).get(0));
            user.setUserName(userRepresentation.getUsername());
            user.setUserId(userRepresentation.getId());
            user.setName(userRepresentation.getFirstName());
            user.setActive(userRepresentation.isEnabled());
            userLoginResponseDTO.setUserDetails(user);
            userLoginResponseDTO.setEmailVerified(userRepresentation.isEmailVerified());
            responseDTO.setResponse(userLoginResponseDTO);

        } catch (Exception e) {
            LOGGER.info(Constants.ERRORLOG, e);
            responseDTO.setMessage(e.getMessage());
            responseDTO.setResponseCode(HttpStatus.UNAUTHORIZED.value());
            return responseDTO;
        }


        return responseDTO;
    }


    private UserResponseDTO createEntryInRegistry(String userName) throws IOException {

        UserResponseDTO response = new UserResponseDTO();
        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        UserRepresentation userRepresentation = keycloakService.getUserByUsername(adminAccessToken, userName, appContext.getRealm());

        LOGGER.debug("Fetching user with username {} from keycloak ", userName);

        keycloakService.updateUser(Constants.BEARER + adminAccessToken, userRepresentation.getId(), userRepresentation, appContext.getRealm());

        ProfileTemplateDto profileTemplateDto = new ProfileTemplateDto();
        profileTemplateDto.setUserName(userRepresentation.getFirstName());
        profileTemplateDto.setUserId(userRepresentation.getId());
        profileTemplateDto.setPhoto("");
        ResponseDTO profileCardResponse = profileCardUtils.generateUserProfileCard(profileTemplateDto);

        String profilePathUrl = "";
        if (profileCardResponse.getResponseCode() == Constants.TWO_HUNDRED) {
            profilePathUrl = profileCardResponse.getResponse().toString();
        }
        List<String> list = userRepresentation.getAttributes().get(Constants.COUNTRY_CODE);
        RegistryUser person = new RegistryUser();
        person.setName(userRepresentation.getFirstName());
        person.setEmailId("");
        person.setSalutation("");
        person.setUserId(userRepresentation.getId());
        person.setCrtdDttm(new java.util.Date().toString());
        person.setUpdtDttm(new java.util.Date().toString());
        person.setPhoto("");
        person.setPhoneNumber(userName);
        person.setActive(true);
        person.setProfileCardUrl(profilePathUrl);
        person.setCountryCode(list != null && !list.isEmpty() ? list.get(0) : null);
        Request request = new Request();
        request.setPerson(person);

        RegistryRequest registryRequest = new RegistryRequest(null, request, RegistryResponse.API_ID.CREATE.getId());

        try {
            Call<RegistryResponse> createRegistryEntryCall = registryDao.createUser(adminAccessToken, registryRequest);
            retrofit2.Response registryUserCreationResponse = createRegistryEntryCall.execute();
            if (!registryUserCreationResponse.isSuccessful()) {
                LOGGER.error("Error Creating registry entry {} ", registryUserCreationResponse.errorBody().string());
                response.setResponseCode(HttpStatus.BAD_REQUEST.value());
                response.setMessage("Error Creating registry entry" + registryUserCreationResponse.errorBody().string());
                return response;
            }
            userRepresentation.getAttributes().put(Constants.REG_ENTRY_CREATED, asList(Boolean.TRUE.toString()));
            retrofit2.Response updateKeycloakUser = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, userRepresentation.getId(), userRepresentation, appContext.getRealm()).execute();
            if (!updateKeycloakUser.isSuccessful()) {
                LOGGER.error("Error Updating user {} ", updateKeycloakUser.errorBody().string());
                response.setResponseCode(HttpStatus.BAD_REQUEST.value());
                response.setMessage("Error Updating user " + updateKeycloakUser.errorBody().string());
                return response;
            }
            LOGGER.info("Registry entry created and user is successfully logged in");

        } catch (IOException e) {
            LOGGER.error("Error creating registry entry : {} ", e);
            response.setResponseCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Error creating registry entry : " + e);
            return response;
        }

        response.setResponseCode(HttpStatus.CREATED.value());
        response.setMessage("Registry entry created successfully.");
        return response;
    }

    @Override
    public ResponseDTO updatePhoneNumberVerification(String phoneNumber, String accessToken, String countryCode) {
        LOGGER.info("============================================API CALL:/update-phone-number-verification=========================================");
        ResponseDTO responseDTO = getCountryCodes();
        List<CountryCode> countryCodes = (List<CountryCode>) responseDTO.getResponse();
        for (CountryCode code : countryCodes) {
            if (countryCode.equals(code.getCode()) && phoneNumber.length() != code.getPhoneNumberLength()) {
                LOGGER.error("ERROR:phone number {} is invalid", phoneNumber);
                return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), Constants.INVALID_PHONE_NO_LENGTH);
            }
        }


        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm());
            String adminAccesToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
            UserRepresentation user = keycloakService.getUserById(" BEARER " + adminAccesToken, userId, appContext.getRealm());


            if (null != user.getEmail() && !user.getEmail().isEmpty()) {
                String otp = generateOtp();
                CompletableFuture.runAsync(() -> {
                    try {
                        sendOTP(user, Constants.UPDATE_PHONE_KEYWORD, "Thanks for registering with Socion. Your verification code is " + otp + ". Kindly verify your mobile number.", otp);
                    } catch (Exception e) {
                        LOGGER.error("Error sending OTP email for user {} for updating emailId.", user.getEmail(), e);
                    }
                });
                return HttpUtils.onSuccess(null, "Sending Otp to email");
            } else {
                return HttpUtils.onSuccess(null, "Email doesn't Exist");
            }
        } catch (VerificationException e) {
            LOGGER.error(Constants.ERRORLOG, e);
            return HttpUtils.onFailure(Constants.FIVE_HUNDRED, e.getMessage());
        }
    }

    private boolean doesUserExists(String phoneNumber, String adminAccessToken) {
        try {
            UserRepresentation user = keycloakService.getUserByUsername(adminAccessToken, phoneNumber, appContext.getRealm());
            if (user != null) {
                return true;
            }
            return doesUserExistsInRegistry(phoneNumber, adminAccessToken);
        } catch (Exception e) {
            LOGGER.error("Exception while checking the existence of user in keycloak: {}", e);
        }
        return false;
    }

    private boolean doesUserExistsInRegistry(String phoneNumber, String adminAccessToken) {
        SlimRegistryUserPhnumberDto searchRegistryUserDto = new SlimRegistryUserPhnumberDto();
        searchRegistryUserDto.setId(RegistryResponse.API_ID.SEARCH.getId());
        searchRegistryUserDto.setVer("1.0");

        PhoneNumberRegistryUser person = new PhoneNumberRegistryUser();
        person.setPhoneNumber(phoneNumber);

        SlimRequestPhNumber request = new SlimRequestPhNumber();
        request.setPerson(person);
        searchRegistryUserDto.setRequest(request);
        RegistryResponse registryResponse;
        try {
            registryResponse = registryDao.searchUserByPhoneNumber(adminAccessToken, searchRegistryUserDto).execute().body();
            if (registryResponse.getResult() != null && !((List<LinkedHashMap<String, Object>>) registryResponse.getResult()).isEmpty()) {
                LOGGER.debug("Retrieved User successfully with phoneNumber : {} ", phoneNumber);
                return true;
            } else {
                LOGGER.error("User does not exist in registry with phone number : {} ", phoneNumber);
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Error in Fetching user from registry: {}", e);
            return false;
        }
    }

    @Override
    public ResponseDTO updatePhoneNumberPostEmailVerification(String newPhoneNumber, String accessToken, String countryCode) {

        LOGGER.info("============================================API CALL:/send-otp-new-phone============================================");

        if (!countryCode.substring(1, 2).equalsIgnoreCase("+")) {
            countryCode = "+" + countryCode.substring(1, countryCode.length());
        }
        LOGGER.info("country code is {}", countryCode);

        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm());
            String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
            if (doesUserExists(newPhoneNumber, adminAccessToken)) {
                return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), Constants.USER_ALREADY_EXISTS);
            }

            UserRepresentation user = keycloakService.getUserById(Constants.BEARER + adminAccessToken, userId, appContext.getRealm());

            if (user.getUsername().equalsIgnoreCase(newPhoneNumber)) {
                return HttpUtils.onFailure(Constants.FOUR_HUNDRED, "Current Phone Number & New Phone Number are same. Please enter a different Phone Number");
            }

            String otp = generateOtp();
            sendOtpAndNewPhoneTemporarily(user, countryCode, newPhoneNumber, otp, otp + " is your verification code for new phone number.");
            return HttpUtils.onSuccess(null, "OTP Sent to New Phone Number");
        } catch (Exception e) {
            LOGGER.error("error logged as " + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_NOT_FOUND);
        }
    }

    private RegistryUserWithOsId updatePhone(String newPhoneNumber, String countryCode, String userId, String adminAccessToken,
                                             UserRepresentation user) {
        RegistryUserWithOsId updatedRegistryUserBody = new RegistryUserWithOsId();
        try {
            updatedRegistryUserBody = getUserFromRegistry(userId, user.getUsername(), adminAccessToken);
            LOGGER.info("++++++++++++++++++++++++New Phone number:{}", newPhoneNumber);
            updatedRegistryUserBody.setPhoneNumber(newPhoneNumber);
            updateUserProfile(adminAccessToken, updatedRegistryUserBody, user.getUsername(), countryCode);
            LocalDateTime dateTime = LocalDateTime.now();
            saveNotification(new NotificationDTO(null, userId, Constants.CHANGE_PHONE_NUM, NotificationEvents.USER.toString(), dateTime.toLocalDate().toString() + " " + dateTime.toLocalTime().toString(), false));
        } catch (Exception e) {
            LOGGER.error("error logged as " + e);
        }

        return updatedRegistryUserBody;
    }

    private void sendOtpAndNewPhoneTemporarily(UserRepresentation userRepresentation, String newCountryCode, String newPhoneNumber, String otp, String message) throws IOException {
        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        String encryptedOtp = aesUtil.encrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), otp);
        userRepresentation.getAttributes().put(Constants.OTP, asList(encryptedOtp));
        userRepresentation.getAttributes().put(Constants.TYPE_OF_OTP, asList(Constants.NEW_PHONE_KEYWORD_OTP));
        userRepresentation.getAttributes().put(Constants.NEW_PHONE_NUMBER, asList(newPhoneNumber));
        userRepresentation.getAttributes().put(Constants.NEW_COUNTRY_CODE, asList(newCountryCode));
        userRepresentation.getAttributes().put(Constants.OTP_EXPIRY_TIME, asList(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT))));
        keycloakService.updateUser(Constants.BEARER + keycloakService.generateAccessToken(appContext.getAdminUserName()), userRepresentation.getId(), userRepresentation, appContext.getRealm());

        String otpPhoneNumber = newCountryCode + newPhoneNumber;
        LOGGER.debug("OTP sending to : {} ", otpPhoneNumber);
        utils.sendOTP(otpPhoneNumber, message);
        LOGGER.debug("OTP sent");
    }

    private RegistryUserWithOsId getUserFromRegistryFromNumber(String countryCode, String phoneNumber, String accessToken) throws IOException {
        RegistryUserWithOsId registryUserWithOsId = new RegistryUserWithOsId();
//        if (cacheConfiguration.redisUserCacheManager().getCache(Constants.CACHE_REGISTRY_USER).get(userId) == null) {

        SlimRegistryUserPhnumberAndCountryCodeDto searchRegistryUserDto = new SlimRegistryUserPhnumberAndCountryCodeDto();
        searchRegistryUserDto.setId(RegistryResponse.API_ID.SEARCH.getId());
        searchRegistryUserDto.setVer("1.0");

        SlimRequestPhoneNumber request=new SlimRequestPhoneNumber();
        PhoneNumberAndCountryCodeRegistryUser person=new PhoneNumberAndCountryCodeRegistryUser(phoneNumber,countryCode);
        request.setPerson(person);
        searchRegistryUserDto.setRequest(request);

        String registryUserString = null;

        LOGGER.debug(Constants.REMOVE_BEARER);
        RegistryResponse searchRegistryUser = registryDao.searchUserByPhoneNumberAndCountryCode(accessToken, searchRegistryUserDto).execute().body();
        LOGGER.debug("Searched User from Registry with phone number : {} ", phoneNumber);

        if (searchRegistryUser.getResult() != null && !((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).isEmpty()) {
            registryUserWithOsId = new RegistryUserWithOsId(((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).get(0));

            LOGGER.debug("Retrieved User successfully with phoneNumber : {} ", registryUserWithOsId.getPhoneNumber());
            registryUserString = HttpUtils.convertJsonObjectToString(registryUserWithOsId);
        } else {
            LOGGER.error("User does not exist in registry with phone number : {} ", phoneNumber);
            throw new IOException();
        }


        return registryUserWithOsId;

    }

    private RegistryUserWithOsId getUserFromRegistry(String userId, String phoneNumber, String accessToken) throws IOException {
        RegistryUserWithOsId registryUserWithOsId = new RegistryUserWithOsId();

        SlimRegistryUserPhnumberDto searchRegistryUserDto = new SlimRegistryUserPhnumberDto();
        searchRegistryUserDto.setId(RegistryResponse.API_ID.SEARCH.getId());
        searchRegistryUserDto.setVer("1.0");

        PhoneNumberRegistryUser person = new PhoneNumberRegistryUser();
        person.setPhoneNumber(phoneNumber);

        SlimRequestPhNumber request = new SlimRequestPhNumber();
        request.setPerson(person);
        searchRegistryUserDto.setRequest(request);

        String registryUserString = null;

        LOGGER.debug(Constants.REMOVE_BEARER);
        RegistryResponse searchRegistryUser = registryDao.searchUserByPhoneNumber(accessToken, searchRegistryUserDto).execute().body();
        LOGGER.debug("Searched User from Registry with phone number : {} ", phoneNumber);

        if (searchRegistryUser.getResult() != null && !((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).isEmpty()) {
            registryUserWithOsId = new RegistryUserWithOsId(((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).get(0));

                LOGGER.debug("Retrieved User successfully with phoneNumber : {} ", registryUserWithOsId.getPhoneNumber());
                registryUserString = HttpUtils.convertJsonObjectToString(registryUserWithOsId);
            } else {
                LOGGER.error("User does not exist in registry with phone number : {} ", phoneNumber);
                throw new IOException();
            }

        return registryUserWithOsId;

    }

    public ResponseDTO updateUserProfile(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String userName, String countryCode) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            responseDTO = updateKeycloakUser(accessToken, updatedRegistryUserBody, userName, countryCode);
            if (responseDTO.getResponseCode() != Constants.TWO_HUNDRED) {
                LOGGER.error("Error updating keycloak user");
                return responseDTO;
            }
            responseDTO = updateRegistryUser(accessToken, updatedRegistryUserBody, userName);
            return responseDTO;
        } catch (NotFoundException e) {
            LOGGER.error(Constants.USER_NOT_FOUND, updatedRegistryUserBody.getPhoneNumber(), e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        } catch (Exception e) {

            LOGGER.error(Constants.USER_UPDATION_FAILED, updatedRegistryUserBody.getPhoneNumber() + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_BAD_REQUEST, Constants.USER_DOES_NOT_EXIST);
        }

    }

    private ResponseDTO updateKeycloakUser(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String userName, String countryCode) {
        try {

            UserRepresentation existingUserRepresentation = keycloakService.getUserByUsername(accessToken, userName, appContext.getRealm());

            LOGGER.debug("Set updated values in Keycloak for user : {}", updatedRegistryUserBody.getPhoneNumber());
            UserRepresentation updatedUserRepresentation = new UserRepresentation();
            LOGGER.debug("Phone Number is updated");
            updatedUserRepresentation.setFirstName(updatedRegistryUserBody.getName());
            updatedUserRepresentation.setUsername(updatedRegistryUserBody.getPhoneNumber());
            updatedUserRepresentation.setEmail(updatedRegistryUserBody.getEmailId());
            updatedUserRepresentation.setAttributes(existingUserRepresentation.getAttributes());
            updatedUserRepresentation.getAttributes().remove(Constants.PHONE_NUMBER);
            updatedUserRepresentation.getAttributes().put(Constants.PHONE_NUMBER, asList(countryCode + updatedRegistryUserBody.getPhoneNumber()));
            String adminAccessToken = Constants.BEARER + keycloakService.generateAccessToken(appContext.getAdminUserName());
            retrofit2.Response keycloakResponse = keycloakDao.updateUser(adminAccessToken, updatedRegistryUserBody.getUserId(), updatedUserRepresentation, appContext.getRealm()).execute();
            if (keycloakResponse.isSuccessful() != Boolean.TRUE) {
                if ("Conflict".equalsIgnoreCase(keycloakResponse.message())) {
                    LOGGER.error(Constants.USER_UPDATION_FAILED, updatedRegistryUserBody.getPhoneNumber());
                    return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_CONFLICT, Constants.PHONE_NUMBER_EXISTS);
                } else {
                    LOGGER.error(Constants.USER_UPDATION_FAILED, updatedRegistryUserBody.getPhoneNumber());
                    return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
                }
            }
        } catch (NotFoundException e) {
            LOGGER.error(Constants.USER_NOT_FOUND, updatedRegistryUserBody.getEmailId() + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        } catch (IOException e) {

            LOGGER.error(Constants.USER_UPDATION_FAILED, updatedRegistryUserBody.getEmailId() + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_BAD_REQUEST, Constants.USER_DOES_NOT_EXIST);
        }
        LOGGER.info("User updation successful in Keycloak for user : {}", updatedRegistryUserBody.getEmailId());
        return HttpUtils.onSuccess(null, Constants.USER_UPDATE_SUCCESS_KEYCLOAK);
    }

    private ResponseDTO updateRegistryUser(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String phoneNumber) {
        try {
            LOGGER.debug("Set updated values in Registry for user : {}", phoneNumber);
            updatedRegistryUserBody.setUpdtDttm(new java.util.Date().toString());
            RequestWithOsId request = new RequestWithOsId();
            request.setPerson(updatedRegistryUserBody);
            RegistryRequestWithOsId registryRequest = new RegistryRequestWithOsId(null, request, RegistryResponse.API_ID.UPDATE.getId());
            RegistryResponse updateRegistryUser = registryDao.updateUser(accessToken, registryRequest).execute().body();
            LOGGER.debug("Successfully updated values in Registry for user : {}", updatedRegistryUserBody.getPhoneNumber());
            if (!REGISTRY_SUCCESS_RESPONSE.equalsIgnoreCase(updateRegistryUser.getResponseParams().getStatus().toString())) {
                LOGGER.error(Constants.USER_UPDATION_FAILED_REGISTRY, updatedRegistryUserBody.getPhoneNumber());
                return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
            }
        } catch (IOException e) {
            LOGGER.error(Constants.USER_UPDATION_FAILED_REGISTRY, updatedRegistryUserBody.getPhoneNumber() + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        }
        LOGGER.info(Constants.USER_UPDATE_SUCCESSFUL_FOR_REGISTRY, updatedRegistryUserBody.getPhoneNumber());
        return HttpUtils.onSuccess(null, Constants.USER_UPDATE_SUCCESS);
    }

    @Override
    public UserResponseDTO resetUserPassword(SetPasswordDto passwordBody, BindingResult bindingResult) {
        LOGGER.info("============================================API CALL:/forget-password============================================");
        userService.valiadtePojo(bindingResult);
        UserResponseDTO response = new UserResponseDTO();
        Keycloak keycloak = userService.getKeycloak();
        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());

        UserRepresentation keycloakUser = keycloakService.getUserByUsername(keycloak.tokenManager().getAccessTokenString(),
                passwordBody.getUserName(), appContext.getRealm());

        ResponseDTO responseDTO = getCountryCodes();
        List<CountryCode> countryCodes = (List<CountryCode>) responseDTO.getResponse();
        for (CountryCode code : countryCodes) {
            if (passwordBody.getCountryCode().equals(code.getCode()) && passwordBody.getUserName().length() != code.getPhoneNumberLength()) {
                LOGGER.error("ERROR:phone number {} is invalid", passwordBody.getUserName());
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setResponseCode(400);
                userResponseDTO.setResponse(null);
                userResponseDTO.setMessage(Constants.INVALID_PHONE_NO_LENGTH);
                return userResponseDTO;
            }
        }

        if (keycloakUser.getUsername() == null) {
            LOGGER.debug(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            response.setResponseCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            return response;
        }

        String decryptedPassword;

        try {
            decryptedPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                    appContext.getSecretKey(), passwordBody.getPassword());
        } catch (Exception e) {
            response.setMessage(Constants.SEND_PASSWORD_IN_ENCRYPTD_FORMAT + e);
            response.setResponseCode(HttpStatus.BAD_REQUEST.value());
            LOGGER.debug(Constants.PASSWORD_NOT_ENCRYPTED_MESSAGE);
            return response;
        }


        if (keycloakUser.getAttributes().get(Constants.OTP_VALIDATED_FOR) == null
                || keycloakUser.getAttributes().get(Constants.OTP_VALIDATED_FOR).get(0) == null
                || !keycloakUser.getAttributes().get(Constants.OTP_VALIDATED_FOR).get(0).equalsIgnoreCase(Constants.FORGOT_PASSWORD_KEYWORD)) {
            LOGGER.debug("OTP not validated before setting the password,");
            response.setResponseCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Please validate the OTP before setting the password.");
            return response;
        }

        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(decryptedPassword);
        credential.setTemporary(false);
        keycloakUser.setCredentials(asList(credential));

        retrofit2.Response<ResponseBody> passwordupdated = null;
        try {
            passwordupdated = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, keycloakUser.getId(), keycloakUser, appContext.getRealm()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        if (!passwordupdated.isSuccessful()) {
            LOGGER.debug("Password was not successfully Set.");
            response.setResponseCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(Constants.USER_WITH_PHONENUMBER_NOTEXISTS);
            return response;
        }

        keycloakUser.getAttributes().remove(Constants.OTP_VALIDATED_FOR);
        retrofit2.Response<ResponseBody> userUpdated = null;
        try {
            userUpdated = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, keycloakUser.getId(), keycloakUser, appContext.getRealm()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }

        LOGGER.info(userUpdated.message());

        LOGGER.info("Password Set Successfully.");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUserName(passwordBody.getUserName());
        loginDTO.setPassword(passwordBody.getPassword());
        loginDTO.setCountryCode(passwordBody.getCountryCode());

        LocalDateTime dateTime = LocalDateTime.now();

        saveNotification(new NotificationDTO(null, keycloakUser.getId(), Constants.FORGOT_PASSWORD, NotificationEvents.USER.toString(), dateTime.toLocalDate().toString() + " " + dateTime.toLocalTime().toString(), false));
        response = getUserLogin(loginDTO);
        return response;
    }

    @Override
    public ResponseDTO fetchUserProfileDetail(String userId, String accessToken) {
        LOGGER.info("============================================API CALL:/get-profile============================================");
        LOGGER.info("Fetching User from Registry");
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            RegistryUserWithOsId searchRegistryUser = getUserFromRegistryByUserID(userId, accessToken);
            responseDTO.setResponse(searchRegistryUser);
            responseDTO.setMessage("SuccessFully fetch user Data");
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_OK);
        } catch (IOException e) {
            LOGGER.error("User does not exist in registry with userId : {} ", userId + e);
            responseDTO.setMessage(Constants.USER_DOES_NOT_EXIST);
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_UNAUTHORIZED);
        }
        return responseDTO;
    }

    public ResponseDTO fetchUserProfileDetailFromPhoneNUmber(PhoneNumberListDTO phoneNumberListDTO, String accessToken) {
        LOGGER.info("============================================API CALL:/get-profile:PHONE-NUMBER============================================");
        LOGGER.info("Fetching User from Registry");
        ResponseDTO responseDTO = new ResponseDTO();
        List<PhoneNumberAndCountryCodeDTO> phoneNumbers = phoneNumberListDTO.getPhoneNumbers();
        List<Map<String, Object>> userDetails = new ArrayList<Map<String, Object>>();
        for ( PhoneNumberAndCountryCodeDTO phoneNumber : phoneNumbers) {
            try {
                RegistryUserWithOsId searchRegistryUser = getUserFromRegistryFromNumber(phoneNumber.getCountryCode(), phoneNumber.getPhoneNumber(), accessToken);
                Map<String, Object> userDetail = new HashMap<>();
                userDetail.put(phoneNumber.getCountryCode().concat(phoneNumber.getPhoneNumber()), searchRegistryUser);
                userDetails.add(userDetail);
            }catch (IOException e){
                Map<String, Object> userDetail = new HashMap<>();
                userDetail.put(phoneNumber.getCountryCode().concat(phoneNumber.getPhoneNumber()), null);
                userDetails.add(userDetail);
                LOGGER.error("Unable to fetch details : {} ", e);
                responseDTO.setResponse(userDetails);
                responseDTO.setMessage("Unable to fetch user details");
                responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return responseDTO;
            }
        }
            responseDTO.setResponse(userDetails);
            responseDTO.setMessage("SuccessFully fetch user Data");
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_OK);

        return responseDTO;
    }


    private RegistryUserWithOsId fetchUserFromRegistry(String userId, String accessToken) throws IOException {
        RegistryUserWithOsId registryUserWithOsId = new RegistryUserWithOsId();
        SlimRegistryUserUserIdDto searchRegistryUserDto = new SlimRegistryUserUserIdDto();
        searchRegistryUserDto.setId(RegistryResponse.API_ID.SEARCH.getId());
        searchRegistryUserDto.setVer("1.0");

        RegistryUserWithUserId person = new RegistryUserWithUserId();
        person.setUserId(userId);


        SlimRequestUserId request = new SlimRequestUserId();
        request.setPerson(person);
        searchRegistryUserDto.setRequest(request);

        String registryUserString = null;

        LOGGER.debug(Constants.REMOVE_BEARER);
        RegistryResponse searchRegistryUser = registryDao.searchUserByUserId(accessToken, searchRegistryUserDto).execute().body();
        LOGGER.debug("Searched User from Registry with userID : {} ", userId);

        if (searchRegistryUser.getResult() != null && !((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).isEmpty()) {
            registryUserWithOsId = new RegistryUserWithOsId(((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).get(0));

            LOGGER.debug("Retrieved User successfully with userId : {} ", registryUserWithOsId.getUserId());
            registryUserString = HttpUtils.convertJsonObjectToString(registryUserWithOsId);
        } else {
            LOGGER.error("User does not exist in registry with user id : {} ", userId);
            throw new IOException();
        }

        return registryUserWithOsId;
    }

    private RegistryUserWithOsId getUserFromRegistryByUserID(String userId, String accessToken) throws IOException {
        RegistryUserWithOsId registryUserWithOsId = new RegistryUserWithOsId();
            SlimRegistryUserUserIdDto searchRegistryUserDto = new SlimRegistryUserUserIdDto();
            searchRegistryUserDto.setId(RegistryResponse.API_ID.SEARCH.getId());
            searchRegistryUserDto.setVer("1.0");

            RegistryUserWithUserId person = new RegistryUserWithUserId();
            person.setUserId(userId);
        SlimRequestUserId request = new SlimRequestUserId();
        request.setPerson(person);
        searchRegistryUserDto.setRequest(request);

        String registryUserString = null;

        LOGGER.debug(Constants.REMOVE_BEARER);
        RegistryResponse searchRegistryUser = registryDao.searchUserByUserId(accessToken, searchRegistryUserDto).execute().body();
        ObjectMapper objectMapper =new ObjectMapper();
        LOGGER.debug("Searched User from Registry with userID : {} ", userId);

        if (searchRegistryUser.getResult() != null && !((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).isEmpty()) {
            registryUserWithOsId = new RegistryUserWithOsId(((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).get(0));
            LOGGER.debug("Retrieved User successfully with userId : {} ", registryUserWithOsId.getUserId());
            registryUserString = HttpUtils.convertJsonObjectToString(registryUserWithOsId);
        } else {

            LOGGER.error("User does not exist in registry with user id : {} ", userId);
            throw new IOException("User does not Exist in Registry");
        }

        return registryUserWithOsId;

    }

    @Override
    public ResponseDTO fetchUserDetailOnScanQrCode(String scannedUserId, String scannerAccessToken) {
        LOGGER.info("============================================API CALL:/scan/user_detail============================================");
        ScanningUserDetailDto scanningUserDetailDto = new ScanningUserDetailDto();
        String adminAccesToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        UserRepresentation scannedUserDetail = keycloakService.getUserById(" BEARER " + adminAccesToken, scannedUserId, appContext.getRealm());
        ResponseDTO responseDTO = new ResponseDTO();

        try {
            RegistryUserWithOsId searchRegistryUser = getUserFromRegistry(scannedUserId, scannedUserDetail.getUsername(), adminAccesToken);
            scanningUserDetailDto = new ScanningUserDetailDto(searchRegistryUser.getUserId(), searchRegistryUser.getName()
                    , searchRegistryUser.getPhoto());
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_OK);
            responseDTO.setResponse(scanningUserDetailDto);
            responseDTO.setMessage(Constants.SCAN_USER_DETAIL);
        } catch (IOException e) {
            LOGGER.error("User does not exist in registry with emailId : {} ", scannedUserDetail.getEmail() + e);
            responseDTO.setMessage(Constants.USER_NOT_FOUND_IN_REGISTRY);
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_BAD_REQUEST);

        }
        return responseDTO;
    }

    @Override
    public ResponseDTO updateUserProfile(UpdateUserProfileDTO updateUserProfileDTO) {
        LOGGER.info("============================================API CALL:/update-profile============================================");
        ResponseDTO codes = getCountryCodes();
        List<CountryCode> countryCodes = (List<CountryCode>) codes.getResponse();
        for (CountryCode code : countryCodes) {
            if (updateUserProfileDTO.getUpdatedRegistryUserBody().getCountryCode().equals(code.getCode()) && updateUserProfileDTO.getPhoneNumber().length() != code.getPhoneNumberLength()) {
                LOGGER.error("ERROR:phone number {} is invalid", updateUserProfileDTO.getPhoneNumber());
                return HttpUtils.onFailure(HttpStatus.BAD_REQUEST.value(), Constants.INVALID_PHONE_NO_LENGTH);
            }
        }

        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String userAccessToken = updateUserProfileDTO.getAccessToken();
            RegistryUserWithOsId updatedRegistryUserBody = updateUserProfileDTO.getUpdatedRegistryUserBody();
            String email = updateUserProfileDTO.getEmail();
            String phoneNumber = updateUserProfileDTO.getPhoneNumber();
            Boolean updateEmail = updateUserProfileDTO.getUpdateEmail();
            Boolean isUpdatedEmailVerified = updateUserProfileDTO.getUpdatedEmailVerified();
            String newemail = updateUserProfileDTO.getNewemail();
            String userIdForUserFetchAfterUpdate = updateUserProfileDTO.getUserId();

	     if((updatedRegistryUserBody.getName().contains("?"))||(updatedRegistryUserBody.getName().contains(","))){
                return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_BAD_REQUEST,"Please Enter a valid name");
            }
            responseDTO = updateKeycloakUser(userAccessToken, updatedRegistryUserBody, phoneNumber, isUpdatedEmailVerified, updateEmail);
            boolean isPhotoUpdated = isProfilePhotoUpdate(phoneNumber, userAccessToken, updatedRegistryUserBody);
            boolean isNameUpdated = isNameUpdate(phoneNumber, userAccessToken, updatedRegistryUserBody);
            if (responseDTO.getResponseCode() != Constants.TWO_HUNDRED) {
                LOGGER.error("Error updating keycloak user");
                return responseDTO;
            }
            responseDTO = updateRegistryUser(userAccessToken, updatedRegistryUserBody, phoneNumber, email, isUpdatedEmailVerified, updateEmail, newemail,updateUserProfileDTO.getEmailUpdateId()==null?null:updateUserProfileDTO.getEmailUpdateId());
            LocalDateTime dateTime = LocalDateTime.now();

	    
            if (isNameUpdated) {
                saveNotification(new NotificationDTO(null, updatedRegistryUserBody.getUserId(), Constants.CHANGE_NAME, NotificationEvents.USER.toString(), dateTime.toLocalDate().toString() + " " + dateTime.toLocalTime().toString(), false));
            }
            RegistryUserWithOsId user = getUserFromRegistryByUserID(userIdForUserFetchAfterUpdate == null ? KeycloakUtil.fetchUserIdFromToken(userAccessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm()) : userIdForUserFetchAfterUpdate, userAccessToken);
            ProfileTemplateDto profileTemplateDto = new ProfileTemplateDto();
            profileTemplateDto.setUserId(user.getUserId());
            profileTemplateDto.setUserName(user.getName());
            profileTemplateDto.setPhoto(user.getPhoto());

            ResponseDTO profileCardResponse = profileCardUtils.generateUserProfileCard(profileTemplateDto);
            if (profileCardResponse.getResponseCode() == Constants.TWO_HUNDRED) {
                LOGGER.info("Profile Card URL  is updated for the user: " + profileTemplateDto.getUserName() + " with userId: " + profileTemplateDto.getUserId());

            }

            return responseDTO;
        } catch (NotFoundException e) {
            LOGGER.error(Constants.USER_NOT_FOUND, updateUserProfileDTO.getPhoneNumber() + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        } catch (Exception e) {

            LOGGER.error(Constants.USER_UPDATION_FAILED, updateUserProfileDTO.getPhoneNumber() + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_BAD_REQUEST, Constants.USER_DOES_NOT_EXIST);
        }

    }

    private ResponseDTO updateRegistryUserProfilePic(String accessToken, String profileCardUrl) {
        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm());
            RegistryUserWithOsId user = fetchUserFromRegistry(userId, accessToken);
            user.setUpdtDttm(new java.util.Date().toString());
            user.setProfileCardUrl(profileCardUrl);
            RequestWithOsId request = new RequestWithOsId();
            request.setPerson(user);
            RegistryRequestWithOsId registryRequest = new RegistryRequestWithOsId(null, request, RegistryResponse.API_ID.UPDATE.getId());
            RegistryResponse updateRegistryUser = registryDao.updateUser(accessToken, registryRequest).execute().body();
            LOGGER.debug("Successfully updated status in Registry for user : {}", user.getUserId());
            if (!REGISTRY_SUCCESS_RESPONSE.equalsIgnoreCase(updateRegistryUser.getResponseParams().getStatus().toString())) {
                LOGGER.error("User Status updation failed in Registry for user : {}", user.getUserId());
                return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
            }


            String registryUserString = HttpUtils.convertJsonObjectToString(user);

            LOGGER.info(Constants.USER_UPDATE_SUCCESSFUL_FOR_REGISTRY, userId);
            return HttpUtils.onSuccess(null, Constants.USER_UPDATE_SUCCESS);
        } catch (IOException | VerificationException e) {
            LOGGER.error("User updation failed in Registry for access_token : {}", accessToken + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        }
    }

    private boolean isNameUpdate(String phoneNumber, String accessToken, RegistryUserWithOsId updatedRegistryUserBody) throws IOException {

        UserRepresentation userRepresentation = keycloakService.getUserByUsername(accessToken, phoneNumber, appContext.getRealm());
        RegistryUserWithOsId userWithOsId = getUserFromRegistryByUserID(userRepresentation.getId(), accessToken);

        return !userWithOsId.getName().equalsIgnoreCase(updatedRegistryUserBody.getName());

    }

    private boolean isProfilePhotoUpdate(String phoneNumber, String accessToken, RegistryUserWithOsId updatedRegistryUserBody) throws IOException {

        UserRepresentation userRepresentation = keycloakService.getUserByUsername(accessToken, phoneNumber, appContext.getRealm());
        RegistryUserWithOsId userWithOsId = getUserFromRegistryByUserID(userRepresentation.getId(), accessToken);

        return !userWithOsId.getPhoto().equalsIgnoreCase(updatedRegistryUserBody.getPhoto());

    }

    private boolean isAddingNewEmail(String phoneNumber, String accessToken, RegistryUserWithOsId updatedRegistryUserBody) throws IOException {

        UserRepresentation userRepresentation = keycloakService.getUserByUsername(accessToken, phoneNumber, appContext.getRealm());
        RegistryUserWithOsId userWithOsId = getUserFromRegistryByUserID(userRepresentation.getId(), accessToken);

        return userWithOsId.getEmailId() == null && updatedRegistryUserBody.getEmailId() != null;
    }

    private ResponseDTO updateKeycloakUser(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String phoneNumber, Boolean isUpdatedEmailIdVerified, boolean updateEmail) {
        try {
            UserRepresentation existingUserRepresentation = keycloakService.getUserByUsername(accessToken, phoneNumber, appContext.getRealm());
            LOGGER.debug("Set updated values in Keycloak for user : {}", updatedRegistryUserBody.getPhoneNumber());
            UserRepresentation updatedUserRepresentation = new UserRepresentation();
            if (!(existingUserRepresentation.getFirstName()).equals(updatedRegistryUserBody.getName())) {
                updatedUserRepresentation.setFirstName(updatedRegistryUserBody.getName());
            }
            if (updateEmail && isUpdatedEmailIdVerified) {
                LOGGER.debug("Email Id is updated. User should verify new email");
                updatedUserRepresentation.setEmail(updatedRegistryUserBody.getEmailId());
            }
            // while updating, always active
            updatedUserRepresentation.setEnabled(Boolean.TRUE);

            String adminAccessToken = Constants.BEARER + keycloakService.generateAccessToken(appContext.getAdminUserName());
            retrofit2.Response keycloakResponse = keycloakDao.updateUser(adminAccessToken, updatedRegistryUserBody.getUserId(), updatedUserRepresentation, appContext.getRealm()).execute();
            if (keycloakResponse.isSuccessful() != Boolean.TRUE) {
                if ("Conflict".equalsIgnoreCase(keycloakResponse.message())) {
                    LOGGER.error(Constants.USER_UPDATION_FAILED, updatedRegistryUserBody.getEmailId());
                    return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_NOT_FOUND, "User Already Exist with this EmailId please try to update from another EmailId");
                } else {
                    LOGGER.error(Constants.USER_UPDATION_FAILED, updatedRegistryUserBody.getEmailId());
                    return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
                }
            }
        } catch (NotFoundException e) {
            LOGGER.error(Constants.USER_NOT_FOUND, updatedRegistryUserBody.getEmailId(), e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        } catch (IOException e) {
            LOGGER.error(Constants.USER_UPDATION_FAILED, updatedRegistryUserBody.getPhoneNumber(), e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_BAD_REQUEST, Constants.USER_DOES_NOT_EXIST);
        }
        LOGGER.info("User updation successful in Keycloak for user : {}", updatedRegistryUserBody.getPhoneNumber());
        return HttpUtils.onSuccess(null, Constants.USER_UPDATE_SUCCESS_KEYCLOAK);
    }

    private ResponseDTO updateRegistryUser(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String phoneNumber, String oldemailId, Boolean isUpdatedEmailIdVerified, Boolean updateEmail, String newEmail,String emailUpdateId) {
        try {
            LOGGER.debug("Set updated values in Registry for user : {}", updatedRegistryUserBody.getEmailId());
            updatedRegistryUserBody.setUpdtDttm(new java.util.Date().toString());
            updatedRegistryUserBody.setPhoneNumber(phoneNumber);
            updatedRegistryUserBody.setSalutation("");
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm());
            LOGGER.info("profile card Updated for user :" + userId);
            updatedRegistryUserBody.setProfileCardUrl(appContext.getAwsS3Url() + "profile-card/" + updatedRegistryUserBody.getUserId());
            RequestWithOsId request = new RequestWithOsId();
            request.setPerson(updatedRegistryUserBody);
            RegistryRequestWithOsId registryRequest = new RegistryRequestWithOsId(null, request, RegistryResponse.API_ID.UPDATE.getId());



            if (updateEmail) {
                if (isUpdatedEmailIdVerified) {
                    LOGGER.debug("Email is verified. Update user registry with new emailId and cleanup the old cache");
                } else {
                    LOGGER.debug("Set old Email Id since email is not yet verified");
                    updatedRegistryUserBody.setEmailId(oldemailId);
                }
            }
            RegistryResponse updateRegistryUser = registryDao.updateUser(accessToken, registryRequest).execute().body();
            LOGGER.debug("Successfully updated values in Registry for user : {}", updatedRegistryUserBody.getEmailId());
            if (!REGISTRY_SUCCESS_RESPONSE.equalsIgnoreCase(updateRegistryUser.getResponseParams().getStatus().toString())) {
                LOGGER.error(Constants.USER_UPDATION_FAILED_REGISTRY, updatedRegistryUserBody.getEmailId());
                return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
            }
            if (updateEmail && !isUpdatedEmailIdVerified) {
                CompletableFuture.runAsync(() -> {
                    try {
                        LOGGER.debug("Triggering a verification email, since email is updated from old email id : {} to new emailId : {}", oldemailId, newEmail);
                        EmailUtils.sendEmail(appContext, newEmail, Constants.EMAIL_ACTION_UPDATE_EMAIL_ID, updatedRegistryUserBody.getUserId(), updatedRegistryUserBody.getName(), null,emailUpdateId);
                    } catch (Exception e) {
                        LOGGER.error("Error sending verification email for user {} for updating emailId.", updatedRegistryUserBody.getEmailId(), e);
                    }
                });

            }
        } catch (IOException | VerificationException e) {
            LOGGER.error(Constants.USER_UPDATION_FAILED_REGISTRY, updatedRegistryUserBody.getUserId() + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        }
        LOGGER.info(Constants.USER_UPDATE_SUCCESSFUL_FOR_REGISTRY, updatedRegistryUserBody.getUserId());
        String registryUserString = HttpUtils.convertJsonObjectToString(updatedRegistryUserBody);
        return HttpUtils.onSuccess(null, Constants.USER_UPDATE_SUCCESS);
    }

    @Override
    public ResponseDTO changeActiveStatus(String accessToken, ChangeStatusDto changeStatusDto) {
        LOGGER.info("============================================API CALL:/change-status============================================");
        if (null != changeStatusDto) {
            return updateRegistryUserStatus(accessToken, changeStatusDto.isActive());
        }
        return HttpUtils.onFailure(Constants.FOUR_HUNDRED, "Request can not be null");
    }

    @Override
    public List<LinkedHashMap> getUsersByUserIds(List<String> useIds) {
        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        SlimRequestUsersDTO slimRequestUsersDTO = new SlimRequestUsersDTO();
        slimRequestUsersDTO.setId(RegistryResponse.API_ID.SEARCH.getId());
        slimRequestUsersDTO.setVer("1.0");

        RegistryUsers person = new RegistryUsers();

        SlimRequestUsers request = new SlimRequestUsers();
        request.setPerson(person);
        slimRequestUsersDTO.setRequest(request);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LOGGER.info(objectMapper.writeValueAsString(slimRequestUsersDTO));
        } catch (JsonProcessingException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        RegistryResponse searchRegistryUser = null;
        try {
            searchRegistryUser = registryDao.getAllUsers(adminAccessToken, slimRequestUsersDTO).execute().body();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG, e);
        }
        List<LinkedHashMap> registryUserWithOsId = new ArrayList<>();

        if (searchRegistryUser.getResult() != null && !((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).isEmpty()) {
            registryUserWithOsId = (List<LinkedHashMap>) searchRegistryUser.getResult();
        } else {
            try {
                throw new IOException();
            } catch (IOException e) {
                LOGGER.error(Constants.ERRORLOG, e);
            }
        }
        List<LinkedHashMap> userWithOsIds = new ArrayList<>();
        registryUserWithOsId.stream().forEach(registryUserWithOsId1 -> {
            if (useIds.contains(registryUserWithOsId1.get("userId"))) {
                userWithOsIds.add(registryUserWithOsId1);
            }
        });

        return userWithOsIds;
    }

    @Override
    public RegistryUserWithOsId getUsersByUserId(String useId) throws IOException {
        LOGGER.debug("Fetching Access token");
        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        return fetchUserFromRegistry(useId, adminAccessToken);
    }

    private ResponseDTO updateRegistryUserStatus(String accessToken, boolean active) {
        try {
            String userId = KeycloakUtil.fetchUserIdFromToken(accessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm());
            RegistryUserWithOsId user = getUserFromRegistryByUserID(userId, accessToken);
            user.setUpdtDttm(new java.util.Date().toString());
            user.setActive(active);
            RequestWithOsId request = new RequestWithOsId();
            request.setPerson(user);
            RegistryRequestWithOsId registryRequest = new RegistryRequestWithOsId(null, request, RegistryResponse.API_ID.UPDATE.getId());
            RegistryResponse updateRegistryUser = registryDao.updateUser(accessToken, registryRequest).execute().body();
            LOGGER.debug("Successfully updated status in Registry for user : {}", user.getUserId());
            if (!REGISTRY_SUCCESS_RESPONSE.equalsIgnoreCase(updateRegistryUser.getResponseParams().getStatus().toString())) {
                LOGGER.error("User Status updation failed in Registry for user : {}", user.getUserId());
                return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
            }
            LocalDateTime now = LocalDateTime.now();
            if (active) {
                saveNotification(new NotificationDTO(null, userId, Constants.REACTIVATE_ACCOUNT, NotificationEvents.USER.toString(), now.toLocalDate().toString() + " " + now.toLocalTime().toString(), false));
            } else {
                saveNotification(new NotificationDTO(null, userId, Constants.DEACTIVATE_ACCOUNT, NotificationEvents.USER.toString(), now.toLocalDate().toString() + " " + now.toLocalTime().toString(), false));
            }
            LOGGER.info(Constants.USER_UPDATE_SUCCESSFUL_FOR_REGISTRY, userId);

            return HttpUtils.onSuccess(null, Constants.USER_STATUS_UPDATE_SUCCESS);
        } catch (IOException | VerificationException e) {
            LOGGER.error("User updation failed in Registry for access_token : {}", accessToken + e);
            return HttpUtils.onFailure(org.apache.http.HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        }
    }


    @Override
    public ResponseDTO updateEmailIdForUserProfile(String newEmailId, String id, Boolean isUpdatedEmailIdVerified, String userAccessToken,String emailUpdateId) {
        LOGGER.info("============================================API CALL:/update-email-id ,update-email-id-post-verification============================================");
        String adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            UserRepresentation user = keycloakService.getUserById(Constants.BEARER + adminAccessToken, id, appContext.getRealm());
            if(!isUpdatedEmailIdVerified){
                user.getAttributes().put(Constants.EMAIL_UPDATE_FLAG,asList(Boolean.TRUE.toString()));
                user.getAttributes().put(Constants.EMAIL_UPDATE_ID,asList(UUID.randomUUID().toString()));
                keycloakDao.updateUser(Constants.BEARER + adminAccessToken, id, user, appContext.getRealm()).execute();
            }
            if((isUpdatedEmailIdVerified && user.getAttributes().get(Constants.EMAIL_UPDATE_FLAG).get(0).equalsIgnoreCase(Boolean.FALSE.toString()))||(isUpdatedEmailIdVerified && !user.getAttributes().get(Constants.EMAIL_UPDATE_ID).get(0).equalsIgnoreCase(emailUpdateId))){
                return HttpUtils.onFailure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Link Expired");
            }
            String oldemail = user.getEmail();
            String phoneNumber = user.getUsername();
            RegistryUserWithOsId updatedRegistryUserBody = getUserFromRegistry(id, phoneNumber, adminAccessToken);
            boolean isNewEmailAdded = false;
            if (isUpdatedEmailIdVerified && user.getAttributes().get(Constants.EMAIL_UPDATE_FLAG).get(0).equalsIgnoreCase(Boolean.TRUE.toString())) {
                user.getAttributes().put(Constants.EMAIL_UPDATE_FLAG,asList(Boolean.FALSE.toString()));
                keycloakDao.updateUser(Constants.BEARER + adminAccessToken, id, user, appContext.getRealm()).execute();
                updatedRegistryUserBody.setEmailId(newEmailId);
                isNewEmailAdded = isAddingNewEmail(phoneNumber, adminAccessToken, updatedRegistryUserBody);
            }
            UpdateUserProfileDTO updateUserProfileDTO = new UpdateUserProfileDTO();
            updateUserProfileDTO.setAccessToken(adminAccessToken);
            updateUserProfileDTO.setUpdatedRegistryUserBody(updatedRegistryUserBody);
            updateUserProfileDTO.setEmail(oldemail);
            updateUserProfileDTO.setPhoneNumber(phoneNumber);
            updateUserProfileDTO.setUpdateEmail(true);
            updateUserProfileDTO.setUpdatedEmailVerified(isUpdatedEmailIdVerified);
            updateUserProfileDTO.setNewemail(newEmailId);
            updateUserProfileDTO.setUserId(id);

            updateUserProfileDTO.setEmailUpdateId(user.getAttributes().get(Constants.EMAIL_UPDATE_ID).get(0));



            responseDTO = updateUserProfile(updateUserProfileDTO);
            LocalDateTime now = LocalDateTime.now();

            if (isUpdatedEmailIdVerified) {
                if (isNewEmailAdded) {
                    saveNotification(new NotificationDTO(null, id, Constants.ADD_EMAIL, NotificationEvents.USER.toString(), now.toLocalDate().toString() + " " + now.toLocalTime().toString(), false));
                } else {
                    saveNotification(new NotificationDTO(null, id, Constants.CHANGE_OLD_EMAIL, NotificationEvents.USER.toString(), now.toLocalDate().toString() + " " + now.toLocalTime().toString(), false));
                }
            }
        } catch (Exception e) {
            responseDTO.setResponseCode(org.apache.http.HttpStatus.SC_UNAUTHORIZED);
            responseDTO.setResponse(Constants.USER_NOT_FOUND);
            LOGGER.error("Error logged as" + e);
        }
        return responseDTO;
    }

    private void saveNotification(NotificationDTO notificationDTO) {
        HttpEntity<NotificationDTO> request = new HttpEntity<>(notificationDTO);
        String requestUrl = appContext.getNotificationServerUrl() + Constants.SAVE_IAM_NOITFICATION;
        RestTemplate restTemplate = new RestTemplate();
        ResponseDTO responseDTO = restTemplate.postForObject(requestUrl, request, ResponseDTO.class);
        LOGGER.info("Notification saved for the user" + notificationDTO.getUserId());
    }

    @Override
    public ResponseDTO getCountryCodes() {
        LOGGER.info("============================================API CALL:/get-country-codes============================================");
        List<CountryCode> countrycodes = new ArrayList<>();
        SlimRequestCountryCodeDTO slimRequestCountryCodeDTO = new SlimRequestCountryCodeDTO();
        SlimRequestCountryCode requestCountryCode = new SlimRequestCountryCode();
        slimRequestCountryCodeDTO.setRequest(requestCountryCode);
        slimRequestCountryCodeDTO.setId(RegistryResponse.API_ID.SEARCH.getId());
        slimRequestCountryCodeDTO.setVer("1.0");

        try {
            String adminAccesToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
            System.out.println(adminAccesToken);
            retrofit2.Response<RegistryResponse> countryCodes = registryDao.getAllCountryCodes(adminAccesToken, slimRequestCountryCodeDTO).execute();
            RegistryResponse body = countryCodes.body();
            if (body.getResult() != null) {
                List<Map<String, Object>> countryCodeResult = (List<Map<String, Object>>) body.getResult();
                for (Map<String, Object> code : countryCodeResult) {
                    countrycodes.add(new CountryCode(code));
                }

            } else {
                throw new IOException();
            }

            return HttpUtils.onSuccessJson(countrycodes, "Fetched Codes");
        } catch (IOException e) {
            LOGGER.error("Exception while fetching country codes: {}", e);
            return HttpUtils.onFailure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Exception while fetching country codes: " + e);
        }
    }

    @Override
    public ResponseDTO updateUserProfilePhoto(String userId, boolean isRemovePhoto) {
        LOGGER.info("============================================API CALL:/update-photo============================================");

        boolean removepic = isRemovePhoto;
        LocalDateTime dateTime = LocalDateTime.now();
        if (removepic == false) {
            saveNotification(new NotificationDTO(null, userId, Constants.UPDATE_PHOTO, NotificationEvents.USER.toString(), dateTime.toLocalDate().toString() + " " + dateTime.toLocalTime().toString(), false));
            return HttpUtils.onSuccess(null, Constants.UPDATE_PROFILE_PHOTO_NOTIFICATION_TRIGGERED);
        } else {
            saveNotification(new NotificationDTO(null, userId, Constants.REMOVE_PHOTO, NotificationEvents.USER.toString(), dateTime.toLocalDate().toString() + " " + dateTime.toLocalTime().toString(), false));
            return HttpUtils.onSuccess(null, Constants.UPDATE_PROFILE_PHOTO_NOTIFICATION_TRIGGERED);
        }

    }

}
