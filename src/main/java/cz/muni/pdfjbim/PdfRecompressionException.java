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
 * Thrown when found problem with recompressing PDF file
 *
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class PdfRecompressionException extends Exception {
    private static final long serialVersionUID = 1L;

    public PdfRecompressionException(String message) {
        super(message);
    }

    public PdfRecompressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PdfRecompressionException(Throwable cause) {
        super(cause);
    }


}
