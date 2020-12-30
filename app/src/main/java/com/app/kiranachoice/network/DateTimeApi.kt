package com.app.kiranachoice.network

import com.app.kiranachoice.data.CurrentDateTime
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DateTimeApi {
    companion object {
        /*"http://worldtimeapi.org/api/timezone/"*/
        private var dateTimeApi: DateTimeApi? = null

        fun getInstance(): DateTimeApi {
            if (dateTimeApi == null) {
                dateTimeApi = Retrofit.Builder()
                    .baseUrl("http://api.timezonedb.com/v2.1/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(DateTimeApi::class.java)
            }
            return dateTimeApi!!
        }
    }


    @GET("list-time-zone")
    fun getTime(@Query("key") key: String, @Query("format") format : String, @Query("country") country: String): Call<CurrentDateTime>
}