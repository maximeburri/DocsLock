package ch.burci.docslock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ch.burci.docslock.controllers.MainActivity;

/**
 * Created by maxime on 07.03.17.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }
}
