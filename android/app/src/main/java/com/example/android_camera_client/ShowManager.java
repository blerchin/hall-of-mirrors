package com.example.android_camera_client;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.Date;

public class ShowManager {
  public final String TAG = "ShowManager";
  Context ctx;
  ShowManager(Context _ctx) {
    ctx = _ctx;
  }

  public void showImage(String s3Key) {
    Downloader download = new Downloader(this);
    download.execute(s3Key);
  }

  public File getTempFile() {
    return new File(ctx.getCacheDir().getPath() + File.separator + "TMP_" + new Date().getTime());
  }

  public void onDownloadComplete(Uri uri) {
    if (uri != Uri.EMPTY) {
      Log.d(TAG, "sending showIntent");

      Intent startIntent = new Intent(ctx, ShowPicture.class);
      startIntent.setData(uri);
      startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      ctx.startActivity(startIntent);
    }
  }
}
