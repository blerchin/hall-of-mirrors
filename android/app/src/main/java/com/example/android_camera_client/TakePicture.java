package com.example.android_camera_client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;


public class TakePicture extends Activity{
	private static final String TAG = "TakePicture";
	Context context = this;
	
	private static final String CAPTURE_INTENT_START = "com.mhzmaster.tlpt.START";
	private static final String CAPTURE_INTENT_STOP = "com.mhzmaster.tlpt.STOP";
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_take_picture);
        startCapture();
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
