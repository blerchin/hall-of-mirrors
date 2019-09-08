package com.example.android_camera_client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadS3 extends AsyncTask<String, Void, Uri> {
  public String TAG = "DownloadS3";
  S3Credentials credentials;
  ShowManager sm;

  public DownloadS3(ShowManager _sm) {
    sm = _sm;
    credentials = new S3Credentials();
    credentials.getCredentials();
  }

  @Override
  protected Uri doInBackground (String... s3Key) {
    if (credentials == null) {
      Log.d(TAG, "Cannot upload to S3. Credentials not found.");
      return null;
    }
    String key = s3Key[0];
    Log.d(TAG, key);
    AmazonS3Client client = new AmazonS3Client(credentials.awsCredentials);
    client.setRegion(Region.getRegion(credentials.region));
    GetObjectRequest request = new GetObjectRequest(credentials.bucketName, key);
    S3Object object = client.getObject(request);
    File file = sm.getTempFile();
    try {
      FileOutputStream os = new FileOutputStream(file);
      IOUtils.copy(object.getObjectContent(), os);

      return Uri.fromFile(file);
    } catch (IOException e) {
      Log.e(TAG, "Failed writing download to disk.");
      e.printStackTrace();
      return Uri.EMPTY;
    }
  }

  @Override
  protected void onPostExecute(Uri uri) {
    sm.onDownloadComplete(uri);
  }
}
