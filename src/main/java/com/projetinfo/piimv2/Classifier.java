package com.projetinfo.piimv2;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_xfeatures2d;

import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_xfeatures2d.*;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by fuji on 05/02/18.
 */



public class Classifier {
    //Sift parameters
    SIFT sift;
    final static int nFeatures = 0;
    final static int nOctaveLayers = 3;
    final static double contrastThreshold = 0.04;
    final static int edgeThreshold = 10;
    final static double sigma = 1.6;

    //Image de l'ImageView
    Mat Img;

    //Données du JSON
    ArrayList<Brand> brands;

    //Données du Vocabulary


    public Classifier(Mat img, ArrayList<Brand> brands) {
        Img = img;
        this.brands = brands;
    }


}
