package com.socion.backend.response;

import org.apache.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommonResponse {

    private CommonResponse() {
    }


    public static Map<String, Object> success(Object response, String message) {
        Map<String, Object> responseValue = new LinkedHashMap<>();
        responseValue.put("responseCode", HttpStatus.SC_OK);
        responseValue.put("response", response);
        responseValue.put("message", message);

        return responseValue;

    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> responseValue = new LinkedHashMap<>();
        responseValue.put("responseCode", HttpStatus.SC_BAD_REQUEST);
        responseValue.put("response", "");
        responseValue.put("message", message);

        return responseValue;

    }


}
