package com.projetinfo.piimv2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;


public class MainActivity extends AppCompatActivity {
    //User interaction
    final int PHOTO_LIB_REQUEST=1;
    final int IMAGE_CAPTURE_REQUEST = 2;

    //Volley parameters
    String URL = "http://www-rech.telecom-lille.fr/freeorb/";

    //Data return from Volley Client (Controller)
    ArrayList<Brand> Brands = new ArrayList<>();


    //Gui objects
    private Button buttonCapturer;
    private Button buttonGalerie;
    private Button buttonAnalyser;
    private ImageView ImageView;
    private String ImagePath;

    //boolean for testing process
    static boolean success;
    static boolean remote_resources_available;

    //image to analyse and compare
    File image = null;
    Brand matchedResult= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Loader.load(opencv_core.class);

        if(!getCacheDir().exists()){
            getCacheDir().mkdirs();
            Log.w("CacheDir", "Creation du cache");
        }

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

        remote_resources_available = false;
        buttonAnalyser = findViewById(R.id.Analyser);
        buttonAnalyser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    getFilesFromVolley();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    protected void onDestroy(){
        super.onDestroy();
        success = false;
        remote_resources_available = false;
        try {
            File dir = getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean cache_success = deleteDir(new File(dir, children[i]));
                if (!cache_success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == PHOTO_LIB_REQUEST && resultCode == RESULT_OK){
            try {
                processPhotoLibraryResult(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    protected void processPhotoLibraryResult(Intent intent) throws IOException {
        Uri photoUri = intent.getData();
        ImageView.setImageURI(photoUri);
        ImagePath = photoUri.getPath();
        this.image = File.createTempFile("TempImage", ".jpg",getCacheDir());
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

    protected void getFilesFromVolley() throws JSONException{

        VolleyClient.getJSON(URL + "index.json", new ServerCallback() {
            @Override
            public void OnSuccess(JSONObject JsonResponse) throws Exception {
                buttonAnalyser.setEnabled(false);
                buttonAnalyser.setText(R.string.analyseEnCours);
                if(remote_resources_available == false) {
                    JSONArray JSONArr = JsonResponse.getJSONArray("brands");
                    Log.w("JSON DATA", JSONArr.length() + "");
                    for (int i = 0; i < JSONArr.length(); i++) {
                        String brandname = JSONArr.getJSONObject(i).getString("brandname");
                        Log.w("Brand", brandname);
                        String url = JSONArr.getJSONObject(i).getString("url");
                        Log.w("Brand", url);
                        String classifier = JSONArr.getJSONObject(i).getString("classifier");
                        Log.w("Brand", classifier);
                        VolleyClient.getFile(URL + "classifiers/", classifier);
                        Brands.add(new Brand(brandname, url, new File(getCacheDir(), classifier)));
                    }

                    VolleyClient.getFile(URL, "vocabulary.yml");
                    remote_resources_available = true;
                }
                String ImagePath = BitmapToFile(((BitmapDrawable) ImageView.getDrawable()).getBitmap()).getAbsolutePath();

                Classifier classifier = new Classifier(ImagePath, Brands);

                for (String file : new File(getCacheDir().getAbsolutePath()).list()){
                    Log.w("CACHE files", file);
                }
                File vocab = new File(getCacheDir(), "vocabulary.yml");

                if(vocab.exists()) {
                    matchedResult = classifier.ProceedtoComparaison(vocab, Brands);
                    Toast.makeText(getBaseContext(), "Il s'agit de la marque : " + matchedResult.getBrandName(), Toast.LENGTH_LONG).show();

                }

                buttonAnalyser.setEnabled(true);
                buttonAnalyser.setText(R.string.analyser);
            }

            @Override
            public void OnError(VolleyError error) {
                Toast.makeText(getBaseContext(), "Impossible de contacter le serveur distant. Veuillez vérifier vos paramètres de connexion", Toast.LENGTH_LONG).show();
                remote_resources_available = false;
            }
        });
    }

    File BitmapToFile(Bitmap bitmap) throws IOException {

        //create a file to write bitmap data
        this.image = new File(this.getCacheDir(), "imagefile");
        //this.image.createNewFile();

        //Convert bitmap to byte array
        Bitmap bmap = bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(this.image);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return this.image;
    }
}
