package com.example.android_camera_client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.UUID;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.util.IOUtils;

public class UploadHTTP extends AsyncTask<UploadParams, Void, String> {
    private String TAG = "UploadHTTP";
    private FileCredentials credentials;
    private UploadParams params;

    UploadHTTP(FileCredentials _credentials) {
        super();
        credentials = _credentials;
    }

    @Override
    protected String doInBackground(UploadParams... _params) {
        try{
            params = _params[0];
            HttpURLConnection connection;
            URL dest = new URL(credentials.uploadURL);
            String boundary = "---------------"+ UUID.randomUUID().toString();
            connection = (HttpURLConnection) dest.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
            multipart.setBoundary(boundary);
            multipart.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            OutputStream request = connection.getOutputStream();
            File imageFile = new File(params.pathToFile);
            InputStream in = new FileInputStream(imageFile);
            multipart.addBinaryBody("file", in, ContentType.DEFAULT_BINARY, imageFile.getName());
            multipart.build().writeTo(request);

            request.flush();
            request.close();

            String result = null;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream response = connection.getInputStream();
                result = IOUtils.toString(response);
                Log.d(TAG, "Key: " + result);
            }
            connection.disconnect();

            return result;
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String key) {
        if (key != null) {
            params.listener.sendSuccess(params.captureId, key);
        }
    }

}
