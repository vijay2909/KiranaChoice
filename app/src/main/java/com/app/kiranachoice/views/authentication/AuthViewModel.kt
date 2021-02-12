package com.app.kiranachoice.views.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.data.network_models.User
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(): ViewModel() {
    private var dbFire = FirebaseFirestore.getInstance().collection(USER_REFERENCE)
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var userSequence: Long = 1
    var phoneNumber = MutableLiveData<String>()

    lateinit var user: User

    private var _otp = MutableLiveData<String>()
    val otp: LiveData<String> get() = _otp

    fun setOTP(otpCode: String){
        _otp.value = otpCode
    }

    init {
        getTotalUserCount()
    }

    // get total documents size in [[ User ]] Collection in firebase
    // it helps to set sequence number of new user
    private fun getTotalUserCount() {
        dbFire.get().addOnSuccessListener { qSnap ->
            userSequence = qSnap.documents.size.plus(1).toLong()
        }
    }

    private var _userAlreadyExist = MutableLiveData<Boolean>()
    val userAlreadyExist: LiveData<Boolean> get() = _userAlreadyExist

    private var _userDoesNotExist = MutableLiveData<Boolean>()
    val userDoesNotExist: LiveData<Boolean> get() = _userDoesNotExist

    fun onAuthSuccess(token: String) {
        mAuth.currentUser?.let { user ->
            val reference = dbFire.document(user.uid)

            reference.get().addOnSuccessListener { documentSnapShot ->
                if (documentSnapShot.exists()) {
                    _userAlreadyExist.postValue(true)
                    // update device token on user login
                    reference.update(mapOf("deviceToken" to token))
                } else {
                    _userDoesNotExist.postValue(true)
                }
            }
        }
    }

    fun eventUserAlreadyExistFinished() {
        _userAlreadyExist.value = false
    }

    fun eventUserDoesNotExistFinished() {
        _userDoesNotExist.value = false
    }

    fun saveUser(name: String, email: String, deviceToken: String?) {
        val userId = mAuth.currentUser!!.uid
        user = User(userSequence, phoneNumber.value, null, name, email, deviceToken, userId)
        dbFire.document(userId).set(user)
            .addOnSuccessListener {
                _userAlreadyExist.value = true
            }
    }

}