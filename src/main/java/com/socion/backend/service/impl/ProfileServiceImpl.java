package com.socion.backend.service.impl;

import com.socion.backend.AesUtil;
import com.socion.backend.config.AppContext;
import com.socion.backend.dto.ProfileTemplateDto;
import com.socion.backend.dto.ResponseDTO;
import com.socion.backend.service.ProfileService;
import com.socion.backend.utils.Constants;
import com.socion.backend.utils.PdfUtils;
import com.itextpdf.html2pdf.HtmlConverter;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Autowired
    AppContext appContext;

    @Override
    public ResponseDTO generateProfileCard(ProfileTemplateDto templateDto)  {
        ResponseDTO responseDTO = new ResponseDTO();

        File htmlTemplateFile = new File(Constants.PROFILE_FRONT_TEMPLATE);

        String htmlString = null;
        try {
            htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");
        } catch (IOException e) {
           LOGGER.error(Constants.ERRORLOG,e);
        }
        String picId = templateDto.getUserId() + "_PROFILE_" + templateDto.getUserName().replaceAll(" ", "");

        String name = templateDto.getUserName();
        String photo = templateDto.getPhoto();
        String userId = templateDto.getUserId();

        //TODO move url to application properties
        if (photo.length() < 1) {
            photo = "Dummy profile pic url";
        }


        htmlString = htmlString.replace("$profilePicPath", photo);
        htmlString = htmlString.replace("$name", name);

        ResponseDTO responseDTO1 = generateQrCodeOfAttestation(templateDto.getUserId(), picId);


        if (responseDTO1.getResponseCode() == HttpStatus.SC_OK) {
            htmlString = htmlString.replace("$qrcodepath", responseDTO1.getResponse().toString());
        }

        File newHtmlFile = new File(Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + userId + Constants.HTML_FORMAT);

        try {
            FileUtils.writeStringToFile(newHtmlFile, htmlString, "UTF-8");
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG,e);
        }

        String src = Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + userId + Constants.HTML_FORMAT;
        String dest = Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + userId + Constants.PDF_FORMAT;
        String cropPdfDest = Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + userId + "cropped" + Constants.PDF_FORMAT;
        try {
            HtmlConverter.convertToPdf(new File(src), new File(dest));
        } catch (IOException e) {

            LOGGER.info(Constants.ERRORLOG+e);

        }

        LOGGER.info("Successfully converted HTML to PDF");

        String pathOfCropedPDF = "";
        //crop PDF
        pathOfCropedPDF = PdfUtils.cropPdf(dest, cropPdfDest, Constants.PDFWIDTH, Constants.PDFHEIGHT, Constants.PDFX, Constants.PDFY);

        // html to pdf

        if (!pathOfCropedPDF.isEmpty() || !"".equals(pathOfCropedPDF)) {
            responseDTO.setResponseCode(HttpStatus.SC_OK);
            responseDTO.setMessage("SuccessFully Generated Profile card file...  ");
            responseDTO.setResponse(pathOfCropedPDF);
        } else {
            responseDTO.setResponseCode(HttpStatus.SC_BAD_REQUEST);
            responseDTO.setMessage("Problem with Generating Profile card file...  ");
        }

        return responseDTO;
    }

    private ResponseDTO generateQrCodeOfAttestation(String userId, String picId) {

        AesUtil aesUtil = new AesUtil(appContext.getKeySize(), appContext.getIterationCount());
        String encryptedUserId = aesUtil.encrypt(appContext.getSaltValue(), appContext.getIvValue(),
                appContext.getSecretKey(), userId);

        ResponseDTO responseDTO = new ResponseDTO();
        ByteArrayOutputStream bout =
                QRCode.from(encryptedUserId)
                        .withSize(Constants.TWO_FIFTY, Constants.TWO_FIFTY)
                        .to(ImageType.PNG)
                        .stream();

        try {
            OutputStream out = new FileOutputStream(Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + picId + ".png");
            bout.writeTo(out);
            out.flush();
            out.close();
            responseDTO.setResponse(Constants.PATH_OF_DIR_WHERE_FILE_BEING_SAVED + picId + ".png");
            responseDTO.setMessage("Successfully created Qr Code");
            responseDTO.setResponseCode(HttpStatus.SC_OK);

        } catch (FileNotFoundException e) {
            LOGGER.error(Constants.ERRORLOG+e);
            responseDTO.setMessage("There is issue during creation Qr Code");
            responseDTO.setResponseCode(HttpStatus.SC_BAD_REQUEST);
        } catch (IOException e) {
            LOGGER.error(Constants.ERRORLOG+e);
        }

        return responseDTO;

    }

}
