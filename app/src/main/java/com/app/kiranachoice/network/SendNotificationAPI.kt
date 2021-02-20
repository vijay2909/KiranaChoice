package com.app.kiranachoice.network


import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface SendNotificationAPI {

    companion object {
        const val BASE_URL = "https://fcm.googleapis.com/"
    }

    @Headers(
        "Authorization: key=AAAA1cZ8NlQ:APA91bF6ML-nZZeI1hX0u9MFRMVpWNpC4iUvx0VT-P9lqd8z6nKpOH03UXbTa2UlqOY0zV_Caky8x7Fg6h1-tAGkP5ILHoawthYiDVQ9HcmBDbFPF_7k0eQfpP4EMO2JZ7J1X2G9sX-0",
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    fun sendChatNotification(@Body payload: JsonObject?): Call<JsonObject>
}