package com.example.android_camera_client;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class AndroidCameraClient extends Service {
    private static String TAG = "AndroidCameraClient";

    PowerManager.WakeLock wakeLock;

    private Context ctx = this;

    private String INCOMING_START_ACTION = "com.mhzmaster.tlpt.START";
    private String INCOMING_STOP_ACTION = "com.mhzmaster.tlpt.STOP";

    private final int WS_MAX_RETRIES = 10;
    private int wsRetries = 0;
    private final Handler retryHandler = new Handler();

    private BroadcastReceiver mIntentReceiver;
    private CameraManager cm;
    private ShowManager sm;

    private OkHttpClient client;
    private FileCredentials credentials;
    private CaptureListener listener;

    public class LocalBinder extends Binder {
        AndroidCameraClient getService() {
            return AndroidCameraClient.this;
        }
    }

    @Override
    public void onCreate() {
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Handle receiver
                String mAction = intent.getAction();
                Log.v(TAG,"received broadcast "+mAction);
                if (mAction.contains(INCOMING_STOP_ACTION) ) {
                    stopSelf();
                }
            }
        };
        ctx.registerReceiver(mIntentReceiver, new IntentFilter(INCOMING_STOP_ACTION));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) ctx.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "time_lapse_camera::AlarmReceiverWakeLock");
        wakeLock.acquire();

        Log.i("AndroidCameraClient", "Received start id " + startId + ": " + intent);
        String intentAction = null;
        try{
            intentAction = intent.getAction();
        } catch(NullPointerException e) {
        }
        Log.i(TAG, "intent is:" + intentAction );
        //initializeCameraPreview();
        cm = new CameraManager(ctx);
        sm = new ShowManager(ctx);
        listenWebsockets();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public void onWebsocketOpen() {
        wsRetries = 0;
        Toast.makeText(ctx, "Websocket opened! ", Toast.LENGTH_LONG).show();

    }

    public void onWebsocketClose(String reason) {
      Toast.makeText(ctx, "Websocket closed. " + reason, Toast.LENGTH_LONG).show();
      wsRetries++;
      int delay = 1000;
      if (wsRetries > WS_MAX_RETRIES) {
        //keep trying forever but not so frequently
        delay = 1000 * 60;
      }
      retryHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
              Log.d(TAG, "retrying...");
              listenWebsockets();
          }
      }, delay);
    }

    public void listenWebsockets() {
        if (listener == null) {
            listener = new CaptureListener(this, cm, sm);
        }
        credentials = new FileCredentials();
        credentials.getCredentials();
        client = new OkHttpClient();
        TelephonyManager tm;
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tm.getDeviceId();
        String wsUrl = credentials.wsURL + "/" + uuid;
        Log.i(TAG, "wsUrl: " + wsUrl);
        Request request = new Request.Builder().url(wsUrl).build();
        client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onDestroy(){
        cm.cleanUp();
        Toast.makeText(this, R.string.picture_taker_stopped, Toast.LENGTH_SHORT).show();
        ctx.unregisterReceiver(mIntentReceiver);
        client.dispatcher().executorService().shutdown();
        wakeLock.release();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


}
