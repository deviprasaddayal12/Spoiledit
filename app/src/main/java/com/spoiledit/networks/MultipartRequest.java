package com.spoiledit.networks;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.spoiledit.constants.Type;
import com.spoiledit.utils.LogUtils;
import com.spoiledit.utils.NetworkUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;


public class MultipartRequest extends StringRequest {
    public static final String TAG = MultipartRequest.class.getCanonicalName();

    private static final String FILE_BODY_KEY = "files[]";

    private Map<String, String> params;
    private List<File> files;
    private Response.Listener<String> stringListener;
    private Response.ErrorListener errorListener;

    private HttpEntity httpEntity;

    public MultipartRequest(String serverUrl, Map<String, String> params, List<File> files,
                            Response.Listener<String> stringListener, Response.ErrorListener errorListener) {
        super(Method.POST, serverUrl, stringListener, errorListener);

        this.params = params;
        this.files = files;
        this.stringListener = stringListener;
        this.errorListener = errorListener;

        httpEntity = buildEntity();
    }

    private HttpEntity buildEntity() {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        if (files != null && files.size() > 0) {
            for (int i = 0; i < files.size(); i++) {
                FileBody fileBody = new FileBody(files.get(i));
                builder.addPart(FILE_BODY_KEY, fileBody);
            }
        }

        for (String key : params.keySet()) {
            builder.addTextBody(key, params.get(key));
        }

        return builder.build();
    }

    @Override
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        return super.setRetryPolicy(NetworkUtils.RETRY_POLICY);
    }

    @Override
    public Request<?> setTag(Object tag) {
        return super.setTag(TAG);
    }

    @Override
    public String getBodyContentType() {
        return httpEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            httpEntity.writeTo(bos);
            return bos.toByteArray();
        } catch (IOException | OutOfMemoryError e) {
            VolleyLog.e("" + e);
            return null;
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success(new String(response.data, StandardCharsets.UTF_8), getCacheEntry());
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return super.parseNetworkError(volleyError);
    }

    @Override
    protected void deliverResponse(String response) {
        LogUtils.logResponse(TAG, response);
        stringListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        LogUtils.logError(TAG, error);
        errorListener.onErrorResponse(error);
    }
}