package com.socion.backend.service;

import com.socion.backend.dto.AccessTokenResponseDTO;
import com.socion.backend.dto.LoginDTO;
import com.socion.backend.dto.ResponseDTO;
import com.socion.backend.dto.UserResponseDTO;
import org.springframework.validation.BindingResult;


public interface LoginService {

    public UserResponseDTO login(LoginDTO loginDTO, BindingResult bindingResult) ;

    public ResponseDTO logout(String id);

    public UserResponseDTO refreshAccessToken(LoginDTO loginDTO);

    public ResponseDTO resendVerifyEmail(String email);

    public AccessTokenResponseDTO userLogin(LoginDTO loginDTO, UserResponseDTO responseDTO);
}
