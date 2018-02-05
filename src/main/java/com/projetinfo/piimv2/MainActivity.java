package com.projetinfo.piimv2;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    //User interaction
    final int PHOTO_LIB_REQUEST=1;
    final int IMAGE_CAPTURE_REQUEST = 2;

    //Volley parameters
    String URL = "http://http://www-rech.telecom-lille.fr/nonfreesift/";

    //JavaCV parameters
    int nFeatures = 0;
    int nOctaveLayers = 3;
    double contrastThreshold = 0.04;
    int edgeThreshold = 10;
    double sigma = 1.6;

    //Gui objects
    private Button buttonCapturer;
    private Button buttonGalerie;
    private Button buttonAnalyser;
    private ImageView ImageView;
    private String ImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final VolleyClient volleyClient = new VolleyClient(this);

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
                volleyClient.getJSON("http://www-rech.telecom-lille.fr/nonfreesift/");
                volleyClient.getFile("http://www-rech.telecom-lille.fr/nonfreesift/", "vocabulary.yml");
                volleyClient.getFile("http://www-rech.telecom-lille.fr/nonfreesift/classifiers/", "Pepsi.xml");

                File f = v.getContext().getCacheDir();
                for (File file: f.listFiles()) {
                    Log.w("File in Cache :", file.getName().toString());
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
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

}
