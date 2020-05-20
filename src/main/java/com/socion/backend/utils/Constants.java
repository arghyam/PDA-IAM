package com.socion.backend.utils;

public class Constants {

    public static final String ERRORLOG="Error Log as:";
    public static final int TWO = 2;
    public static final int TWO_ZERO_ONE = 201;
    public static final int TWO_HUNDRED = 200;
    public static final int FIVE_HUNDRED = 500;
    public static final int TWO_FIFTY = 500;
    public static final String OK = "OK";

    public static final int FOUR_HUNDRED = 400;
    public static final int THREE_SIX_HUNDRED=3600;
    public static final int PDFWIDTH=21;
    public static final int PDFHEIGHT=20;
    public static final int PDFX=0;
    public static final int PDFY=619;
    public static final int INTSIXTY=60;
    public static final int FOUR_ZERO_FOUR=404;
    public static final String REMOVE_BEARER = "Removing Bearer string from Authorization token";
    public static final String USER_UPDATE_SUCCESSFUL_FOR_REGISTRY ="User update successful in Registry for user : {}";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_UPDATION_FAILED_REGISTRY = "User updation failed in Registry for user : {}";
    public static final String USER_UPDATION_FAILED = "User updation failed in Keycloak for user : {}";
    public static final int FOUR_ZERO_NINE = 409;
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String BEARER = "Bearer ";
    public static final int NINE_LAKH = 900000;
    public static final int THOUSAND = 1000;
    public static final double SIXTY = 60.0;

    public static final float WIDTH = 8.5f;
    public static final float HEIGHT = 6f;
    public static final float TOLERANCE = 1f;


    public  static final int TEN=10;
    public static final int FIVE = 5;
    public static final String SEND_PASSWORD_IN_ENCRYPTD_FORMAT = "Please send the password in the encrypted format. Error -> ";
    public static final int ONE_LAKH = 100000;
    public static final String PASSWORD_NOT_ENCRYPTED_MESSAGE = "Password not in encrypted format.";
    public static final String USER_WITH_PHONENUMBER_NOTEXISTS = "User with PhoneNumber does not exist.";
    public static final String KEYCLOAK_REGISTER_API = "admin/realms/{realm}/users";
    public static final String GENERATE_ACCESS_TOKEN = "realms/{realm}/protocol/openid-connect/token";
    public static final String GET_USER_API = "admin/realms/{realm}/users/{id}";
    public static final String LOGOUT_API = "/auth/admin/realms/{realm}/users/{id}/logout";
    public static final String SEARCH_USERS = "admin/realms/{realm}/users";
    public static final String UPDATE_USER = "admin/realms/{realm}/users/{id}";
    public static final String FIELD_INVALID = " is invalid";
    public static final String PASSWORD = "password";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String RESET_PASSWORD = "/auth/admin/realms/{realm}/users/{id}/execute-actions-email";
    public static final String REGISRY_ADD_USER = "add";
    public static final String REGISRY_SEARCH_USER = "search";
    public static final String REGISRY_SEARCH = "search";
    public static final String REGISTRY_UPDATE_USER = "update";
    public static final String USER_REGISTRATION_COMPLETE_SIGN_UP = "api/v1/user/complete-sign-up?emailId=";
    public static final String USER_EMAIL_UPDATE = "api/v1/user/update-email-id?emailId=";
    public static final String USER_EMAIL_UPDATE_V2 = "api/v2/user/update-email-id-post-verification?emailId=";
    public static final String SAVE_IAM_NOITFICATION = "session/notifications/save-iam-notification";
    public static final String GET_USER_PROFILE_CARD = "template/get-user-profile-card";
    public static final String SEND_VERIFICATION_EMAIL = "admin/realms/{realm}/users/{id}/send-verify-email";
    public static final String DELETE_USER="admin/realms/{realm}/users/{id}";
    public static final String FORGOT_PASSWORD = "Forgot password";
    public static final String UPDATE_PROFILE_PHOTO_NOTIFICATION_TRIGGERED = "notification triggered for profile photo update";
    public static final String CHANGE_NAME = "Change name";
    public static final String CHANGE_PHONE_NUM = "Change phone no.";
    public static final String CHANGE_OLD_EMAIL = "Change Existing email";
    public static final String UPDATE_PHOTO = "Update photo";
    public static final String REMOVE_PHOTO="Remove photo";
    public static final String ADD_EMAIL = "Add new email";
    public static final String CHANGE_PASSWORD = "Reset Password";
    public static final String REACTIVATE_ACCOUNT = "Reactivate_account";
    public static final String DEACTIVATE_ACCOUNT = "Deactivate_account";
    public static final String EMAIL_UPDATE_FLAG="email_update_flag";
    public static final String EMAIL_UPDATE_ID="email_update_id";
    //Response messages
    public static final String EMAIL_NOT_VERIFIED = "Email is not verified. Please verify email and then try to sign in";
    public static final String USER_CREATED_SUCCESSFULLY = "User Created Successfully. Please validate the OTP to set the Password.";
    public static final String EMAIL_SENT_SUCCESSFULLY = "Email sent successfully";
    public static final String ERROR_SENDING_EMAIL = "Error sending email to the user. Resend the email";
    public static final String USER_ALREADY_EXISTS = "User already exists.";
    public static final String INVALID_PHONE_NO_LENGTH = "This phone number is not valid for this country";
    public static final String ERROR_REFRESHING_TOKEN = "Error refreshing token";
    public static final String USER_DOES_NOT_EXIST = "User does not exist with this PhoneNumber";
    public static final String INVALID_EMAIL_ID = "Email Id is not registered. Sign up ?";
    public static final String INVALID_PASSWORD = "Invalid Password";
    public static final String USER_UPDATE_SUCCESS = "User profile details updated successfully";
    public static final String USER_STATUS_UPDATE_SUCCESS = "User Status updated successfully";
    public static final String USER_UPDATE_SUCCESS_KEYCLOAK = "User updated successfully in Keycloak";
    public static final String USER_UPDATE_SUCCESS_REGISTRY = "User updated successfully in Registry";
    public static final String USER_REGISTRATION_COMPLETE = "You Successfully Completed Sign Up Process. Login from App to Continue";
    public static final String EMAIL_VERIFICATION_LINK_EXPIRED = "Your email verification link is expired. Please resend the verification link from app";
    public static final String USER_NOT_FOUND_IN_REGISTRY = "User does not exist in registry with this phone number";
    public static final String SCAN_USER_DETAIL = "Successfully read user detail on Scan QR Code";

