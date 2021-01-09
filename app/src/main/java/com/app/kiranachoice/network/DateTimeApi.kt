package com.app.kiranachoice.network

import com.app.kiranachoice.data.CurrentDateTime
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DateTimeApi {
    companion object {
        const val BASE_URL = "http://api.timezonedb.com/v2.1/"
    }

    @GET("list-time-zone")
    fun getTime(
        @Query("key") key: String,
        @Query("format") format: String,
        @Query("country") country: String
    ): Call<CurrentDateTime>
}