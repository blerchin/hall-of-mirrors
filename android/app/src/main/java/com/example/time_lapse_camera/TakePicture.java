package com.example.time_lapse_camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class TakePicture extends Activity{
	
	private static final String TAG = "TakePicture";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
	Context context = this;
	
	private static final String CAPTURE_INTENT_START = "com.mhzmaster.tlpt.START";
	private static final String CAPTURE_INTENT_STOP = "com.mhzmaster.tlpt.STOP";
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_take_picture);
                
        
    }
    public void startCapture( View view ) {
    	startCapture();    
    			
    }
    
    public void startCapture() {
    	Intent startCapture = new Intent(CAPTURE_INTENT_START);
    	context.startService( startCapture );
    	
    }
    public void stopCapture( View view ) {
    	stopCapture();
    }
    public void stopCapture() {
    	Intent stopCapture = new Intent(CAPTURE_INTENT_STOP);
    	context.sendBroadcast(stopCapture);
    }
    
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_take_picture, menu);
        return true;
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	
    	//doUnbindPTService();
    }
    
    
   
    
 

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){

        File mediaStorageDir = this.getFilesDir();


        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    
    
    
    public void onPause(){
    	super.onPause();
    
    }
    
    void saveJpegToDisk( byte[] jpegData, Camera camera) {
    	File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions: " );
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(jpegData);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }


}
