package com.app.kiranachoice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.provider.Settings
import androidx.databinding.library.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    companion object {
        const val CHANNEL_ORDER_BOOKED_ID = "Order booked"
        const val CHANNEL_CHAT = "Chat Notification"
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
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

            notificationManager.createNotificationChannel(channelOrderBooked)
            notificationManager.createNotificationChannel(chatChannel)
        }

    }
}