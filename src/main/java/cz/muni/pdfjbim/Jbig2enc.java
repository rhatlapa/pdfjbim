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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class Jbig2enc {

    private static final Logger log = LoggerFactory.getLogger(Jbig2enc.class);
    private String jbig2enc; // path to jbig2enc encoder executable
    private double defaultThresh = 0.85;
    private boolean autoThresh = false;
    private int bwThresh = 188;
    private boolean useOcr = false; // enables OCR usage in jbig2enc
    private String lang = null; // sets language used by OCR engine (without effect if not enable use of OCR)
    private boolean forced = false; // forces ocr usage even for unknown resolution
    private boolean segment = false; // puts images separatelly (jbig2enc option -S)

    public Jbig2enc(String jbig2enc) {
        if (jbig2enc == null) {
            throw new NullPointerException("No path to encoder given!");
        }
        this.jbig2enc = jbig2enc;
    }

    public Jbig2enc() {
        this.jbig2enc = "jbig2";
    }

    public boolean isForcedOcrForUnknownResolution() {
        return forced;
    }

    public void setForcedOcrForUnknownResolution(boolean forced) {
        this.forced = forced;
    }

    public String getLang() {
        return lang;
    }

    /**
     * sets language of input document used by jbig2enc and its OCR API
     *
     * @param lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isAutoThresh() {
        return autoThresh;
    }

    public void setAutoThresh(boolean autoThresh) {
        this.autoThresh = autoThresh;
    }

    public int getBwThresh() {
        return bwThresh;
    }

    public void setBwThresh(int bwThresh) {
        if ((bwThresh < 0) || (bwThresh > 255)) {
            throw new IllegalArgumentException("bwThresh");
        }
        this.bwThresh = bwThresh;
    }

    public double getDefaultThresh() {
        return defaultThresh;
    }

    /**
     * @param defaultThresh sets default thresholding used while running jbig2enc
     */
    public void setDefaultThresh(double defaultThresh) {
        if ((defaultThresh < 0.4) || (defaultThresh > 0.9)) {
            throw new IllegalArgumentException("defaultThresh");
        }
        this.defaultThresh = defaultThresh;
    }

    public String getJbig2enc() {
        return jbig2enc;
    }

    public void setJbig2enc(String jbig2enc) {
        this.jbig2enc = jbig2enc;
    }

    public boolean isUseOcr() {
        return useOcr;
    }

    public void setUseOcr(boolean useOcr) {
        this.useOcr = useOcr;
    }

    /**
     * run jbig2enc with symbol coding used and output in format suitable for PDF
     *
     * @param basename base
     * @param imageList list of images to be compressed
     * @throws PdfRecompressionException if any problem occurs while running jbig2enc
     */
    public void run(List<String> imageList, String basename) throws PdfRecompressionException {
        if (basename == null) {
            basename = "output";
        }

        if (imageList == null) {
            throw new NullPointerException("imageList");
        }

        if (imageList.isEmpty()) {
            log.info("there are no images for running jbig2enc at (given list is empty)");
            return;
        }


        List<String> toRun = new ArrayList<String>();

        toRun.add(jbig2enc);
        toRun.add("-s");
        toRun.add("-p");
        toRun.add("-b");
        toRun.add(basename);
        toRun.add("-t");
        toRun.add(String.valueOf(defaultThresh));
        toRun.add("-T");
        toRun.add(String.valueOf(bwThresh));
        
        if (segment) {
            toRun.add("-S");
        }

        if (autoThresh) {
            toRun.add("--auto-thresh");

            if (useOcr) {
                toRun.add("--use-ocr");
                if (lang != null) {
                    toRun.add("--lang");
                    toRun.add(lang);
                }
            }
        }

        if (forced) {
            toRun.add("-ff");
        }


        toRun.addAll(imageList);

        String[] run = new String[toRun.size()];
        run = toRun.toArray(run);

        Runtime runtime = Runtime.getRuntime();
        Process pr1;
        try {
            log.debug("Executing {}", toRun);
            pr1 = runtime.exec(run);
            InputStream erStream = pr1.getErrorStream();
            int exitValue = pr1.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(erStream));
            String line;
            while ((line = reader.readLine()) != null) {
//                writes only a number of symbols recognised by encoder and number of pages
//                if (line.contains("JBIG2 compression complete")) {
//                    String[] word = line.split(" ");
//                    for (int i = 0; i < word.length; i++) {
//                        if (word[i].contains("symbols:")) {
//                            int differenciator = word[i].indexOf(":");
//                            String symNum = word[i].substring(differenciator + 1);
//                            System.err.print(";" + symNum);
//                        }
//                        if (word[i].contains("pages:")) {
//                            int differenciator = word[i].indexOf(":");
//                            String pageNum = word[i].substring(differenciator + 1);
//                            System.err.print(";" + pageNum);
//                        }
//                    }
//                }


                log.debug(line);
            }
            if (exitValue != 0) {
                log.warn("jbig2enc ended with error " + exitValue);
                Tools.deleteFilesFromList(imageList);
                throw new PdfRecompressionException("jbig2enc ended with error " + exitValue);
            }
        } catch (IOException ex) {
            log.warn("running jbig2enc caused IOException", ex);
        } catch (InterruptedException ex2) {
            log.warn("running jbig2enc was interupted", ex2);
        } finally {
            Tools.deleteFilesFromList(imageList);
        }
    }
}
