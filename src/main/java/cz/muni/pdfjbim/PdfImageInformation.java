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

/**
 * Contains information about size of image and it's position in original PDF
 *
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class PdfImageInformation {


    private String key;
    private int width;
    private int height;
    private int pageNumber;
    private int objectNum;
    private int objectGenNum;


    /**
     *
     * @param key represents pdf object key to which was associated this image
     * @param width represents width of image
     * @param height represents height of image
     * @param objectNum 
     * @param genNum 
     * @param pageNumber represents page number in original pdf dokument
     */
    public PdfImageInformation(String key, int width, int height, int objectNum, int genNum, int pageNumber) {
        this.key = key;
        this.width = width;
        this.height = height;
        this.objectGenNum = genNum;
        this.objectNum = objectNum;
        this.pageNumber = pageNumber;
    }

    /**
     *
     * @param key represents pdf object key to which was associated this image
     * @param width represents width of image
     * @param height represents height of image
     * @param objectNum
     * @param genNum  
     */
    public PdfImageInformation(String key, int width, int height, int objectNum, int genNum) {
        this.key = key;
        this.width = width;
        this.height = height;
        this.objectGenNum = genNum;
        this.objectNum = objectNum;
    }

     /**
     * @return height of image
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return pdf object key of image
     */
    public String getKey() {
        return key;
    }

    /**
     * @return page number where the image in pdf was at
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return width of image
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return object generic number
     */
    public int getObjectGenNum() {
        return objectGenNum;
    }

    /**
     * sets PDF object generic number
     * @param objectGenNum generic number to be set
     */
    public void setObjectGenNum(int objectGenNum) {
        this.objectGenNum = objectGenNum;
    }

    /**
     * @return object number
     */
    public int getObjectNum() {
        return objectNum;
    }

    /**
     * @param objectNum number to be set as PDF object number
     */
    public void setObjectNum(int objectNum) {
        this.objectNum = objectNum;
    }

    @Override
    public String toString() {
        return "Image " + "{" + objectNum + "," + objectGenNum + "} "
                + "with dimensions = (" + width + "," + height + ")";
    }
}
