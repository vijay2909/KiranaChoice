package com.app.kiranachoice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.grpc.android.BuildConfig

private const val REQUEST_CODE = 123

class SplashActivity : AppCompatActivity() {

    private var appUpdateTypeSupported: Int? = null
    private lateinit var updateListener: InstallStateUpdatedListener
    private lateinit var appUpdateManager: AppUpdateManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkForUpdate()
    }

    private fun checkForUpdate() {
        val appVersion: String = getAppVersion(this) // 1.1.0

        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val minVersion = remoteConfig.getString("min_version_of_app")
        val currentVersion = remoteConfig.getString("latest_version_of_app")

        if (BuildConfig.DEBUG) {
            appUpdateManager = FakeAppUpdateManager(this)
            (appUpdateManager as FakeAppUpdateManager).setUpdateAvailable(4)
        } else {
            appUpdateManager = AppUpdateManagerFactory.create(baseContext)
        }
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            if (!TextUtils.isEmpty(minVersion) && !TextUtils.isEmpty(appVersion) && checkMandateVersionApplicable(
                    getAppVersionWithoutAlphaNumeric(minVersion),
                    getAppVersionWithoutAlphaNumeric(appVersion)
                )
            ) {
//           [[ mandatoryUpdate ]]
                appUpdateTypeSupported = IMMEDIATE
                handleImmediateUpdate(appUpdateManager, appUpdateInfo)
            } else if (!TextUtils.isEmpty(currentVersion) && !TextUtils.isEmpty(appVersion) && !TextUtils.equals(
                    currentVersion,
                    appVersion
                )
            ) {
                // update need not mandatory
                appUpdateTypeSupported = AppUpdateType.FLEXIBLE
                handleFlexibleUpdate(appUpdateManager, appUpdateInfo)
            } else {
                moveForward()
            }
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


    private fun handleImmediateUpdate(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: AppUpdateInfo
    ) {
        //1
        if ((appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                    //2
                    appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) &&
            //3
            appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)
        ) {
            //4
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                IMMEDIATE,
                this,
                REQUEST_CODE
            )

            if (BuildConfig.DEBUG) {
                val fakeAppUpdate = appUpdateManager as FakeAppUpdateManager
                if (fakeAppUpdate.isImmediateFlowVisible) {
                    fakeAppUpdate.userAcceptsUpdate()
                    fakeAppUpdate.downloadStarts()
                    fakeAppUpdate.downloadCompletes()
                    popupSnackBarForCompleteUpdate()
                }
            }
        }
    }

    private fun handleFlexibleUpdate(manager: AppUpdateManager, appUpdateInfo: AppUpdateInfo) {
        if ((appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                    appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        ) {
            setUpdateAction(manager, appUpdateInfo)
        }
    }

    private fun setUpdateAction(appUpdateManager: AppUpdateManager, appUpdateInfo: AppUpdateInfo) {

        updateListener = InstallStateUpdatedListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                popupSnackBarForCompleteUpdate()
            }
        }

        appUpdateManager.registerListener(updateListener)

        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            this,
            REQUEST_CODE
        )

        if (BuildConfig.DEBUG) {
            val fakeAppUpdate = appUpdateManager as FakeAppUpdateManager
            if (fakeAppUpdate.isConfirmationDialogVisible) {
                fakeAppUpdate.userAcceptsUpdate()
                fakeAppUpdate.downloadStarts()
                fakeAppUpdate.downloadCompletes()
                fakeAppUpdate.completeUpdate()
                fakeAppUpdate.installCompletes()
            }
        }

    }

    private fun popupSnackBarForCompleteUpdate() {
        appUpdateManager.unregisterListener(updateListener)
        Snackbar.make(
            findViewById(R.id.rootLayout),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            setActionTextColor(resources.getColor(R.color.white, null))
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //1
        if (REQUEST_CODE == requestCode) {
            when (resultCode) {
                //2
                Activity.RESULT_OK -> {
                    if (appUpdateTypeSupported == IMMEDIATE) {
                        Toast.makeText(baseContext, R.string.toast_updated, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(baseContext, R.string.toast_started, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                //3
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(baseContext, R.string.toast_cancelled, Toast.LENGTH_SHORT).show()
                }
                //4
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Toast.makeText(baseContext, R.string.toast_failed, Toast.LENGTH_SHORT).show()
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If the update is downloaded but not installed,
            // notify the user to complete the update.
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            }
            if (appUpdateInfo.updateAvailability()
                == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                // If an in-app update is already running, resume the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    IMMEDIATE,
                    this,
                    REQUEST_CODE
                )
            }
        }
    }


//    private fun onUpdateNeeded(isMandatoryUpdate: Boolean) {
//        val dialogBuilder = AlertDialog.Builder(this)
//            .setTitle(getString(R.string.update_app))
//            .setCancelable(false)
//            .setMessage(if (isMandatoryUpdate) getString(R.string.dialog_update_available_message) else "A new version is found on Play store, please update for better usage.")
//            .setPositiveButton(getString(R.string.update_now))
//            { _, _ ->
//                openAppOnPlayStore(this)
//            }
//
//        if (!isMandatoryUpdate) {
//            dialogBuilder.setNegativeButton(getString(R.string.later)) { dialog, _ ->
//                moveForward()
//                dialog?.dismiss()
//            }.create()
//        }
//        val dialog: AlertDialog = dialogBuilder.create()
//        dialog.show()
//    }

    private fun moveForward() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

//    private fun openAppOnPlayStore(ctx: Context) {
//        val packageName = ctx.packageName
//        startActivity(
//            Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
//            )
//        )
//    }

//    private fun openAppOnPlayStore(ctx: Context) {
//        val packageName = ctx.packageName
//        val uri = Uri.parse("market://details?id=$packageName")
//        openURI(ctx, uri )
//    }

//    private fun openURI(ctx: Context, uri: Uri?) {
//        val i = Intent(Intent.ACTION_VIEW, uri)
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//        if (ctx.packageManager.queryIntentActivities(i, 0).size > 0) {
//            ctx.startActivity(i)
//        } else {
//            Toast.makeText(this, "Play Store not found in your device", Toast.LENGTH_SHORT).show()
//        }
//    }
}