package com.app.kiranachoice.network

import com.app.kiranachoice.models.CurrentDateTime
import retrofit2.Call
import retrofit2.http.GET

interface DateTimeApi {
    companion object {
        const val BASE_URL: String = "http://worldtimeapi.org/api/timezone/"
    }


    @GET("Asia/Kolkata")
    fun getTime(): Call<CurrentDateTime>
}