package com.pda.backend.service;

import com.pda.backend.dto.PasswordBodyDto;
import com.pda.backend.dto.ResponseDTO;
import com.pda.backend.dto.UserRegisterDTO;
import com.pda.backend.entity.RegistryUserWithOsId;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.validation.BindingResult;



public interface UserService {

    public ResponseDTO register(UserRegisterDTO user, BindingResult bindingResult) ;

    public void valiadtePojo(BindingResult bindingResult);

    public Keycloak getKeycloak();

    public void sendPasswordUpdateConfirmationEmail(String emailId) ;

    public ResponseDTO fetchUserProfileDetail(String email, String accessToken) ;

    public ResponseDTO updateUserProfile(String accessToken, RegistryUserWithOsId registryUser, String userEmail, Boolean isUpdatedEmailIdVerified);

    public ResponseDTO changeUserPassword(String email, PasswordBodyDto passwordBodyDto);

    public void sendVerificationEmailAndCreateRegistryEntry(UserRepresentation userRepresentation) ;

    public ResponseDTO createEntryInRegistryAfterEmailVerification(String email);

    public ResponseDTO updateEmailIdForUserProfile(String emailId, String userId, Boolean isUpdatedEmailIdVerified);

    public ResponseDTO fetchUserDetailOnScanQrCode(String userId, String accessToken);

    public void sendVerificationEmailForCompletingSignup(String emailId);



}
