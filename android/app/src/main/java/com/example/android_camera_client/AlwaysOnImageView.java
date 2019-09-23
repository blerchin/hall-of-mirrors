package com.example.android_camera_client;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class AlwaysOnImageView extends ImageView {
  String TAG = "AlwaysOnImageView";
  Window window;
  public AlwaysOnImageView(Context context) {
    super(context);
    init(context);
  }

  public AlwaysOnImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public AlwaysOnImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context ctx) {
    if (ctx instanceof Activity) {
      window = ((Activity) ctx).getWindow();
      window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
      window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
      window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (window != null) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
      window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
      window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
  }
}
