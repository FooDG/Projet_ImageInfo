package com.projetinfo.piimv2;

import android.content.Context;
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


public class VolleyClient {
    private Context context;
    private static  RequestQueue myQueue;
    private static String Response;
    private static Bitmap BitmapResponse;
    private static boolean hasFailed;
    private static JSONObject jsonResponse;


    private StringRequest stringRequest;
    private JsonObjectRequest JsonObjectRequest;

    public VolleyClient(Context context){
        this.context = context;
        this.myQueue = Volley.newRequestQueue(this.context);
    }

    //TODO getText
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

    //TODO getJSON
    public String getJSON(String URL) {
        JsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Response = response.toString();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Response = error.toString();
            }
        });
        myQueue.add(JsonObjectRequest);
        return Response;
    }
}
