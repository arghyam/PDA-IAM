package com.pda.backend.service;

import com.pda.backend.dto.AccessTokenResponseDTO;
import com.pda.backend.dto.LoginDTO;
import com.pda.backend.dto.ResponseDTO;
import com.pda.backend.dto.UserResponseDTO;
import org.springframework.validation.BindingResult;


public interface LoginService {

    public UserResponseDTO login(LoginDTO loginDTO, BindingResult bindingResult) ;

    public ResponseDTO logout(String id);

    public UserResponseDTO refreshAccessToken(LoginDTO loginDTO);

    public ResponseDTO resendVerifyEmail(String email);

    public AccessTokenResponseDTO userLogin(LoginDTO loginDTO, UserResponseDTO responseDTO);
}
