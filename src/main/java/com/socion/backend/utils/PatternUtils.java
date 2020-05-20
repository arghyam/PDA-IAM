package com.socion.backend.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {
    private static String MOBILE_PATTERN = "[0-9]";

    private PatternUtils() {
    }

    public static boolean validatePhone(String phoneNumber) {
        Pattern pattern = Pattern.compile(MOBILE_PATTERN);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();

    }

}
