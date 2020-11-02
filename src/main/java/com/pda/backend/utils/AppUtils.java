package com.pda.backend.utils;

import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AppUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUtils.class);


    private AppUtils() {
    }

    public static boolean compareCountryCode(String countryCode, String countryCodeKeycloak) {
        if (countryCode != null && countryCodeKeycloak != null) {
            return countryCode.equalsIgnoreCase(countryCodeKeycloak);
        }
        return false;
    }

    public static String getCountryCode(UserRepresentation user) {
        List<String> list = user.getAttributes().get(Constants.COUNTRY_CODE);
        return null != list && !list.isEmpty() ? list.get(0) : Constants.COUNTRY_CODE_IND;
    }

    public static String getNewCountryCode(UserRepresentation user) {
        List<String> list = user.getAttributes().get(Constants.NEW_COUNTRY_CODE);
        return null != list && !list.isEmpty() ? list.get(0) : Constants.COUNTRY_CODE_IND;
    }
}
