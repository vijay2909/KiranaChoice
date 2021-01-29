package com.app.kiranachoice.views.authentication

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.kiranachoice.MessagingService
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.credentials.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit


private const val RC_HINT = 1

class LoginFragment : Fragment() {

    private var _bindingLogin: FragmentLoginBinding? = null
    private val binding get() = _bindingLogin!!

    private val viewModel: AuthViewModel by activityViewModels()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbFireStore: FirebaseFirestore
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var phoneNumber: String? = null
    private var otpCode: String? = null
    private lateinit var phoneNumberWithCountryCode: String
    private lateinit var mCredentialsClient: CredentialsClient
    private lateinit var timer: CountDownTimer


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingLogin = FragmentLoginBinding.inflate(inflater, container, false)

        mCredentialsClient = Credentials.getClient(requireActivity())
        mAuth = FirebaseAuth.getInstance()
        dbFireStore = FirebaseFirestore.getInstance()

        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        // initialize timer
        timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                binding.textResend.isEnabled = true
                binding.textTimer.setText(R.string.sixty)
            }

            override fun onTick(l: Long) {
                binding.textTimer.text = getString(R.string.zero_with_colon, ("" + l / 1000))
                binding.textResend.isEnabled = false
            }
        }


        return binding.root
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            hideProgressBar()
            if (e is FirebaseAuthInvalidCredentialsException) {
                binding.etPhoneNumber.error = getString(R.string.invalid_number)
            } else if (e is FirebaseTooManyRequestsException) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.too_many_request_attempt),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {

            binding.apply {
                // hide get otp button
                btnGetOTP.visibility = View.GONE

                // login button
                btnLogin.apply {
                    isEnabled = false // disable by default
                    visibility = View.VISIBLE // show login button
                }

                // show resend button
                resendCodeLayout.visibility = View.VISIBLE
                // show otp view
                otpLayout.visibility = View.VISIBLE
            }

            // start countdown
            timer.start()

            storedVerificationId = verificationId
            resendToken = token

            // hide progress bar
            hideProgressBar()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHintPhoneNumber()

        viewModel.userDoesNotExist.observe(viewLifecycleOwner, {
            if (it) {
                hideProgressBar()
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToUserDetailsFragment(phoneNumber!!)
                )
                viewModel.eventUserDoesNotExistFinished()
            }
        })

        viewModel.userAlreadyExist.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_successful),
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressBar()
                viewModel.eventUserAlreadyExistFinished()
                requireActivity().finish()
            }
        })


        binding.btnGetOTP.setOnClickListener {
            if (validatePhoneNumber()) {

                // show progress bar
                showProgressBar()

                phoneNumberWithCountryCode =
                    getString(R.string.country_code).plus(phoneNumber)


                startPhoneNumberVerification(phoneNumberWithCountryCode)
            }
        }

        binding.btnLogin.setOnClickListener {
            otpCode = binding.otpLayout.text.toString()
            verifyPhoneNumberWithCode(storedVerificationId, otpCode)
        }

        binding.textResend.setOnClickListener {
            resendVerificationCode(phoneNumberWithCountryCode, resendToken)
        }

        binding.otpLayout.addTextChangedListener {
            binding.btnLogin.isEnabled = it.toString().length == 6
        }

    }

    companion object {
        private const val TAG = "LoginFragment"
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String?) {
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
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    showProgressBar()
                    viewModel.onAuthSuccess(MessagingService.getToken(requireContext())!!)
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        binding.etOtpCode.error = getString(R.string.invalid_code)
                    }
                }
            }
    }


    private fun getHintPhoneNumber() {
        val hintRequest = HintRequest.Builder()
            .setHintPickerConfig(
                CredentialPickerConfig.Builder()
                    .setShowCancelButton(true)
                    .build()
            )
            .setPhoneNumberIdentifierSupported(true)
            .build()

        val intent = mCredentialsClient.getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, RC_HINT, null, 0, 0, 0, null)
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_HINT) {
            if (resultCode == Activity.RESULT_OK) {
                val credential: Credential? = data!!.getParcelableExtra(Credential.EXTRA_KEY)

                binding.etPhoneNumber.apply {
                    setText(credential!!.id.substringAfter("+91"))
                    setSelection(this.text.length)
                }

            }
        }
    }


    private fun validatePhoneNumber(): Boolean {
        phoneNumber = viewModel.phoneNumber.value
        if (phoneNumber.isNullOrEmpty()) {
            binding.etPhoneNumber.error = getString(R.string.empty_number)
            return false
        }

        return true
    }


    override fun onPause() {
        super.onPause()
        timer.cancel()
    }


    /*private fun validateOTP(): Boolean {
        otpCode = binding.etOtpCode.text.toString().trim()
        if (TextUtils.isEmpty(otpCode)) {
            binding.etOtpCode.error = getString(R.string.empty_otp)
            return false
        }
        return true
    }*/


    /**
     * show the progress bar
     * */
    private fun showProgressBar() {
        binding.progressBar.root.visibility = View.VISIBLE
    }


    /**
     * hide the progress bar
     * */
    private fun hideProgressBar() {
        binding.progressBar.root.visibility = View.GONE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _bindingLogin = null
    }

}