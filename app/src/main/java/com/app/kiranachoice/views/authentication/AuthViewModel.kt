package com.app.kiranachoice.views.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.models.User
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private var dbFire: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    private var userSequence: Long = 1
    var phoneNumber : String? = null

    lateinit var user : User

    init {
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
        getTotalUserCount()
    }

    // get total documents size in [[ User ]] Collection in firebase
    // it helps to set sequence number of new user
    private fun getTotalUserCount() {
        viewModelScope.launch(Dispatchers.IO) {
            dbFire?.collection(USER_REFERENCE)?.get()
                ?.addOnSuccessListener { qSnap ->
                    userSequence = qSnap.documents.size.plus(1).toLong()
                }
        }
    }

    private var _userAlreadyExist = MutableLiveData<Boolean>()
    val userAlreadyExist: LiveData<Boolean> get() = _userAlreadyExist

    private var _userDoesNotExist = MutableLiveData<Boolean>()
    val userDoesNotExist: LiveData<Boolean> get() = _userDoesNotExist

    fun onAuthSuccess() {
        viewModelScope.launch(Dispatchers.IO) {
            dbFire?.collection(USER_REFERENCE)?.document(mAuth?.currentUser!!.uid)
                ?.get()
                ?.addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot.exists()) _userAlreadyExist.postValue(true)
                    else _userDoesNotExist.postValue(true)
                }
        }
    }

    fun eventUserAlreadyExistFinished(){
        _userAlreadyExist.value = false
    }

    fun eventUserDoesNotExistFinished(){
        _userDoesNotExist.value = false
    }

    fun saveUser(name: String, email : String) {
        user = User(userSequence,  phoneNumber, null, name, email)
        dbFire?.collection(USER_REFERENCE)?.document(mAuth?.currentUser!!.uid)?.set(user)
            ?.addOnSuccessListener {
                _userAlreadyExist.value = true
            }
    }

    override fun onCleared() {
        super.onCleared()
        if (!this::user.isInitialized){
            mAuth?.signOut()
        }
    }
}