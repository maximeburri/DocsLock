package ch.burci.docslock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ch.burci.docslock.DocsLockService;

/**
 * Created by ciccius on 02.11.17.
 */

public class ShutdownReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        DocsLockService.setStateDevice(false);
    }


}
