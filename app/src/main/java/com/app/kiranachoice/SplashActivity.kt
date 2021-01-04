package com.app.kiranachoice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

private const val REQUEST_CODE = 123

class SplashActivity : AppCompatActivity() {

    private var appUpdateTypeSupported: Int? = null
    private lateinit var appUpdateManager: AppUpdateManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        moveForward()
//        checkForUpdate()
    }

    private fun checkForUpdate() {

        appUpdateManager = AppUpdateManagerFactory.create(baseContext)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            appUpdateManager.registerListener(updateListener)

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ){
                if(appUpdateInfo.updatePriority() > 3 && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)){
                    // trigger Immediate flow
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,// Pass the intent that is returned by 'getAppUpdateInfo()'.
                        IMMEDIATE,// Or 'AppUpdateType.IMMEDIATE' for Immediate updates.
                        this,// The current activity making the update request.
                        REQUEST_CODE// Include a request code to later monitor this update request.
                    )
                }
                else if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                    // trigger Flexible flow
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,// Pass the intent that is returned by 'getAppUpdateInfo()'.
                        AppUpdateType.FLEXIBLE,// Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        this,// The current activity making the update request.
                        REQUEST_CODE// Include a request code to later monitor this update request.
                    )
                }
            }else{
                moveForward()
            }
        }
    }

    private val updateListener = InstallStateUpdatedListener {
        if (it.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackBarForCompleteUpdate()
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


    override fun onResume() {
        super.onResume()
        /*appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
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
        }*/
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //1
        if (REQUEST_CODE == requestCode) {
            when (resultCode) {
                //2
                Activity.RESULT_OK -> {
                    if (appUpdateTypeSupported == IMMEDIATE) {
                        Toast.makeText(this, R.string.toast_updated, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, R.string.toast_started, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                //3
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, R.string.toast_cancelled, Toast.LENGTH_SHORT).show()
                    checkForUpdate()
                }
                //4
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show()
                    checkForUpdate()
                }
            }
        }
    }


    private fun moveForward() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

}