package com.pda.backend.service;

import com.pda.backend.dto.*;
import com.pda.backend.entity.RegistryUserWithOsId;

import org.springframework.validation.BindingResult;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public interface LoginV2Service {

    ResponseDTO register(BindingResult bindingResult, SignUpDTO signUpDTO) throws IOException;

    public ResponseDTO changeUserPassword(String accessToken, PasswordBodyDto passwordBodyDto, BindingResult bindingResult);

    public UserResponseDTO login(LoginDTO loginDTO, BindingResult bindingResult);

    public ResponseDTO validateOtp(ValidateOtpDto otpDto, BindingResult bindingResult) ;

    public UserResponseDTO setUserPassword(SetPasswordDto passwordBodyDto, BindingResult bindingResult) ;

    public ResponseDTO validateUserNameAndSendOTP(String phoneNumber, String typeOfOTP, String countryCode) throws IOException;

    public UserResponseDTO resetUserPassword(SetPasswordDto passwordDto, BindingResult bindingResult) ;

    ResponseDTO fetchUserProfileDetail(String userId, String accesstoken) ;

    ResponseDTO fetchUserProfileDetailFromPhoneNUmber(PhoneNumberListDTO phoneNumberListDTO, String accessToken);

    ResponseDTO fetchUserDetailOnScanQrCode(String scannedUserId, String scannerAccessToken) ;

    ResponseDTO updateEmailIdForUserProfile(String newEmailId, String id, Boolean isUpdatedEmailIdVerified, String accessToken,String emailUpdateId);

    ResponseDTO updateUserProfile(UpdateUserProfileDTO updateUserProfileDTO);

    ResponseDTO changeActiveStatus(String accessToken, ChangeStatusDto changeStatusDto);

    List<LinkedHashMap> getUsersByUserIds(List<String> useIds);

    ResponseDTO updateUserProfilePhoto(String userId,boolean isRemovePhoto);

    RegistryUserWithOsId getUsersByUserId(String useId) throws IOException;

    ResponseDTO updatePhoneNumberVerification(String phoneNumber, String accessToken, String countryCode) ;

    ResponseDTO updatePhoneNumberPostEmailVerification(String newPhoneNumber, String userId, String countryCode);

    ResponseDTO getCountryCodes();

}
