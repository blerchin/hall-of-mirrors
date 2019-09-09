package com.example.android_camera_client;

import android.os.Environment;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileCredentials {
  public String TAG = "com.example.time_lapse_camera.FileCredentials";
  public String downloadURL;
  public String uploadURL;
  public String wsURL;
  public String method;
  public String bucketName;
  public String region;
  public AWSCredentials awsCredentials;

  public void getCredentials() {
    try {
      File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File credFile = new File(dir, "fileCredentials.json");
      if (!credFile.exists()) {
        (new FileOutputStream(credFile)).write("put credentials here!".getBytes());
        throw new IOException("Credentials file not found.");
      }
      FileInputStream is = new FileInputStream(credFile);
      byte[] data = new byte[is.available()];
      is.read(data);
      String json = new String(data, "UTF-8");
      JSONObject creds = (JSONObject) new JSONTokener(json).nextValue();
      wsURL = creds.getString("WS_URL");
      method = creds.getString("METHOD");
      if (method.equals("s3")) {
        bucketName = creds.getString("AWS_S3_BUCKET");
        region = creds.getString("AWS_S3_REGION");
        awsCredentials = new BasicAWSCredentials(
            creds.getString("AWS_S3_KEY"),
            creds.getString("AWS_S3_SECRET")
        );
      } else if (method.equals("http")) {
        downloadURL = creds.getString("DOWNLOAD_URL");
        uploadURL = creds.getString("UPLOAD_URL");
      } else {
        throw new IOException("method " + method + " not supported.");
      }
    } catch (JSONException e) {
      Log.d(TAG, e.getMessage());
    } catch (IOException e) {
      Log.d(TAG, e.getMessage());
    }
  }
}