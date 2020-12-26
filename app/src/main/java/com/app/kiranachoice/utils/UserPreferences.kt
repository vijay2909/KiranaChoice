package com.app.kiranachoice.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

private const val USER_PREFERENCES_KEY = "User Preferences Key"

class UserPreferences(val context: Context) {

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