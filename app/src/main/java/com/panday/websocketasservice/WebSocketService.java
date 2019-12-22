package com.panday.websocketasservice;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends Service {

    private static String WSSTAG = "WSS";
    PowerManager pm;
    PowerManager.WakeLock wl;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new WebSocketEcho().run();
        Toast.makeText(this, "WebSocket Service Started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WebSocketWakeLock");
        wl.acquire(10*60*1000L /*10 minutes*/);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "WebSocket Service Stopped", Toast.LENGTH_SHORT);
        super.onDestroy();
        wl.release();
        // stop web socket
    }

    public void BroadcastWSEcho(String echo) {
        Intent local = new Intent();
        local.setAction("service.to.activity.transfer");
        local.putExtra("wsdata", echo);
        this.sendBroadcast(local);
    }

    class WebSocketEcho extends WebSocketListener {
        WebSocket mWebSocket;

        private void run() {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(0,  TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url("ws://echo.websocket.org")
                    .build();
            client.newWebSocket(request, this);

            // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
            client.dispatcher().executorService().shutdown();
        }

        @Override public void onOpen(WebSocket webSocket, Response response) {
            mWebSocket = webSocket;
            webSocket.send("Hello...");
            StartPinging();
        }

        private void StartPinging() {
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable periodicUpdate = new Runnable() {
                @Override
                public void run() {
                    mWebSocket.send("X");
                    String report = "Sending message X as Ping - " + DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString();
                    Log.d(WSSTAG, report);
                    BroadcastWSEcho(report);
                    handler.postDelayed(this, 10*1000);
                }
            };
            handler.post(periodicUpdate);
        }

        @Override public void onMessage(WebSocket webSocket, String text) {
            String report = "MESSAGE (TEXT) - " + text + " On - " + DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString();
            Log.d(WSSTAG, report);
            BroadcastWSEcho(report);
        }

        @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
            String report ="MESSAGE (BYTES) - " + bytes.hex() + " On - " + DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString();
            Log.d(WSSTAG, report);
            BroadcastWSEcho(report);
        }

        @Override public void onClosing(WebSocket webSocket, int code, String reason) {
            String report ="CLOSE - Code : " + code + "\nReason : " + reason  +" On - " + DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString();
            Log.d(WSSTAG, report);
            BroadcastWSEcho(report);
            webSocket.close(1000, null);
        }

        @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            String report ="Failure - Throws : " + t + "\nResponse : " + response  +" On - " + DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString();
            Log.d(WSSTAG, report);
            BroadcastWSEcho(report);
        }
    }

}
