package com.pda.backend.dao;

import com.pda.backend.dto.AccessTokenResponseDTO;
import com.pda.backend.dto.LoginDTO;
import com.pda.backend.dto.ResponseDTO;
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
