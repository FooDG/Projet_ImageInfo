package com.projetinfo.piimv2;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.File;
import java.util.Map;

/**
 * Created by fuji on 25/01/18.
 */

public class FileRequest extends Request<byte[]>{
    private final Response.Listener<byte[]> mListener;
    private Map<String, String> params;
    public Map<String, String> responseHeaders;

    public FileRequest(int method, String url, Response.Listener<byte[]> mListener, Response.ErrorListener listener, Map<String, String> params) {
        super(method, url, listener);
        setShouldCache(false);
        this.mListener = mListener;
        this.params = params;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        responseHeaders = response.headers;
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }
}
