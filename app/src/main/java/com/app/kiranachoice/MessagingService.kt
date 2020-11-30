package com.app.kiranachoice

import android.content.Context
import com.app.kiranachoice.utils.DEVICE_TOKEN
import com.google.firebase.messaging.FirebaseMessagingService

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val pref = applicationContext.getSharedPreferences("_", MODE_PRIVATE)
        pref.edit().putString(DEVICE_TOKEN, token).apply()
    }

    companion object {
        fun getToken(context : Context) : String? {
            return context.getSharedPreferences("_", MODE_PRIVATE).getString(DEVICE_TOKEN, null)
        }
    }
}