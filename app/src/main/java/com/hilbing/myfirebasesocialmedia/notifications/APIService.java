package com.hilbing.myfirebasesocialmedia.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA5xoxPzc:APA91bE1mEn2DpaTg7eYr2b79hS3DtCixyTvO3qYpdwb9klJRppQrNor0tv_fUSBPK442KQ8z05JexdDAZVqZ5eah2YqZJo15KgGpAgOM5SapqzZNKdenHFBCPj2McZt7l-NS9R9ntIg"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
