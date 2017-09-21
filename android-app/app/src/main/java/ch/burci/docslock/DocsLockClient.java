package ch.burci.docslock;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by maxime on 11/09/17.
 */

public interface DocsLockClient {
    @GET("/device")
    Call<List<Device>> getDevices();

    @FormUrlEncoded
    @POST("/device")
    Call<Device> createDevice(@Field("mac") String mac);

    @FormUrlEncoded
    @POST("/device")
    Call<Device> setStateDevice(@Field("isActive") Boolean isActive);
}
