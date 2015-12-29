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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will read a pdf and extract images (names of images are stored in list and put back their compressed version)
 * 
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 * @deprecated
 */
public class PdfImageProcessor {

    private int imageCounter = 1;
    private List<String> namesOfImages = new ArrayList<String>();
    private List<PdfImageInformation> originalImageInformations = new ArrayList<PdfImageInformation>();
    private static final Logger log = LoggerFactory.getLogger(PdfImageProcessor.class);

    /**
     * @return names of images in a list
     */
    public List<String> getNamesOfImages() {
        return namesOfImages;
    }

    /**
     *
     * @return list of informations about images
     */
    public List<PdfImageInformation> getOriginalImageInformations() {
        return originalImageInformations;
    }

    /**
     * This method extracts images from PDF
     * @param pdfFile input PDF file
     * @param password password for access to PDF if needed
     * @param pagesToProcess list of pages which should be processed if null given => processed all pages
     *      -- not working yet
     * @param binarize -- enables processing of nonbitonal images as well (LZW is still not
     *      processed because of output with inverted colors)
     * @throws PdfRecompressionException if problem to extract images from PDF
     */
    public void extractImages(File pdfFile, String password, Set<Integer> pagesToProcess, Boolean binarize) throws PdfRecompressionException {
        if (binarize == null) {
            binarize = false;
        }
        // checking arguments and setting appropriate variables
        if (pdfFile == null) {
            throw new IllegalArgumentException("pdfFile");
        }

        String prefix = null;

        // if prefix is not set then prefix set to name of pdf without .pdf
        // if pdfFile has unconsistent name (without suffix .pdf) and name longer than 4 chars then last for chars are removed
        // and this string set as prefix
        if ((prefix == null) && (pdfFile.length() > 4)) {
            String fileName = pdfFile.getName();
            prefix = fileName.substring(0, fileName.length() - 4);
        }
        try {
            InputStream is = new FileInputStream(pdfFile);
            extractImagesUsingPdfParser(is, prefix, password, pagesToProcess, binarize);
        } catch (FileNotFoundException ex) {
            throw new PdfRecompressionException("File doesn't exist", ex);
        }
    }

    /**
     * This method extracts images from PDF
     * @param pdfFile name of input PDF file
     * @param password password for access to PDF if needed
     * @param pagesToProcess list of pages which should be processed if null given => processed all pages
     *      -- not working yet     
     * @param binarize -- enables processing of nonbitonal images as well (LZW is still not
     *      processed because of output with inverted colors)
     * @throws PdfRecompressionException if problem to extract images from PDF
     */
    public void extractImages(String pdfFile, String password, Set<Integer> pagesToProcess, Boolean binarize) throws PdfRecompressionException {
        if (binarize == null) {
            binarize = false;
        }
        // checking arguments and setting appropriate variables
        if (pdfFile == null) {
            throw new IllegalArgumentException(pdfFile);
        }

        String prefix = null;

        // if prefix is not set then prefix set to name of pdf without .pdf
        // if pdfFile has unconsistent name (without suffix .pdf) and name longer than 4 chars then last for chars are removed
        // and this string set as prefix
        if ((prefix == null) && (pdfFile.length() > 4)) {
            prefix = pdfFile.substring(0, pdfFile.length() - 4);
        }

        try {
            InputStream is = new FileInputStream(pdfFile);
            extractImagesUsingPdfParser(is, prefix, password, pagesToProcess, binarize);
        } catch (FileNotFoundException ex) {
            throw new PdfRecompressionException("File doesn't exist", ex);
        }
    }

    /**
     * This method extracts images by going through all COSObjects pointed from xref table
     * @param pdfFile name of input PDF file
     * @param password password for access to PDF if needed
     * @param pagesToProcess list of pages which should be processed if null given => processed all pages
     *      -- not working yet
     * @param binarize -- enables processing of nonbitonal images as well (LZW is still not
     *      processed because of output with inverted colors)
     * @throws PdfRecompressionException if problem to extract images from PDF
     */
    public void extractImages(InputStream is, String password, Set<Integer> pagesToProcess, Boolean binarize) throws PdfRecompressionException {
        if (binarize == null) {
            binarize = false;
        }
        // checking arguments and setting appropriate variables
        String prefix = PdfImageProcessor.class.getName();
        extractImagesUsingPdfParser(is, prefix, password, pagesToProcess, binarize);
    }

