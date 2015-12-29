/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.pdfjbim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author radim
 */
public class ToolsTest {

    File testDir = new File("testData");

    @Before
    public void setUp() throws IOException {
//        if (testDir.exists()) {
//            File fileOne = new File(testDir, "test1Delete");
//            File fileTwo = new File(testDir, "test2Delete");
//            File fileThree = new File(testDir, "test3Delete");
//            fileOne.delete();
//            fileTwo.delete();
//            fileThree.delete();
//        } else {
//            testDir.createNewFile();
//        }

    }

    @After
    public void tearDown() {
        
    }

    /**
     * Test of runJbig2enc method, of class Tools.
     * @throws Exception 
     */
    @Test
    public void testRunJbig2enc() throws Exception {
//        fail("Test not done");
    }

    /**
     * Test of deleteFilesFromList method, of class Tools.
     */
   // @Test
    public void testDeleteFilesFromList() {
        String first = testDir + "/test1Delete";
        String two = testDir + "/test2Delete";
        String three = testDir + "/test3Delete";
        List<String> filesToDelete = new ArrayList<String>();
        filesToDelete.add(first);
        filesToDelete.add(two);
        filesToDelete.add(three);

        for (String fileName : filesToDelete) {
            File file = new File(fileName);
            try {
                file.createNewFile();
            } catch (IOException ex) {
                fail("unable to create new file => unable to preform this test");
            }
        }
        filesToDelete.remove(1);

        Tools.deleteFilesFromList(filesToDelete);
        assertFalse(new File(first).exists());
        assertFalse(new File(three).exists());
        assertTrue(new File(two).exists());
    }

}