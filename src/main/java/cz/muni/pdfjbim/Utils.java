/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pdfjbim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author rhatlapa
 */
public class Utils {
    public static Map<String,List<String> > splitListOfStrings(List<String> listToSplit, int factor, String basename) {
        if (listToSplit == null) {
            return null;
        }
        Map <String,List<String> > splittedList = new TreeMap<String,List<String>>();
        int start = 0;
        int basenameSuffix = 0;
        while (listToSplit.size() >= start+factor) {
            splittedList.put(basename + basenameSuffix,listToSplit.subList(start, start+factor));
            basenameSuffix++;
            start += factor;
        }
        if (start < listToSplit.size()) {
            splittedList.put(basename + basenameSuffix, listToSplit.subList(start, listToSplit.size()));
        }
        
        return splittedList;
    }
    
    public static Map<String,List<PdfImageInformation> > splitListOfPdfImageInfo(List<PdfImageInformation> listToSplit, int factor, String basename) {
        if (listToSplit == null) {
            return null;
        }
        Map <String,List<PdfImageInformation> > splittedList = new TreeMap<String,List<PdfImageInformation>>();
        int start = 0;
        int basenameSuffix = 0;
        while (listToSplit.size() >= start+factor) {
            splittedList.put(basename + basenameSuffix,listToSplit.subList(start, start+factor));
            basenameSuffix++;
            start += factor;
        }
        if (start < listToSplit.size()) {
            splittedList.put(basename + basenameSuffix, listToSplit.subList(start, listToSplit.size()));
        }
        
        return splittedList;
    }
}
