/*
 *  Copyright 2010 Radim Hatlapatka.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package cz.muni.pdfjbim;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class representing JBIG2 image in format suitable for pdf (without header,...)
 *
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class PdfImage {
    
    private int objectNumber;
    private int generationNumber;
    private PdfImageInformation pdfImageInformation;
    private File imageDataFile;
    private static final Logger log = LoggerFactory.getLogger(PdfImage.class);

    /**
     * constructor sets pointer to file containing image data, pdfImageInformation will be set later
     * @param imageDataFile represents pointer to file containing image data
     */
    public PdfImage(File imageDataFile) {
        if (imageDataFile == null) {
            throw new NullPointerException("imageDataFile");
        }
        this.imageDataFile = imageDataFile;
    }

    /**
     * constructor which sets both atributes (image data and informations about this image)
     * @param imageData represents data of image
     * @param pdfImageInformation represents associated information of image like width, height, position in original pdf,...
     */
    public PdfImage(File imageData, PdfImageInformation pdfImageInformation) {
        this.imageDataFile = imageData;
        this.pdfImageInformation = pdfImageInformation;
    }

    /**
     * return byte array of image data
     * @return byte array with image data
     * @throws PdfRecompressionException if file wasn't found
     *      or there is too much data in the file that cannot be contained in one byte array
     */
    public byte[] getImageData() throws PdfRecompressionException {
        Long sizeOfFile = imageDataFile.length();
        int imageSize = 0;
        FileInputStream jbImageInput = null;
        log.debug("Getting image data from {}", imageDataFile);
        try {
            jbImageInput = new FileInputStream(imageDataFile);
            if (sizeOfFile > Integer.MAX_VALUE) {
                throw new PdfRecompressionException("cannot process image greater than " + Integer.MAX_VALUE);
            }
            
            DataInput inputData = new DataInputStream(jbImageInput);
            imageSize = sizeOfFile.intValue();
            byte[] imageBytes = new byte[imageSize];
            inputData.readFully(imageBytes);
            return imageBytes;
        } catch (FileNotFoundException ex) {
            throw new PdfRecompressionException(ex);
        } catch (IOException ioEx) {
            throw new PdfRecompressionException("io error", ioEx);
        }
    }

    /**
     * sets information of pdf image by calling constructor
     * @param key represents pdf object key
     * @param pageNum represents number of page in PDF where is this image belong to
     * @param width represents with of image
     * @param height represents height of image
     * @param objNum
     * @param genNum  
     */
    public void setPdfImageInformation(String key, int pageNum, int width, int height, int objNum, int genNum) {
        pdfImageInformation = new PdfImageInformation(key, width, height, objNum, genNum);
    }

    /**
     * sets information of pdf image dirrectly
     * @param pdfImageInformation represents information about image
     */
    public void setPdfImageInformation(PdfImageInformation pdfImageInformation) {
        this.pdfImageInformation = pdfImageInformation;
    }

    /**
     * @return informations about image
     */
    public PdfImageInformation getPdfImageInformation() {
        return pdfImageInformation;
    }

    /**
     *
     * @return file containing image data
     */
    public File getImageDataFile() {
        return imageDataFile;
    }

    @Override
    public String toString() {
        return "PdfImage{" + "objectNumber=" + objectNumber + ", generationNumber=" + generationNumber + ", pdfImageInformation=" + pdfImageInformation + ", imageDataFile=" + imageDataFile + '}';
    }        
}
