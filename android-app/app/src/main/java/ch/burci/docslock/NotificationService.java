package ch.burci.docslock;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.google.android.gms.internal.zzs.TAG;

public class NotificationService extends FirebaseInstanceIdService {
    public NotificationService() {
    }

    private static String token;
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        token = refreshedToken;
        DocsLockService.setFirebaseToken(refreshedToken);
    }

    public static String getFirebaseToken() {
        return token;
    }
}
