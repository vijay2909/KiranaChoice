package com.app.kiranachoice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.kiranachoice.utils.ConnectivityLiveData
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    private var mSnackbar : Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val connectivityLiveData = ConnectivityLiveData(application)

        connectivityLiveData.observe(this, {mBoolean ->
            mBoolean?.let {
                if (it) {
                    mSnackbar?.dismiss()
                } else {
                    mSnackbar = Snackbar.make(window.decorView.rootView, "No Network connection", Snackbar.LENGTH_LONG)
                    mSnackbar?.duration = Snackbar.LENGTH_INDEFINITE
                    mSnackbar?.show()
                }
            }
        })

    }
}