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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Radim Hatlapatka (hata.radim@gmail.com)
 */
public class Tools {

    private static final Logger logger = LoggerFactory.getLogger(Tools.class);

    /**
     * @param filesToDelete list of fileNames to be deleted
     */
    public static void deleteFilesFromList(List<String> filesToDelete) {
        for (int i = 0; i < filesToDelete.size(); i++) {
            File fileToDelete = new File(filesToDelete.get(i));
            if (!fileToDelete.delete()) {
                logger.warn("problem to delete file: " + fileToDelete.getPath());
            }
        }
    }

    /**
     * @param filesToDelete list of fileNames to be deleted
     */
    public static void deleteFilesFromList(File[] filesToDelete) {
        for (int i = 0; i < filesToDelete.length; i++) {
            File fileToDelete = filesToDelete[i];
            if (!fileToDelete.delete()) {
                logger.warn("problem to delete file: " + fileToDelete.getPath());
            }
        }
    }

    /**
     * copy file
     * @param fromFile what file should be copied
     * @param toFile where the file should be copied
     * @throws IOException
     */
    public static void copy(File fromFile, File toFile) throws IOException {
        if (!fromFile.exists()) {
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFile.getName());
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFile.getName());
        }
        if (!fromFile.canRead()) {
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFile.getName());
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination file is unwriteable: " + toFile.getName());
            }
            logger.info("Overwrite existing file " + toFile.getName());
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * copy all files from source directory to destination directory -- used by JUnit tests
     * @param sourceDir source directory
     * @param destinationDir destination directory
     * @throws IOException
     */
    public static void copyDir(File sourceDir, File destinationDir) throws IOException {
        if (sourceDir == null) {
            throw new NullPointerException("sourceDir");
        }

        if (destinationDir == null) {
            throw new NullPointerException("toDir");
        }

        if ((!sourceDir.exists()) || (!destinationDir.exists())) {
            throw new IllegalArgumentException(sourceDir.getPath() + " or "
                    + destinationDir.getPath() + " doesn't exist");
        }

        if (!sourceDir.isDirectory()) {
            throw new IllegalArgumentException(sourceDir.getPath() + " is not a directory");
        }

        if (!destinationDir.isDirectory()) {
            throw new IllegalArgumentException(destinationDir.getPath() + " is not a directory");
        }

        File[] sourceFiles = sourceDir.listFiles();
        for (int i = 0; i < sourceFiles.length; i++) {
            if (!sourceFiles[i].isDirectory()) {
                copy(sourceFiles[i], destinationDir);
            }
        }
    }
}
