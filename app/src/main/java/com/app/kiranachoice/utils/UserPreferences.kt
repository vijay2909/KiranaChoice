package com.app.kiranachoice.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_PREFERENCES_KEY = "User Preferences Key"

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context: Context) {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(USER_PREFERENCES_KEY, MODE_PRIVATE)
    private var edit: SharedPreferences.Editor = sharedPreferences.edit()

    fun setUserName(name: String) {
        edit.putString("_", name).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("_", null)
    }
}