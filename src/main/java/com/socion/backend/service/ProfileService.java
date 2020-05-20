package com.socion.backend.service;


import com.socion.backend.dto.ProfileTemplateDto;
import com.socion.backend.dto.ResponseDTO;

public interface ProfileService {

    public ResponseDTO generateProfileCard(ProfileTemplateDto templateDto);

}
