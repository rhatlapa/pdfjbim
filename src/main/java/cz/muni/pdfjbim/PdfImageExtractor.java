/*
 *  Copyright 2011 Radim Hatlapatka.
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

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import cz.muni.pdfjbim.pdf.MyImageRenderListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * class allowing extraction of images from a PDF document
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class PdfImageExtractor {

    private int imageCounter = 1;
    private List<String> namesOfImages = new ArrayList<String>();
    private List<PdfImageInformation> originalImageInformations = new ArrayList<PdfImageInformation>();
    private static final Logger log = LoggerFactory.getLogger(PdfImageExtractor.class);
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

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
     * @param is input stream containing input PDF file
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
        String prefix = PdfImageExtractor.class.getName();
        extractImagesUsingPdfParser(is, prefix, password, pagesToProcess, binarize);
    }
    
    /**
     * Parses a PDF and extracts all the images.
     * @param filename 
     * @throws IOException
     * @throws DocumentException  
     */
    public static void extractImages(String filename) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(filename);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        MyImageRenderListener listener = new MyImageRenderListener("Img%s.%s");
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            parser.processContent(i, listener);
        }
    }

    /**
     * Extracts JBIG2Images from Input stream even if they are stored together with global dictionary in separate PDF object
     * doesn't work yet, its in development stage
     * @param is
     * @throws PdfRecompressionException 
     * @deprecated 
     */
    public void extractJbig2Images(InputStream is) throws PdfRecompressionException {
        if (is == null) {
            throw new IllegalArgumentException("InputStream not given");
        }


        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(is);

            for (int i = 0; i <= pdfReader.getNumberOfPages(); i++) {
                PdfDictionary d = pdfReader.getPageN(i);
                PdfIndirectReference ir = d.getAsIndirectObject(PdfName.CONTENTS);
                PdfObject o = pdfReader.getPdfObject(ir.getNumber());
                PdfStream stream = (PdfStream) o;
                PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
                if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                    byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);
                    OutputStream out = new FileOutputStream(new File("pdfRecompressor", String.format("%1$05d", i) + ".jpg"));
                    out.write(img);
                    out.flush();
                    out.close();
                }

            }


        } catch (IOException ex) {
            log.error("IOException caught while trying to extract jbig2 images from PDF", ex);
            throw new PdfRecompressionException("IOException caught while trying to extract jbig2 images from PDF", ex);
        } finally {
            if (pdfReader != null) {
                pdfReader.close();
            }
        }

    }

    private List<Image> GetImagesFromPdfDict(PdfDictionary dict, PdfReader doc) throws IOException {
        List<Image> images = new ArrayList<Image>();
        PdfDictionary res = (PdfDictionary) (PdfReader.getPdfObject(dict.get(PdfName.RESOURCES)));
        PdfDictionary xobj = (PdfDictionary) (PdfReader.getPdfObject(res.get(PdfName.XOBJECT)));

        if (xobj != null) {
            for (PdfName name : xobj.getKeys()) {
                PdfObject obj = xobj.get(name);
                if (obj.isIndirect()) {
                    PdfDictionary tg = (PdfDictionary) (PdfReader.getPdfObject(obj));
                    PdfName subtype = (PdfName) (PdfReader.getPdfObject(tg.get(PdfName.SUBTYPE)));
                    if (PdfName.IMAGE.equals(subtype)) {
                        int xrefIdx = ((PRIndirectReference) obj).getNumber();
                        PdfObject pdfObj = doc.getPdfObject(xrefIdx);
                        PdfStream str = (PdfStream) (pdfObj);
                        byte[] bytes = PdfReader.getStreamBytesRaw((PRStream) str);

                        String filter = tg.get(PdfName.FILTER).toString();
                        String width = tg.get(PdfName.WIDTH).toString();
                        String height = tg.get(PdfName.HEIGHT).toString();
                        String bpp = tg.get(PdfName.BITSPERCOMPONENT).toString();

                        if ("/FlateDecode".equals(filter)) {
                            bytes = PdfReader.FlateDecode(bytes, true);
                            try {
                                images.add(Image.getInstance(bytes));
                            } catch (BadElementException ex) {
                                log.warn("problem to process FlatDecoded Image", ex);
                            }
                        } else if (PdfName.FORM.equals(subtype) || PdfName.GROUP.equals(subtype)) {
                            images.addAll(GetImagesFromPdfDict(tg, doc));
                        }
                    }
                }
            }
        }
        return images;
    }

    /**
     * This method extracts images by going through all COSObjects pointed from xref table
     * @param is input stream containing PDF file
     * @param prefix output basename for images
     * @param password password for access to PDF if needed
     * @param pagesToProcess list of pages which should be processed if null given => processed all pages
     *      -- not working yet
     * @param binarize -- enables processing of nonbitonal images as well (LZW is still not
     *      processed because of output with inverted colors)
     * @throws PdfRecompressionException if problem to extract images from PDF
     */
    public void extractImagesUsingPdfParser(InputStream is, String prefix, String password, Set<Integer> pagesToProcess,
            Boolean binarize) throws PdfRecompressionException {
        // checking arguments and setting appropriate variables
        if (binarize == null) {
            binarize = false;
        }

        log.debug("Extracting images (binarize set to {})", binarize);

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

                        log.debug("Detected image with color depth: {} bits", image.getBitsPerComponent());
                        if (filters == null) {
                            continue;
                        }
                        log.debug("Detected filters: {}", filters.toString());


                        if ((image.getBitsPerComponent() > 1) && (!binarize)) {
                            log.info("It is not a bitonal image => skipping");
                            continue;
                        }

                        // at this moment for preventing bad output (bad coloring) from LZWDecode filter
                        if (filters.contains(COSName.LZW_DECODE.getName())) {
                            log.info("This is LZWDecoded => skipping");

                            continue;

                        }

                        if (filters.contains(COSName.FLATE_DECODE.getName())) {
                            log.debug("FlateDecoded image detected");
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
                        log.info("Writing image: {}", name);
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
            Tools.deleteFilesFromList(namesOfImages);
            throw new PdfRecompressionException("Unable to parse PDF document", ex);
        } catch (Exception ex) {
            Tools.deleteFilesFromList(namesOfImages);
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
     * @param prefix 
     * @param password password for access to PDF if needed
     * @param pagesToProcess list of pages which should be processed if null given => processed all pages
     *      -- not working yet
    //    * @param silent -- if true error messages are not written to output otherwise they are
     * @param binarize -- enables processing of nonbitonal images as well (LZW is still not
     *      processed because of output with inverted colors)
     * @throws PdfRecompressionException if problem to extract images from PDF
     */
    public void extractImagesUsingPdfObjectAccess(String pdfFile, String prefix, String password,
            Set<Integer> pagesToProcess, Boolean binarize) throws PdfRecompressionException {
        if (binarize == null) {
            binarize = false;
        }
        // checking arguments and setting appropriate variables
        if (pdfFile == null) {
            throw new IllegalArgumentException(pdfFile);
        }


        InputStream inputStream = null;
        if (password != null) {
            try {
                log.debug("PDF probably encrypted, trying to decrypt using given password {}", password);
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

                                if (image.getBitsPerComponent() > 1 && !binarize) {
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
            Tools.deleteFilesFromList(namesOfImages);
            throw new PdfRecompressionException("Unable to parse PDF document", ex);
        } catch (Exception ex) {
            Tools.deleteFilesFromList(namesOfImages);
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
}
