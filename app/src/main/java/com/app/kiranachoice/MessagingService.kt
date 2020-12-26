package com.app.kiranachoice

import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.app.kiranachoice.utils.DEVICE_TOKEN
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    private lateinit var dbFire: CollectionReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        dbFire = FirebaseFirestore.getInstance().collection(USER_REFERENCE)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val pref = applicationContext.getSharedPreferences("_", MODE_PRIVATE)
        pref.edit().putString(DEVICE_TOKEN, token).apply()
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification!!.title
            val body = remoteMessage.notification!!.body

            val notification = NotificationCompat.Builder(this, App.CHANNEL_CHAT)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(getColor(R.color.colorPrimary))
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification.build())
        }
    }


    companion object {
        fun getToken(context: Context): String? {
            return context.getSharedPreferences("_", MODE_PRIVATE).getString(DEVICE_TOKEN, null)
        }
    }
}