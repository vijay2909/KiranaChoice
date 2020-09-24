package com.app.kiranachoice

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class KiranaChoiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebaseRemoteConfig()
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