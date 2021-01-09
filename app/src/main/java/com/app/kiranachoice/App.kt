package com.app.kiranachoice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.*
import android.os.Build
import android.provider.Settings
import com.app.kiranachoice.listeners.InternetConnectionListener
import com.app.kiranachoice.network.DateTimeApi
import com.app.kiranachoice.utils.NetworkConnectionInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class App : Application() {

    private var apiService: DateTimeApi? = null
    private var mInternetConnectionListener: InternetConnectionListener? = null

    companion object {
        const val CHANNEL_ORDER_BOOKED_ID = "Order booked"
        const val CHANNEL_CHAT = "Chat Notification"
    }

    override fun onCreate() {
        super.onCreate()
        /* val name = UserPreferences(this).getUserName()

         FirebaseMessaging.getInstance().subscribeToTopic("user_$name")*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelOrderBooked = NotificationChannel(
                CHANNEL_ORDER_BOOKED_ID,
                getString(R.string.order_notification_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            channelOrderBooked.description = getString(R.string.notification_description)
            channelOrderBooked.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, null)

            val chatChannel = NotificationChannel(
                CHANNEL_CHAT,
                getString(R.string.chat_notification_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            chatChannel.description = getString(R.string.chat_notification_description)
            chatChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, null)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelOrderBooked)
            notificationManager.createNotificationChannel(chatChannel)
        }

    }

    fun setInternetConnectionListener(listener: InternetConnectionListener) {
        mInternetConnectionListener = listener
    }

    fun removeInternetConnectionListener() {
        mInternetConnectionListener = null
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }


    fun getApiService(): DateTimeApi {
        if (apiService == null) {
            apiService = Retrofit.Builder()
                .baseUrl(DateTimeApi.BASE_URL)
                .client(provideOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DateTimeApi::class.java)
        }
        return apiService!!
    }

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(object : NetworkConnectionInterceptor() {
                override fun isInternetAvailable(): Boolean {
                    return isNetworkAvailable()
                }

                override fun onInternetUnavailable() {
                    mInternetConnectionListener?.onInternetUnavailable()
                }
            })
            .build()
    }
}