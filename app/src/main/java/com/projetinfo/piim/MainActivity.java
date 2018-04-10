package com.projetinfo.piim;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.soundcloud.android.crop.Crop;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_shape;
import org.bytedeco.javacpp.opencv_xfeatures2d;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

public class MainActivity extends AppCompatActivity {

    public AssetManager assetManager;

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

    //boolean for testing process
    static boolean success;
    static boolean remote_resources_available;

    //image to analyse and compare
    File image = null;

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
        matcher.match(descriptors[0], descriptors[1], matches);

        float dist_moy = 0;
        for(int i=0; i< matches.size(); i++) {
            dist_moy = dist_moy + matches.get(i).distance();
        }

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

                Bitmap IVPicture = ((BitmapDrawable)ImageView.getDrawable()).getBitmap();
                opencv_core.Mat image;

                try {
                    image = imread(BitmapToFile(IVPicture).getAbsolutePath());
                    Log.w("CLICK","CLICK !!!");


                    String resultat = StartAnalysis(image);
                    Toast.makeText(buttonAnalyser.getContext(), "" + resultat, Toast.LENGTH_SHORT);



                } catch (IOException e) {
                    Toast.makeText(buttonAnalyser.getContext(), "Oops ! Something went wrong", Toast.LENGTH_SHORT);
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
        if(requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK){
            try {
                Log.w("Capture", "Capture Image");
                processPhotoCaptureResult(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            Log.w("PICK", "PICK Image");
            beginCrop(intent.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            Log.w("Crop", "Crop Image");
            handleCrop(resultCode, intent);
        }
    }

    protected void startPhotoLibraryActivity(){
        Crop.pickImage(this);

    }

    protected  void  startPhotoCaptureActivity(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        }
    }

    protected void processPhotoCaptureResult(Intent intent) throws IOException {
        Bundle extras = intent.getExtras();
        Bitmap image = (Bitmap) extras.get("data");
        File imagefile = BitmapToFile(image);
        Uri imageURI = Uri.fromFile(imagefile);
        beginCrop(imageURI);
    }

    protected String StartAnalysis(opencv_core.Mat PickedImage)   {
        try {
            Log.w("Start", "Analysis Started !");

            buttonAnalyser = (Button) findViewById(R.id.Analyser);
            buttonAnalyser.setText("Analyse en cours...");
            buttonAnalyser.setEnabled(false);

            assetManager = this.getAssets();
            String[] Path = assetManager.list("Pictures");
            Bitmap bmap;
            File f = null;

            ArrayList<Float> tmp_result = new ArrayList();
            ArrayList<String> tmp_filename = new ArrayList();

            for(String filename : Path) {
                Log.w(filename, "Working on " + filename + "...");
                bmap = getBitmapFromAsset(this, "Pictures/" +  filename);
                f = BitmapToFile(bmap);

                tmp_filename.add(filename);
                tmp_result.add(Compare(PickedImage, imread(f.getPath())));
            }

            Log.w("Array", "Array filled !");

            Float smallest = tmp_result.get(0);

            Log.w("Test", "" + tmp_result.toString());
            Log.w("Test", "" + tmp_filename.toString());

            for(Float x : tmp_result ){
                if (x < smallest) {
                    smallest = x;
                }
            }

            int index= 0;
            while(tmp_result.get(index) != smallest) {
                index++;
            }

            Log.w("Final Result", "Final Result: " + smallest);
            Log.w("Final Result", "Final Result: " + tmp_filename.get(index));

            MainActivity.this.buttonAnalyser.setText(R.string.analyser);
            MainActivity.this.buttonAnalyser.setEnabled(true);

            return tmp_filename.get(index);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }


    Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    File BitmapToFile(Bitmap bitmap) throws IOException {

        //create a file to write bitmap data
        this.image = new File(this.getCacheDir(), "imagefile");

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

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            ImageView.setImageURI(null);
            ImageView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

