package com.app.kiranachoice.ui.authentication

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.credentials.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private const val RC_HINT = 1

class AuthActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityAuthBinding

    private lateinit var viewModel :AuthViewModel

    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbFireStore: FirebaseFirestore
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var phoneNumber: String? = null
    private var otpCode: String? = null
    private lateinit var phoneNumberWithCountryCode: String
    private lateinit var mCredentialsClient: CredentialsClient
    private var timer : CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val parentLayout = findViewById<View>(android.R.id.content)
        mCredentialsClient = Credentials.getClient(this)
        mAuth = FirebaseAuth.getInstance()
        dbFireStore = FirebaseFirestore.getInstance()

        getHintPhoneNumber()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.i("auth", "onVerificationCompleted() called")
                binding.progressBar.root.visibility = View.GONE
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.i("auth", "onVerificationFailed() called")
                binding.progressBar.root.visibility = View.GONE
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.i("auth", "onVerificationFailed() FirebaseAuthInvalidCredentialsException")
                    binding.etPhoneNumber.error = getString(R.string.invalid_number)
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.i("auth", "onVerificationFailed() FirebaseTooManyRequestsException")
                    Snackbar.make(
                        parentLayout,
                        getString(R.string.too_many_request_attempt),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                binding.progressBar.root.visibility = View.GONE
                Log.i("auth", "onCodeSent()")
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        viewModel.userAlreadyExist.observe(this, {
            if (it) {
                Toast.makeText(
                    this,
                    getString(R.string.login_successful),
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.root.visibility = View.GONE
                finish()
            }
        })

        binding.btnLogin.setOnClickListener(this)
        binding.textResend.setOnClickListener(this)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        Log.i("auth", "startPhoneNumberVerification()")
        Log.i("auth", "phoneNumber : $phoneNumber")
        timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                binding.textResend.isEnabled = true
                binding.textTimer.setText(R.string.sixty)
            }

            override fun onTick(long: Long) {
                binding.textTimer.text = ("" + long / 1000)
                binding.textResend.isEnabled = false
            }
        }.start()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60L, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        )
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String?) {
        Log.i("auth", "verifyPhoneNumberWithCode()")
        if (!code.isNullOrEmpty()) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }
    }


    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        Log.i("auth", "resendVerificationCode()")
        timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                binding.textResend.isEnabled = true
                binding.textTimer.setText(R.string.sixty)
            }

            override fun onTick(l: Long) {
                binding.textTimer.text = ("" + l / 1000)
                binding.textResend.isEnabled = false
            }
        }.start()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60L,
            TimeUnit.SECONDS,
            this,
            callbacks,
            token
        )
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.i("auth", "signInWithPhoneAuthCredential()")
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.progressBar.root.visibility = View.VISIBLE
                    viewModel.onAuthSuccess()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.i(
                            "auth",
                            "signInWithPhoneAuthCredential() exception : ${task.exception.toString()}"
                        )
                        binding.etOtpCode.error = getString(R.string.invalid_code)
                    }

                }
            }
    }


    private fun getHintPhoneNumber() {
        thread(start = true) {
            val hintRequest = HintRequest.Builder()
                .setHintPickerConfig(
                    CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build()
                )
                .setPhoneNumberIdentifierSupported(true)
                .build()

            val intent: PendingIntent = mCredentialsClient.getHintPickerIntent(hintRequest)
            try {
                startIntentSenderForResult(intent.intentSender, RC_HINT, null, 0, 0, 0, null)
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_HINT) {
            if (resultCode == Activity.RESULT_OK) {
                val credential: Credential? = data!!.getParcelableExtra(Credential.EXTRA_KEY)
                binding.etPhoneNumber.setText(credential!!.id.substringAfter("+91"))
            }
        }
    }

    private fun validatePhoneNumber(): Boolean {
        phoneNumber = binding.etPhoneNumber.text.toString().trim()
        if (TextUtils.isEmpty(phoneNumber)) {
            binding.etPhoneNumber.error = getString(R.string.empty_number)
            return false
        }
        // save phone number on viewModel
        viewModel.phoneNumber = phoneNumber
        return true
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    private fun validateOTP(): Boolean {
        otpCode = binding.etOtpCode.text.toString().trim()
        if (TextUtils.isEmpty(otpCode)) {
            binding.etOtpCode.error = getString(R.string.empty_otp)
            return false
        }
        return true
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.btnLogin.id -> {
                // get Button text and perform action accordingly to text
                when (binding.btnLogin.text) {
                    getString(R.string.send_otp) -> {
                        if (validatePhoneNumber()) {
                            // after number validation.. set otp layout visibility to VISIBLE
                            binding.otpCard.visibility = View.VISIBLE
                            // and change login button text "SEND OTP" to "Continue"
                            binding.btnLogin.text = getString(R.string.continue_text)
                            // set visibility

                            phoneNumberWithCountryCode =
                                getString(R.string.country_code).plus(phoneNumber)

                            // show progress bar for few seconds
                            binding.progressBar.root.visibility = View.VISIBLE

                            startPhoneNumberVerification(phoneNumberWithCountryCode)

                            // set visibility to resend code layout to visible
                            binding.resendCodeLayout.visibility = View.VISIBLE
                            binding.etOtpCode.requestFocus()
                        }
                    }
                    getString(R.string.continue_text) -> {
                        if (validateOTP()) {
                            verifyPhoneNumberWithCode(storedVerificationId, otpCode)
                        }
                    }
                }
            }
            binding.textResend.id -> {
                resendVerificationCode(phoneNumberWithCountryCode, resendToken)
            }
        }
    }
}