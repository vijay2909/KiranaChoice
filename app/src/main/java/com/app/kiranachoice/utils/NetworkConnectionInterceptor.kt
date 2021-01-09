package com.app.kiranachoice.utils

import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService
import okhttp3.Interceptor
import okhttp3.Response


abstract class NetworkConnectionInterceptor : Interceptor {

    abstract fun isInternetAvailable(): Boolean

    abstract fun onInternetUnavailable()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!isInternetAvailable()) {
            onInternetUnavailable()
        }

        return chain.proceed(request)
    }
}