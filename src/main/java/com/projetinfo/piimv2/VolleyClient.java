package com.projetinfo.piimv2;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class VolleyClient {
    private Context context;
    private static  RequestQueue myQueue;
    private static String Response;
    private static Bitmap BitmapResponse;
    private static boolean hasFailed;
    private static JSONObject jsonResponse;


    private StringRequest stringRequest;
    private JsonObjectRequest JsonObjectRequest;
    private FileRequest fileRequest;

    public VolleyClient(Context context){
        this.context = context;
        this.myQueue = Volley.newRequestQueue(this.context);
    }

    public String getText(String URL){
        this.stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Response = response.toString();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Error Text = ", "Erreur " + error.toString());
                Response = error.toString();
            }
        });
        myQueue.add(stringRequest);
        return Response;
    }

    public String getJSON(String URL) {
        JsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Response = response.toString();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Response = error.toString();
                Log.w("Error :: ", Response);
            }
        });
        myQueue.add(JsonObjectRequest);
        return Response;
    }

    public void getFile(String path, final String filename){
        String URL = path + filename;

        fileRequest = new FileRequest(Request.Method.GET, URL, new Response.Listener<byte[]>(){
            @Override
            public void onResponse(byte[] response) {
                try{
                   FileOutputStream fos;
                   String outputPath = context.getCacheDir().getPath() + "/" + filename;
                   Log.w("Cache path ", outputPath);
                    fos = new FileOutputStream(outputPath);
                    fos.write(response);
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("Error :: ", error.toString());
            }
        }, null);

        myQueue.add(fileRequest);
    }


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

}
