package com.example.time_lapse_camera;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import java.net.URI;

import android.os.AsyncTask;
import android.os.Binder;;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.app.AlarmManager;
import android.widget.Toast;

import java.util.List;

public class TimeLapsePictureTaker extends Service {
    private static String TAG = "TimeLapsePictureTaker";
    private static long UPLOAD_INTERVAL = 60 * 1000;

    private CameraPreview cp;
    private WindowManager wm;
    private Camera mCamera;
    PowerManager.WakeLock wakeLock;

    private Context ctx = this;

    private String INCOMING_START_ACTION = "com.mhzmaster.tlpt.START";
    private String INCOMING_STOP_ACTION = "com.mhzmaster.tlpt.STOP";

    private AlarmManager am;
    private BroadcastReceiver mIntentReceiver;
    private PendingIntent scheduledIntent;

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
                    unscheduleNext();
                    stopSelf();
                }
            }
        };

        ctx.registerReceiver(mIntentReceiver, new IntentFilter(INCOMING_STOP_ACTION));

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) ctx.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "time_lapse_camera::AlarmReceiverWakeLock");
        wakeLock.acquire();

        Log.i("TimeLapsePictureTaker", "Received start id " + startId + ": " + intent);
        String intentAction = null;
        try{
            intentAction = intent.getAction();
        } catch(NullPointerException e) {
        }
        Log.i(TAG, "intent is:" + intentAction );
        initializeCameraPreview();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        cleanUp();
        // Tell the user we stopped.
        Toast.makeText(this, R.string.picture_taker_stopped, Toast.LENGTH_SHORT).show();
        ctx.unregisterReceiver(mIntentReceiver);
    }

    void scheduleNext() {
        unscheduleNext();
        am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        scheduledIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(INCOMING_START_ACTION), 0);
        am.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + UPLOAD_INTERVAL,
                scheduledIntent
        );
        Log.d(TAG, "intent scheduled");
    }

    void unscheduleNext() {
        if (scheduledIntent != null) {
            am.cancel(scheduledIntent);
        }
    }

    public void cleanUp() {
        // remove overlaid view
        if (cp != null) {
            wm.removeView(cp);
            wakeLock.release();
            cp = null;
        }
        Log.d(TAG,"view Removed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
        }
        return c; // returns null if camera is unavailable
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    final void previewWasSaved(URI pathToFile) {
        cleanUp();
        UploadS3 s3 = new UploadS3();
        AsyncTask<URI,Void,Long> upload = s3.execute(pathToFile);
        while(upload.getStatus() == AsyncTask.Status.PENDING) {}
        scheduleNext();
        //stopSelf();
    }

    public void initializeCameraPreview(){
        Log.d(TAG,"checking camera hardware");
        if (checkCameraHardware( this ) ){
            try{
                mCamera = getCameraInstance();
                cp = new CameraPreview(ctx,mCamera);
                Log.d(TAG,"CameraPreview Started");
            } catch (Exception e) {
                e.getStackTrace();
            }
            cp.setOnSavePreview(new CameraPreview.CameraPreviewCallback() {
                @Override
                public void onPreviewSaved(URI pathToFile) {
                    previewWasSaved(pathToFile);
                }
            });
            // Gnarly hack thanks to http://stackoverflow.com/questions/2386025/android-camera-without-preview
            // Allows camera to be run as a persistent service even when screen is off
            // or user is doing other things (!!!)
            wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            );
            params.width = 1;
            params.height = 1;
            Log.d(TAG,"WindowManager created");
            try {
                wm.addView(cp, params);
                Log.d(TAG,"View Added");
                cp.init();
            } catch (Exception e) {
                Log.d(TAG,"issue while initializing CameraPreview");
            }
            SurfaceHolder mHolder = cp.getHolder();
            Log.d(TAG,"got SurfaceHolder");
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mHolder.setFormat(PixelFormat.TRANSPARENT);

            try {
                mCamera.stopPreview();
                List<Camera.Size> previewSize = mCamera.getParameters().getSupportedPreviewSizes();
                Camera.Size maxPreviewSize = previewSize.get(previewSize.size() - 1);
                Log.d(TAG,"preview size set to "+maxPreviewSize.width+" x "+maxPreviewSize.height);
                mCamera.getParameters().setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }
    }
}
