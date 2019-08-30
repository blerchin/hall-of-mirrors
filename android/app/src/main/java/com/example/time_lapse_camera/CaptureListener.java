package com.example.time_lapse_camera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class CaptureListener extends WebSocketListener {
    public String TAG = "CaptureListener";
    private CameraManager cm;
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            cm.capture();
        }
    };

    public void setCameraManager(CameraManager _cm) {
        cm = _cm;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {

    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Got message: " + text);
        try {
            JSONObject data = new JSONObject(text);
            String command = data.getString("command");

            if (command.equals("capture:now")) {
                Log.d(TAG, "capturing");
                Message message = handler.obtainMessage();
                message.sendToTarget();
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }

}