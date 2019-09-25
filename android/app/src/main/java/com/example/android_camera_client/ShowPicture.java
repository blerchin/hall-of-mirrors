package com.example.android_camera_client;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ShowPicture extends Activity {
  public String TAG = "ShowPicture";
  AlwaysOnImageView imageView;
  Bitmap bitmap;

  BroadcastReceiver showReceiver;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_picture);
    Uri imageToShow = getIntent().getData();
    showImage(imageToShow);
  }

  public void showImage(Uri uri) {
    if(bitmap != null) {
      bitmap.recycle();
    }
    bitmap = BitmapFactory.decodeFile(uri.getPath());
    if (imageView == null) {
      imageView = (AlwaysOnImageView) findViewById(R.id.showPicture_imageView);
    }
    imageView.setImageBitmap(bitmap);
  }

  @Override
  protected void onPause() {
    // We are currently creating a new Activity each time an image is shown. So to avoid creating
    // a memory leak we have to clear out the image data.
    Log.d(TAG, "onPause");
    if (bitmap != null) {
      ((BitmapDrawable) imageView.getDrawable()).getBitmap().recycle();
      bitmap.recycle();
      bitmap = null;
      imageView.setImageDrawable(null);
    }
    super.onPause();
  }
}

