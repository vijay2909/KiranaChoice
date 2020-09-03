package com.app.kiranachoice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.lang.NumberFormatException

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkForUpdate()
    }

    private fun checkForUpdate() {
        val appVersion: String = getAppVersion(this) // 1.0

        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val minVersion = remoteConfig.getString("min_version_of_app")
        val currentVersion = remoteConfig.getString("latest_version_of_app")

        Log.i(TAG, "appVersion : $appVersion")
        Log.i(TAG, "minVersion : $minVersion")
        Log.i(TAG, "currentVersion : $currentVersion")

        if (!TextUtils.isEmpty(minVersion) && !TextUtils.isEmpty(appVersion) && checkMandateVersionApplicable(
                getAppVersionWithoutAlphaNumeric(minVersion),
                getAppVersionWithoutAlphaNumeric(appVersion)
            )
        ) {
            onUpdateNeeded(true)
        } else if (!TextUtils.isEmpty(currentVersion) && !TextUtils.isEmpty(appVersion) && !TextUtils.equals(
                currentVersion,
                appVersion
            )
        ) {
            onUpdateNeeded(false)
        } else {
            moveForward()
        }
    }

    private fun getAppVersionWithoutAlphaNumeric(result: String): String {
        return result.replace(".", "")
    }

    private fun checkMandateVersionApplicable(minVersion: String, appVersion: String): Boolean {
        return try {
            val minVersionInt = minVersion.toInt()
            val appVersionInt = appVersion.toInt()
            minVersionInt > appVersionInt
        } catch (exp: NumberFormatException) {
            false
        }
    }

    private fun getAppVersion(context: Context): String {
        var result = ""
        try {
            result = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("TAG", e.message.toString())
        }
        return result
    }

    private fun onUpdateNeeded(isMandatoryUpdate: Boolean) {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_app))
            .setCancelable(false)
            .setMessage(if (isMandatoryUpdate) getString(R.string.dialog_update_available_message) else "A new version is found on Play store, please update for better usage.")
            .setPositiveButton(getString(R.string.update_now))
            { _, _ ->
                openAppOnPlayStore(this, null)
            }

        if (!isMandatoryUpdate) {
            dialogBuilder.setNegativeButton(getString(R.string.later)) { dialog, _ ->
                moveForward()
                dialog?.dismiss()
            }.create()
        }
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
    }

    private fun moveForward() {
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }, 2000)
    }

    private fun openAppOnPlayStore(ctx: Context, package_name: String?) {
        var package_name = package_name
        if (package_name == null) {
            package_name = ctx.packageName
        }
        Log.i(TAG, "packageName : $package_name")
        val uri = Uri.parse("market://details?id=$package_name")
        openURI(ctx, uri, "Play Store not found in your device")
    }

    private fun openURI(
        ctx: Context,
        uri: Uri?,
        error_msg: String?
    ) {
        val i = Intent(Intent.ACTION_VIEW, uri)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        if (ctx.packageManager.queryIntentActivities(i, 0).size > 0) {
            ctx.startActivity(i)
        } else if (error_msg != null) {
            Toast.makeText(this, error_msg, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}