package com.mobitribe.app.qreader.network;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Author: Muhammad Shahab
 * Date: 5/5/17.
 * Description: Interface that contains the services
 */

public interface WebServices {

    @FormUrlEncoded
    @POST(ApiEndPoints.INSERT_CONTACTS)
    Call<ResponseBody> insertContacts(@Field("contact_data") String data, @Path("device_uuid") String id);
}
