package com.socion.backend.dao;

import com.socion.backend.dto.AccessTokenResponseDTO;
import com.socion.backend.dto.LoginDTO;
import com.socion.backend.dto.ResponseDTO;
import org.keycloak.representations.idm.UserRepresentation;



public interface KeycloakService {

    public void register(String token, UserRepresentation userRepresentation);


    public ResponseDTO logout(String id) ;

    public UserRepresentation getUserById(String token, String id, String realm) ;

    public UserRepresentation getUserByUsername(String token, String username, String realm) ;

    public void updateUser(String token, String id, UserRepresentation user, String realm) ;

    public AccessTokenResponseDTO refreshAccessToken(LoginDTO loginDTO) ;

    public ResponseDTO forgotPassword(String emailId);

    public ResponseDTO sendVerificationEmail(String accessToken, String id, String realm) ;

    public String generateAccessToken(String username);
}
