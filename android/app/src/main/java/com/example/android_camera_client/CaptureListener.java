package com.example.android_camera_client;

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
    private ShowManager sm;
    private WebSocket ws;
    private boolean isOpen = false;
    private CaptureListener self = this;

    CaptureListener(CameraManager _cm, ShowManager _sm) {
        super();
        cm = _cm;
        sm = _sm;
    }

    Handler captureHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Integer captureId = (Integer) message.obj;
            cm.capture(captureId, self);
        }
    };

    Handler showHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String s3Key = (String) message.obj;
            sm.showImage(s3Key);
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
        public void handleMessage(Message message) {}
    };

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

            if (command.equals("capture:now")) {
                Log.d(TAG, "capturing");
                int captureId = data.getInt("captureId");
                Message message = captureHandler.obtainMessage(0, 0, 0, new Integer(captureId));
                message.sendToTarget();
            } else if (command.equals("show:image")) {
                String s3Key = data.getString("s3Key");
                Message message = showHandler.obtainMessage(0, 0, 0, s3Key);
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