package com.projetinfo.piimv2;

import android.util.Log;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_xfeatures2d;

import java.io.File;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_ml.SVM.C_SVC;
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
    String ImgPath;

    //Données du JSON
    ArrayList<Brand> brands;

    //Données du Vocabulary
    CvFileStorage cv = new CvFileStorage();

    public Classifier(String ImgPath, ArrayList<Brand> brands) {
        this.ImgPath = ImgPath;
        this.brands = brands;
    }

    public void ProceedtoComparaison(File vocabularyFile, ArrayList<Brand> Brands){
        Loader.load(opencv_core.class);
        if (vocabularyFile.exists()){
            Log.w("exists ?", "il existe !");
        }else
        {
            Log.w("exists ?", "il existe pas !");
        }

        //Creation du vocabulaire
        CvFileStorage fileStorage = cvOpenFileStorage(vocabularyFile.getAbsolutePath(), null, CV_STORAGE_READ);
        Pointer pointer = opencv_core.cvReadByName(fileStorage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(pointer);
        Mat vocabulary = new opencv_core.Mat(cvMat);
        Log.w("VOCAB", "vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(fileStorage);

        final opencv_xfeatures2d.SIFT detector;
        detector = SIFT.create(this.nFeatures, this.nOctaveLayers, this.contrastThreshold, this.edgeThreshold, this.sigma);

        final FlannBasedMatcher matcher;
        matcher = new FlannBasedMatcher();

        final BOWImgDescriptorExtractor bowide;
        bowide = new BOWImgDescriptorExtractor(detector, matcher);
        bowide.setVocabulary(vocabulary);

        //Creation de l'histogram pour l'image à comparer
        Mat descriptorImg;
        KeyPointVector keypoints = new KeyPointVector();

        Mat Img = opencv_imgcodecs.imread(ImgPath, opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        if(Img.empty()){
            Log.w("Empty ?", "OUI ELLE EST VIDE !!! ");
        }

        detector.detect(Img, keypoints);

        Mat HistoImg = new Mat();
        bowide.compute(Img, keypoints,HistoImg, new IntVectorVector(),new Mat());

        if(HistoImg.empty()){
            Log.w("Empty ?", "OUI ELLE EST VIDE !!! ");
        }

        float minF = Float.MAX_VALUE;
        String bestMatch = null;

        final opencv_ml.SVM[] classifiers;
        classifiers = new opencv_ml.SVM[Brands.size()];

        for (int i = 0 ; i < Brands.size() ; i++) {
            classifiers[i] = opencv_ml.SVM.create();
            classifiers[i] = opencv_ml.SVM.load(Brands.get(i).XMLclassifier.getAbsolutePath());
            Log.w("Var count => ", classifiers[i].getVarCount() + "");

            Mat resultMat = new Mat();
            float result = classifiers[i].predict(HistoImg, resultMat, 1 );
            FloatRawIndexer indexer = resultMat.createIndexer();

            if (resultMat.cols() > 0 && resultMat.rows() > 0){
                result = indexer.get(0,0);
            }

            if (result < minF) {
                minF = result;
                bestMatch = Brands.get(i).getBrandName();
            }
        }
        Log.w("|-> ", "best match is " + bestMatch );


    }


}
