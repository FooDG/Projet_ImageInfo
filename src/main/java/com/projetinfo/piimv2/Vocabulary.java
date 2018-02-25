package com.projetinfo.piimv2;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;

import java.io.File;

/**
 * Created by fuji on 09/02/18.
 */

public class Vocabulary {
    File YAMLVocabulary;

    public Vocabulary(File YAMLVocabulary){
        this.YAMLVocabulary = YAMLVocabulary;
    }

    public void getHistogram(String pathToYAMLFile){
        
        
        
        //opencv_core.cvReleaseFileStorage(fileStorage);
    }
}
