package com.example.time_lapse_camera;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


public class AlarmReceiver extends WakefulBroadcastReceiver {
    private String CAPTURE_INTENT_START = "com.mhzmaster.tlpt.START";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("AlarmReceiver", "Received Command");
        Intent startCapture = new Intent(CAPTURE_INTENT_START);
        startWakefulService(context, startCapture);
    }
}
