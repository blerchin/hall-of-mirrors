package com.example.android_camera_client;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class UploadHTTP extends AsyncTask<URI, Void, Long> {
    private HttpURLConnection connection;
    private String TAG = "BatchToHTTP";

    @Override
    protected Long doInBackground(URI... pathToFile) {

        try{
            URL dest = new URL("http://8cc5924d.ngrok.io/upload");
            connection = (HttpURLConnection) dest.openConnection();
            connection.setRequestMethod("POST");
        } catch(Exception e){
            Log.d(TAG, e.getMessage());
        }

        connection.setDoOutput(true);
        connection.setDoInput(true);

        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Content-Type", "image/jpeg");

        Log.i(TAG, "HTTP client initialized");

        try{
            OutputStream request = connection.getOutputStream();
            File imageFile = new File(pathToFile[0]);
            InputStream in = new FileInputStream(imageFile);
            copy(in, request);
            in.close();

            request.flush();
            request.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, readStream(connection.getInputStream()));
            }
            connection.disconnect();

        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return Long.valueOf(0);
        }

        return Long.valueOf(1);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) { break; }
            out.write(buffer, 0, bytesRead);
        }
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

}
