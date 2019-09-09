package com.example.android_camera_client;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader extends AsyncTask<String, Void, Uri> {
  public String TAG = "Downloader";
  FileCredentials credentials;
  File localFile;
  ShowManager sm;

  public Downloader(ShowManager _sm) {
    sm = _sm;
    localFile = sm.getTempFile();
    credentials = new FileCredentials();
    credentials.getCredentials();
  }

  @Override
  protected Uri doInBackground (String... _key) {
    String key = _key[0];
    Log.d(TAG, "downloading " + key);
    try {
      if (credentials == null) {
        Log.d(TAG, "Cannot download. Credentials not found.");
        return null;
      } else if (credentials.method == "s3") {
        downloadS3(key);
      } else {
        downloadHTTP(key);
      }
      return Uri.fromFile(localFile);
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

  private void downloadHTTP(String key) throws IOException {
    URL url = new URL(credentials.downloadURL + "/" + key);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    InputStream response = connection.getInputStream();
    OutputStream os = new FileOutputStream(localFile);
    IOUtils.copy(response, os);
  }

  private void downloadS3(String key) throws IOException {
    AmazonS3Client client = new AmazonS3Client(credentials.awsCredentials);
    client.setRegion(Region.getRegion(credentials.region));
    GetObjectRequest request = new GetObjectRequest(credentials.bucketName, key);
    S3Object object = client.getObject(request);
    OutputStream os = new FileOutputStream(localFile);
    IOUtils.copy(object.getObjectContent(), os);
  }
}
