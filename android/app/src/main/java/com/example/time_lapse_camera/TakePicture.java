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
import android.widget.Toast;

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
    }
    
    
    public void onPause(){
    	super.onPause();
    }

}
