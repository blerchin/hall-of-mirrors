package com.example.android_camera_client;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

public class ShowPicture extends Activity {
  public String TAG = "ShowPicture";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_picture);
    Uri imageToShow = getIntent().getData();
    showImage(imageToShow);
  }

  private void showImage(Uri uri) {
    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
    AlwaysOnImageView imageView = (AlwaysOnImageView) findViewById(R.id.showPicture_imageView);
    imageView.setImageBitmap(bitmap);
  }
}
