package com.app.kiranachoice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

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
            e.printStackTrace()
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
                openAppOnPlayStore(this)
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

    private fun openAppOnPlayStore(ctx: Context) {
        val packageName = ctx.packageName
        val uri = Uri.parse("market://details?id=$packageName")
        openURI(ctx, uri )
    }

    private fun openURI(ctx: Context, uri: Uri?) {
        val i = Intent(Intent.ACTION_VIEW, uri)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        if (ctx.packageManager.queryIntentActivities(i, 0).size > 0) {
            ctx.startActivity(i)
        } else {
            Toast.makeText(this, "Play Store not found in your device", Toast.LENGTH_SHORT).show()
        }
    }
}