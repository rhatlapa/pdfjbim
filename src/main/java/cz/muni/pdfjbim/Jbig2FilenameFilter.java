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
package cz.muni.pdfjbim;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Radim Hatlapatka (208155@mail.muni.cz)
 */
public class Jbig2FilenameFilter implements FilenameFilter {

    private String basename = "output";

    public Jbig2FilenameFilter() {
    }
    
    public Jbig2FilenameFilter(String basename) {
        this.basename = basename;
    }

    public boolean accept(File dir, String name) {
        if (name == null) {
            return false;
        }
        return name.startsWith(basename);
    }

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }
    
    
}