    public static final String EMAILID_UPDATE_FAILURE = "Your EmailId udpate failed due to : ";

    public static final String PHONE_UPDATE_SUCCESS = "User Phone Number is updated successfully";
    public static final String PHONE_UPDATE_FAILURE = "User Phone Number udpate failed. Reason : ";


    public static final String PHONE_UPDATE_VERIFY_FAILURE = "User Phone Number Verification failed. Reason : ";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String SALUTATION = "salutation";
    public static final String PROFILE_CARD_CREATED = "profileCardUrl";
    public static final String REG_ENTRY_CREATED = "registry_entry_created";
    public static final String TRUE = "true";
    public static final String TEXTFORMAT = "text/html";
    public static final String EMAIL_ACTION_RESET_PWD = "reset_password";
    public static final String EMAIL_ACTION_VERIFY_ACCOUNT = "verify_account";
    public static final String EMAIL_ACTION_UPDATE_EMAIL_ID = "update_email_id";
    public static final String EMAIL_ACTION_UPDATE_PHONE = "update_phone";

    public static final int MAX_IDLE = 512;
    public static final int MAX_TOTAL = 128;
    public static final int MIN_IDLE = 16;
    public static final int SIXTY_SECONDS = 60;
    public static final int THIRTY_SECONDS = 30;
    public static final long THIRTY_SECONDS_L = 30L;
    public static final String REALMS ="realms/";

    public static final String USER_NOT_AUTHORIZED="User is not Authorized";


    public static final String SENT_VERIFY_EMAIL_FOR_SIGN_UP = "SENT_VERIFY_EMAIL_FOR_SIGN_UP";
    public static final String OTP = "OTP";
    public static final String OTP_EXPIRY_TIME = "otp_expiry_time";
    public static final String TYPE_OF_OTP = "type_of_otp";
    public static final String NEW_PHONE_NUMBER = "new_phone_number";
    public static final String NEW_COUNTRY_CODE = "new_country_code";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String COUNTRY_CODE = "country_code";
    public static final String OTP_VALIDATED_FOR = "otp_validated_for";
    public static final String REGISTRATION_KEYWORD = "Registration-OTP";
    public static final String FORGOT_PASSWORD_KEYWORD = "ForgotPassword-OTP";
    public static final String UPDATE_PHONE_KEYWORD = "UpdatePhone-OTP";
    public static final String NEW_PHONE_KEYWORD_OTP = "NewPhone-OTP";
    public static final String UPDATE_EMAIL_KEYWORD = "UpdateEmail-OTP";
    public static final String IS_USER_VALIDATED = "is_user_validated";

    //Cache manager values
    public static final String CACHE_ACCESS_TOKEN = "accessToken";
    public static final String CACHE_REGISTRY_USER = "registryUser";

    public static final String PHONE_NUMBER_EXISTS = "User Already Exist with this phone number please try to update from another phone number";

    //profile
    public static final String PATH_OF_DIR_WHERE_FILE_BEING_SAVED = "{DIRECTORY PATH}";
    public static final String PDF_FORMAT = ".pdf";
    public static final String HTML_FORMAT = ".html";
    public static final String PROFILE_FRONT_TEMPLATE = "{TEMPLATE PATH}";
    public static final String PNG_FORMAT = ".png";
    public static final String CROPPED = "cropped";
    public static final String PROFILE_CARD = "/profile-card/";
    public static final String COUNTRY_CODE_IND = "+91";
    public static final String PROPERTY_SOURCE_NAME = "defaultProperties";


    private Constants() {
    }
}




