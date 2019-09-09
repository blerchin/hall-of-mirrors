package com.example.android_camera_client;

import java.net.URI;

class UploadParams {
  UploadParams(URI path, int id, CaptureListener cl) {
    pathToFile = path;
    captureId = id;
    listener = cl;
  }
  public URI pathToFile;
  public int captureId;
  public final CaptureListener listener;
}

public class Uploader {
  static void start(UploadParams params) {
    FileCredentials credentials = new FileCredentials();
    credentials.getCredentials();
    if (credentials.method.equals("s3")) {
      UploadS3 uploader = new UploadS3(credentials);
      uploader.execute(params);
    } else {
      UploadHTTP uploader = new UploadHTTP(credentials);
      uploader.execute(params);
    }
  }
}
