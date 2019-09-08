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

public class S3Credentials {
  public String TAG = "com.example.time_lapse_camera.S3Credentials";
  public String bucketName;
  public String region;
  public AWSCredentials awsCredentials;

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
      awsCredentials = new BasicAWSCredentials(
          creds.getString("AWS_S3_KEY"),
          creds.getString("AWS_S3_SECRET")
      );
    } catch (JSONException e) {
      Log.d(TAG, e.getMessage());
    } catch (IOException e) {
      Log.d(TAG, e.getMessage());
    }
  }
}