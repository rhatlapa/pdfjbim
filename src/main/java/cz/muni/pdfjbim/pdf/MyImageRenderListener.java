/*
 *  Copyright 2012 Radim Hatlapatka.
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
package cz.muni.pdfjbim.pdf;



    import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

/**
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 */
public class MyImageRenderListener implements RenderListener {

    private static final Logger log = LoggerFactory.getLogger(MyImageRenderListener.class);

    /** The new document to which we've added a border rectangle. */
    protected String path = "";

    /**
     * Creates a RenderListener that will look for images.
     * @param path
     */
    public MyImageRenderListener(String path) {
        this.path = path;
    }

    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#beginTextBlock()
     */
    public void beginTextBlock() {
    }

    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#endTextBlock()
     */
    public void endTextBlock() {
    }

    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#renderImage(
     *     com.itextpdf.text.pdf.parser.ImageRenderInfo)
     */
    public void renderImage(ImageRenderInfo renderInfo) {
        try {
            String filename;

            PdfImageObject image = renderInfo.getImage();
            if (image == null) {
                return;
            }
            filename = String.format(path, renderInfo.getRef().getNumber(), image.getFileType());
            try (FileOutputStream os = new FileOutputStream(filename)) {
                os.write(image.getImageAsBytes());
                os.flush();
            } catch (IOException ex) {
                log.warn("IOException occurred when storing image object to file");
            }
        } catch (IOException e) {
            log.warn("IOException occurred when rendering pdf image object", e);
        }
    }

    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#renderText(
     *     com.itextpdf.text.pdf.parser.TextRenderInfo)
     */
    public void renderText(TextRenderInfo renderInfo) {
    }
}

