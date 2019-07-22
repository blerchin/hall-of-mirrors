package com.example.time_lapse_camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;


import android.content.Context;
import android.hardware.Camera;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "CameraPreview";
	
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int saveCount = 0;

	public static abstract class CameraPictureCallback {
		public void onPictureSaved(URI pathToFile){
		}
	}

	private CameraPictureCallback callback;

	public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

    }
    public void init(){
    	 // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
    	mHolder = getHolder();
    	mHolder.addCallback(this);
    }
    
    /** Create a File for saving an image or video */
    private File getOutputMediaFile(Date date) throws IOException {
        File mediaStorageDir = new File(this.getContext().getFilesDir(), "TimeLapseCamera");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                throw (IOException) new IOException().initCause(new Throwable( "cannot access storage device."));
            }
        }
        
        return new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ getTimeStamp(date) + ".jpg");
    }
    
    private static String getTimeStamp(Date date){
    	//return new SimpleDateFormat("yyyyMMdd_HHmmss").format( date );
    	return String.valueOf( new Date().getTime() );
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
    	mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
    	//Log.d(TAG,"surfaceChanged called");
        //if (mHolder.getSurface() == null){
	        try{
	        	mCamera.setPreviewDisplay(mHolder);
	        	mCamera.startPreview();
	        } catch (IOException e){
	        	Log.d(TAG, "camera preview was not attached to mHolder");
	        }
     	//}
		setPreviewCallback();
        // stop preview before making changes
        try {
            //mCamera.stopPreview();
        } catch (Exception e){
        }
    }

	public void setOnSavePicture(CameraPictureCallback cb) {
    	callback = cb;
	}

	void onSavePicture(URI pathToFile) {
    	if (callback != null) {
			callback.onPictureSaved(pathToFile);
		}
	}

	Camera.Size getPictureSize(Camera.Parameters cameraParams) {
    	int goalSize = 2048;
    	int index = 0;
    	int bestErr = goalSize;
		List<Camera.Size> sizes = cameraParams.getSupportedPictureSizes();
		for(int i = 0; i < sizes.size(); i++) {
			int err = Math.abs(sizes.get(i).width - goalSize);
			if (err < bestErr) {
				index = i;
				bestErr = err;
			}
		}
		return sizes.get(index);
	}

	Location getLastKnownLocation() {
    	LocationManager lm = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);

		boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (!isGPSEnabled && !isNetworkEnabled) {
			return null;
		} else if (isGPSEnabled) {
			return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} else {
			return lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
	}

    void savePicture(Camera camera) {
    	if (saveCount > 0) {
    		return;
		}
    	saveCount++;
		Camera.Parameters params = camera.getParameters();
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
		Camera.Size size = getPictureSize(params);
		params.setPictureSize(size.width, size.height);
		params.setJpegQuality(50);
		params.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
		Location location = getLastKnownLocation();
		if (location != null) {
			params.setGpsLatitude(location.getLatitude());
			params.setGpsLongitude(location.getLongitude());
			params.setGpsAltitude(location.getAltitude());
			params.setGpsTimestamp(location.getTime() / 1000);
			params.setGpsProcessingMethod(location.getProvider());
		}
		camera.setParameters(params);

		camera.takePicture(null, null, new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] bytes, Camera camera) {
				Date mDate = new Date();
				try {
					File saveFile = getOutputMediaFile(mDate);
					OutputStream outToFile = new FileOutputStream(saveFile);
					outToFile.write(bytes);
					onSavePicture(saveFile.toURI());
				} catch(IOException e) {
					Log.d(TAG,"Couldn't create media file");
				}
			}
		});
	}
    
   public void setPreviewCallback(){
	   try {
           //We have to set the callback where the Preview is started
           mCamera.setPreviewCallback( 
           		new Camera.PreviewCallback() {
   					@Override
   					public void onPreviewFrame(byte[] data, Camera camera) {
						Log.d(TAG, "Preview Callback");
						savePicture(camera);
					}
   				});
       } catch (Exception e){
           Log.d(TAG, "Error starting camera preview: " + e.getMessage());
   }
  }
}