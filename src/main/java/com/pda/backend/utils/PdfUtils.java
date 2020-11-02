package com.pda.backend.utils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfStamper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;

public class PdfUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfUtils.class);

    private PdfUtils() {
    }


    public static String cropPdf(String src, String dest, int w, int h, int x, int y) {

        float width = Constants.WIDTH * w;
        float height = Constants.HEIGHT * h;
        float tolerance = Constants.TOLERANCE;


        PdfReader reader = null;
        try {
            reader = new PdfReader(src);
        } catch (IOException e) {
            LOGGER.error("Error With Log:", e);
        }

        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            Rectangle rectangle = reader.getCropBox(i);
            Rectangle cropBox = new Rectangle(x, y, rectangle.getWidth(), rectangle.getHeight());

            float widthToAdd = width - cropBox.getWidth();
            float heightToAdd = height - cropBox.getHeight();
            if (Math.abs(widthToAdd) > tolerance || Math.abs(heightToAdd) > tolerance) {
                float[] newBoxValues = new float[]{
                        cropBox.getLeft() - widthToAdd / Constants.TWO,
                        cropBox.getBottom() - heightToAdd / Constants.TWO,
                        cropBox.getRight() + widthToAdd / Constants.TWO,
                        cropBox.getTop() + heightToAdd / Constants.TWO
                };
                PdfArray newBox = new PdfArray(newBoxValues);

                PdfDictionary pageDict = reader.getPageN(i);
                pageDict.put(PdfName.CROPBOX, newBox);
                pageDict.put(PdfName.MEDIABOX, newBox);
            }
        }

        PdfStamper stamper = null;
        try {
            stamper = new PdfStamper(reader, new FileOutputStream(dest));
            stamper.close();
        } catch (DocumentException e) {
            LOGGER.error("Log Error at:", e);
        } catch (IOException e) {
            LOGGER.error("Log Error at:", e);
        }

        return dest;

    }
}
