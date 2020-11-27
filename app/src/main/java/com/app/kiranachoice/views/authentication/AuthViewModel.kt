package com.app.kiranachoice.views.authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.User
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private var dbFire: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var userSequence: Long = 1
    var phoneNumber : String? = null

    lateinit var user : User

    init {
        getTotalUserCount()
    }

    // get total documents size in [[ User ]] Collection in firebase
    // it helps to set sequence number of new user
    private fun getTotalUserCount() {
        dbFire.collection(USER_REFERENCE).get().addOnSuccessListener { qSnap ->
                userSequence = qSnap.documents.size.plus(1).toLong()
            }
    }

    private var _userAlreadyExist = MutableLiveData<Boolean>()
    val userAlreadyExist: LiveData<Boolean> get() = _userAlreadyExist

    private var _userDoesNotExist = MutableLiveData<Boolean>()
    val userDoesNotExist: LiveData<Boolean> get() = _userDoesNotExist

    fun onAuthSuccess() {
        Log.d(TAG, "onAuthSuccess: called")
        mAuth.currentUser?.let { user ->
            dbFire.collection(USER_REFERENCE).document(user.uid).get()
                .addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot.exists()) {
                        Log.i(TAG, "onAuthSuccess: user exists")
                        _userAlreadyExist.postValue(true)
                    }
                    else {
                        Log.w(TAG, "onAuthSuccess: else block" )
                        _userDoesNotExist.postValue(true)
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "onAuthSuccess: failure: ${it.message}")
                }
        }
    }

    fun eventUserAlreadyExistFinished(){
        _userAlreadyExist.value = false
    }

    fun eventUserDoesNotExistFinished(){
        _userDoesNotExist.value = false
    }

    fun saveUser(name: String, email : String, deviceToken: String?) {
        user = User(userSequence,  phoneNumber, null, name, email, deviceToken)
        dbFire.collection(USER_REFERENCE).document(mAuth.currentUser!!.uid).set(user)
            .addOnSuccessListener {
                _userAlreadyExist.value = true
            }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}