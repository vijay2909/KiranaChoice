package com.app.kiranachoice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class App : Application(){

    companion object {
        const val CHANNEL_ORDER_BOOKED_ID = "Order booked"
    }

    override fun onCreate() {
        super.onCreate()
        initFirebaseRemoteConfig()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val channelOrderBooked = NotificationChannel(
                CHANNEL_ORDER_BOOKED_ID,
                getString(R.string.notification_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            channelOrderBooked.description = getString(R.string.notification_description)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelOrderBooked)
        }
    }

    private fun initFirebaseRemoteConfig() {
        FirebaseApp.initializeApp(this)
        FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10)
                .build()

            setConfigSettingsAsync(configSettings)

            setDefaultsAsync(R.xml.remote_config_defaults)

            fetchAndActivate().addOnCompleteListener { task ->
                val update = task.result
            }
        }
    }
}