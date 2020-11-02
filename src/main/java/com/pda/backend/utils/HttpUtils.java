package com.pda.backend.utils;

import com.pda.backend.dto.ResponseDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class HttpUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private HttpUtils() {
    }


    public static ResponseDTO onSuccess(Object responseObject, String message) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setResponseCode(HttpStatus.OK.value());
        responseDTO.setMessage(message);
        if (responseObject != null) {
            Gson gson = new GsonBuilder().create();
            String response = ((Gson) gson).toJson(responseObject);
            responseDTO.setResponse(response);
        }
        return responseDTO;
    }


    public static ResponseDTO onSuccessJson(Object responseObject, String message) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setResponseCode(HttpStatus.OK.value());
        responseDTO.setMessage(message);
        if (responseObject != null) {
            responseDTO.setResponse(responseObject);
        }
        return responseDTO;
    }



    public static ResponseDTO onFailure(int statusCode, String message) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setResponseCode(statusCode);
        responseDTO.setMessage(message);
        return responseDTO;
    }

    public static String convertJsonObjectToString(Object object) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(object);
    }

    public static <T extends Object> T convertStringToJsonObject(String jsonString, Class<T> type) {
        Gson gson = new GsonBuilder().create();
        return ((Gson) gson).fromJson(jsonString, type);
    }

    public static PublicKey toPublicKey(String publicKeyString) {
        try {
            byte[] bytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpecification = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpecification);
        } catch (Exception e) {
            LOGGER.error("Error Creating public key"+e);
            return null;
        }
    }


}
