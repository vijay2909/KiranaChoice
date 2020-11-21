package com.app.kiranachoice.views.authentication

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.credentials.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private const val RC_HINT = 1

class LoginFragment : Fragment(), View.OnClickListener {

    private var _bindingLogin: FragmentLoginBinding? = null
    private val binding get() = _bindingLogin!!


    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbFireStore: FirebaseFirestore
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var phoneNumber: String? = null
    private var otpCode: String? = null
    private lateinit var phoneNumberWithCountryCode: String
    private lateinit var mCredentialsClient: CredentialsClient
    private var timer: CountDownTimer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingLogin = FragmentLoginBinding.inflate(inflater, container, false)

        mCredentialsClient = Credentials.getClient(requireActivity())
        mAuth = FirebaseAuth.getInstance()
        dbFireStore = FirebaseFirestore.getInstance()

        getHintPhoneNumber()

        return binding.root
    }


    private val viewModel: AuthViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        view,
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

        viewModel.userDoesNotExist.observe(viewLifecycleOwner, {
            if (it) {
                binding.progressBar.root.visibility = View.GONE
                viewModel.eventUserDoesNotExistFinished()
                view.findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToUserDetailsFragment(phoneNumber!!)
                )
            }
        })

        viewModel.userAlreadyExist.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_successful),
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.root.visibility = View.GONE
                viewModel.eventUserAlreadyExistFinished()
                requireActivity().finish()
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

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
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
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        if (this::resendToken.isInitialized) {
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

            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .setForceResendingToken(token)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.i("auth", "signInWithPhoneAuthCredential()")
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding.progressBar.root.visibility = View.VISIBLE
                    viewModel.onAuthSuccess()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
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
        if (phoneNumber.isNullOrEmpty()) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingLogin = null
    }

}