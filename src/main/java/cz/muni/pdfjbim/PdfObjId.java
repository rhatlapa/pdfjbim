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
 * Represents PDF object identifier
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class PdfObjId {
    private int objectNumber;
    private int generationNumber;

    public PdfObjId(int objectNumber, int generationNumber) {
        this.objectNumber = objectNumber;
        this.generationNumber = generationNumber;
    }

    public int getGenerationNumber() {
        return generationNumber;
    }

    public void setGenerationNumber(int generationNumber) {
        this.generationNumber = generationNumber;
    }

    public int getObjectNumber() {
        return objectNumber;
    }

    public void setObjectNumber(int objectNumber) {
        this.objectNumber = objectNumber;
    }

    @Override
    public String toString() {
        return objectNumber + " " + generationNumber + " obj";
    }

    public String getAsObjReference() {
        return objectNumber + " " + generationNumber + " R";
    }

    public String getAsObjIdentifier() {
        return this.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof PdfObjId) {
                PdfObjId objId = (PdfObjId) obj;
                return (objId.objectNumber == this.objectNumber) && (objId.generationNumber == this.generationNumber);
            }
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.objectNumber;
        hash = 97 * hash + this.generationNumber;
        return hash;
    }



}
