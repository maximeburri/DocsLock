package ch.burci.docslock;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class DeviceWithGroup extends Device {
    private Group group;

    public DeviceWithGroup() {
        super();
    }

    public Group getGroup() {
        return group;
    }

    public static DeviceWithGroup fromJSON(String json){
        Gson gson = new GsonBuilder().create();
        DeviceWithGroup device = gson.fromJson(json, DeviceWithGroup.class);
        return device;
    }

    public String toJSON(){
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
