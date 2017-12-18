package com.projetinfo.piim;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_shape;
import org.bytedeco.javacpp.opencv_xfeatures2d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class MainActivity extends AppCompatActivity {
    final int PHOTO_LIB_REQUEST=1;
    final int IMAGE_CAPTURE_REQUEST = 2;
    int nFeatures = 0;
    int nOctaveLayers = 3;
    double contrastThreshold = 0.04;
    int edgeThreshold = 10;
    double sigma = 1.6;
    opencv_xfeatures2d.SIFT sift;

    private Button buttonCapturer;
    private Button buttonGalerie;
    private Button buttonAnalyser;
    private ImageView ImageView;
    private String ImagePath;

    public static File ToCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);
        Log.w("PATH", "Path :" + file.getPath());
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(Path);
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static float Compare(opencv_core.Mat IMG1, opencv_core.Mat IMG2) {
        int nFeatures = 0;
        int nOctaveLayers = 3;
        double contrastThreshold = 0.04;
        int edgeThreshold = 10;
        double sigma = 1.6;
        Loader.load(opencv_calib3d.class);
        Loader.load(opencv_shape.class);

        opencv_xfeatures2d.SIFT sift;
        sift= opencv_xfeatures2d.SIFT.create(nFeatures,nOctaveLayers,contrastThreshold,edgeThreshold,sigma);

        opencv_core.Mat[] images = new opencv_core.Mat[2];
        images[0] = IMG1;
        images[1] = IMG2;
        opencv_core.KeyPointVector[] keyPoints = new opencv_core.KeyPointVector[2];
        keyPoints[0] = new opencv_core.KeyPointVector();
        keyPoints[1] = new opencv_core.KeyPointVector();

        opencv_core.Mat[] descriptors = new opencv_core.Mat[2];

        for(int i=0; i<=1; i++) {
            sift.detect(images[i], keyPoints[i],null);
            descriptors[i] = new opencv_core.Mat();
            sift.compute(images[i], keyPoints[i], descriptors[i]);
        }

        opencv_features2d.BFMatcher matcher = new opencv_features2d.BFMatcher();
        opencv_core.DMatchVector matches = new opencv_core.DMatchVector();
        Log.w("test D0", "D0 : " + descriptors[0].empty());
        Log.w("test D1", "D1 : " + descriptors[1].empty());
        matcher.match(descriptors[0], descriptors[1], matches);


        float dist_moy = 0;
        for(int i=0; i< matches.size(); i++) {
            Log.w("Calcul", "Calcul => " + dist_moy + "+" + matches.get(i).distance());
            dist_moy = dist_moy + matches.get(i).distance();
        }
        Log.w("R", "Resultat = " + dist_moy + "/" + matches.size());
        dist_moy = dist_moy / matches.size();
        return dist_moy;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView = findViewById(R.id.ImageViewer);

        buttonGalerie = findViewById(R.id.galerie);
        buttonGalerie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoLibraryActivity();
            }
        });

        buttonCapturer = findViewById(R.id.capturer);
        buttonCapturer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoCaptureActivity();
            }
        });

        buttonAnalyser = findViewById(R.id.Analyser);
        buttonAnalyser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sift= opencv_xfeatures2d.SIFT.create(nFeatures,nOctaveLayers,contrastThreshold,edgeThreshold,sigma);
                opencv_core.Mat image;
                image = imread(ImagePath);
                StartAnalysis(image);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        /*1) requestCde sert à identifier l'origine du résultat pour savoir quel traitement effectuer
        *       - PHOTO_LIB_REQUEST => Code pour l'activité de chargement d'une image depuis le téléphone*/
        if(requestCode == PHOTO_LIB_REQUEST && resultCode == RESULT_OK){
            processPhotoLibraryResult(intent);
        }
        if(requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK){
            processPhotoCaptureResult(intent);
        }
    }

    protected void startPhotoLibraryActivity(){
        Intent photoLibraryIntent;
        photoLibraryIntent = new Intent();
        photoLibraryIntent.setType("image/*");
        photoLibraryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(photoLibraryIntent, "Pick a Picture !"), PHOTO_LIB_REQUEST);
    }

    protected void processPhotoLibraryResult(Intent intent) {
        Uri photoUri = intent.getData();
        ImageView.setImageURI(photoUri);
        ImagePath = photoUri.getPath();
    }

    protected  void  startPhotoCaptureActivity(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        }
    }

    protected void processPhotoCaptureResult(Intent intent){
        Bundle extras = intent.getExtras();
        Bitmap image = (Bitmap) extras.get("data");
        ImageView.setImageBitmap(image);

        Uri photoUri = intent.getData();
        ImagePath = photoUri.getPath();
    }

    protected void StartAnalysis(opencv_core.Mat PickedImage){
        try {
            String[] Path = {"Coca_1.jpg", "jager_1.jpg", "Pepsi_1.jpg"};
            for(String filename : Path) {
                Log.w("Name", "Name = " + filename);
                File f = ToCache(this,  filename,filename);
                Thread.sleep(1000);
                Log.w("PATH", "Path2 :" + f.getPath());
                float result = Compare(PickedImage, imread(f.getPath(), IMREAD_COLOR));
                Log.w("Resultat", "Resultat pour " + filename + " : " + result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
