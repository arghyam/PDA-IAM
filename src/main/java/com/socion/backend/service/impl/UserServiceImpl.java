package com.socion.backend.service.impl;

import com.socion.backend.AesUtil;
import com.socion.backend.config.AppContext;
import com.socion.backend.config.CacheConfiguration;
import com.socion.backend.dao.KeycloakDao;
import com.socion.backend.dao.KeycloakService;
import com.socion.backend.dao.RegistryDao;
import com.socion.backend.dto.ResponseDTO;
import com.socion.backend.dto.UserRegisterDTO;
import com.socion.backend.dto.RequestWithOsId;
import com.socion.backend.dto.RegistryRequestWithOsId;
import com.socion.backend.dto.RegistryResponse;
import com.socion.backend.dto.SlimRegistryUserDto;
import com.socion.backend.dto.SlimRequest;
import com.socion.backend.dto.PasswordBodyDto;
import com.socion.backend.dto.AccessTokenResponseDTO;
import com.socion.backend.dto.Request;
import com.socion.backend.dto.RegistryRequest;
import com.socion.backend.dto.ScanningUserDetailDto;
import com.socion.backend.entity.RegistryUser;
import com.socion.backend.entity.RegistryUserWithOsId;
import com.socion.backend.entity.SlimRegistryUser;
import com.socion.backend.exceptions.NotFoundException;
import com.socion.backend.exceptions.UnprocessableEntitiesException;
import com.socion.backend.exceptions.UserCreateException;
import com.socion.backend.exceptions.ValidationError;
import com.socion.backend.service.UserService;
import com.socion.backend.utils.Constants;
import com.socion.backend.utils.EmailUtils;
import com.socion.backend.utils.HttpUtils;
import com.socion.backend.utils.KeycloakUtil;
import com.amazonaws.services.connect.model.UserNotFoundException;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseBody;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    AppContext appContext;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    KeycloakDao keycloakDao;

    @Autowired
    RegistryDao registryDao;

    @Autowired
    CacheConfiguration cacheConfiguration;

    @Autowired
    Environment env;

    private static String REGISTRY_SUCCESS_RESPONSE = "SUCCESSFUL";

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);


    @Override
    public ResponseDTO register(UserRegisterDTO userRegisterDTO, BindingResult bindingResult)  {

        valiadtePojo(bindingResult);
        Keycloak kc = getKeycloak();
        ResponseDTO response = new ResponseDTO();

        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        String decryptedPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), userRegisterDTO.getPassword());
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(decryptedPassword);
        credential.setTemporary(false);

        UserRepresentation userRepresentation = new UserRepresentation();


        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(userRegisterDTO.getEmail());
        if (matcher.find()) {
            userRepresentation.setUsername(userRegisterDTO.getEmail());
        } else {
            throw new UserCreateException("invalid email Id");
        }

        userRepresentation.setCredentials(asList(credential));
        userRepresentation.setEmail(userRegisterDTO.getEmail());
        userRepresentation.setEnabled(Boolean.TRUE);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(Constants.SALUTATION, asList(userRegisterDTO.getSalutation()));
        attributes.put(Constants.REG_ENTRY_CREATED, asList(Boolean.FALSE.toString()));
        attributes.put(Constants.SENT_VERIFY_EMAIL_FOR_SIGN_UP, asList(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT))));
        userRepresentation.setAttributes(attributes);

        Response result = kc.realm(appContext.getRealm()).users().create(userRepresentation);


        if (result.getStatus() != HttpStatus.SC_CREATED && result.getStatus() == HttpStatus.SC_CONFLICT) {
            LOGGER.error("User already exists");
            response = HttpUtils.onFailure(result.getStatus(), Constants.USER_ALREADY_EXISTS);
            return response;
        }

        UserRepresentation keycloakUser = null;
        keycloakUser = keycloakService.getUserByUsername(kc.tokenManager().getAccessTokenString(),
                userRegisterDTO.getEmail(), appContext.getRealm());

        LOGGER.debug("Keycloak user with username {}  {} returned", keycloakUser.getFirstName(), keycloakUser.getLastName());

        LOGGER.debug("calling method for send email verification registry entry ");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        UserRepresentation finalKeycloakUser = keycloakUser;

        CompletableFuture.runAsync(
                () -> {
                    try {
                        sendVerificationEmailAndCreateRegistryEntry(finalKeycloakUser);
                        executor.shutdown();
                        LOGGER.info("Done Executing");
                    } catch (Exception e) {
                        LOGGER.error("Error sending email and/or creating registry entry,Exception:",e);
                    } finally {
                        if (executor.isTerminated()) {
                            LOGGER.debug("Already terminated");
                        }
                        if (executor.isShutdown()) {
                            LOGGER.debug("Executor shutdown");
                        } else {
                            executor.shutdownNow();
                            LOGGER.debug("Shutting down now");
                        }
                    }
                }, executor);

        response = HttpUtils.onSuccess(null, Constants.USER_CREATED_SUCCESSFULLY);
        return response;

    }




    @Override
    public Keycloak getKeycloak() {
        return  KeycloakBuilder.builder()
                .serverUrl(appContext.getKeyCloakServiceUrl())
                .realm(appContext.getRealm())
                .username(appContext.getAdminUserName())
                .password(appContext.getAdminUserpassword())
                .clientId(appContext.getClientId())
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(Constants.TEN).build())
                .build();
    }


    @Override
    public void valiadtePojo(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ValidationError> errorList = new ArrayList<>();

            for (FieldError error : bindingResult.getFieldErrors()) {
                ValidationError validationError = new ValidationError(error.getField(), error.getDefaultMessage());
                errorList.add(validationError);
            }
            throw new UnprocessableEntitiesException(errorList);
        }
    }


    @Override
    public void sendPasswordUpdateConfirmationEmail(String emailId)  {
        LOGGER.debug("Sending confirmation email asynchronously");
        CompletableFuture.runAsync(() -> {
            try {
                EmailUtils.sendEmail(appContext, emailId, Constants.EMAIL_ACTION_RESET_PWD, null, null, null,null);
                LOGGER.debug("Confirmation email sent successfully");
            } catch (Exception e) {
                LOGGER.error("Error sending confirmation email : {} ", e);
            }
        });
    }

    @Override
    public ResponseDTO fetchUserProfileDetail(String email, String accessToken)  {
        LOGGER.info("Fetching User from Registry");
        ResponseDTO responseDTO = new ResponseDTO();


        try {
            RegistryUserWithOsId searchRegistryUser = getUserFromRegistry(email, accessToken);
            responseDTO.setResponse(searchRegistryUser);
            responseDTO.setMessage("SuccessFully fetch user Data");
            responseDTO.setResponseCode(HttpStatus.SC_OK);
        } catch (Exception e) {
            LOGGER.error("User does not exist in registry with emailId : {}  Exception: ", email,e);
            responseDTO.setMessage(Constants.USER_DOES_NOT_EXIST);
            responseDTO.setResponseCode(HttpStatus.SC_UNAUTHORIZED);
        }
        return responseDTO;
    }

    @Override
    public ResponseDTO updateUserProfile(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String userEmail, Boolean isUpdatedEmailIdVerified) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            responseDTO = updateKeycloakUser(accessToken, updatedRegistryUserBody, userEmail, isUpdatedEmailIdVerified);
            if (responseDTO.getResponseCode() != HttpStatus.SC_OK) {
                LOGGER.error("Error updating keycloak user");
                return responseDTO;
            }
            responseDTO = updateRegistryUser(accessToken, updatedRegistryUserBody, userEmail, isUpdatedEmailIdVerified);
            return responseDTO;
        } catch (NotFoundException e) {
            LOGGER.error("User not found : {},Exception:", updatedRegistryUserBody.getEmailId(),e);
            return HttpUtils.onFailure(HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        } catch (Exception e) {
            LOGGER.error("User updation failed in Keycloak for user : {},Exception:", updatedRegistryUserBody.getEmailId(),e);
            return HttpUtils.onFailure(HttpStatus.SC_BAD_REQUEST, Constants.USER_DOES_NOT_EXIST);
        }

    }


    private ResponseDTO updateKeycloakUser(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String userEmail, Boolean isUpdatedEmailIdVerified) {
        try {

            LOGGER.info( accessToken,userEmail);
            LOGGER.debug("Set updated values in Keycloak for user : {}", updatedRegistryUserBody.getEmailId());
            UserRepresentation updatedUserRepresentation = new UserRepresentation();
            if (isUpdatedEmailIdVerified) {
                LOGGER.debug("Email Id is updated. User should verify new email");
                updatedUserRepresentation.setUsername(updatedRegistryUserBody.getEmailId());
                updatedUserRepresentation.setEmail(updatedRegistryUserBody.getEmailId());
            }

            String adminAccessToken = Constants.BEARER + keycloakService.generateAccessToken(appContext.getAdminUserName());
            retrofit2.Response keycloakResponse = keycloakDao.updateUser(adminAccessToken, updatedRegistryUserBody.getUserId(), updatedUserRepresentation, appContext.getRealm()).execute();
            if (keycloakResponse.isSuccessful() != Boolean.TRUE) {
                if ("Conflict".equalsIgnoreCase(keycloakResponse.message())) {
                    LOGGER.error("User updation failed in Keycloak for user : {}", updatedRegistryUserBody.getEmailId());
                    return HttpUtils.onFailure(HttpStatus.SC_NOT_FOUND, "User Already Exist with this EmailId please try to update from another EmailId");
                } else {
                    LOGGER.error("User updation failed in Keycloak for user : {}", updatedRegistryUserBody.getEmailId());
                    return HttpUtils.onFailure(HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
                }
            }
        } catch (NotFoundException e) {
            LOGGER.error("User not found : {}", updatedRegistryUserBody.getEmailId(),e);
            return HttpUtils.onFailure(HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        } catch (IOException e) {
            LOGGER.error("User updation failed in Keycloak for user : {},Exception:", updatedRegistryUserBody.getEmailId(),e);
            return HttpUtils.onFailure(HttpStatus.SC_BAD_REQUEST, Constants.USER_DOES_NOT_EXIST);
        }
        LOGGER.info("User updation successful in Keycloak for user : {}", updatedRegistryUserBody.getEmailId());
        return HttpUtils.onSuccess(null, Constants.USER_UPDATE_SUCCESS_KEYCLOAK);
    }

    private ResponseDTO updateRegistryUser(String accessToken, RegistryUserWithOsId updatedRegistryUserBody, String emailId, Boolean isUpdatedEmailIdVerified) {
        try {
            LOGGER.debug("Set updated values in Registry for user : {}", updatedRegistryUserBody.getEmailId());
            updatedRegistryUserBody.setUpdtDttm(new java.util.Date().toString());
            RequestWithOsId request = new RequestWithOsId();
            request.setPerson(updatedRegistryUserBody);
            RegistryRequestWithOsId registryRequest = new RegistryRequestWithOsId(null, request, RegistryResponse.API_ID.UPDATE.getId());
            String updatedUserEmailId = updatedRegistryUserBody.getEmailId();
            if (isUpdatedEmailIdVerified) {
                LOGGER.debug("Email is verified. Update user registry with new emailId and cleanup the old cache");
                //cacheConfiguration.redisUserCacheManager().getCache(Constants.CACHE_REGISTRY_USER).evict(emailId);
            } else {
                LOGGER.debug("Set old Email Id since email is not yet verified");
                updatedRegistryUserBody.setEmailId(emailId);
            }
            RegistryResponse updateRegistryUser = registryDao.updateUser(accessToken, registryRequest).execute().body();
            LOGGER.debug("Successfully updated values in Registry for user : {}", updatedRegistryUserBody.getEmailId());
            if (!REGISTRY_SUCCESS_RESPONSE.equalsIgnoreCase(updateRegistryUser.getResponseParams().getStatus().toString())) {
                LOGGER.error("User updation failed in Registry for user : {}", updatedRegistryUserBody.getEmailId());
                return HttpUtils.onFailure(HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
            }

            if (!isUpdatedEmailIdVerified) {
                CompletableFuture.runAsync(() -> {
                    try {
                        LOGGER.debug("Triggering a verification email, since email is updated from old email id : {} to new emailId : {}", emailId, updatedUserEmailId);
                        EmailUtils.sendEmail(appContext, updatedUserEmailId, Constants.EMAIL_ACTION_UPDATE_EMAIL_ID, updatedRegistryUserBody.getUserId(), updatedRegistryUserBody.getName(), null,null);
                    } catch (Exception e) {
                        LOGGER.error("Error sending verification email for user {} for updating emailId.", updatedRegistryUserBody.getEmailId(), e);
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error("User updation failed in Registry for user : {},Exception:", updatedRegistryUserBody.getEmailId(),e);
            return HttpUtils.onFailure(HttpStatus.SC_UNAUTHORIZED, Constants.USER_DOES_NOT_EXIST);
        }
        LOGGER.info("User update successful in Registry for user : {}", updatedRegistryUserBody.getEmailId());

        String registryUserString = HttpUtils.convertJsonObjectToString(updatedRegistryUserBody);

       // cacheConfiguration.redisUserCacheManager().getCache(Constants.CACHE_REGISTRY_USER).put(updatedRegistryUserBody.getEmailId(), registryUserString);


        return HttpUtils.onSuccess(null, Constants.USER_UPDATE_SUCCESS);
    }


    private RegistryUserWithOsId getUserFromRegistry(String email, String accessToken)  {
        RegistryUserWithOsId registryUserWithOsId = new RegistryUserWithOsId();
       // if (cacheConfiguration.redisUserCacheManager().getCache(Constants.CACHE_REGISTRY_USER).get(email) == null) {

            SlimRegistryUserDto searchRegistryUserDto = new SlimRegistryUserDto();
            searchRegistryUserDto.setId(RegistryResponse.API_ID.SEARCH.getId());
            searchRegistryUserDto.setVer("1.0");

            SlimRegistryUser person = new SlimRegistryUser();
            person.setEmailId(email);

            SlimRequest request = new SlimRequest();
            request.setPerson(person);
            searchRegistryUserDto.setRequest(request);

            String registryUserString = null;

            LOGGER.debug("Removing Bearer string from Authorization token");
            RegistryResponse searchRegistryUser = null;
            try {
                searchRegistryUser = registryDao.searchUser(accessToken, searchRegistryUserDto).execute().body();
            } catch (IOException e) {
                LOGGER.error(Constants.ERRORLOG+e);
            }
            LOGGER.debug("Searched User from Registry with emailId : {} ", email);

            if (searchRegistryUser.getResult() != null && !((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).isEmpty()) {
                registryUserWithOsId = new RegistryUserWithOsId(((List<LinkedHashMap<String, Object>>) searchRegistryUser.getResult()).get(0));

                LOGGER.debug("Retrieved User successfully with email : {} ", registryUserWithOsId.getEmailId());
                registryUserString = HttpUtils.convertJsonObjectToString(registryUserWithOsId);
              //  cacheConfiguration.redisUserCacheManager().getCache(Constants.CACHE_REGISTRY_USER).put(email, registryUserString);
            } else {
                LOGGER.error("User does not exist in registry with emailId : {} ", email);
                throw new UserNotFoundException("User is not found");
            }
       /* } else {
            LOGGER.debug("User details are cached. Returning from cache");
            String registryUserString = cacheConfiguration.redisUserCacheManager().getCache(Constants.CACHE_REGISTRY_USER).get(email).get().toString();
            registryUserWithOsId = HttpUtils.convertStringToJsonObject(registryUserString, RegistryUserWithOsId.class);
        }*/

        return registryUserWithOsId;

    }

    @Override
    public ResponseDTO changeUserPassword(String email, PasswordBodyDto passwordBodyDto)  {
        ResponseDTO response = new ResponseDTO();

        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        String decryptedCurrentPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), passwordBodyDto.getCurrentPassword());

        String decryptedUpdatedPassword = aesUtil.decrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), passwordBodyDto.getNewPassword());

        String adminAccessToken = null;
        adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());

        UserRepresentation userRepresentation = null;
        userRepresentation = keycloakService.getUserByUsername(adminAccessToken, email, appContext.getRealm());
        if (userRepresentation.getEmail() == null) {
            response = HttpUtils.onSuccess(null, "User with this EmailId does not exist");
            return response;
        }

        if (decryptedCurrentPassword.equals(decryptedUpdatedPassword)) {
            LOGGER.debug("Current password and updated password should be different");
            return HttpUtils.onFailure(Constants.FOUR_ZERO_FOUR, "Current password and updated password should be different");
        }

        retrofit2.Response<AccessTokenResponseDTO> userAccessToken = null;
        try {
            userAccessToken = keycloakDao.generateAccessTokenUsingCredentials(appContext.getRealm(), userRepresentation.getUsername(),
                    decryptedCurrentPassword, appContext.getClientId(), appContext.getGrantType(), appContext.getClientSecret()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG+e);
        }

        if (!userAccessToken.isSuccessful()) {
            LOGGER.debug("Current password value is incorrect");
            return  HttpUtils.onFailure(Constants.FOUR_ZERO_FOUR, "Current password value is incorrect");
        }
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(decryptedUpdatedPassword);
        credential.setTemporary(false);
        userRepresentation.setCredentials(asList(credential));
        retrofit2.Response<ResponseBody> passwordupdated = null;
        try {
            passwordupdated = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, userRepresentation.getId(), userRepresentation, appContext.getRealm()).execute();
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG+e);
        }

        if (passwordupdated.isSuccessful()) {
            LOGGER.info("Updated Password Successfully");
            response = HttpUtils.onSuccess(null, "Updated Password Successfully");

            CompletableFuture.runAsync(() -> {
                try {
                    sendPasswordUpdateConfirmationEmail(email);
                } catch (Exception e) {
                    LOGGER.error("Error sending password change confirmation email : {}", e);
                }
            });

        }
        return response;

    }

    @Override
    public void sendVerificationEmailAndCreateRegistryEntry(UserRepresentation userRepresentation)  {


        boolean sentVerifyEmailTForSignupTime = userRepresentation.getAttributes().containsKey(Constants.SENT_VERIFY_EMAIL_FOR_SIGN_UP);
        if (sentVerifyEmailTForSignupTime) {
            LOGGER.debug("Setting timeout value for email expiration : {}.", userRepresentation.getAttributes().get(Constants.SENT_VERIFY_EMAIL_FOR_SIGN_UP).get(0));
        } else {
            LOGGER.info("User created before sentVerifyEmailTForSignupTime attribute creation");
        }

        userRepresentation.getAttributes().put(Constants.SENT_VERIFY_EMAIL_FOR_SIGN_UP, asList(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))));

        keycloakService.updateUser(Constants.BEARER + keycloakService.generateAccessToken(appContext.getAdminUserName()), userRepresentation.getId(), userRepresentation, appContext.getRealm());

        try {
            sendVerificationEmailForCompletingSignup(userRepresentation.getEmail());
        } catch (Exception e) {
            LOGGER.error(Constants.ERRORLOG+e);
        }

    }

    @Override
    public void sendVerificationEmailForCompletingSignup(String emailId) {

        LOGGER.debug("Email sending from : {} ", appContext.getSourceEmail());
        try {
            EmailUtils.sendEmail(appContext, emailId, Constants.EMAIL_ACTION_VERIFY_ACCOUNT, null, null, null,null);
        } catch (Exception e) {
            LOGGER.error(Constants.ERRORLOG+e);
        }
        LOGGER.info("Email sent successfully");
    }


    @Override
    public ResponseDTO createEntryInRegistryAfterEmailVerification(String email)  {


        String adminAccessToken = null;
        adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        UserRepresentation userRepresentation = null;
        userRepresentation = keycloakService.getUserByUsername(adminAccessToken, email, appContext.getRealm());

        LOGGER.debug("Fetching user with email{} from keycloak ", email);

        String emailSentTimeString = userRepresentation.getAttributes().get(Constants.SENT_VERIFY_EMAIL_FOR_SIGN_UP).get(0);
        LOGGER.debug("Email Sent time in string format : {} ", emailSentTimeString);

        Date emailSentTimeDateFormat = null;
        try {
            emailSentTimeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(emailSentTimeString);
        } catch (ParseException e) {
            LOGGER.error(Constants.ERRORLOG+e);
        }

        LOGGER.debug("Sent time - Day is : {} , hour : {}, minutes : {} ", emailSentTimeDateFormat.getDate(), emailSentTimeDateFormat.getHours(), emailSentTimeDateFormat.getMinutes());

        LocalDateTime currentTime = LocalDateTime.now();
        LOGGER.debug("Current time - Day is : {} , hour : {} , minutes : {} ", currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute());

        int minuteForDay = (currentTime.getDayOfMonth() - emailSentTimeDateFormat.getDate()) * Constants.THREE_SIX_HUNDRED;
        int minuteForHour = (currentTime.getHour() - emailSentTimeDateFormat.getHours()) * Constants.INTSIXTY;
        int minuteforMinute = currentTime.getMinute() - emailSentTimeDateFormat.getMinutes();

        int totalTimeTakenToVerifyMail = minuteForDay + minuteForHour + minuteforMinute;

        LOGGER.debug("Time from when email was sent is : {} " + totalTimeTakenToVerifyMail);

        if (totalTimeTakenToVerifyMail <= appContext.getLinkExpirationTime()) {

            userRepresentation.setEmailVerified(Boolean.TRUE);
            keycloakService.updateUser(Constants.BEARER + adminAccessToken, userRepresentation.getId(), userRepresentation, appContext.getRealm());


            List<String> list = userRepresentation.getAttributes().get(Constants.COUNTRY_CODE);

            RegistryUser person=new RegistryUser();
            person.setName(userRepresentation.getFirstName());
            person.setEmailId(userRepresentation.getEmail());
            person.setSalutation(userRepresentation.getAttributes().get(Constants.SALUTATION).get(0));
            person.setUserId(userRepresentation.getId());
            person.setCrtdDttm(new java.util.Date().toString());
            person.setUpdtDttm(new java.util.Date().toString());
            person.setPhoto("");
            person.setPhoneNumber("");
            person.setActive(true);
            person.setProfileCardUrl("");
            person.setCountryCode(list != null && !list.isEmpty() ? list.get(0) : null);
            Request request = new Request();
            request.setPerson(person);

            RegistryRequest registryRequest = new RegistryRequest(null, request, RegistryResponse.API_ID.CREATE.getId());

            try {
                Call<RegistryResponse> createRegistryEntryCall = registryDao.createUser(adminAccessToken, registryRequest);
                retrofit2.Response registryUserCreationResponse = createRegistryEntryCall.execute();
                if (!registryUserCreationResponse.isSuccessful()) {
                    LOGGER.error("Error Creating registry entry {} ", registryUserCreationResponse.errorBody().string());
                }

                userRepresentation.getAttributes().put(Constants.REG_ENTRY_CREATED, asList(Boolean.TRUE.toString()));
                retrofit2.Response updateKeycloakUser = keycloakDao.updateUser(Constants.BEARER + adminAccessToken, userRepresentation.getId(), userRepresentation, appContext.getRealm()).execute();
                if (!updateKeycloakUser.isSuccessful()) {
                    LOGGER.error("Error Updating user {} ", updateKeycloakUser.errorBody().string());
                }
                LOGGER.info("Registry entry created and user is successfully logged in");

            } catch (IOException e) {
                LOGGER.error("Error creating registry entry : {} ", e);
            }

        } else {
            LOGGER.error("verification sign up link is expired ");
            return HttpUtils.onFailure(HttpStatus.SC_METHOD_NOT_ALLOWED, "please click on resend verify email link to complete sing up process");
        }

        return HttpUtils.onSuccess(null, "Registry entry created successfully and user is emailVerified");
    }

    @Override
    public ResponseDTO updateEmailIdForUserProfile(String newEmailId, String id, Boolean isUpdatedEmailIdVerified)  {
        String adminAccessToken = null;
        adminAccessToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String emailId = keycloakService.getUserById(Constants.BEARER + adminAccessToken, id, appContext.getRealm()).getEmail();
            RegistryUserWithOsId updatedRegistryUserBody = getUserFromRegistry(emailId, adminAccessToken);
            updatedRegistryUserBody.setEmailId(newEmailId);
            responseDTO = updateUserProfile(adminAccessToken, updatedRegistryUserBody, emailId, isUpdatedEmailIdVerified);
        } catch (Exception e) {
            responseDTO.setResponseCode(HttpStatus.SC_UNAUTHORIZED);
            responseDTO.setResponse("User not found");
            LOGGER.error(Constants.ERRORLOG+e);
        }
        return responseDTO;
    }

    @Override
    public ResponseDTO fetchUserDetailOnScanQrCode(String scannedUserId, String scannerAccessToken)  {
        ScanningUserDetailDto scanningUserDetailDto = new ScanningUserDetailDto();
        String emailIdOfSessionCreator = null;
        try {
            emailIdOfSessionCreator = KeycloakUtil.fetchEmailIdFromToken(scannerAccessToken, appContext.getKeyCloakServiceUrl(), appContext.getRealm(),appContext.getKeycloakPublickey());
        } catch (VerificationException e) {
            LOGGER.error(Constants.ERRORLOG+e);
        }
        String adminAccesToken = null;
        adminAccesToken = keycloakService.generateAccessToken(appContext.getAdminUserName());
        UserRepresentation scannedUserDetail = null;
        scannedUserDetail = keycloakService.getUserById(" Bearer " + adminAccesToken, scannedUserId, appContext.getRealm());
        LOGGER.info("Session Creator with emailId : {},  scanning data of user of eamilId : {}", emailIdOfSessionCreator, scannedUserDetail.getEmail());
        ResponseDTO responseDTO = new ResponseDTO();

        try {
            RegistryUserWithOsId searchRegistryUser = getUserFromRegistry(scannedUserDetail.getEmail(), adminAccesToken);
            scanningUserDetailDto = new ScanningUserDetailDto(searchRegistryUser.getUserId(), searchRegistryUser.getName()
                    , searchRegistryUser.getPhoto());
            responseDTO.setResponseCode(HttpStatus.SC_OK);
            responseDTO.setResponse(scanningUserDetailDto);
            responseDTO.setMessage(Constants.SCAN_USER_DETAIL);
        } catch (Exception e) {
            LOGGER.error("User does not exist in registry with emailId : {} ,EXCEPTION: ", scannedUserDetail.getEmail(),e);
            responseDTO.setMessage(Constants.USER_NOT_FOUND_IN_REGISTRY);
            responseDTO.setResponseCode(HttpStatus.SC_BAD_REQUEST);

        }
        return responseDTO;
    }

}
