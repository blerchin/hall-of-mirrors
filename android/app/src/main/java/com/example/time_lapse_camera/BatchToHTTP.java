package com.example.time_lapse_camera;

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

public class BatchToHTTP extends AsyncTask< URI, Void, Long> {
    private URL dest;
    private HttpURLConnection connection;
    private String TAG = "BatchToHTTP";
    private String boundary = "*****";
    private String twoHyphens = "--";
    private String crlf = "\r\n";



    @Override
    protected Long doInBackground(URI... pictureData) {


        //if(  == WifiManager.WIFI_STATE_ENABLED)
        try{
            dest = new URL("http://95327140.ngrok.io/upload");
            //HTTP_HOST = new URI("http://192.168.1.100:3000/upload");
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

        //Log.i(TAG, "about to start HTTP");
        try{
            OutputStream request = connection.getOutputStream();
            URI fileURI = pictureData[0];
            File imageFile = new File(fileURI);
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
        } finally{

        }
        //deleteFiles(pictureData);
        //Log.d(TAG,"deleted files");
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

    private void deleteFiles(URI[] pictures){
        for( URI p : pictures) {
            File pictureFile = new File( p);
            pictureFile.delete();
        }
    }


}
