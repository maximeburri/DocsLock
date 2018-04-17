package ch.burci.docslock.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import ch.burci.docslock.Config;
import ch.burci.docslock.DocsLockService;
import ch.burci.docslock.controllers.MainActivity;
import ch.burci.docslock.models.PrefUtils;

/**
 * Created by maxime on 19.03.18.
 */

public class WebSocketService extends Service{
    final String TAG = "WebSocketService";

    private Socket mSocket;

    static public String ACTION_NAME = "WebSocketStatusChange";

    private final IBinder mBinder = new WebSocketBinder();

    boolean connected = false;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class WebSocketBinder extends Binder {
        public WebSocketService getService() {
            // Return this instance of LocalService so clients can call public methods
            return WebSocketService.this;
        }
    }

    public WebSocketService() {
        super();
        Log.d(TAG, "WebSocketCreated");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start");
        super.onStartCommand(intent, flags, startId);
        start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Exit");
        Intent broadcastIntent = new Intent("ch.burci.docslock.receivers.RestartWebSocketService");
        sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Connected");
            connected = true;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_NAME);
            broadcastIntent.putExtra("connected", true);
            sendBroadcast(broadcastIntent);
            connected = true;
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Disconnected");
            connected = false;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_NAME);
            broadcastIntent.putExtra("connected", false);
            sendBroadcast(broadcastIntent);

        }
    };

    // On update device by the client
    private Emitter.Listener onUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Update");
            JSONObject obj = (JSONObject)args[0];
            Intent startIntent = new Intent(WebSocketService.this, MainActivity.class);
            startIntent.putExtra("UPDATE", true);
            try {
                startIntent.putExtra("device",
                        obj.getJSONObject("data")
                                .getJSONObject("device")
                                .toString());
                startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(startIntent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public boolean isConnected(){
        return connected;
    }

    public void start(){
        if(mSocket != null) {
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
        }
        String deviceId = DocsLockService.getDeviceId(this.getApplicationContext());

        IO.Options opts = new IO.Options();

        // Todo : try in x seconds...
        if(deviceId == null){
            Log.e(TAG, "No device id..");
            return;
        }

        opts.query = "deviceId=" + deviceId;
        opts.reconnectionDelay = Config.WEB_SOCKET_RECONNECTION_DELAY;
        opts.reconnectionDelayMax = Config.WEB_SOCKET_RECONNECTION_DELAY_MAX;
        opts.forceNew = true;
        try {
            mSocket = IO.socket(PrefUtils.getServerURL(this), opts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on("update", onUpdate);
        mSocket.connect();
        Log.d(TAG, "socket.connect()");
    }

    public void stop() {
        if(mSocket != null) {
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
        }
    }
}
