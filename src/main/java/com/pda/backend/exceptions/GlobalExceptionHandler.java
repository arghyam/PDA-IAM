package com.pda.backend.exceptions;

import com.pda.backend.dto.ResponseDTO;
import com.pda.backend.utils.HttpUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    /**
     * Handle internal unauthorized exception response entity.
     *
     * @param e        the e
     * @param response the response
     * @return the response entity
     * @throws IOException the io exception
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity handleUnauthorizedException(UnauthorizedException e,
                                                      HttpServletResponse response)
            throws IOException {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle internal UserCreateException exception response entity.
     *
     * @param e        the e
     * @param response the response
     * @return the response entity
     * @throws IOException the io exception
     */
    @ExceptionHandler(UserCreateException.class)
    public ResponseEntity handleUnableToCreateUserException(UserCreateException e,
                                                            HttpServletResponse response)
            throws IOException {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle internal InvalidOtpException exception response entity.
     *
     * @param e        the e
     * @param response the response
     * @return the response entity
     * @throws IOException the io exception
     */
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity habdleInvaidOtpException(InvalidOtpException e,
                                                   HttpServletResponse response)
            throws IOException {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }


    /**
     * Handle internal InvalidOtpException exception response entity.
     *
     * @param e        the e
     * @param response the response
     * @return the response entity
     * @throws IOException the io exception
     */
    @ExceptionHandler(UnprocessableEntitiesException.class)
    public ResponseEntity handleUnprocessableException(UnprocessableEntitiesException e,
                                                       HttpServletResponse response)
            throws IOException {
        return new ResponseEntity<>(new ValidationErrorResponse(e.getErrors()),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }


    /**
     * Handle internal NOTFOUNTEXCEPTION exception response entity.
     *
     * @param e        the e
     * @param response the response
     * @return the response entity
     * @throws NotFoundException the io exception
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseDTO handleNotFoundException(NotFoundException e,
                                               HttpServletResponse response) {

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO = HttpUtils.onFailure(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return responseDTO;
    }

}
