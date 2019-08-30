package com.example.time_lapse_camera;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;


public class UploadS3 extends AsyncTask<URI, Void, Long> {
    public String TAG = "UploadS3";

    AWSCredentials credentials = null;
    String bucketName = null;
    String region = null;

    public UploadS3() {
        getCredentials();
    }

    public void getCredentials() {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File credFile = new File(dir, "s3credentials.json");
            if (!credFile.exists()) {
                (new FileOutputStream(credFile)).write("put credentials here!".getBytes());
                throw new IOException("Credentials file not found.");
            }
            FileInputStream is = new FileInputStream(credFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            String json = new String(data, "UTF-8");
            JSONObject creds = (JSONObject) new JSONTokener(json).nextValue();
            bucketName = creds.getString("AWS_S3_BUCKET");
            region = creds.getString("AWS_S3_REGION");
            credentials = new BasicAWSCredentials(
                    creds.getString("AWS_S3_KEY"),
                    creds.getString("AWS_S3_SECRET")
            );
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected Long doInBackground(URI... uris) {
        if (credentials == null) {
            Log.d(TAG, "Cannot upload to S3. Credentials not found.");
            return Long.valueOf(0);
        }
        File file = new File(uris[0]);
        String key = file.getName();
        Log.d(TAG, key);
        AmazonS3Client client = new AmazonS3Client(credentials);
        client.setRegion(Region.getRegion(region));
        PutObjectResult result = client.putObject(bucketName, key, file);
        return Long.valueOf(1);
    }
}
