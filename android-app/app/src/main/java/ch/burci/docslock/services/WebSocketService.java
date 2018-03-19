package ch.burci.docslock.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import ch.burci.docslock.Config;
import ch.burci.docslock.DocsLockService;
import ch.burci.docslock.controllers.MainActivity;

/**
 * Created by maxime on 19.03.18.
 */

public class WebSocketService extends Service{
    final String TAG = "WebSocketService";

    private Socket mSocket;

    public WebSocketService() {
        super();
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
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Disconnected");
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

    public void start(){
        IO.Options opts = new IO.Options();
        String deviceId = DocsLockService.getDeviceId(this.getApplicationContext());

        // Todo : try in x seconds...
        if(deviceId == null){
            Log.e(TAG, "No device id..");
            return;
        }

        opts.query = "deviceId=" + deviceId;
        opts.reconnectionDelay = Config.WEB_SOCKET_RECONNECTION_DELAY;
        try {
            mSocket = IO.socket("http://" + Config.SERVER_IP_PORT, opts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on("update", onUpdate);
        mSocket.connect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
