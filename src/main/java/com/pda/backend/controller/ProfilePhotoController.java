package com.pda.backend.controller;

import com.pda.backend.aws.AwsConfigService;
import com.pda.backend.config.AppContext;
import com.pda.backend.dto.ProfileTemplateDto;
import com.pda.backend.dto.ResponseDTO;
import com.pda.backend.entity.User;
import com.pda.backend.service.ProfileService;
import com.pda.backend.utils.Constants;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.File;


@RestController
@RequestMapping(value = "/api/v1/", produces = {"application/json"})
public class ProfilePhotoController {

    @Autowired
    AwsConfigService awsConfigService;

    @Autowired
    AppContext appContext;

    @Autowired
    ProfileService profileService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilePhotoController.class);


    @PostMapping("template/get-user-profile-card")
    public ResponseDTO generateProfileCardForUser(@RequestBody ProfileTemplateDto templateDto)  {
        ResponseDTO responseDTO = new ResponseDTO();

        String picId = templateDto.getUserId() + "_PROFILE_" + templateDto.getUserName().replaceAll(" ", "");
        ResponseDTO responseDTO1 = profileService.generateProfileCard(templateDto);

        try {
            AmazonS3 amazonS3 = awsConfigService.awsS3Configuration();
            awsConfigService.putProfileCardObjectInAwsS3(responseDTO1.getResponse().toString(), templateDto.getUserId(), amazonS3);
            String s3BucketUrl = appContext.getAwsS3Url() + appContext.getAwsS3BucketName() + Constants.PROFILE_CARD + templateDto.getUserId();
            File htmlFile = new File(Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + templateDto.getUserId() + Constants.HTML_FORMAT);
            File pdfFile = new File(Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + templateDto.getUserId() + Constants.PDF_FORMAT);
            File qrcodeFile = new File(Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + picId + Constants.PNG_FORMAT);
            File croppedPdf = new File(Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + templateDto.getUserId() + Constants.CROPPED + Constants.PDF_FORMAT);

            if (htmlFile.delete() && pdfFile.delete() && qrcodeFile.delete() && croppedPdf.delete()) {
                LOGGER.info("File deleted successfully");
            } else {
                LOGGER.error("Failed to delete the file");
            }
            responseDTO.setResponse(s3BucketUrl);
            responseDTO.setMessage("SuccessFully uploaded profileCard to AWS S3");
            responseDTO.setResponseCode(HttpStatus.OK.value());
        } catch (AmazonServiceException e) {
            responseDTO.setResponse(e);
            responseDTO.setMessage("Issue with upload profileCard to AWS S3");
            responseDTO.setResponseCode(HttpStatus.NOT_FOUND.value());
        } catch (SdkClientException e) {
            responseDTO.setResponse(e);
            responseDTO.setMessage("Issue with upload profileCard to AWS S3");
            responseDTO.setResponseCode(HttpStatus.NOT_FOUND.value());
        }

        return responseDTO;
    }

    @GetMapping("/test")
    public User generateProfileCfardForUser() {
        return new User();
    }
}
