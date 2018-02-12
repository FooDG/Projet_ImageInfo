package com.projetinfo.piimv2;

import android.util.Log;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_xfeatures2d;

import java.io.File;
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
    CvFileStorage cv = new CvFileStorage();

    public Classifier(Mat img, ArrayList<Brand> brands) {
        Img = img;
        this.brands = brands;
    }

    public void ProceedtoComparaison(String vocabularyFilePath, ArrayList<Brand> Brands){
        FileStorage fileStorage = new opencv_core.FileStorage(vocabularyFilePath, opencv_core.FileStorage.FORMAT_YAML);
        Pointer pointer = new Pointer(fileStorage);
        opencv_core.Mat Vocabulary = new opencv_core.Mat(pointer);

        final opencv_xfeatures2d.SIFT detector;
        detector = SIFT.create(this.nFeatures, this.nOctaveLayers, this.contrastThreshold, this.edgeThreshold, this.sigma);

        final FlannBasedMatcher matcher;
        matcher = new FlannBasedMatcher();

        final BOWImgDescriptorExtractor bowide;
        bowide = new BOWImgDescriptorExtractor(detector, matcher);
        bowide.setVocabulary(Vocabulary);

        final opencv_ml.SVM[] classifiers;
        classifiers = new opencv_ml.SVM[Brands.size()];

        KeyPointVector keypoints = new KeyPointVector();
        Mat inputDescriptors = new Mat();

        for (int i = 0 ; i < Brands.size() ; i++) {
            Mat response_hist = new Mat();
            FileStorage BrandFS = new FileStorage();
            BrandFS.open(Brands.get(i).getClassifier().getAbsolutePath(),CV_STORAGE_READ);
            Pointer BrandPointer = new Pointer(BrandFS);
            classifiers[i] = new opencv_ml.SVM(BrandPointer);
            Mat imgTest = new Mat(BrandPointer);

            detector.detectAndCompute(imgTest, Mat.EMPTY, keypoints, inputDescriptors);
            bowide.compute(imgTest, keypoints,response_hist);

            // Finding best match
            float minf = Float.MAX_VALUE;
            String bestMatch = null;

            long timePrediction = System.currentTimeMillis();
            for (int j = 0; j < Brands.size(); i++) {
                // classifier prediction based on reconstructed histogram
                float res = classifiers[i].predict(response_hist);
                //System.out.println(class_names[i] + " is " + res);
                if (res < minf) {
                    minf = res;
                    bestMatch = Brands.get(j).getBrandName();
                }
            }
            timePrediction = System.currentTimeMillis() - timePrediction;
            Log.w("", "Predicted as " + bestMatch + " in " + timePrediction + " ms");

        }

    }
}
