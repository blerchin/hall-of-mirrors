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
    private int KEEP_ALIVE_INTERVAL = 1000;
    private CameraManager cm;
    private WebSocket ws;
    private boolean isOpen = false;
    private CaptureListener self = this;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Integer captureId = (Integer) message.obj;
            cm.capture(captureId, self);
        }
    };

    public void sendSuccess(int captureId, String s3Key) {
        try {
            JSONObject data = new JSONObject();
            data.put("result", "capture:success");
            data.put("s3Key", s3Key);
            data.put("captureId", captureId);
            ws.send(data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Handler wsKeepAlive = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {

        }
    };

    public void setCameraManager(CameraManager _cm) {
        cm = _cm;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        ws = webSocket;
        isOpen = true;
        keepAlive();
    }

    public void keepAlive() {
        wsKeepAlive.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject data = new JSONObject();
                    data.put("command", "keep:alive");
                    ws.send(data.toString());
                    if(isOpen) {
                        keepAlive();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, KEEP_ALIVE_INTERVAL);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Got message: " + text);
        try {
            JSONObject json = new JSONObject(text);
            String command = json.getString("command");
            JSONObject data = json.getJSONObject("data");
            int captureId = data.getInt("captureId");

            if (command.equals("capture:now")) {
                Log.d(TAG, "capturing");
                Message message = handler.obtainMessage(0, 0, 0, new Integer(captureId));
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
        isOpen = false;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        isOpen = false;
        t.printStackTrace();
    }

}