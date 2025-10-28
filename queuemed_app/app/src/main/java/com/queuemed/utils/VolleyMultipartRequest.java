package com.queuemed.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> listener;
    private final Response.ErrorListener errorListener;

    private Map<String, String> params = new HashMap<>();
    private Map<String, DataPart> byteData = new HashMap<>();

    private static final String boundary = "apiclient-" + System.currentTimeMillis();

    public VolleyMultipartRequest(
            int method,
            String url,
            Response.Listener<NetworkResponse> listener,
            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
    }

    @Override
    protected Map<String, String> getParams() {
        return params != null ? params : new HashMap<>();
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    bos.write(("--" + boundary + "\r\n").getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes());
                    bos.write((entry.getValue() + "\r\n").getBytes());
                }
            }

            if (byteData != null) {
                for (Map.Entry<String, DataPart> entry : byteData.entrySet()) {
                    DataPart dataPart = entry.getValue();
                    bos.write(("--" + boundary + "\r\n").getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" +
                            dataPart.getFileName() + "\"\r\n").getBytes());
                    bos.write(("Content-Type: " + dataPart.getType() + "\r\n\r\n").getBytes());
                    bos.write(dataPart.getContent());
                    bos.write("\r\n".getBytes());
                }
            }

            bos.write(("--" + boundary + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    /** Added safely overridable method **/
    protected Map<String, DataPart> getByteData() {
        return byteData != null ? byteData : new HashMap<>();
    }

    public void setByteData(Map<String, DataPart> byteData) {
        this.byteData = byteData;
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}
