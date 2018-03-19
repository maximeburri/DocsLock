package ch.burci.docslock;

import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void parsing_device() throws Exception {
        /*
        {
                "group": {
                  "name": "Group 3",
                  "createdAt": "2017-10-13T08:58:38.808Z",
                  "updatedAt": "2017-10-20T11:03:23.457Z",
                  "id": 4,
                  "isLocked": false
                },
                "mac": "Maxime Natel",
                "isActive": false,
                "createdAt": "2017-10-17T08:47:59.795Z",
                "updatedAt": "2017-10-20T11:03:23.460Z",
                "id": 36,
                "isLocked": "false"
              }
         */
        String sb = "{" +
                "    \"group\": {" +
                "      \"name\": \"Group 3\"," +
                "      \"createdAt\": \"2017-10-13T08:58:38.808Z\"," +
                "      \"updatedAt\": \"2017-10-20T11:03:23.457Z\"," +
                "      \"id\": 4," +
                "      \"isLocked\": false" +
                "    }," +
                "    \"mac\": \"Maxime Natel\"," +
                "    \"isActive\": false," +
                "    \"createdAt\": \"2017-10-17T08:47:59.795Z\"," +
                "    \"updatedAt\": \"2017-10-20T11:03:23.460Z\"," +
                "    \"id\": 36," +
                "    \"isLocked\": \"false\"" +
                "  }";

        Gson gson = new GsonBuilder().create();
        DeviceWithGroup device = gson.fromJson(sb, DeviceWithGroup.class);
        assertEquals(device.getGroup().getName(), "Group 3");
        assertEquals(device.getGroup().isLocked(), false);
    }

    @Test
    public void parsing_deviceNoGroup() throws Exception {
        String sb2 = "{" +
                "    \"group\": null," +
                "    \"mac\": \"Maxime Natel\"," +
                "    \"isActive\": false," +
                "    \"createdAt\": \"2017-10-17T08:47:59.795Z\"," +
                "    \"updatedAt\": \"2017-10-20T11:03:23.460Z\"," +
                "    \"id\": 36," +
                "    \"isLocked\": \"false\"" +
                "  }";

        Gson gson2 = new GsonBuilder().create();
        DeviceWithGroup device2 = gson2.fromJson(sb2, DeviceWithGroup.class);
        assertEquals(device2.getGroup(), null);
    }
}