package com.example.time_lapse_camera;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Binder;;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;;
import android.app.AlarmManager;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class TimeLapsePictureTaker extends Service {
    private static String TAG = "TimeLapsePictureTaker";

    PowerManager.WakeLock wakeLock;

    private Context ctx = this;

    private String INCOMING_START_ACTION = "com.mhzmaster.tlpt.START";
    private String INCOMING_STOP_ACTION = "com.mhzmaster.tlpt.STOP";

    private BroadcastReceiver mIntentReceiver;
    private CameraManager cm;

    private OkHttpClient client;

    public class LocalBinder extends Binder {
        TimeLapsePictureTaker getService() {
            return TimeLapsePictureTaker.this;
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
            PowerManager.PARTIAL_WAKE_LOCK,
            "time_lapse_camera::AlarmReceiverWakeLock");
        wakeLock.acquire();

        Log.i("TimeLapsePictureTaker", "Received start id " + startId + ": " + intent);
        String intentAction = null;
        try{
            intentAction = intent.getAction();
        } catch(NullPointerException e) {
        }
        Log.i(TAG, "intent is:" + intentAction );
        //initializeCameraPreview();
        cm = new CameraManager(ctx);
        listenWebsockets();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public void listenWebsockets() {
        client = new OkHttpClient();
        TelephonyManager tm;
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tm.getDeviceId();
        Request request = new Request.Builder().url("ws://hall-of-mirrors.herokuapp.com/ws/" + uuid).build();
        CaptureListener listener = new CaptureListener();
        listener.setCameraManager(cm);
        WebSocket ws = client.newWebSocket(request, listener);
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
