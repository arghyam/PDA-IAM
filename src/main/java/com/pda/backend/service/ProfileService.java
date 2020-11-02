package com.pda.backend.service;


import com.pda.backend.dto.ProfileTemplateDto;
import com.pda.backend.dto.ResponseDTO;

public interface ProfileService {

    public ResponseDTO generateProfileCard(ProfileTemplateDto templateDto);

}
