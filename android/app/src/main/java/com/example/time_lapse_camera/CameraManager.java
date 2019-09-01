package com.example.time_lapse_camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class CameraManager {
    private CameraPreview cp;
    private WindowManager wm;
    private Camera mCamera;
    private static String TAG = "CameraManager";
    Context ctx;

    CameraManager(Context _ctx) {
        ctx = _ctx;
    }

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

    public void cleanUp() {
        // remove overlaid view
        if (cp != null) {
            wm.removeView(cp);
            cp = null;
        }

        Log.d(TAG,"view Removed");
    }

    void uploadS3(UploadParams params) {
        UploadS3 s3 = new UploadS3();
        AsyncTask<UploadParams,Void,String> upload = s3.execute(params);
        Log.d(TAG, "Uploaded a picture!");
    }

    public void capture(final int captureId, final CaptureListener listener){
        Log.d(TAG,"checking camera hardware");
        if (checkCameraHardware(ctx) ){
            mCamera = getCameraInstance();
            cp = new CameraPreview(ctx, mCamera);
            Log.d(TAG,"CameraPreview Started");

            cp.setOnSavePicture(new CameraPreview.CameraPictureCallback() {
                @Override
                public void onPictureSaved(URI pathToFile) {
                    cleanUp();
                    uploadS3(new UploadParams(pathToFile, captureId, listener));
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
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }
    }
}
