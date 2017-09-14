package ch.burci.docslock;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import ch.burci.docslock.models.PrefUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by maxime on 14/09/17.
 */

public class DocsLockService {
    private static String API_BASE_URL = Config.SERVER_URL;
    private static DocsLockClient client;
    private static String deviceId;

    // Init retrofit client
    private static void initClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit retrofit =
            new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
            .client(
                httpClient.build()
            )
            .build();

        client = retrofit.create(DocsLockClient.class);
    }

    // Init retrofit (if necessary) and create device on server (if not created)
    public static void init(Context context){
        if(client == null){
            initClient();

            deviceId = PrefUtils.getDeviceId(context);
            if(deviceId == null){
                createDevice(context);
            }
        }
    }

    // Create device on server
    private static void createDevice(final Context context){
        client.createDevice(getWifiMacAddress()).enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
               setDeviceId(context, response.body().getId());
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                Log.e("DocsLockService", t.getMessage());
            }
        });
    }


    private static void setDeviceId(Context context, String id){
        deviceId = id;
        PrefUtils.setDeviceId(id, context);
    }

    // Get MAC address
    // From https://stackoverflow.com/questions/31329733/how-to-get-the-missing-wifi-mac-address-in-android-marshmallow-and-later/32948723#32948723
    private static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)){
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac==null){
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length()>0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    /* Example :
    getClient().getDevices().enqueue(new Callback<List<Device>>() {
        @Override
        public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
            return; // Breakpoint here to prove that's okay
        }

        @Override
        public void onFailure(Call<List<Device>> call, Throwable t) {
        }
    });
    */
    public DocsLockClient getClient() {
        return client;
    }

}
