package com.socion.backend.utils;

import com.socion.backend.config.AppContext;
import org.keycloak.RSATokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.exceptions.TokenNotActiveException;
import org.keycloak.exceptions.TokenSignatureInvalidException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeycloakUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUtil.class);

    private KeycloakUtil() {
    }

    public static String fetchEmailIdFromToken(String accessToken, String baseUrl, String realm, String publicKeyString) throws VerificationException {
        try {
            PublicKey publicKey = toPublicKey(publicKeyString);
            AccessToken token = RSATokenVerifier.verifyToken(accessToken, publicKey, baseUrl + Constants.REALMS + realm);
            return token.getEmail();
        } catch (TokenSignatureInvalidException exception) {
            LOGGER.error("Sinature of  access token is improper. Missed some content of Access Token : {},EXCEPTION:", exception.getLocalizedMessage(),exception);
            throw new TokenSignatureInvalidException(exception.getToken(), exception.getCause());

        } catch (TokenNotActiveException e) {
            LOGGER.error("Inactive access  token. Please try with fresh  access token : {},EXCEPTION:", e.getLocalizedMessage(),e);
            throw new TokenNotActiveException(e.getToken(), e.getCause());

        } catch (VerificationException e) {
            LOGGER.error("Invalid  access token. Please verify the access token : {},EXCEPTION:", e.getLocalizedMessage(),e);
            throw new VerificationException();
        }
    }

    public static String fetchPhoneNumberFromToken(String accessToken, String baseUrl, String realm,String publicKeyString) throws VerificationException {
        try {
            PublicKey publicKey = toPublicKey(publicKeyString);
            AccessToken token = RSATokenVerifier.verifyToken(accessToken, publicKey, baseUrl + Constants.REALMS + realm);
            return token.getPreferredUsername();
        } catch (TokenSignatureInvalidException exception) {
            LOGGER.error("Sinature of  access token is improper. Missed some content of Access Token : {},EXCEPTION:", exception.getLocalizedMessage(),exception);
            throw new TokenSignatureInvalidException(exception.getToken(), exception.getCause());

        } catch (TokenNotActiveException e) {
            LOGGER.error("Inactive Access Token. Please try with fresh  access token : {},EXCEPTION:", e.getLocalizedMessage(),e);
            throw new TokenNotActiveException(e.getToken(), e.getCause());

        } catch (VerificationException e) {
            LOGGER.error("Invalid access Token. Please verify the access token : {},EXCEPTION:", e.getLocalizedMessage(),e);
            throw new VerificationException();
        }
    }

    public static String fetchUserIdFromToken(String accessToken, String baseUrl, String realm,String publicKeyString) throws VerificationException {
        try {
            PublicKey publicKey = toPublicKey(publicKeyString);
            AccessToken token = RSATokenVerifier.verifyToken(accessToken, publicKey, baseUrl + Constants.REALMS + realm);
            return token.getSubject();
        } catch (VerificationException e) {
            LOGGER.error("Invalid access token. Please verify the access token : {},EXCEPTION:", e.getLocalizedMessage(),e);
            throw new VerificationException();
        }
    }

    public static String checkValidityOfToken(String accessToken, String baseUrl, String realm,String publicKeyString) throws VerificationException {
        try {
            PublicKey publicKey = toPublicKey(publicKeyString);
            AccessToken token = RSATokenVerifier.verifyToken(accessToken, publicKey, baseUrl + Constants.REALMS + realm);
            return String.valueOf(token.getExpiration());
        } catch (VerificationException e) {
            LOGGER.error("Invalid Access  token. Please verify the access token : {},EXCEPTION:", e.getLocalizedMessage(),e);
            throw new VerificationException();
        }
    }

    private static PublicKey toPublicKey(String publicKeyString) {
        try {
            byte[] bytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpecification = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpecification);
        } catch (Exception e) {
            LOGGER.error("Error Creating public key,EXCEPTION:",e);
            return null;
        }
    }

    public static void verifyToken(String accessToken, String keyCloakServiceUrl, String realm) {
    }
}
