package ch.burci.docslock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ch.burci.docslock.services.WebSocketService;

/**
 * Created by maxime on 19.03.18.
 */

public class WebSocketServiceRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(WebSocketServiceRestartReceiver.class.getSimpleName(), "Service Stops.. Try restarting");
        context.startService(new Intent(context, WebSocketService.class));
    }
}