    /**
     * This method extracts images by going through all COSObjects pointed from xref table
     * @param is input stream containing PDF file
     * @param password password for access to PDF if needed
     * @param pagesToProcess list of pages which should be processed if null given => processed all pages
     *      -- not working yet
     * @param binarize -- enables processing of nonbitonal images as well (LZW is still not
     *      processed because of output with inverted colors)
     * @throws PdfRecompressionException if problem to extract images from PDF
     */
    public void extractImagesUsingPdfParser(InputStream is, String prefix, String password, Set<Integer> pagesToProcess, Boolean binarize) throws PdfRecompressionException {
        // checking arguments and setting appropriate variables
        if (binarize == null) {
            binarize = false;
        }

        InputStream inputStream = null;
        if (password != null) {
            try {
                ByteArrayOutputStream decryptedOutputStream = null;
                PdfReader reader = new PdfReader(is, password.getBytes());
                PdfStamper stamper = new PdfStamper(reader, decryptedOutputStream);
                stamper.close();
                inputStream = new ByteArrayInputStream(decryptedOutputStream.toByteArray());
            } catch (DocumentException ex) {
                throw new PdfRecompressionException(ex);
            } catch (IOException ex) {
                throw new PdfRecompressionException("Reading file caused exception", ex);
            }
        } else {
            inputStream = is;
        }


        PDFParser parser = null;
        COSDocument doc = null;
        try {
            parser = new PDFParser(inputStream);
            parser.parse();
            doc = parser.getDocument();


            List<COSObject> objs = doc.getObjectsByType(COSName.XOBJECT);
            if (objs != null) {
                for (COSObject obj : objs) {
                    COSBase subtype = obj.getItem(COSName.SUBTYPE);
                    if (subtype.toString().equalsIgnoreCase("COSName{Image}")) {
                        COSBase imageObj = obj.getObject();
                        COSBase cosNameObj = obj.getItem(COSName.NAME);
                        String key;
                        if (cosNameObj != null) {
                            String cosNameKey = cosNameObj.toString();
                            int startOfKey = cosNameKey.indexOf("{") + 1;
                            key = cosNameKey.substring(startOfKey, cosNameKey.length() - 1);
                        } else {
                            key = "im0";
                        }
                        int objectNum = obj.getObjectNumber().intValue();
                        int genNum = obj.getGenerationNumber().intValue();
                        PDXObjectImage image = (PDXObjectImage) PDXObjectImage.createXObject(imageObj);

                        PDStream pdStr = new PDStream(image.getCOSStream());
                        List filters = pdStr.getFilters();

                        if ((image.getBitsPerComponent() > 1) && (!binarize)) {
                            log.info("It is not a bitonal image => skipping");

                            continue;
                        }

                        // at this moment for preventing bad output (bad coloring) from LZWDecode filter
                        if (filters.contains(COSName.LZW_DECODE.getName())) {
                            log.info("This is LZWDecoded => skipping");
                            continue;

                        }

                        // detection of unsupported filters by pdfBox library
                        if (filters.contains("JBIG2Decode")) {
                            log.warn("Allready compressed according to JBIG2 standard => skipping");
                            continue;
                        }

                        if (filters.contains("JPXDecode")) {
                            log.warn("Unsupported filter JPXDecode => skipping");
                            continue;
                        }

                        String name = getUniqueFileName(prefix, image.getSuffix());
                        log.info("Writing image:" + name);
                        image.write2file(name);


                        PdfImageInformation pdfImageInfo =
                                new PdfImageInformation(key, image.getWidth(), image.getHeight(), objectNum, genNum);
                        originalImageInformations.add(pdfImageInfo);

                        namesOfImages.add(name + "." + image.getSuffix());

                    }
//                    }
                }
            }
        } catch (IOException ex) {
            throw new PdfRecompressionException("Unable to parse PDF document", ex);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException ex) {
                    throw new PdfRecompressionException(ex);
                }
            }
        }
    }

    /**
     * @deprecated -- do not use doesn't work properly yet
     * This method extracts images by going through PDF tree structure
     * @param pdfFile name of input PDF file
     * @param password password for access to PDF if needed
     * @param pagesToProcess list of pages which should be processed if null given => processed all pages
     *      -- not working yet
     * @param silent -- if true error messages are not written to output otherwise they are
     * @param binarize -- enables processing of nonbitonal images as well (LZW is still not
     *      processed because of output with inverted colors)
     * @throws PdfRecompressionException if problem to extract images from PDF
     */
    public void extractImagesUsingPdfObjectAccess(String pdfFile, String password, Set<Integer> pagesToProcess, Boolean silent, Boolean binarize) throws PdfRecompressionException {
        if (binarize == null) {
            binarize = false;
        }
        // checking arguments and setting appropriate variables
        if (pdfFile == null) {
            throw new IllegalArgumentException(pdfFile);
        }

        String prefix = null;

        InputStream inputStream = null;
        if (password != null) {
            try {
                ByteArrayOutputStream decryptedOutputStream = null;
                PdfReader reader = new PdfReader(pdfFile, password.getBytes());
                PdfStamper stamper = new PdfStamper(reader, decryptedOutputStream);
                stamper.close();
                inputStream = new ByteArrayInputStream(decryptedOutputStream.toByteArray());
            } catch (DocumentException ex) {
                throw new PdfRecompressionException(ex);
            } catch (IOException ex) {
                throw new PdfRecompressionException("Reading file caused exception", ex);
            }
        } else {
            try {
                inputStream = new FileInputStream(pdfFile);
            } catch (FileNotFoundException ex) {
                throw new PdfRecompressionException("File wasn't found", ex);
            }
        }


        // if prefix is not set then prefix set to name of pdf without .pdf
        // if pdfFile has unconsistent name (without suffix .pdf) and name longer than 4 chars then last for chars are removed
        // and this string set as prefix
        if ((prefix == null) && (pdfFile.length() > 4)) {
            prefix = pdfFile.substring(0, pdfFile.length() - 4);
        }


        PDFParser parser = null;
        PDDocument doc = null;
        try {
            parser = new PDFParser(inputStream);
            parser.parse();
            doc = parser.getPDDocument();


            AccessPermission accessPermissions = doc.getCurrentAccessPermission();

            if (!accessPermissions.canExtractContent()) {
                throw new PdfRecompressionException("Error: You do not have permission to extract images.");
            }

            // going page by page
            List pages = doc.getDocumentCatalog().getAllPages();
            for (int pageNumber = 0; pageNumber < pages.size(); pageNumber++) {
                if ((pagesToProcess != null) && (!pagesToProcess.contains(pageNumber + 1))) {
                    continue;
                }
                PDPage page = (PDPage) pages.get(pageNumber);
                PDResources resources = page.getResources();
                Map xobjs = resources.getXObjects();

                if (xobjs != null) {
                    Iterator xobjIter = xobjs.keySet().iterator();
                    while (xobjIter.hasNext()) {
                        String key = (String) xobjIter.next();
                        PDXObject xobj = (PDXObject) xobjs.get(key);
                        Map images;
                        if (xobj instanceof PDXObjectForm) {
                            PDXObjectForm xform = (PDXObjectForm) xobj;
                            images = xform.getResources().getImages();
                        } else {
                            images = resources.getImages();
                        }

                        // reading images from each page and saving them to file
                        if (images != null) {
                            Iterator imageIter = images.keySet().iterator();
                            while (imageIter.hasNext()) {
                                String imKey = (String) imageIter.next();
                                PDXObjectImage image = (PDXObjectImage) images.get(imKey);

                                PDStream pdStr = new PDStream(image.getCOSStream());
                                List filters = pdStr.getFilters();

                                if (image.getBitsPerComponent() > 1) {
                                    log.info("It is not a bitonal image => skipping");
                                    continue;
                                }

                                // at this moment for preventing bad output (bad coloring) from LZWDecode filter
                                if (filters.contains(COSName.LZW_DECODE.getName())) {
                                    log.info("This is LZWDecoded => skipping");
                                    continue;

                                }

                                // detection of unsupported filters by pdfBox library
                                if (filters.contains("JBIG2Decode")) {
                                    log.info("Allready compressed according to JBIG2 standard => skipping");
                                    continue;
                                }
                                if (filters.contains("JPXDecode")) {
                                    log.info("Unsupported filter JPXDecode => skipping");
                                    continue;
                                }


                                COSObject cosObj = new COSObject(image.getCOSObject());
                                int objectNum = cosObj.getObjectNumber().intValue();
                                int genNum = cosObj.getGenerationNumber().intValue();
                                log.debug(objectNum + " " + genNum + " obj");

                                String name = getUniqueFileName(prefix + imKey, image.getSuffix());
                                log.debug("Writing image:" + name);
                                image.write2file(name);

                                PdfImageInformation pdfImageInfo =
                                        new PdfImageInformation(key, image.getWidth(), image.getHeight(), objectNum, genNum);
                                originalImageInformations.add(pdfImageInfo);
                                log.debug(pdfImageInfo.toString());

                                namesOfImages.add(name + "." + image.getSuffix());
                            }
                        }

                    }
                }

            }
        } catch (IOException ex) {
            throw new PdfRecompressionException("Unable to parse PDF document", ex);
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException ex) {
                    throw new PdfRecompressionException(ex);
                }
            }
        }
    }

    /**
     * get file name that is not used right now
     * @param prefix represents prefix of the name of file
     * @param suffix represents suffix of the name of file
     * @return file name that is not used right now
     */
    public String getUniqueFileName(String prefix, String suffix) {
        String uniqueName = null;
        File f = null;
        while ((f == null) || (f.exists())) {
            uniqueName = prefix + "-" + imageCounter;
            f = new File(uniqueName + "." + suffix);
            imageCounter++;
        }
        return uniqueName;
    }

    /**
     * replace images by they recompressed version according to JBIG2 standard
     * positions and image data given in imagesData
     * @param pdfName represents name of original PDF file
     * @param os represents output stream for writing changed PDF file
     * @param imagesData contains compressed images according to JBIG2 standard and informations about them
     * @throws PdfRecompressionException if version of PDF is lower than 1.4 or was catch DocumentException or IOException
     */
    public void replaceImageUsingIText(String pdfName, OutputStream os, Jbig2ForPdf imagesData) throws PdfRecompressionException {
        if (pdfName == null) {
            throw new NullPointerException("pdfName");
        }

        if (os == null) {
            throw new NullPointerException("os");
        }

        if (imagesData == null) {
            throw new NullPointerException("imagesData is null => nothing to recompress");
        }

        Map<PdfObjId, PdfImage> jbig2Images = imagesData.getMapOfJbig2Images();


        PdfReader pdf;
        PdfStamper stp = null;
        try {
            pdf = new PdfReader(pdfName);
            stp = new PdfStamper(pdf, os);
            PdfWriter writer = stp.getWriter();

            int version;
            if ((version = Integer.parseInt(String.valueOf(pdf.getPdfVersion()))) < 4) {
                writer.setPdfVersion(PdfWriter.PDF_VERSION_1_4);
            }

            Iterator itImages = jbig2Images.values().iterator();
            String key;
            if (itImages.hasNext()) {
                PdfImage myImg = (PdfImage) itImages.next();
                key = myImg.getPdfImageInformation().getKey();
            } else {
                key = "im0";
            }

            for (int pageNum = 1; pageNum <= pdf.getNumberOfPages(); pageNum++) {

                PdfDictionary pg = pdf.getPageN(pageNum);
                PdfDictionary resPg =
                        (PdfDictionary) PdfReader.getPdfObject(pg.get(PdfName.RESOURCES));

                PdfDictionary xobjResPg =
                        (PdfDictionary) PdfReader.getPdfObject(resPg.get(PdfName.XOBJECT));

                PdfObject obj = null;
                if (xobjResPg != null) {
                    for (Iterator it = xobjResPg.getKeys().iterator(); it.hasNext();) {
                        PdfObject pdfObjIndirect = xobjResPg.get((PdfName) it.next());
                        if (pdfObjIndirect.isIndirect()) {
                            PdfDictionary pdfObj2 = (PdfDictionary) PdfReader.getPdfObject(pdfObjIndirect);
                            PdfDictionary xobj2Res = (PdfDictionary) PdfReader.getPdfObject(pdfObj2.get(PdfName.RESOURCES));
                            if (xobj2Res != null) {
                                for (Iterator it2 = xobj2Res.getKeys().iterator(); it2.hasNext();) {
                                    PdfObject resObj = xobj2Res.get((PdfName) it2.next());
                                }
                                PdfDictionary xobj = (PdfDictionary) PdfReader.getPdfObject(xobj2Res.get(PdfName.XOBJECT));
                                if (xobj == null) {
                                    continue;
                                }
                                obj = xobj.get(new PdfName(key));
                            } else {
                                obj = xobjResPg.get(new PdfName(key));
                                if (obj == null) {
                                    obj = pdfObjIndirect;
                                }
                            }
                        }
                    }
                }

                if ((obj != null) && (obj.isIndirect())) {

                    PdfDictionary tg = (PdfDictionary) PdfReader.getPdfObject(obj);
                    if (tg == null) {
                        continue;
                    }
                    PdfName type =
                            (PdfName) PdfReader.getPdfObject(tg.get(PdfName.SUBTYPE));
                    if (PdfName.IMAGE.equals(type)) {
                        PRIndirectReference ref = (PRIndirectReference) obj;
                        PdfObjId imId = new PdfObjId(ref.getNumber(), ref.getGeneration());
                        PdfImage jbImage = jbig2Images.get(imId);
                        if (jbImage == null) {
                            continue;
                        }
                        PdfImageInformation jbImageInfo = jbImage.getPdfImageInformation();
                        Image img = Image.getInstance(jbImageInfo.getWidth(), jbImageInfo.getHeight(), jbImage.getImageData(), imagesData.getGlobalData());

                        PdfReader.killIndirect(obj);
                        Image maskImage = img.getImageMask();

                        if (maskImage != null) {
                            writer.addDirectImageSimple(maskImage);
                        }
                        writer.addDirectImageSimple(img, (PRIndirectReference) obj);
                    }
                }
            }
            stp.close();
        } catch (IOException ioEx) {
            throw new PdfRecompressionException(ioEx);
        } catch (DocumentException dEx) {
            throw new PdfRecompressionException(dEx);
        } finally {
            Tools.deleteFilesFromList(imagesData.getJbFiles().toArray(new File[0]));
        }
    }
}
