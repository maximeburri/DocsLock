package ch.burci.docslock.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

import ch.burci.docslock.Config;

/**
 * Created by maxime on 19.03.18.
 */

public class WebSocketService extends Service{
    final String TAG = "WebSocketService";

    private Socket mSocket;

    public WebSocketService() {
        super();
        try {
            mSocket = IO.socket("http://" + Config.SERVER_IP_PORT);
        } catch (URISyntaxException e) {}
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

    public void start(){
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.connect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
