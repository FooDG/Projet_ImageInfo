package com.projetinfo.piimv2;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fuji on 07/02/18.
 * Interface de callback permet d'attendre le retour des requetes pour le JSON
 */

public interface ServerCallback{
    //Callbacks for the JSON request
    void OnSuccess(JSONObject JsonResponse) throws Exception;
    void OnError(VolleyError error);
}
