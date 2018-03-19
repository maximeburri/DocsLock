package ch.burci.docslock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import ch.burci.docslock.controllers.MainActivity;
import ch.burci.docslock.models.PrefUtils;

public class MessagingService extends FirebaseMessagingService {
    public MessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("MessagingService", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("MessagingService", "Message data payload: " + remoteMessage.getData());

            // Save to last device
            Map<String, String> data = remoteMessage.getData();

            // Re-open app or restart with "UPDATE" message intent

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("MessagingService", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
