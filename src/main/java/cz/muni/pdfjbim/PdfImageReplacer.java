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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class for replacing images inside PDF document by their recompressed version
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 */
public class PdfImageReplacer {

    private static final Logger log = LoggerFactory.getLogger(PdfImageReplacer.class);

    /**
     * replace images by they recompressed version according to JBIG2 standard positions and image
     * data given in imagesData
     *
     * @param pdfName represents name of original PDF file
     * @param os represents output stream for writing changed PDF file
     * @param imagesData contains compressed images according to JBIG2 standard and informations
     * about them
     * @throws PdfRecompressionException if version of PDF is lower than 1.4 or was catch
     * DocumentException or IOException
     */
    public void replaceImageUsingIText(String pdfName, OutputStream os, List<Jbig2ForPdf> imagesData)
            throws PdfRecompressionException {

        try {
            replaceImageUsingIText(new FileInputStream(pdfName), os, imagesData);
        } catch (FileNotFoundException ex) {
            log.error("Original PDF not found", ex);
            throw new PdfRecompressionException(ex);
        }
    }

    /**
     * replace images by they recompressed version according to JBIG2 standard positions and image
     * data given in imagesData
     *
     * @param originalPdf represents name of original PDF file
     * @param os represents output stream for writing changed PDF file
     * @param imagesData contains compressed images according to JBIG2 standard and informations
     * about them
     * @throws PdfRecompressionException if version of PDF is lower than 1.4 or was catch
     * DocumentException or IOException
     */
    public void replaceImageUsingIText(InputStream originalPdf, OutputStream os, List<Jbig2ForPdf> imagesDataList) throws PdfRecompressionException {
        if (originalPdf == null) {
            throw new NullPointerException("pdfName");
        }

        if (os == null) {
            throw new NullPointerException("os");
        }

        if (imagesDataList == null) {
            throw new NullPointerException("imagesData is null => nothing to recompress");
        }


        log.info("Replacing old images in PDF with their equivalent encoded according to standard JBIG2");
        PdfReader pdf;
        PdfStamper stp = null;
        try {
            pdf = new PdfReader(originalPdf);
            stp = new PdfStamper(pdf, os);
            PdfWriter writer = stp.getWriter();

            int version;
            if ((version = Integer.parseInt(String.valueOf(pdf.getPdfVersion()))) < 4) {
                log.debug("PDF version of original PDF was {} => changing to PDF version 1.4", pdf.
                        getPdfVersion());
                writer.setPdfVersion(PdfWriter.PDF_VERSION_1_4);
            }

            for (Jbig2ForPdf imagesData : imagesDataList) {

                Map<PdfObjId, PdfImage> jbig2Images = imagesData.getMapOfJbig2Images();

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
                                PdfDictionary xobj2Res = (PdfDictionary) PdfReader.getPdfObject(pdfObj2.
                                        get(PdfName.RESOURCES));
                                if (xobj2Res != null) {
                                    for (Iterator it2 = xobj2Res.getKeys().iterator(); it2.hasNext();) {
                                        PdfObject resObj = xobj2Res.get((PdfName) it2.next());
                                    }
                                    PdfDictionary xobj = (PdfDictionary) PdfReader.getPdfObject(xobj2Res.
                                            get(PdfName.XOBJECT));
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

                            log.debug("Replacing image {}", jbImage);
                            PdfImageInformation jbImageInfo = jbImage.getPdfImageInformation();
                            Image img = Image.getInstance(jbImageInfo.getWidth(), jbImageInfo.
                                    getHeight(), jbImage.getImageData(), imagesData.getGlobalData());

                            PdfReader.killIndirect(obj);
                            Image maskImage = img.getImageMask();

                            if (maskImage != null) {
                                writer.addDirectImageSimple(maskImage);
                            }
                            writer.addDirectImageSimple(img, (PRIndirectReference) obj);
                        }
                    }
                }
            }
        } catch (IOException ioEx) {
            throw new PdfRecompressionException(ioEx);
        } catch (DocumentException dEx) {
            throw new PdfRecompressionException(dEx);
        } finally {
            log.debug("Deleting temporary files created during process of PDF recompression");
            for (Jbig2ForPdf imagesData : imagesDataList) {
                Tools.deleteFilesFromList(imagesData.getJbFiles().toArray(new File[0]));
            }
            try {
                if (stp != null) {
                    stp.close();
                }
            } catch (DocumentException ex) {
                log.error("Exception thrown while closing stream", ex);
            } catch (IOException ex) {
                log.error("Exception thrown while closing stream", ex);
            }
        }

    }
}
