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
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    //User interaction
    final int PHOTO_LIB_REQUEST=1;
    final int IMAGE_CAPTURE_REQUEST = 2;

    //Volley parameters
    String URL = "http://http://www-rech.telecom-lille.fr/nonfreesift/";

    //Data return from Volley Client (Controller)

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

    //boolean for testing process
    static boolean success;

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
                success = true;
                try {
                    getFilesFromVolley();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Si la recuperation des données distantes est reussie
                if (success) {

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

    protected void getFilesFromVolley() throws JSONException {
        VolleyClient.getJSON("http://www-rech.telecom-lille.fr/nonfreesift/index.json", new ServerCallback() {
            @Override
            public void OnSuccess(JSONObject JsonResponse) throws JSONException {
                JSONArray JSONArr = JsonResponse.getJSONArray("brands");
                for (int i=0; i<JSONArr.length(); i++){
                    String filename = JSONArr.getJSONObject(i).getString("classifier");
                    VolleyClient.getFile("http://www-rech.telecom-lille.fr/nonfreesift/classifiers/", filename);
                    Log.w("File in Cache :", filename);

                    VolleyClient.getFile("http://www-rech.telecom-lille.fr/nonfreesift/", "vocabulary.yml");
                }
            }

            @Override
            public void OnError(VolleyError error) {
                Toast.makeText(getBaseContext(), "Can't get the information from server. Please check your internet connexion and retry.", Toast.LENGTH_LONG).show();
                success = false;
            }
        });

    }

    
}
