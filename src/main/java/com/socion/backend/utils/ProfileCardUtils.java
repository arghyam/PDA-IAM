package com.socion.backend.utils;

import com.socion.backend.config.AppContext;
import com.socion.backend.dto.ProfileTemplateDto;
import com.socion.backend.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProfileCardUtils {

    @Autowired
    AppContext appContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileCardUtils.class);


    public ResponseDTO generateUserProfileCard(ProfileTemplateDto templateDto) {
        HttpEntity<ProfileTemplateDto> request = new HttpEntity<>(templateDto);
        String requestUrl = appContext.getEntityServerUrl() + Constants.GET_USER_PROFILE_CARD;
        RestTemplate restTemplate = new RestTemplate();
        LOGGER.info("Generating profile card for user..");
        return restTemplate.postForObject(requestUrl, request, ResponseDTO.class);

    }
}
